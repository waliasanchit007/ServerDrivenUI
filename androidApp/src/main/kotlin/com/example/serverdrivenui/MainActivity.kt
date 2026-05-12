@file:Suppress("DEPRECATION", "OPT_IN_USAGE")

package com.example.serverdrivenui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.serverdrivenui.shared.App
import com.example.serverdrivenui.shared.SduiAppService
import com.example.serverdrivenui.shared.HostConsole
import com.example.serverdrivenui.shared.HostSnackbar
import com.example.serverdrivenui.shared.RealHostSnackbar
import com.example.serverdrivenui.shared.DevConfig
import com.example.serverdrivenui.shared.HotReloadManager
import dev.konduit.treehouse.TreehouseAppFactory
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import okhttp3.OkHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import app.cash.zipline.loader.LoaderEventListener
import app.cash.zipline.Zipline
import dev.konduit.treehouse.EventListener
import app.cash.zipline.ZiplineService
import app.cash.zipline.ZiplineManifest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import com.example.serverdrivenui.schema.protocol.host.SduiSchemaHostProtocol

import androidx.lifecycle.lifecycleScope

/**
 * Android HostConsole impl. Routes guest console output through
 * `android.util.Log` so messages land in logcat under the SDUI-JS tag.
 * (commonMain can't do this — it doesn't have android.util.Log on the
 * classpath. iOS has its own equivalent IosRealHostConsole.)
 */
class AndroidRealHostConsole : HostConsole {
    override fun log(message: String) {
        Log.d("SDUI-JS", message)
    }
}

/**
 * Main Activity for Android.
 * SIMPLIFIED: All navigation is handled by Guest via BackHandler widget.
 */
class MainActivity : ComponentActivity() {
    private val hotReloadManager = HotReloadManager()
    private val manifestUrlFlow = MutableStateFlow(DevConfig.manifestUrl)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("SDUI", "MainActivity onCreate - Guest-Driven Navigation")

        // Phase 3a — let the dev controller drive a manifest re-fetch on Retry.
        // We bump the URL with a timestamp param so Treehouse treats it as new.
        com.example.serverdrivenui.shared.KonduitDevController.registerRetryCallback {
            manifestUrlFlow.value = "${DevConfig.manifestUrl}?retry=${System.currentTimeMillis()}"
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("SDUI", "HTTP Request: ${request.url}")
                try {
                    val response = chain.proceed(request)
                    Log.d("SDUI", "HTTP Response: ${response.code} for ${request.url}")
                    response
                } catch (e: Exception) {
                    Log.e("SDUI", "HTTP Error: ${e.message} for ${request.url}")
                    throw e
                }
            }
            .build()
        
        Log.d("SDUI", "Creating TreehouseAppFactory...")
        
        val treehouseAppFactory = TreehouseHelper.createTreehouseAppFactory(
            applicationContext,
            httpClient.asZiplineHttpClient(),
            ManifestVerifier.Companion.NO_SIGNATURE_CHECKS,
            SduiSchemaHostProtocol.Factory
        )

        Log.d("SDUI", "Manifest URL: ${DevConfig.manifestUrl}")
        Log.d("SDUI", "Hot Reload URL: ${DevConfig.hotReloadUrl}")

        val spec = object : dev.konduit.treehouse.TreehouseApp.Spec<SduiAppService>() {
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
            // `androidHostSnackbar` is `lateinit` rather than `val` because
            // its constructor requires the zipline dispatcher, which only
            // exists once TreehouseApp has been created. Populating it
            // inside bindServices guarantees the wiring is in place before
            // Zipline accepts any guest call. The Spec instance itself is
            // rooted by `treehouseAppFactory.create` via the returned
            // TreehouseApp, which lives as long as MainActivity.
            private val androidHostConsole = AndroidRealHostConsole()
            private lateinit var androidHostSnackbar: RealHostSnackbar

            override suspend fun bindServices(
                treehouseApp: dev.konduit.treehouse.TreehouseApp<SduiAppService>,
                zipline: Zipline
            ) {
                Log.d("SDUI-Host", "bindServices called")

                zipline.bind<HostConsole>("console", androidHostConsole)
                Log.d("SDUI-Host", "console service bound")

                // Construct RealHostSnackbar with the zipline-confined
                // dispatcher (constructor-required since gotcha #12). The
                // showWithResult callback path crosses the QuickJS boundary
                // and MUST be invoked from this dispatcher — JVM Zipline
                // tolerates the wrong thread by luck, iOS K/N reliably
                // stack-overflows. Both platforms wire it for parity.
                androidHostSnackbar = RealHostSnackbar(
                    ziplineDispatcher = treehouseApp.dispatchers.zipline,
                )

                // Snackbar: guest calls showHostSnackbar(message) → Zipline RPC
                // → RealHostSnackbar (in commonMain Protocol.kt) → M3
                // SnackbarHostState (SnackbarHub) → SnackbarHost composable
                // (anchored in App.kt). All four pieces must be present;
                // before this line was added, the bind only happened in a
                // commonMain SduiAppSpec class that was never referenced
                // (dead code), so guest take() got a proxy that 404'd at
                // first use. See decisions log entry "Batch 3.x snackbar
                // bind-site fix" in KONDUIT_PLAN.md.
                zipline.bind<HostSnackbar>("snackbar", androidHostSnackbar)
                Log.d("SDUI-Host", "snackbar service bound")
            }

            override fun create(zipline: Zipline): SduiAppService {
                return zipline.take<SduiAppService>("app")
            }
        }

