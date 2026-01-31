package com.example.serverdrivenui.native.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serverdrivenui.common.theme.CaliclanTheme
import com.example.serverdrivenui.native.viewmodels.AttendanceScanState
import com.example.serverdrivenui.native.viewmodels.AttendanceViewModel
import kotlinx.coroutines.delay

/**
 * Full-screen QR scanner for attendance marking.
 * 
 * This is a pure Compose Multiplatform screen (not Zipline/Redwood).
 * The camera preview is handled by platform-specific code.
 * 
 * @param viewModel The AttendanceViewModel for state management
 * @param cameraPreview Platform-specific camera preview composable
 * @param onDismiss Called when user wants to close the scanner
 */
@Composable
fun AttendanceScannerScreen(
    viewModel: AttendanceViewModel,
    cameraPreview: @Composable (onQrDetected: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Auto-dismiss after success (with longer delay for user to read)
    LaunchedEffect(state) {
        if (state is AttendanceScanState.Success || state is AttendanceScanState.AlreadyCheckedIn) {
            delay(3000)
            onDismiss()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CaliclanTheme.Background)
    ) {
        // Camera Preview (full screen)
        when (state) {
            is AttendanceScanState.Idle,
            is AttendanceScanState.Scanning,
            is AttendanceScanState.Processing -> {
                cameraPreview { qrCode ->
                    // This callback is invoked when QR is detected
                    // Will be handled by coroutine scope in parent
                }
                
                // Scanning overlay
                ScannerOverlay(
                    isProcessing = state is AttendanceScanState.Processing
                )
            }
            else -> {
                // Show result screen with gradient background instead of camera
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CaliclanTheme.Background)
                )
            }
        }
        
        // Top bar with close button
        TopBar(
            onClose = onDismiss,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Result overlay - full screen centered
        AnimatedVisibility(
            visible = state !is AttendanceScanState.Idle && 
                      state !is AttendanceScanState.Scanning &&
                      state !is AttendanceScanState.Processing,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(300)
            ),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            ResultCard(
                state = state,
                onRetry = { 
                    viewModel.resetState()
                    viewModel.startScanning()
                },
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Scanner overlay with targeting frame
 */
@Composable
private fun ScannerOverlay(
    isProcessing: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent overlay around the scanner frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Scanner frame
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.Transparent)
                    .border(
                        width = 3.dp,
                        color = if (isProcessing) CaliclanTheme.Accent else Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Scanning line animation
                if (!isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = (280 * scanLineOffset).dp)
                            .background(CaliclanTheme.Accent)
                    )
                }
                
                // Processing indicator
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        color = CaliclanTheme.Accent,
                        strokeWidth = 4.dp
                    )
                }
            }
            
            // Instructions
            Text(
                text = if (isProcessing) "Verifying..." else "Point at gym QR code",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            // Subtitle
            if (!isProcessing) {
                Text(
                    text = "Make sure the QR code is within the frame",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Top bar with close button
 */
@Composable
private fun TopBar(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Scan Attendance",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}

/**
 * Result card showing success/error/already checked in
 */
@Composable
private fun ResultCard(
    state: AttendanceScanState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, iconColor, backgroundColor, title, message, emoji) = when (state) {
        is AttendanceScanState.Success -> {
            ResultData(
                icon = Icons.Default.Check,
                iconColor = CaliclanTheme.Success,
                backgroundColor = CaliclanTheme.SuccessBg,
                title = "You're Checked In!",
                message = state.message,
                emoji = "🎉"
            )
        }
        is AttendanceScanState.Error -> {
            ResultData(
                icon = Icons.Default.Close,
                iconColor = CaliclanTheme.Error,
                backgroundColor = CaliclanTheme.ErrorBg,
                title = "Oops!",
                message = state.message,
                emoji = "😕"
            )
        }
        is AttendanceScanState.AlreadyCheckedIn -> {
            ResultData(
                icon = Icons.Default.Info,
                iconColor = CaliclanTheme.Accent,
                backgroundColor = CaliclanTheme.AccentMuted,
                title = "Already Checked In",
                message = "You checked in today at ${state.time}",
                emoji = "✅"
            )
        }
        else -> return
    }
    
    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CaliclanTheme.Surface
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Large emoji
            Text(
                text = emoji,
                fontSize = 64.sp
            )
            
            // Icon with background
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = iconColor
                )
            }
            
            // Title
            Text(
                text = title,
                color = CaliclanTheme.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Message
            Text(
                text = message,
                color = CaliclanTheme.TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            when (state) {
                is AttendanceScanState.Error -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CaliclanTheme.Accent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Try Again",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Cancel",
                                color = CaliclanTheme.TextSecondary
                            )
                        }
                    }
                }
                is AttendanceScanState.Success -> {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CaliclanTheme.Success
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                is AttendanceScanState.AlreadyCheckedIn -> {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CaliclanTheme.Accent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Got it!",
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

/**
 * Data class for result card content
 */
private data class ResultData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val title: String,
    val message: String,
    val emoji: String
)
