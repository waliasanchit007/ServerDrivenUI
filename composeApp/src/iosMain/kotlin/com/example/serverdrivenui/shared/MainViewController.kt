@file:Suppress("DEPRECATION", "OPT_IN_USAGE")
@file:OptIn(
    dev.konduit.RedwoodCodegenApi::class,
    dev.konduit.leaks.RedwoodLeakApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)

package com.example.serverdrivenui.shared

import androidx.compose.ui.window.ComposeUIViewController
import dev.konduit.treehouse.EventListener
import dev.konduit.treehouse.TreehouseApp
import dev.konduit.treehouse.TreehouseAppFactory
import dev.konduit.treehouse.MemoryStateStore
import dev.konduit.leaks.LeakDetector
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.ZiplineService
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.serverdrivenui.schema.protocol.host.SduiSchemaHostProtocol
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import platform.Foundation.NSURLSession
import platform.Foundation.NSURL
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLRequestUseProtocolCachePolicy
import platform.Foundation.addValue
import platform.Foundation.dataTaskWithRequest
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.IOException

/**
 * iOS-specific HTTP client using NSURLSession.
 */
class IosZiplineHttpClient(
    private val urlSession: NSURLSession = NSURLSession.sharedSession
) : ZiplineHttpClient() {
    override suspend fun download(
        url: String,
        requestHeaders: List<Pair<String, String>>,
    ): ByteString {
        val nsUrl = NSURL(string = url)
        return suspendCancellableCoroutine { continuation: CancellableContinuation<ByteString> ->
            val completionHandler = CompletionHandler(url, continuation)

            val task = urlSession.dataTaskWithRequest(
                request = NSMutableURLRequest(
                    uRL = nsUrl,
                    cachePolicy = NSURLRequestUseProtocolCachePolicy,
                    timeoutInterval = 60.0,
                ).apply {
                    for ((name, value) in requestHeaders) {
                        addValue(value = value, forHTTPHeaderField = name)
                    }
                },
                completionHandler = completionHandler::invoke,
            )

            continuation.invokeOnCancellation {
                task.cancel()
            }

            task.resume()
        }
    }
}

private class CompletionHandler(
    private val url: String,
    private val continuation: CancellableContinuation<ByteString>,
) {
    fun invoke(data: NSData?, response: NSURLResponse?, error: NSError?) {
        if (error != null) {
            continuation.resumeWithException(IOException(error.description))
            return
        }

        if (response !is NSHTTPURLResponse || data == null) {
            continuation.resumeWithException(IOException("unexpected response: $response"))
            return
        }

        if (response.statusCode !in 200 until 300) {
            continuation.resumeWithException(
                IOException("failed to fetch $url: ${response.statusCode}"),
            )
            return
        }

        continuation.resume(data.toByteString())
    }
}

/**
 * Host console for Guest logging.
 */
class IosRealHostConsole : HostConsole {
    override fun log(message: String) {
        println("JS: $message")
    }
}

private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
private var treehouseApp: TreehouseApp<SduiAppService>? = null
private val manifestUrlFlow = MutableStateFlow(DevConfig.manifestUrl)
private val hotReloadManager = HotReloadManager()

/**
 * Phase 3a — iOS event listener bridges Treehouse loader events to the
 * shared [KonduitDevController] so the host can render error fallbacks
 * and the dev banner.
 */
private object IosKonduitEventListener : EventListener() {
    /**
     * Phase 5c — curated lifecycle log on the "Konduit/" prefix. Sits
     * alongside the raw "SDUI-iOS-Zipline" debug stream below. iOS
     * doesn't have logcat-style level routing; we encode the level in
     * the prefix so a `grep '^Konduit/'` against the Xcode console
     * filters cleanly.
     */
    private val konduitLog = KonduitDevLog { level, message ->
        val prefix = when (level) {
            KonduitLogLevel.D -> "Konduit/D"
            KonduitLogLevel.W -> "Konduit/W"
            KonduitLogLevel.E -> "Konduit/E"
        }
        println("$prefix: $message")
    }

