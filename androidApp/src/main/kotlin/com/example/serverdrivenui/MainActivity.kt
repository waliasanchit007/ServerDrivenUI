@file:Suppress("DEPRECATION", "OPT_IN_USAGE")
// @file:OptIn(app.cash.redwood.RedwoodCodegenApi::class) removed to rely on compiler args
// @file:OptIn(app.cash.redwood.InternalRedwoodApi::class)

package com.example.serverdrivenui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.serverdrivenui.shared.App
import com.example.serverdrivenui.shared.CmpWidgetFactory
// import com.example.serverdrivenui.shared.SduiAppSpec
import com.example.serverdrivenui.shared.SduiAppService
import com.example.serverdrivenui.shared.HostConsole
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import okhttp3.OkHttpClient
import kotlinx.coroutines.flow.flowOf
import app.cash.zipline.loader.LoaderEventListener
import app.cash.zipline.Zipline
import app.cash.redwood.treehouse.EventListener
import app.cash.zipline.ZiplineService
import app.cash.zipline.ZiplineManifest

import com.example.serverdrivenui.schema.protocol.host.SduiSchemaHostProtocol
import com.example.serverdrivenui.schema.widget.SduiSchemaWidgetSystem

import androidx.lifecycle.lifecycleScope
import app.cash.redwood.RedwoodCodegenApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        // For emulator use 10.0.2.2, for physical device use your machine's IP
        val manifestUrl = "http://192.168.1.3:8080/manifest.zipline.json"
        Log.d("SDUI", "Manifest URL: $manifestUrl")

        Log.d("SDUI", "TreehouseApp created, setting content (loading triggered by TreehouseContent)...")

        val spec = object : app.cash.redwood.treehouse.TreehouseApp.Spec<SduiAppService>() {
            override val name = "sdui"
            override val manifestUrl = flowOf(manifestUrl)

            override suspend fun bindServices(
                treehouseApp: app.cash.redwood.treehouse.TreehouseApp<SduiAppService>,
                zipline: Zipline
            ) {
                Log.d("SDUI-Host", "Inline Spec bindServices called")
                zipline.bind<HostConsole>("console", AndroidRealHostConsole())
                Log.d("SDUI-Host", "Inline Spec console bound")
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

        setContent {
            App(app)
        }
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