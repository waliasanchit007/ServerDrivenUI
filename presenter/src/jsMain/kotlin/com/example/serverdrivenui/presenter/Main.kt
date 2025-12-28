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

fun main() {
    val zipline = Zipline.get()
    
    var hostConsole: HostConsole? = null
    try {
        hostConsole = zipline.take<HostConsole>("console")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to take host console: ${e.message}")
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
    // zipline.bind<SduiAppService>("app", SduiAppServiceImpl(), com.example.serverdrivenui.shared.ManualSduiAppServiceAdapter(emptyList())) 
    // Commented out because adapter doesn't exist yet, but checking syntax via IDE/Compiler would require existence.
    // I will try to compile it assuming it exists. I will create a dummy class first.
    zipline.bind<SduiAppService>("app", SduiAppServiceImpl())
    println("Zipline JS: Service binding completed")
}
