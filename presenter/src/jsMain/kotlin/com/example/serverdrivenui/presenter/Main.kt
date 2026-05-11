package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import app.cash.zipline.Zipline
import dev.konduit.treehouse.TreehouseUi
import dev.konduit.treehouse.StandardAppLifecycle
import dev.konduit.treehouse.asZiplineTreehouseUi
import dev.konduit.treehouse.ZiplineTreehouseUi
import com.example.serverdrivenui.shared.SduiAppService
import com.example.serverdrivenui.schema.SduiSerializersModule
import com.example.serverdrivenui.schema.protocol.guest.SduiSchemaProtocolWidgetSystemFactory
import kotlinx.serialization.json.Json
import com.example.serverdrivenui.shared.HostConsole
import com.example.serverdrivenui.shared.HostSnackbar

/**
 * SduiAppService implementation - Entry point for the Zipline app.
 * Uses the new Guest-Driven Navigation architecture.
 */
class SduiAppServiceImpl : SduiAppService {
    override val appLifecycle = StandardAppLifecycle(
        protocolWidgetSystemFactory = SduiSchemaProtocolWidgetSystemFactory,
        // Json must register SchemaColor (and any future enum used as a
        // @Modifier field) so generated ContextualSerializer references can
        // be resolved. See SduiSerializers.kt for the gory details.
        json = Json { serializersModule = SduiSerializersModule },
        widgetVersion = 1U,
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

/**
 * Guest-side bridge to the host's snackbar queue. The host binds a
 * `HostSnackbar` Zipline service (see RealHostSnackbar in composeApp's
 * Protocol.kt); this object holds the bound instance so the guest can
 * post snackbars from anywhere via [showHostSnackbar].
 *
 * Single-process singleton: there's exactly one Zipline runtime per
 * guest, so a top-level holder is fine. Visible to all `:presenter`
 * code (not just Main.kt) for showcase / app code to call.
 */
object HostSnackbarBridge {
    var instance: HostSnackbar? = null
}

/**
 * Top-level helper: enqueue a snackbar message on the host. No-op (with
 * a log) if called before the host binding completes — that should only
 * happen during very early bootstrap. Standard call sites (button
 * onClicks, async completions) always hit this after binding is done.
 *
 * [durationMillis] semantics:
 *   - <= 0 → indefinite (host displays until dismissed by another show()
 *     or the user taps the action button if present)
 *   - 1..6000 → short (~4 s, M3 default)
 *   - > 6000 → long (~10 s)
 *
 * [onResult] is the optional action-result callback. Fires AFTER the
 * snackbar is dismissed:
 *   - true  → user tapped the action button.
 *   - false → timeout, swipe, or superseded by a later show().
 * Null actionLabel implies no action button; onResult will only ever
 * receive false in that case (and you can omit it). When onResult is
 * null this routes through the cheaper fire-and-forget HostSnackbar.show.
 */
fun showHostSnackbar(
    message: String,
    actionLabel: String? = null,
    durationMillis: Long = 4000L,
    onResult: ((Boolean) -> Unit)? = null,
) {
    val bridge = HostSnackbarBridge.instance
    if (bridge == null) {
        println("showHostSnackbar called before HostSnackbar bound: $message")
        return
    }
    // Defensive try/catch — a throw out of a Zipline RPC on the guest
    // side can disrupt the surrounding Compose recomposition (observed
    // empirically as a blank screen on iOS). Swallow + log so the guest
    // UI stays alive even if the host-side service misbehaves.
    try {
        if (onResult != null) {
            bridge.showWithResult(message, actionLabel, durationMillis, onResult)
        } else {
            bridge.show(message, actionLabel, durationMillis)
        }
    } catch (t: Throwable) {
        println("showHostSnackbar bridge call threw for '$message': ${t.message}")
    }
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

    // Bind host snackbar queue.
    try {
        HostSnackbarBridge.instance = zipline.take<HostSnackbar>("snackbar")
        println("Zipline JS: HostSnackbar bound successfully")
    } catch (e: Throwable) {
        println("Zipline JS: Failed to take host snackbar: ${e.message}")
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
