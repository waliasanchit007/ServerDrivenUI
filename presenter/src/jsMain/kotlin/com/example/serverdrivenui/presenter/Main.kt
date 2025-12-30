package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import app.cash.zipline.Zipline
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.StandardAppLifecycle
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import com.example.serverdrivenui.shared.SduiAppService
import com.example.serverdrivenui.schema.protocol.guest.SduiSchemaProtocolWidgetSystemFactory
import kotlinx.serialization.json.Json

import com.example.serverdrivenui.shared.HostConsole
import com.example.serverdrivenui.shared.NavigationService
import com.example.serverdrivenui.shared.RouteService
import com.example.serverdrivenui.shared.BackPressHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SduiAppServiceImpl : SduiAppService {
    override val appLifecycle = StandardAppLifecycle(
        protocolWidgetSystemFactory = SduiSchemaProtocolWidgetSystemFactory,
        json = Json,
        widgetVersion = 1U
    )

    override fun launch(): ZiplineTreehouseUi {
        println("SduiAppServiceImpl: launch() called")
        val treehouseUi = object : TreehouseUi {
            @Composable override fun Show() {
                SduiPresenter()
            }
        }
        return treehouseUi.asZiplineTreehouseUi(appLifecycle)
    }

    override fun close() {}
}

/**
 * BackPressHandler implementation that forwards back press from host to guest.
 * This is called by the host when iOS swipe-back gesture is detected.
 */
class RealBackPressHandler : BackPressHandler {
    override suspend fun handleBackPress() {
        // Use GlobalScope to break the stack trace completely and avoid re-entrancy issues
        // from Zipline's suspend call handling.
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            // Delay slightly to ensure we are out of any Zipline frame
            delay(10)
            val callback = onGlobalBackPress
            if (callback != null) {
                callback()
            }
        }
    }
    
    override fun close() {}
}

fun main() {
    val zipline = Zipline.get()
    
    // Bind host console for logging
    var hostConsole: HostConsole? = null
    try {
        hostConsole = zipline.take<HostConsole>("console")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to take host console: ${e.message}")
    }
    
    // Bind NavigationService for native navigation
    try {
        navigationService = zipline.take<NavigationService>("navigation")
        println("Zipline JS: NavigationService bound successfully")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to take navigation service: ${e.message}")
    }
    
    // Bind RouteService to get initial route
    try {
        routeService = zipline.take<RouteService>("route")
        initialRoute = routeService?.getCurrentRoute() ?: "dashboard"
        println("Zipline JS: RouteService bound, initialRoute=$initialRoute")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to take route service: ${e.message}")
        initialRoute = "dashboard"
    }

    val consolePolyfill: dynamic = js("{}")
    consolePolyfill.log = { message: Any? -> 
        try {
            if (hostConsole != null) hostConsole.log(message.toString()) else println("JS: $message")
        } catch (e: Throwable) {
            // Ignore failure to log to host
        }
    }
    consolePolyfill.error = { message: Any? -> 
        try {
            if (hostConsole != null) hostConsole.log("ERROR: $message") else println("JS ERROR: $message")
        } catch (e: Throwable) {
            // Ignore failure to log to host
        }
    }
    consolePolyfill.warn = { message: Any? -> 
        try {
            if (hostConsole != null) hostConsole.log("WARN: $message") else println("JS WARN: $message")
        } catch (e: Throwable) {
            // Ignore failure to log to host
        }
    }
    
    // Assign to global scope
    js("globalThis.console = consolePolyfill")

    println("Zipline JS: Service binding started")
    zipline.bind<SduiAppService>("app", SduiAppServiceImpl())
    println("Zipline JS: app service bound")
    
    // Register BackPressHandler via NavigationService
    // This allows the host to call us back for iOS swipe gestures
    val backPressHandler = RealBackPressHandler()
    try {
        navigationService?.setGuestBackHandler(backPressHandler)
        println("Zipline JS: Registered guest back handler with NavigationService")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to register back handler: ${e.message}")
    }
    
    println("Zipline JS: Service binding completed")
}


