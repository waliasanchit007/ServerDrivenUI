package com.example.serverdrivenui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A wrapper around [painterResource] for consistent resource loading.
 * 
 * Note: Direct try-catch around Composable functions like [painterResource] 
 * is not supported by the Compose compiler. Safety is primarily ensured 
 * by the AGP 8.8+ resource packaging and instrumentation tests.
 */
@Composable
fun safePainterResource(resource: DrawableResource): Painter {
    return painterResource(resource)
}
