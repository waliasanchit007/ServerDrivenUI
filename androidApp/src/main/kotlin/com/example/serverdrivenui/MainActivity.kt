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
import com.example.serverdrivenui.shared.DevConfig
import com.example.serverdrivenui.shared.HotReloadManager
import com.example.serverdrivenui.shared.SharedAppSpec
import com.example.serverdrivenui.shared.HostApiConfig
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import okhttp3.OkHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import app.cash.zipline.loader.LoaderEventListener
import app.cash.zipline.Zipline
import app.cash.redwood.treehouse.EventListener
import app.cash.zipline.ZiplineService
import app.cash.zipline.ZiplineManifest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import com.example.serverdrivenui.schema.protocol.host.SduiSchemaHostProtocol

import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

// ============= Supabase Config =============

private object SupabaseConfig {
    const val PROJECT_URL = "https://tumdkgcpikspixovmzrw.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR1bWRrZ2NwaWtzcGl4b3ZtenJ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjcyNDkxNTEsImV4cCI6MjA4MjgyNTE1MX0.Qewg6JqydPboYKSkQ1wxuoHeD2fWMBez9XiO1CTcyA4"
}

// ============= Host Console =============

class AndroidRealHostConsole : HostConsole {
    init {
        Log.d("SDUI-Host", "AndroidRealHostConsole initialized")
    }
    override fun log(message: String) {
        Log.d("SDUI-JS", message)
    }
}

/**
 * Main Activity for Android.
 * Uses SharedAppSpec with RealGymService for Supabase connectivity.
 */
class MainActivity : ComponentActivity() {
    private val hotReloadManager = HotReloadManager()
    private val manifestUrlFlow = MutableStateFlow(DevConfig.manifestUrl)
    
    // Ktor HTTP Client with OkHttp engine (Android native networking)
    private val ktorHttpClient = HttpClient(OkHttp) {
        engine {
            // OkHttp configuration if needed
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d("SDUI", "MainActivity onCreate - Using SharedAppSpec with RealGymService")

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

        // Use SharedAppSpec with Ktor HttpClient for RealGymService
        val hostApiConfig = HostApiConfig(
            supabaseUrl = SupabaseConfig.PROJECT_URL,
            supabaseKey = SupabaseConfig.ANON_KEY
        )
        
        val spec = SharedAppSpec(
            manifestUrl = manifestUrlFlow.asStateFlow(),
            httpClient = ktorHttpClient,
            hostApi = hostApiConfig,
            hostConsole = AndroidRealHostConsole()
        )

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
                    manifestUrlFlow.value = "${DevConfig.manifestUrl}?t=$refreshTrigger"
                }
            }
            
            // Render the app
            App(treehouseApp = app)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hotReloadManager.disconnect()
        ktorHttpClient.close()
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
    override fun create(app: app.cash.redwood.treehouse.TreehouseApp<*>, manifestUrl: String?): EventListener {
        return SDUIZiplineEventListener
    }
    override fun close() {}
}

object SDUIZiplineEventListener : EventListener() {
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
    }

    override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
        Log.d("SDUI-Zipline", "codeLoadSuccess: modules=${manifest.modules.keys}")
    }

    override fun codeLoadFailed(exception: Exception, startValue: Any?) {
        Log.e("SDUI-Zipline", "codeLoadFailed: ${exception.message}", exception)
    }

    override fun downloadStart(url: String): Any? {
        Log.d("SDUI-Zipline", "downloadStart: $url")
        return null
    }

    override fun downloadSuccess(url: String, startValue: Any?) {
        Log.d("SDUI-Zipline", "downloadSuccess: $url")
    }

    override fun downloadFailed(url: String, exception: Exception, startValue: Any?) {
        Log.e("SDUI-Zipline", "downloadFailed: $url, error=${exception.message}", exception)
    }

    override fun manifestReady(manifest: ZiplineManifest) {
        Log.d("SDUI-Zipline", "manifestReady: modules=${manifest.modules.keys.size}")
    }

    override fun manifestParseFailed(exception: Exception) {
        Log.e("SDUI-Zipline", "manifestParseFailed: ${exception.message}", exception)
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
    }
}