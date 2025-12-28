@file:Suppress("DEPRECATION", "OPT_IN_USAGE")
// @file:OptIn(app.cash.redwood.RedwoodCodegenApi::class) removed to rely on compiler args
// @file:OptIn(app.cash.redwood.InternalRedwoodApi::class)

package com.example.serverdrivenui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.serverdrivenui.shared.App
import com.example.serverdrivenui.shared.CmpWidgetFactory
import com.example.serverdrivenui.shared.SduiAppService
import com.example.serverdrivenui.shared.HostConsole
import com.example.serverdrivenui.shared.DevConfig
import com.example.serverdrivenui.shared.HotReloadManager
import com.example.serverdrivenui.shared.NavigationService
import com.example.serverdrivenui.shared.RealNavigationService
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
import androidx.compose.runtime.remember

import com.example.serverdrivenui.schema.protocol.host.SduiSchemaHostProtocol
import com.example.serverdrivenui.schema.widget.SduiSchemaWidgetSystem

import androidx.lifecycle.lifecycleScope
import app.cash.redwood.RedwoodCodegenApi

class MainActivity : ComponentActivity() {
    private val hotReloadManager = HotReloadManager()
    private val manifestUrlFlow = MutableStateFlow(DevConfig.manifestUrl)
    private lateinit var nativeNavigator: AndroidNativeNavigator
    
    // Current route from Intent
    private val currentRoute: String
        get() = intent.getStringExtra(AndroidNativeNavigator.EXTRA_ROUTE) 
            ?: AndroidNativeNavigator.DEFAULT_ROUTE
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize native navigator
        nativeNavigator = AndroidNativeNavigator(this)
        
        Log.d("SDUI", "MainActivity onCreate with route: $currentRoute")

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

        Log.d("SDUI", "TreehouseApp created, setting content (loading triggered by TreehouseContent)...")

        // Create NavigationService for binding to Zipline
        val navigationService = RealNavigationService(nativeNavigator)

        val spec = object : app.cash.redwood.treehouse.TreehouseApp.Spec<SduiAppService>() {
            override val name = "sdui"
            override val manifestUrl = manifestUrlFlow.asStateFlow()

            override suspend fun bindServices(
                treehouseApp: app.cash.redwood.treehouse.TreehouseApp<SduiAppService>,
                zipline: Zipline
            ) {
                Log.d("SDUI-Host", "Inline Spec bindServices called")
                zipline.bind<HostConsole>("console", AndroidRealHostConsole())
                Log.d("SDUI-Host", "console service bound")
                
                // Bind NavigationService for guest access
                zipline.bind<NavigationService>("navigation", navigationService)
                Log.d("SDUI-Host", "navigation service bound")
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
                    manifestUrlFlow.value = "${DevConfig.manifestUrl}?t=$refreshTrigger"
                }
            }
            
            // Pass route to App for presenter to use
            App(app, currentRoute)
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle singleTop re-launches
        setIntent(intent)
        val newRoute = intent.getStringExtra(AndroidNativeNavigator.EXTRA_ROUTE)
        Log.d("SDUI", "onNewIntent with route: $newRoute")
        // Trigger recomposition with new route by updating manifest URL
        manifestUrlFlow.value = "${DevConfig.manifestUrl}?t=${System.currentTimeMillis()}"
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