@file:Suppress("OPT_IN_USAGE")

package com.example.serverdrivenui.shared

import androidx.compose.runtime.Composable
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.composeui.TreehouseContent
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import com.example.serverdrivenui.schema.widget.SduiSchemaWidgetSystem
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

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
 * SIMPLIFIED: All navigation and back handling is now managed by the Guest
 * via the BackHandler and ScreenStack widgets. The Host just renders.
 * 
 * @param treehouseApp The Treehouse app instance
 */
@Composable
fun App(treehouseApp: TreehouseApp<SduiAppService>?) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (treehouseApp != null) {
                println("SDUI: App composable - rendering TreehouseContent")
                val widgetSystem = SduiSchemaWidgetSystem(CmpWidgetFactory)
                TreehouseContent(
                    treehouseApp = treehouseApp,
                    widgetSystem = widgetSystem,
                    contentSource = SduiContentSource(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                println("SDUI: App composable - treehouseApp is NULL")
                androidx.compose.material3.Text("Redwood not initialized on this platform")
            }
        }
    }
}