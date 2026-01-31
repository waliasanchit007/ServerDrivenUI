package com.example.serverdrivenui.common.theme

import androidx.compose.ui.graphics.Color

/**
 * Caliclan Design Tokens - matches web app neutral-950 palette
 * 
 * This theme is shared between:
 * - Zipline/Redwood widgets (Protocol.kt)
 * - Native KMP screens (AttendanceScannerScreen, etc.)
 */
object CaliclanTheme {
    // Backgrounds (from web tailwind neutral scale)
    val Background = Color(0xFF0A0A0A)  // neutral-950
    val Surface = Color(0xFF171717)     // neutral-900
    val SurfaceVariant = Color(0xFF262626)  // neutral-800
    val SurfaceElevated = Color(0xFF262626) // neutral-800
    
    // Accent colors (amber scale)
    val Accent = Color(0xFFF59E0B)      // amber-500
    val AccentDark = Color(0xFF92400E)  // amber-900/50 approx
    val AccentMuted = Color(0xFF78350F) // amber-950/40 approx
    
    // Status colors
    val Success = Color(0xFF059669)     // emerald-600
    val SuccessBg = Color(0xFF064E3B)   // emerald-950/40 approx
    val SuccessMuted = Color(0xFF064E3B).copy(alpha = 0.4f)
    val Error = Color(0xFFDC2626)       // red-600
    val ErrorBg = Color(0xFF7F1D1D)     // red-950/40 approx
    
    // Text
    val TextPrimary = Color(0xFFFAFAFA)   // neutral-50
    val TextSecondary = Color(0xFFA3A3A3) // neutral-400
    val TextMuted = Color(0xFF737373)     // neutral-500
    
    // Borders
    val Border = Color(0xFF262626)        // neutral-800
    val BorderLight = Color(0xFF404040)   // neutral-700
    
    // Overlay for modals/sheets
    val Overlay = Color.Black.copy(alpha = 0.5f)
}
