package com.example.serverdrivenui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState

/**
 * Phase 3a — debug-only banner showing guest reload state.
 *
 * Renders a small slide-down strip at the top of the screen when something
 * interesting happens (download / reload / load success). Auto-dismisses
 * after 1.5s on success. The Error state is handled separately by
 * [KonduitErrorFallback]; this banner doesn't render anything for Error.
 *
 * Usage:
 * ```
 * Box {
 *     TreehouseContent(...)              // main content
 *     KonduitDevOverlay(
 *         stateFlow = KonduitDevController.state,
 *         enabled = isDebugBuild,
 *         modifier = Modifier.align(Alignment.TopCenter),
 *     )
 * }
 * ```
 */
@Composable
fun KonduitDevOverlay(
    stateFlow: StateFlow<KonduitDevState>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (!enabled) return

    val state by stateFlow.collectAsState()
    var visible by remember { mutableStateOf(false) }

    // Decide visibility per state. Success auto-dismisses after 1.5s.
    LaunchedEffect(state) {
        when (state) {
            is KonduitDevState.Idle -> visible = false
            is KonduitDevState.Downloading -> visible = true
            is KonduitDevState.Reloading -> visible = true
            is KonduitDevState.LoadSuccess -> {
                visible = true
                delay(1500)
                visible = false
            }
            is KonduitDevState.Error -> {
                // Error fallback covers the screen — banner not needed.
                visible = false
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        BannerContent(state)
    }
}

@Composable
private fun BannerContent(state: KonduitDevState) {
    val (background, label) = when (state) {
        is KonduitDevState.Downloading -> Color(0xFFFFC107) to "Kompiling…"
        is KonduitDevState.Reloading -> Color(0xFFFFC107) to "Reloading…"
        is KonduitDevState.LoadSuccess -> {
            val ms = state.duration.inWholeMilliseconds
            val source = if (state.fresh) "fresh" else "cached"
            Color(0xFF4CAF50) to "✓ Loaded ${ms}ms ($source)"
        }
        else -> Color.Transparent to ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

/**
 * Full-screen fallback when guest fails to load. Shown by [App] in place of
 * TreehouseContent when [KonduitDevController.state] is [KonduitDevState.Error].
 */
@Composable
fun KonduitErrorFallback(
    error: KonduitDevState.Error,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "✕  Konduit guest failed to load",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            error.detail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Button(
                onClick = error.onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text("Retry")
            }
        }
    }
}
