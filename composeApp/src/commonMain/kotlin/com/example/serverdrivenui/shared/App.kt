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
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Caliclan Design Tokens - Premium Dark Theme.
 * 
 * Design Philosophy:
 * - Calm > Flashy
 * - Structured > Motivational hype
 * - Informative > Gamified
 * - Daily-use friendly
 */
object CaliclanColors {
    // Core colors
    val Background = Color(0xFF121212)      // Near-black background
    val Surface = Color(0xFF1E1E1E)         // Card surfaces
    val SurfaceVariant = Color(0xFF2C2C2C)  // Elevated surfaces
    
    // Accent
    val Accent = Color(0xFFFFC107)          // Warm amber/gold
    val AccentVariant = Color(0xFFFFD54F)   // Lighter amber
    
    // Semantic
    val Success = Color(0xFF4CAF50)         // Active/Attended
    val Warning = Color(0xFFFFA726)         // Expiring
    val Error = Color(0xFFE57373)           // Expired
    
    // Text
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB0B0B0)
    val TextTertiary = Color(0xFF757575)
    
    // Utility
    val Border = Color(0xFF2C2C2C)
    val Divider = Color(0xFF333333)
}

/**
 * Caliclan Material3 Dark Color Scheme.
 */
private val CaliclanDarkColorScheme = darkColorScheme(
    primary = CaliclanColors.Accent,
    onPrimary = Color.Black,
    primaryContainer = CaliclanColors.AccentVariant,
    onPrimaryContainer = Color.Black,
    secondary = CaliclanColors.Accent,
    onSecondary = Color.Black,
    background = CaliclanColors.Background,
    onBackground = CaliclanColors.TextPrimary,
    surface = CaliclanColors.Surface,
    onSurface = CaliclanColors.TextPrimary,
    surfaceVariant = CaliclanColors.SurfaceVariant,
    onSurfaceVariant = CaliclanColors.TextSecondary,
    outline = CaliclanColors.Border,
    outlineVariant = CaliclanColors.Border,
    error = CaliclanColors.Error,
    onError = Color.Black
)

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
 * Uses Caliclan dark theme with premium styling.
 * 
 * @param treehouseApp The Treehouse app instance
 */
@Composable
fun App(treehouseApp: TreehouseApp<SduiAppService>?) {
    MaterialTheme(colorScheme = CaliclanDarkColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CaliclanColors.Background
        ) {
            if (treehouseApp != null) {
                println("SDUI: App composable - rendering TreehouseContent with Caliclan theme")
                val widgetSystem = SduiSchemaWidgetSystem(CmpWidgetFactory)
                TreehouseContent(
                    treehouseApp = treehouseApp,
                    widgetSystem = widgetSystem,
                    contentSource = SduiContentSource(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                println("SDUI: App composable - treehouseApp is NULL")
                androidx.compose.material3.Text(
                    text = "Redwood not initialized on this platform",
                    color = CaliclanColors.TextSecondary
                )
            }
        }
    }
}