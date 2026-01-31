package com.example.serverdrivenui.native.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.serverdrivenui.common.theme.CaliclanTheme
import com.example.serverdrivenui.native.viewmodels.AttendanceViewModel
import io.ktor.client.*
import kotlinx.coroutines.launch

/**
 * AppWithNativeOverlay - Wraps any content and adds native screen overlays
 * 
 * This composable adds:
 * - A floating action button for attendance scanning
 * - A modal scanner overlay when FAB is tapped
 * 
 * The scanner screen is native (not Zipline/Redwood) because:
 * 1. Camera access requires platform APIs
 * 2. QR scanning needs real-time processing
 * 3. Immediate feedback is critical for UX
 * 
 * Note: This uses direct Ktor HTTP calls, NOT core-data module,
 * so that composeApp stays independent and core-data can be OTA updated.
 * 
 * @param httpClient The shared Ktor HttpClient
 * @param supabaseUrl Supabase project URL
 * @param supabaseKey Supabase anon key
 * @param userId Current logged-in user ID (from storage)
 * @param accessToken Current access token (from storage)
 * @param cameraPreview Platform-specific camera preview composable (Android/iOS)
 * @param onAttendanceMarked Called when attendance is successfully marked
 * @param content The main app content (typically TreehouseContent)
 */
@Composable
fun AppWithNativeOverlay(
    httpClient: HttpClient?,
    supabaseUrl: String,
    supabaseKey: String,
    userId: String?,
    accessToken: String?,
    cameraPreview: (@Composable (onQrDetected: (String) -> Unit) -> Unit)? = null,
    onAttendanceMarked: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var showScanner by remember { mutableStateOf(false) }
    
    // ViewModel is created when httpClient is available
    val viewModel = remember(httpClient, userId, accessToken) { 
        httpClient?.let { 
            AttendanceViewModel(
                httpClient = it,
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey,
                userId = userId,
                accessToken = accessToken,
                onAttendanceMarked = onAttendanceMarked
            )
        }
    }
    val scope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main app content
        content()
        
        // FAB - positioned above bottom navigation (about 80dp from bottom + nav bar padding)
        // Only show if camera preview and httpClient are available AND user is logged in
        if (cameraPreview != null && httpClient != null && userId != null) {
            FloatingActionButton(
                onClick = { 
                    showScanner = true
                    viewModel?.resetState()
                    viewModel?.startScanning()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .padding(bottom = 80.dp) // Above bottom navigation
                    .navigationBarsPadding(),
                containerColor = CaliclanTheme.Accent,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                // QR Code scan icon (using text as icon since we don't have Material Icons Extended)
                Text(
                    text = "⊞",  // QR-like symbol
                    color = CaliclanTheme.Background,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        
        // Scanner overlay (full screen modal)
        if (showScanner && cameraPreview != null && viewModel != null) {
            AttendanceScannerScreen(
                viewModel = viewModel,
                cameraPreview = { onQrDetected ->
                    cameraPreview { code ->
                        scope.launch {
                            viewModel.onQrCodeDetected(code)
                        }
                    }
                },
                onDismiss = { showScanner = false }
            )
        }
    }
}
