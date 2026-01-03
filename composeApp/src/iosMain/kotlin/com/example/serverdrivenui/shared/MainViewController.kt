@file:Suppress("DEPRECATION", "OPT_IN_USAGE")
@file:OptIn(
    app.cash.redwood.RedwoodCodegenApi::class,
    app.cash.redwood.leaks.RedwoodLeakApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)

package com.example.serverdrivenui.shared

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.redwood.treehouse.MemoryStateStore
import app.cash.redwood.leaks.LeakDetector
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
import io.ktor.client.*
import io.ktor.client.engine.darwin.*

// ============= Supabase Config =============

private object SupabaseConfig {
    const val PROJECT_URL = "https://tumdkgcpikspixovmzrw.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR1bWRrZ2NwaWtzcGl4b3ZtenJ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjcyNDkxNTEsImV4cCI6MjA4MjgyNTE1MX0.Qewg6JqydPboYKSkQ1wxuoHeD2fWMBez9XiO1CTcyA4"
}

// ============= Zipline HTTP Client (for code loading) =============

/**
 * iOS-specific HTTP client using NSURLSession for Zipline code loading.
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

// ============= Host Console =============

/**
 * Host console for Guest logging.
 */
class IosRealHostConsole : HostConsole {
    override fun log(message: String) {
        println("JS: $message")
    }
}

// ============= App Initialization =============

private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
private var treehouseApp: TreehouseApp<SduiAppService>? = null
private val manifestUrlFlow = MutableStateFlow(DevConfig.manifestUrl)
private val hotReloadManager = HotReloadManager()

// Ktor HTTP Client with Darwin engine (iOS native networking)
private val ktorHttpClient = HttpClient(Darwin) {
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}

/**
 * Initialize the TreehouseApp instance.
 * Uses SharedAppSpec with RealGymService for Supabase connectivity.
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
    
    // Use SharedAppSpec with Ktor HttpClient for RealGymService
    val hostApiConfig = HostApiConfig(
        supabaseUrl = SupabaseConfig.PROJECT_URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    )
    
    val spec = SharedAppSpec(
        manifestUrl = manifestUrlFlow.asStateFlow(),
        httpClient = ktorHttpClient,
        hostApi = hostApiConfig,
        hostConsole = IosRealHostConsole(),
        storage = IosStorageService()
    )
    
    val app = treehouseAppFactory.create(
        appScope = appScope,
        spec = spec
    )
    
    // Connect to hot reload WebSocket
    hotReloadManager.connect(DevConfig.hotReloadUrl)
    
    treehouseApp = app
    println("SDUI-iOS: TreehouseApp created with RealGymService")
    return app
}

/**
 * Main View Controller - Entry point for iOS app.
 * Uses SharedAppSpec for platform-agnostic service binding.
 */
fun MainViewController() = ComposeUIViewController {
    val app = initializeTreehouseApp()
    
    // Observe hot reload triggers
    val refreshTrigger by hotReloadManager.refreshTrigger.collectAsState()
    
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            println("SDUI-iOS: Hot reload triggered at $refreshTrigger")
            manifestUrlFlow.value = "${DevConfig.manifestUrl}?t=$refreshTrigger"
        }
    }
    
    // Render the app
    App(treehouseApp = app)
}