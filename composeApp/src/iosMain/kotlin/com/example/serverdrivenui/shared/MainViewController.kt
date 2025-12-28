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
import app.cash.zipline.Zipline
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
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.IOException

// iOS-specific HTTP client using NSURLSession (copied from Zipline's URLSessionZiplineHttpClient)
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

// Simple event listener for iOS  
class IosRealHostConsole : HostConsole {
    override fun log(message: String) {
        println("JS: $message")
    }
}

private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

private var treehouseApp: TreehouseApp<SduiAppService>? = null
private val manifestUrlFlow = MutableStateFlow(DevConfig.manifestUrl)
private val hotReloadManager = HotReloadManager()

// Navigation state
private var iosNavigator: IosNativeNavigator? = null
private var navigationController: UINavigationController? = null

/**
 * Set the navigation controller for iOS navigation.
 * Call this from Swift before using navigation.
 */
fun setNavigationController(navController: UINavigationController?) {
    navigationController = navController
    println("SDUI-iOS: Navigation controller set: ${navController != null}")
}

/**
 * Create a new view controller for the given route.
 * Used by IosNativeNavigator when navigating.
 */
fun createViewControllerForRoute(route: String): UIViewController {
    println("SDUI-iOS: Creating view controller for route: $route")
    return ComposeUIViewController {
        val app = treehouseApp
        if (app != null) {
            App(app, route)
        }
    }
}

fun initializeTreehouseApp(): TreehouseApp<SduiAppService> {
    val existing = treehouseApp
    if (existing != null) return existing
    
    println("SDUI-iOS: Creating TreehouseAppFactory...")
    
    // Initialize iOS navigator
    iosNavigator = IosNativeNavigator(
        getNavigationController = { navigationController },
        createViewController = { route -> createViewControllerForRoute(route) }
    )
    
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
    
    println("SDUI-iOS: Manifest URL: ${DevConfig.manifestUrl}")
    println("SDUI-iOS: Hot Reload URL: ${DevConfig.hotReloadUrl}")
    
    // Create NavigationService for binding
    val navigationService = RealNavigationService(iosNavigator!!)
    
    val spec = object : TreehouseApp.Spec<SduiAppService>() {
        override val name = "sdui"
        override val manifestUrl = manifestUrlFlow.asStateFlow()
        
        override suspend fun bindServices(
            treehouseApp: TreehouseApp<SduiAppService>,
            zipline: Zipline
        ) {
            println("SDUI-iOS: bindServices called")
            zipline.bind<HostConsole>("console", IosRealHostConsole())
            println("SDUI-iOS: console bound")
            
            // Bind NavigationService for guest access
            zipline.bind<NavigationService>("navigation", navigationService)
            println("SDUI-iOS: navigation service bound")
        }
        
        override fun create(zipline: Zipline): SduiAppService {
            return zipline.take<SduiAppService>("app")
        }
    }
    
    val app = treehouseAppFactory.create(
        appScope = appScope,
        spec = spec
    )
    
    // Connect to hot reload WebSocket
    hotReloadManager.connect(DevConfig.hotReloadUrl)
    
    treehouseApp = app
    println("SDUI-iOS: TreehouseApp created")
    return app
}

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
    
    App(app, "dashboard")
}