    override fun ziplineCreated(zipline: Zipline) {
        println("SDUI-iOS-Zipline: ziplineCreated")
    }

    override fun bindService(name: String, service: ZiplineService) {
        println("SDUI-iOS-Zipline: bindService name=$name")
    }

    override fun takeService(name: String, service: ZiplineService) {
        println("SDUI-iOS-Zipline: takeService name=$name")
    }

    override fun serviceLeaked(name: String) {
        println("SDUI-iOS-Zipline: serviceLeaked name=$name")
        konduitLog.serviceLeaked(name)
    }

    override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
        println("SDUI-iOS-Zipline: codeLoadSuccess: modules=${manifest.modules.keys.size}")
        konduitLog.codeLoadSuccess(applicationName = "sdui")
        KonduitDevController.reportLoadSuccess(fresh = true)
    }

    override fun codeLoadFailed(exception: Exception, startValue: Any?) {
        println("SDUI-iOS-Zipline: codeLoadFailed: ${exception.message}")
        konduitLog.codeLoadFailed(exception.message)
        KonduitDevController.reportError(
            message = "Guest code load failed",
            detail = exception.message,
        )
    }

    override fun downloadStart(url: String): Any? {
        println("SDUI-iOS-Zipline: downloadStart: $url")
        if (url.endsWith("manifest.zipline.json")) {
            konduitLog.manifestDownloadStart(url)
        }
        KonduitDevController.reportDownloadStart()
        return null
    }

    override fun downloadFailed(url: String, exception: Exception, startValue: Any?) {
        println("SDUI-iOS-Zipline: downloadFailed: $url, ${exception.message}")
        konduitLog.downloadFailed(url, exception.message)
        KonduitDevController.reportError(
            message = "Manifest download failed",
            detail = "$url\n${exception.message}",
        )
    }

    override fun manifestReady(manifest: ZiplineManifest) {
        println("SDUI-iOS-Zipline: manifestReady: modules=${manifest.modules.keys.size}")
        konduitLog.manifestReady(manifest.modules.size)
    }

    override fun manifestParseFailed(exception: Exception) {
        println("SDUI-iOS-Zipline: manifestParseFailed: ${exception.message}")
        konduitLog.manifestParseFailed(exception.message)
        KonduitDevController.reportError(
            message = "Manifest parse failed",
            detail = exception.message,
        )
    }

    override fun uncaughtException(exception: Throwable) {
        println("SDUI-iOS-Zipline: uncaughtException: ${exception.message}")
        konduitLog.uncaughtException(exception.message)
    }
}

private object IosKonduitEventListenerFactory : EventListener.Factory {
    override fun create(app: TreehouseApp<*>, manifestUrl: String?): EventListener =
        IosKonduitEventListener
    override fun close() {}
}

/**
 * Initialize the TreehouseApp instance.
 * SIMPLIFIED: No navigation services - Guest handles all navigation via BackHandler widget.
 */
