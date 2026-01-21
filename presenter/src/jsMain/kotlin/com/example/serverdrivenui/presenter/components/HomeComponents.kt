package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * GreetingHeaderComposable - Welcome header with user name
 */
@Composable
fun GreetingHeaderComposable(
    subtitle: String,
    title: String
) {
    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
        StyledText(text = subtitle, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
        StyledText(text = title, style = "headline", color = "primary", fontWeight = "bold", letterSpacing = 0)
    }
}

/**
 * TrainingSessionCardComposable - Today's session with goals
 */
@Composable
fun TrainingSessionCardComposable(
    label: String,
    focus: String,
    goals: List<String>,
    onClick: (() -> Unit)?
) {
    SduiCard(
        onClick = onClick, 
        backgroundColor = "surface", 
        borderColor = "accent", 
        borderWidth = 2, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
            // Header
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 0, padding = 0) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledBox(backgroundColor = "accentmuted", borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                        StyledText(text = label.uppercase(), style = "labelSmall", color = "accent", fontWeight = "bold", letterSpacing = 1)
                    }
                    Spacer(width = 0, height = 4)
                    StyledText(text = focus, style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                }
                StyledText(text = "›", style = "title", color = "secondary", fontWeight = "normal", letterSpacing = 0)
            }
            
            // Goals
            if (goals.isNotEmpty()) {
                Divider(color = "border")
                StyledText(text = "Focus Areas", style = "bodySmall", color = "secondary", fontWeight = "medium", letterSpacing = 0)
                FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                    goals.forEach { goal ->
                        StyledBox(backgroundColor = "surfacevariant", borderRadius = 16, padding = 0, paddingHorizontal = 12, paddingVertical = 6, width = -1, height = -1, contentAlignment = "Center") {
                            StyledText(text = goal, style = "labelSmall", color = "primary", fontWeight = "medium", letterSpacing = 0)
                        }
                    }
                }
            }
        }
    }
}

/**
 * AnnouncementCardComposable - Coach updates
 */
@Composable
fun AnnouncementCardComposable(
    label: String,
    title: String,
    message: String
) {
    SduiCard(
        onClick = null, 
        backgroundColor = "surface", 
        borderColor = "accent", 
        borderWidth = 1, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
            StyledBox(backgroundColor = "accentmuted", borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                StyledText(text = label.uppercase(), style = "labelSmall", color = "accent", fontWeight = "bold", letterSpacing = 1)
            }
            StyledText(text = title, style = "titleSmall", color = "primary", fontWeight = "semibold", letterSpacing = 0)
            StyledText(text = message, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
        }
    }
}

/**
 * StatusCardComposable - Membership status display
 */
@Composable
fun StatusCardComposable(
    status: String,
    title: String,
    subtitle: String,
    daysLeft: Int,
    onClick: (() -> Unit)?
) {
    // Status-based styling
    val borderColor = when (status.lowercase()) {
        "active" -> "success"
        "expiring" -> "accent"
        else -> "error"
    }
    val statusBgColor = when (status.lowercase()) {
        "active" -> "successbg"
        "expiring" -> "accentmuted"
        else -> "surfacevariant"
    }
    val statusTextColor = when (status.lowercase()) {
        "active" -> "success"
        "expiring" -> "accent"
        else -> "error"
    }
    
    SduiCard(
        onClick = onClick, 
        backgroundColor = "surface", 
        borderColor = borderColor, 
        borderWidth = 2, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = title, style = "titleSmall", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                    StyledText(text = subtitle, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End", spacing = 2, padding = 0) {
                    StyledText(text = "$daysLeft", style = "headlineMedium", color = statusTextColor, fontWeight = "bold", letterSpacing = 0)
                    StyledText(text = "days left", style = "labelSmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
            }
            StyledBox(backgroundColor = statusBgColor, borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                StyledText(text = status.uppercase(), style = "labelSmall", color = statusTextColor, fontWeight = "bold", letterSpacing = 1)
            }
        }
    }
}

/**
 * WeeklyAttendanceComposable - 7-day attendance strip
 */
@Composable
fun WeeklyAttendanceComposable(
    streak: Int,
    days: List<String>,
    summary: String
) {
    SduiCard(
        onClick = null, 
        backgroundColor = "surface", 
        borderColor = "border", 
        borderWidth = 1, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                StyledText(text = "This Week", style = "bodySmall", color = "secondary", fontWeight = "medium", letterSpacing = 0)
                FlexRow(horizontalArrangement = "End", verticalAlignment = "CenterVertically", spacing = 4, padding = 0) {
                    StyledText(text = "🔥", style = "body", color = "primary", fontWeight = "normal", letterSpacing = 0)
                    StyledText(text = "$streak", style = "titleSmall", color = "accent", fontWeight = "bold", letterSpacing = 0)
                }
            }
            
            FlexRow(horizontalArrangement = "SpaceEvenly", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, status ->
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "CenterHorizontally", spacing = 4, padding = 0) {
                        StyledText(text = dayLabels.getOrElse(index) { "?" }, style = "labelSmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                        val (icon, color) = when (status) {
                            "attended" -> "✓" to "success"
                            "today" -> "●" to "accent"
                            "missed" -> "✗" to "error"
                            else -> "○" to "muted"
                        }
                        StyledText(text = icon, style = "body", color = color, fontWeight = "bold", letterSpacing = 0)
                    }
                }
            }
            
            StyledText(text = summary, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
        }
    }
}

/**
 * CoachCardComposable - Coach preview card
 */
@Composable
fun CoachCardComposable(
    name: String,
    role: String,
    photoUrl: String,
    onClick: () -> Unit
) {
    SduiCard(
        onClick = onClick, 
        backgroundColor = "surface", 
        borderColor = "border", 
        borderWidth = 1, 
        borderRadius = 12, 
        padding = 12
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "CenterHorizontally", spacing = 8, padding = 0) {
            AsyncImage(url = photoUrl, contentDescription = name, size = 64, circular = true)
            StyledText(text = name, style = "titleSmall", color = "primary", fontWeight = "semibold", letterSpacing = 0)
            StyledText(text = role, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
        }
    }
}
