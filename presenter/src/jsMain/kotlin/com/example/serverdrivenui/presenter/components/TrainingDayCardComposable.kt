package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * TrainingDayCard - Composable built from styled primitives
 * 
 * Uses StyledText, StyledBox with CaliclanTheme semantic colors for proper UI.
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
    // Card with conditional border styling
    val borderColor = if (isToday) "accent" else "border"
    val borderWidth = if (isToday) 2 else 1
    
    SduiCard(
        onClick = null, 
        backgroundColor = "surface", 
        borderColor = borderColor, 
        borderWidth = borderWidth, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
            // Day Header Row
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 0, padding = 0) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    // Day name with optional badges
                    FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                        if (isToday) {
                            // Highlighted day for today - accent color
                            StyledText(text = day, style = "titleSmall", color = "accent", fontWeight = "semibold", letterSpacing = 0)
                            StyledBox(backgroundColor = "accent", borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                                StyledText(text = "TODAY", style = "labelSmall", color = "surface", fontWeight = "bold", letterSpacing = 0)
                            }
                        } else if (attended) {
                            StyledText(text = day, style = "titleSmall", color = "secondary", fontWeight = "medium", letterSpacing = 0)
                            StyledBox(backgroundColor = "successbg", borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                                StyledText(text = "✓", style = "labelSmall", color = "success", fontWeight = "bold", letterSpacing = 0)
                            }
                        } else {
                            StyledText(text = day, style = "titleSmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                        }
                    }
                    // Focus title
                    StyledText(text = focus, style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                }
                // Date on the right
                StyledText(text = date, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
            }
            
            // Primary Goals Section
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 4, padding = 0) {
                StyledText(text = "🎯", style = "body", color = "primary", fontWeight = "normal", letterSpacing = 0)
                StyledText(text = "Primary Goals", style = "bodySmall", color = "secondary", fontWeight = "medium", letterSpacing = 0)
            }
            
            // Goals as chips
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                goals.forEach { goal ->
                    StyledBox(backgroundColor = "surfacevariant", borderRadius = 16, padding = 0, paddingHorizontal = 12, paddingVertical = 6, width = -1, height = -1, contentAlignment = "Center") {
                        StyledText(text = goal, style = "labelSmall", color = "primary", fontWeight = "medium", letterSpacing = 0)
                    }
                }
            }
            
            // Supporting Section (if not empty)
            if (supporting.isNotEmpty()) {
                FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 4, padding = 0) {
                    StyledText(text = "🕐", style = "body", color = "primary", fontWeight = "normal", letterSpacing = 0)
                    StyledText(text = "Supporting", style = "bodySmall", color = "secondary", fontWeight = "medium", letterSpacing = 0)
                }
                StyledText(text = supporting.joinToString(" • "), style = "bodySmall", color = "muted", fontWeight = "normal", letterSpacing = 0)
            }
        }
    }
}
