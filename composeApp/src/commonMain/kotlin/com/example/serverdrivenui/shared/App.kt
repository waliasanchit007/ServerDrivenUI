@file:Suppress("OPT_IN_USAGE")

package com.example.serverdrivenui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor2.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.example.serverdrivenui.schema.widget.SduiSchemaWidgetSystem
import dev.konduit.treehouse.TreehouseApp
import dev.konduit.treehouse.TreehouseContentSource
import dev.konduit.treehouse.ZiplineTreehouseUi
import dev.konduit.treehouse.composeui.TreehouseContent

/**
 * Builds the singleton ImageLoader Coil uses for AsyncImage. ktor3-based
 * network fetcher (multiplatform — works on Android via okhttp engine,
 * iOS via Darwin engine).
 */
private fun newImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(KtorNetworkFetcherFactory()) }
        .crossfade(true)
        .build()

/**
 * Content source that creates the ZiplineTreehouseUi.
 * The Guest handles all navigation internally.
 */
class SduiContentSource : TreehouseContentSource<SduiAppService> {
    override fun get(app: SduiAppService): ZiplineTreehouseUi {
        println("SDUI: SduiContentSource.get() called")
        val ui = app.launch()
        println("SDUI: app.launch() returned $ui")
        return ui
    }
}

/**
 * Main App composable for the SDUI system.
 *
 * Phase 3a wraps TreehouseContent with:
 *   - An error boundary: when [KonduitDevController.state] is Error, the
 *     guest is replaced by [KonduitErrorFallback] (with retry). Guest
 *     crashes never propagate to the host's compose tree.
 *   - A debug overlay: top-of-screen banner showing reload state.
 *
 * @param treehouseApp The Treehouse app instance
 * @param showDevOverlay false for release builds. Defaults to true since
 *                       Caliclan is debug-only today.
 */
@Composable
fun App(
    treehouseApp: TreehouseApp<SduiAppService>?,
    showDevOverlay: Boolean = true,
) {
    setSingletonImageLoaderFactory { context -> newImageLoader(context) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (treehouseApp == null) {
                Text("Konduit not initialized on this platform")
                return@Surface
            }

            val devState by KonduitDevController.state.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                // Error boundary: when guest can't load, replace TreehouseContent
                // with a retry screen instead of leaving a white screen behind.
                when (val s = devState) {
                    is KonduitDevState.Error -> {
                        KonduitErrorFallback(error = s)
                    }
                    else -> {
                        // Stable identities so TreehouseContent doesn't tear down
                        // and restart the guest session every recomposition.
                        val widgetSystem = remember { SduiSchemaWidgetSystem(CmpWidgetFactory) }
                        val contentSource = remember { SduiContentSource() }
                        TreehouseContent(
                            treehouseApp = treehouseApp,
                            widgetSystem = widgetSystem,
                            contentSource = contentSource,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                // Debug-only banner; auto-dismisses after success.
                KonduitDevOverlay(
                    stateFlow = KonduitDevController.state,
                    enabled = showDevOverlay,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
