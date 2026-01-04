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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
    
    // NOTE: GymServiceProvider uses lazy initialization - no need to call initialize() here
    // The service is taken from Zipline on first screen access

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
    
    // Inject AbortController Polyfill for Ktor (ES5 Syntax)
    js("""
        if (typeof AbortController === 'undefined') {
            globalThis.AbortController = function AbortController() {
                this.signal = {
                    aborted: false,
                    addEventListener: function() {},
                    removeEventListener: function() {}
                };
                this.abort = function() {
                    this.signal.aborted = true;
                };
            };
        }
        if (typeof AbortSignal === 'undefined') {
             globalThis.AbortSignal = function AbortSignal() {};
             globalThis.AbortSignal.abort = function() {
                return { aborted: true, addEventListener: function() {}, removeEventListener: function() {} };
             };
        }
    """)

    // Polyfill TextEncoder/TextDecoder
    js("""
    if (typeof TextEncoder === 'undefined') {
        globalThis.TextEncoder = function TextEncoder() {};
        globalThis.TextEncoder.prototype.encode = function(s) {
            var octets = [];
            var length = s.length;
            var i = 0;
            while (i < length) {
                var code = s.charCodeAt(i);
                if (code <= 0x7f) {
                    octets.push(code);
                    i += 1;
                } else if (code <= 0x7ff) {
                    octets.push(0xc0 | (code >>> 6));
                    octets.push(0x80 | (code & 0x3f));
                    i += 1;
                } else if (code >= 0xd800 && code <= 0xdbff) {
                    if (i + 1 < length) {
                        var extra = s.charCodeAt(i + 1);
                        if ((extra & 0xfc00) === 0xdc00) {
                            var full = ((code & 0x3ff) << 10) + (extra & 0x3ff) + 0x10000;
                            octets.push(0xf0 | (full >>> 18));
                            octets.push(0x80 | ((full >>> 12) & 0x3f));
                            octets.push(0x80 | ((full >>> 6) & 0x3f));
                            octets.push(0x80 | (full & 0x3f));
                            i += 2;
                            continue;
                        }
                    }
                    i += 1;
                } else {
                    octets.push(0xe0 | (code >>> 12));
                    octets.push(0x80 | ((code >>> 6) & 0x3f));
                    octets.push(0x80 | (code & 0x3f));
                    i += 1;
                }
            }
            return new Uint8Array(octets);
        };
    }
    if (typeof TextDecoder === 'undefined') {
        globalThis.TextDecoder = function TextDecoder() {};
        globalThis.TextDecoder.prototype.decode = function(bytes) {
            var string = "";
            var i = 0;
            while (i < bytes.length) {
                var b = bytes[i];
                if (b <= 0x7f) {
                    string += String.fromCharCode(b);
                    i += 1;
                } else if ((b & 0xe0) === 0xc0) {
                    string += String.fromCharCode(((b & 0x1f) << 6) | (bytes[i+1] & 0x3f));
                    i += 2;
                } else if ((b & 0xf0) === 0xe0) {
                    string += String.fromCharCode(((b & 0x0f) << 12) | ((bytes[i+1] & 0x3f) << 6) | (bytes[i+2] & 0x3f));
                    i += 3;
                } else {
                    string += String.fromCharCode(((b & 0x07) << 18) | ((bytes[i+1] & 0x3f) << 12) | ((bytes[i+2] & 0x3f) << 6) | (bytes[i+3] & 0x3f));
                    i += 4;
                }
            }
            return string;
        };
    }
    """)

    // Polyfill setTimeout using Kotlin Coroutines
    // We need to define this in a scope where coroutines are available
    val global = js("globalThis")
    if (global.setTimeout == undefined) {
        println("Main.kt: Polyfilling setTimeout...")
        val polyfillScope = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Unconfined + kotlinx.coroutines.SupervisorJob()
        )
        
        val setTimeout: (dynamic, Int) -> Any = { func, delayMs ->
            // Simple ID generation
            val id = (kotlin.random.Random.nextDouble() * 1000000).toInt()
            polyfillScope.launch {
                kotlinx.coroutines.delay(delayMs.toLong())
                try {
                    func()
                } catch (e: Throwable) {
                    println("setTimeout callback failed: ${e.message}")
                }
            }
            id
        }

        val clearTimeout: (Int) -> Unit = { id ->
            // Minimal implementation: no-op since we don't track jobs yet.
            // This is usually fine for network timeouts that just want to cancel, 
            // but for Ktor it might be important. 
            // However, Ktor usually cancels the Job if the request is cancelled.
        }

        global.setTimeout = setTimeout
        global.clearTimeout = clearTimeout
    }

    // Assign to global scope
    js("globalThis.console = consolePolyfill")

    println("Zipline JS: Service binding started")
    zipline.bind<SduiAppService>("app", SduiAppServiceImpl())
    println("Zipline JS: app service bound")
    
    println("Zipline JS: Service binding completed - Guest-Driven Navigation ready!")
}