fun initializeTreehouseApp(): TreehouseApp<SduiAppService> {
    val existing = treehouseApp
    if (existing != null) return existing
    
    println("SDUI-iOS: Creating TreehouseAppFactory...")
    println("SDUI-iOS: Manifest URL: ${DevConfig.manifestUrl}")
    println("SDUI-iOS: Hot Reload URL: ${DevConfig.hotReloadUrl}")
    
    @Suppress("UnstableRedwoodApi")
    val treehouseAppFactory = TreehouseAppFactory(
        httpClient = IosZiplineHttpClient(),
        manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
        embeddedFileSystem = null,
        embeddedDir = null,
        cacheName = "zipline",
        cacheMaxSizeInBytes = 50L * 1024L * 1024L,
        concurrentDownloads = 8,
        stateStore = MemoryStateStore(),
        leakDetector = LeakDetector.none(),
        hostProtocolFactory = SduiSchemaHostProtocol.Factory
    )
    
    val spec = object : TreehouseApp.Spec<SduiAppService>() {
        override val name = "sdui"
        override val manifestUrl = manifestUrlFlow.asStateFlow()
        override val serializersModule = com.example.serverdrivenui.schema.SduiSerializersModule

        // Strong refs to bound host services. Konduit/Zipline does NOT
        // retain services internally — see konduit-treehouse-host
        // EventListener.kt#serviceLeaked: "Invoked when a service is
        // garbage collected without being closed." Anonymous instances
        // passed inline to `bind(...)` become GC-eligible the moment
        // bindServices returns; first guest call then errors with
        // "no such service (service closed?)". Hold them as `lateinit
        // var` (or `val`) properties of the Spec to keep them alive
        // for its lifetime.
        //
        // `iosHostSnackbar` is `lateinit` rather than `val` because its
        // constructor requires the zipline dispatcher, which only exists
        // once TreehouseApp has been created. The Spec instance survives
        // long enough for bindServices to populate the field BEFORE any
        // guest call can fire (Zipline doesn't accept calls until bind
        // returns), so the lateinit contract is honored.
        //
        // Note: this Spec mirrors the AndroidMain Spec in MainActivity.kt;
        // both must wire the same set of services (any divergence shows
        // up as platform-specific guest crashes).
        private val iosHostConsole = IosRealHostConsole()
        private lateinit var iosHostSnackbar: RealHostSnackbar

        override suspend fun bindServices(
            treehouseApp: TreehouseApp<SduiAppService>,
            zipline: Zipline
        ) {
            println("SDUI-iOS: bindServices called")

            zipline.bind<HostConsole>("console", iosHostConsole)
            println("SDUI-iOS: console bound")

            // Construct RealHostSnackbar WITH the zipline-confined
            // dispatcher — see gotcha #12. The constructor parameter is
            // required precisely so this wiring can't be forgotten.
            iosHostSnackbar = RealHostSnackbar(
                ziplineDispatcher = treehouseApp.dispatchers.zipline,
            )

            // Snackbar: see Android Spec for the architecture rationale.
            // Both platforms must bind the same set of services.
            zipline.bind<HostSnackbar>("snackbar", iosHostSnackbar)
            println("SDUI-iOS: snackbar bound")
        }

        override fun create(zipline: Zipline): SduiAppService {
            return zipline.take<SduiAppService>("app")
        }
    }
    
    val app = treehouseAppFactory.create(
        appScope = appScope,
        spec = spec,
        eventListenerFactory = IosKonduitEventListenerFactory,
    )

    // Phase 3a — let the dev controller drive a manifest re-fetch on Retry.
    KonduitDevController.registerRetryCallback {
        manifestUrlFlow.value = "${DevConfig.manifestUrl}?retry=${kotlin.time.TimeSource.Monotonic.markNow().hashCode()}"
    }

    // Connect to hot reload WebSocket
    hotReloadManager.connect(DevConfig.hotReloadUrl)
    
    treehouseApp = app
    println("SDUI-iOS: TreehouseApp created")
    return app
}

/**
 * Main View Controller - Entry point for iOS app.
 * SIMPLIFIED: Just renders TreehouseContent. No navigation handling.
 */
fun MainViewController() = ComposeUIViewController {
    val app = initializeTreehouseApp()
    
    // Observe hot reload triggers
    val refreshTrigger by hotReloadManager.refreshTrigger.collectAsState()
    
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            println("SDUI-iOS: Hot reload triggered at $refreshTrigger")
            KonduitDevController.reportReloading()
            manifestUrlFlow.value = "${DevConfig.manifestUrl}?t=$refreshTrigger"
        }
    }
    
    // Just render the app - Guest handles everything!
    App(treehouseApp = app)
}