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

/**
 * SduiAppService implementation - Entry point for the Zipline app.
 * Uses the new Guest-Driven Navigation architecture.
 */
class SduiAppServiceImpl : SduiAppService {
    override val appLifecycle = StandardAppLifecycle(
        protocolWidgetSystemFactory = SduiSchemaProtocolWidgetSystemFactory,
        json = Json,
        widgetVersion = 1U
    )

    override fun launch(): ZiplineTreehouseUi {
        println("SduiAppServiceImpl: launch() called")
        val treehouseUi = object : TreehouseUi {
            @Composable 
            override fun Show() {
                // Use the new RootUi with Navigator
                RootUi(initialRoute = "home")
            }
        }
        return treehouseUi.asZiplineTreehouseUi(appLifecycle)
    }

    override fun close() {}
}

fun main() {
    val zipline = Zipline.get()
    
    // Bind host console for logging
    var hostConsole: HostConsole? = null
    try {
        hostConsole = zipline.take<HostConsole>("console")
        println("Zipline JS: HostConsole bound successfully")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to take host console: ${e.message}")
    }

    // Capture original console for fallback
    val originalConsole: dynamic = js("console")
    val consolePolyfill: dynamic = js("{}")
    
    consolePolyfill.log = { message: Any? -> 
        try {
            if (hostConsole != null) hostConsole.log(message.toString()) else originalConsole.log(message)
        } catch (e: Throwable) {
            originalConsole.log("Failed to log to host: $message")
        }
    }
    consolePolyfill.error = { message: Any? -> 
        try {
            if (hostConsole != null) hostConsole.log("ERROR: $message") else originalConsole.error(message)
        } catch (e: Throwable) {
            originalConsole.error("Failed to log error to host: $message")
        }
    }
    consolePolyfill.warn = { message: Any? -> 
        try {
            if (hostConsole != null) hostConsole.log("WARN: $message") else originalConsole.warn(message)
        } catch (e: Throwable) {
            originalConsole.warn("Failed to log warn to host: $message")
        }
    }
    
    // Assign to global scope
    js("globalThis.console = consolePolyfill")

    println("Zipline JS: Service binding started")
    zipline.bind<SduiAppService>("app", SduiAppServiceImpl())
    println("Zipline JS: app service bound")
    
    println("Zipline JS: Service binding completed - Guest-Driven Navigation ready!")
}
