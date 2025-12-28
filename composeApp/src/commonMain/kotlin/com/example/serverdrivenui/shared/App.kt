package com.example.serverdrivenui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.composeui.TreehouseContent
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import com.example.serverdrivenui.shared.SduiAppService
import com.example.serverdrivenui.schema.widget.SduiSchemaWidgetSystem
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

/**
 * Content source that creates the ZiplineTreehouseUi.
 * Note: Route is passed via host-bound service, not directly here.
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
 * @param treehouseApp The Treehouse app instance
 * @param route The current route for navigation (default: "dashboard")
 */
@Composable
fun App(treehouseApp: TreehouseApp<SduiAppService>?, route: String = "dashboard") {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (treehouseApp != null) {
                println("SDUI: App composable called with treehouseApp, route=$route")
                val widgetSystem = SduiSchemaWidgetSystem(CmpWidgetFactory)
                TreehouseContent(
                    treehouseApp = treehouseApp,
                    widgetSystem = widgetSystem,
                    contentSource = SduiContentSource(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                println("SDUI: App composable called but treehouseApp is NULL")
                androidx.compose.material3.Text("Redwood not initialized on this platform")
            }
        }
    }
}