        val app = treehouseAppFactory.create(
            appScope = lifecycleScope,
            spec = spec,
            eventListenerFactory = SDUIZiplineEventListenerFactory
        )
        
        // Connect to hot reload WebSocket
        hotReloadManager.connect(DevConfig.hotReloadUrl)

        setContent {
            // Observe hot reload triggers
            val refreshTrigger by hotReloadManager.refreshTrigger.collectAsState()
            
            LaunchedEffect(refreshTrigger) {
                if (refreshTrigger > 0) {
                    Log.d("SDUI", "Hot reload triggered at $refreshTrigger")
                    com.example.serverdrivenui.shared.KonduitDevController.reportReloading()
                    manifestUrlFlow.value = "${DevConfig.manifestUrl}?t=$refreshTrigger"
                }
            }
            
            // Just render the app - Guest handles everything!
            App(treehouseApp = app)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hotReloadManager.disconnect()
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App(null)
}

object LoggingLoaderEventListener : LoaderEventListener() {
    override fun cacheStorageFailed(applicationName: String?, e: Exception) {
        Log.e("SDUI-Zipline", "cacheStorageFailed app=$applicationName: ${e.message}", e)
    }
}

object SDUIZiplineEventListenerFactory : EventListener.Factory {
    override fun create(app: dev.konduit.treehouse.TreehouseApp<*>, manifestUrl: String?): EventListener {
        return SDUIZiplineEventListener
    }
    override fun close() {}
}

object SDUIZiplineEventListener : EventListener() {
    /**
     * Phase 5c — curated lifecycle log on the "Konduit" tag. Sits
     * alongside the raw "SDUI-Zipline" debug stream below; you can
     * `adb logcat -s Konduit` to see only the curated view.
     */
    private val konduitLog = com.example.serverdrivenui.shared.KonduitDevLog { level, message ->
        when (level) {
            com.example.serverdrivenui.shared.KonduitLogLevel.D -> Log.d("Konduit", message)
            com.example.serverdrivenui.shared.KonduitLogLevel.W -> Log.w("Konduit", message)
            com.example.serverdrivenui.shared.KonduitLogLevel.E -> Log.e("Konduit", message)
        }
    }

    override fun ziplineCreated(zipline: Zipline) {
        Log.d("SDUI-Zipline", "ziplineCreated")
    }

    override fun bindService(name: String, service: ZiplineService) {
        Log.d("SDUI-Zipline", "bindService name=$name")
    }

    override fun takeService(name: String, service: ZiplineService) {
        Log.d("SDUI-Zipline", "takeService name=$name")
    }

    override fun serviceLeaked(name: String) {
        Log.w("SDUI-Zipline", "serviceLeaked name=$name")
        konduitLog.serviceLeaked(name)
    }

    override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
        Log.d("SDUI-Zipline", "codeLoadSuccess: modules=${manifest.modules.keys}")
        konduitLog.codeLoadSuccess(applicationName = "sdui")
        com.example.serverdrivenui.shared.KonduitDevController.reportLoadSuccess(fresh = true)
    }

    override fun codeLoadFailed(exception: Exception, startValue: Any?) {
        Log.e("SDUI-Zipline", "codeLoadFailed: ${exception.message}", exception)
        konduitLog.codeLoadFailed(exception.message)
        com.example.serverdrivenui.shared.KonduitDevController.reportError(
            message = "Guest code load failed",
            detail = exception.message,
        )
    }

    override fun downloadStart(url: String): Any? {
        Log.d("SDUI-Zipline", "downloadStart: $url")
        // Only treat the manifest URL as a "download start" for the
        // curated stream — module downloads are too noisy and aren't
        // a useful lifecycle signal at this level.
        if (url.endsWith("manifest.zipline.json")) {
            konduitLog.manifestDownloadStart(url)
        }
        com.example.serverdrivenui.shared.KonduitDevController.reportDownloadStart()
        return null
    }

    override fun downloadSuccess(url: String, startValue: Any?) {
        Log.d("SDUI-Zipline", "downloadSuccess: $url")
    }

    override fun downloadFailed(url: String, exception: Exception, startValue: Any?) {
        Log.e("SDUI-Zipline", "downloadFailed: $url, error=${exception.message}", exception)
        konduitLog.downloadFailed(url, exception.message)
        com.example.serverdrivenui.shared.KonduitDevController.reportError(
            message = "Manifest download failed",
            detail = "$url\n${exception.message}",
        )
    }

    override fun manifestReady(manifest: ZiplineManifest) {
        Log.d("SDUI-Zipline", "manifestReady: modules=${manifest.modules.keys.size}")
        konduitLog.manifestReady(manifest.modules.size)
    }

    override fun manifestParseFailed(exception: Exception) {
        Log.e("SDUI-Zipline", "manifestParseFailed: ${exception.message}", exception)
        konduitLog.manifestParseFailed(exception.message)
        com.example.serverdrivenui.shared.KonduitDevController.reportError(
            message = "Manifest parse failed",
            detail = exception.message,
        )
    }

    override fun mainFunctionStart(applicationName: String): Any? {
        Log.d("SDUI-Zipline", "mainFunctionStart app=$applicationName")
        return null
    }

    override fun mainFunctionEnd(applicationName: String, startValue: Any?) {
        Log.d("SDUI-Zipline", "mainFunctionEnd app=$applicationName")
    }

    override fun uncaughtException(exception: Throwable) {
        Log.e("SDUI-Zipline", "uncaughtException: ${exception.message}", exception)
        konduitLog.uncaughtException(exception.message)
    }
}