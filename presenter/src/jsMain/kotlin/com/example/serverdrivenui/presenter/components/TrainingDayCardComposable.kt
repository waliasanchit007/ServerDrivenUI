package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * TrainingDayCard - Training schedule card with proper alignment
 * Reference: Training.tsx lines 90-155
 * 
 * Layout:
 * ┌─────────────────────────────────────────┐
 * │ Monday     ✓ TODAY              Jan 27  │
 * │ Pull Strength                           │
 * │                                         │
 * │ 🎯 Primary Goals                        │
 * │ [Chip] [Chip] [Chip]                    │
 * │                                         │
 * │ 🕐 Supporting                           │
 * │ Core Stability • Shoulder Mobility      │
 * └─────────────────────────────────────────┘
 */
@Composable
fun TrainingDayCardComposable(
    day: String,
    date: String,
    focus: String,
    goals: List<String>,
    supporting: List<String>,
    isToday: Boolean,
    attended: Boolean
) {
    // Card border based on state
    val borderColor = when {
        isToday -> "accent"
        attended -> "border"
        else -> "border"
    }
    val borderWidth = if (isToday) 2 else 1
    val backgroundColor = if (isToday) "accentmuted" else "surface"
    
    SduiCard(
        onClick = null, 
        backgroundColor = backgroundColor, 
        borderColor = borderColor, 
        borderWidth = borderWidth, 
        borderRadius = 16, 
        padding = 20
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 16, padding = 0) {
            // Header Row: Day + badges on left, Date on right
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 16, padding = 0) {
                // Left column: Day, badges, focus
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                    // Day name row with badges
                    FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                        // Day name
                        val dayColor = when {
                            isToday -> "accent"
                            attended -> "secondary"
                            else -> "secondary"
                        }
                        StyledText(text = day, style = "bodySmall", color = dayColor, fontWeight = "normal", letterSpacing = 0)
                        
                        // Today badge
                        if (isToday) {
                            StyledBox(backgroundColor = "accent", borderRadius = 12, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                                StyledText(text = "TODAY", style = "labelSmall", color = "background", fontWeight = "bold", letterSpacing = 1)
                            }
                        }
                        
                        // Attended check
                        if (attended && !isToday) {
                            StyledBox(backgroundColor = "successmuted", borderRadius = 12, padding = 0, paddingHorizontal = 0, paddingVertical = 0, width = 20, height = 20, contentAlignment = "Center") {
                                StyledText(text = "✓", style = "labelSmall", color = "success", fontWeight = "bold", letterSpacing = 0)
                            }
                        }
                    }
                    
                    // Focus title (larger)
                    StyledText(text = focus, style = "titleLarge", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                }
                
                // Date on right - formatted like "Jan 27"
                StyledText(text = date, style = "bodySmall", color = "muted", fontWeight = "normal", letterSpacing = 0)
            }
            
            // Primary Goals Section
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                    StyledText(text = "🎯", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    StyledText(text = "Primary Goals", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
                
                // Goals as chips with wrap
                FlexRow(horizontalArrangement = "Wrap", verticalAlignment = "Top", spacing = 8, padding = 0) {
                    goals.forEach { goal ->
                        val chipBg = if (isToday) "accentmuted" else "surfacevariant"
                        val chipTextColor = if (isToday) "accent" else "primary"
                        StyledBox(backgroundColor = chipBg, borderRadius = 8, padding = 0, paddingHorizontal = 12, paddingVertical = 8, width = -1, height = -1, contentAlignment = "Center") {
                            StyledText(text = goal, style = "bodySmall", color = chipTextColor, fontWeight = "normal", letterSpacing = 0)
                        }
                    }
                }
            }
            
            // Supporting Section
            if (supporting.isNotEmpty()) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                        StyledText(text = "🕐", style = "bodySmall", color = "muted", fontWeight = "normal", letterSpacing = 0)
                        StyledText(text = "Supporting", style = "bodySmall", color = "muted", fontWeight = "normal", letterSpacing = 0)
                    }
                    StyledText(text = supporting.joinToString(" • "), style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
            }
        }
    }
}
