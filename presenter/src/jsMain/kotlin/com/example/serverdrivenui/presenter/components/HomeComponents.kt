package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * GreetingHeaderComposable - Welcome header with user name
 * Reference: Home.tsx lines 86-89
 */
@Composable
fun GreetingHeaderComposable(
    subtitle: String,
    title: String
) {
    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
        StyledText(text = subtitle, style = "body", color = "secondary", fontWeight = "normal", letterSpacing = 0)
        StyledText(text = title, style = "headlineMedium", color = "primary", fontWeight = "bold", letterSpacing = 0)
    }
}

/**
 * StatusCardComposable - Membership status card
 * Reference: Home.tsx lines 91-122
 * Layout:
 * ┌─────────────────────────────────────┐
 * │ MEMBERSHIP                     📅   │
 * │ Active                              │
 * │                                     │
 * │ Expires on                          │
 * │ February 15, 2025                   │
 * │ 30 days remaining                   │
 * └─────────────────────────────────────┘
 */
@Composable
fun StatusCardComposable(
    status: String,
    expiryDate: String?,
    daysLeft: Int,
    onClick: (() -> Unit)?
) {
    // Status-based styling matching reference design
    val borderColor = when (status.lowercase()) {
        "active" -> "success"
        "expiring" -> "accent"
        else -> "error"
    }
    
    val statusText = when (status.lowercase()) {
        "active" -> "Active"
        "expiring" -> "Expiring Soon"
        "expired", "inactive" -> "Inactive"
        else -> status
    }
    
    SduiCard(
        onClick = onClick, 
        backgroundColor = "surface", 
        borderColor = borderColor, 
        borderWidth = 2, 
        borderRadius = 16, 
        padding = 20
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 16, padding = 0) {
            // Header row: "MEMBERSHIP" label + calendar icon
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                StyledText(text = "MEMBERSHIP", style = "labelSmall", color = "secondary", fontWeight = "medium", letterSpacing = 1)
                StyledText(text = "📅", style = "body", color = "secondary", fontWeight = "normal", letterSpacing = 0)
            }
            
            // Status text (large)
            StyledText(text = statusText, style = "titleLarge", color = "primary", fontWeight = "semibold", letterSpacing = 0)
            
            // Expiry info section
            if (status.lowercase() == "active" || status.lowercase() == "expiring") {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = "Expires on", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    StyledText(text = expiryDate ?: "—", style = "titleMedium", color = "primary", fontWeight = "medium", letterSpacing = 0)
                    if (daysLeft > 0 && daysLeft <= 30) {
                        Spacer(width = 0, height = 4)
                        StyledText(text = "$daysLeft days remaining", style = "bodySmall", color = "accent", fontWeight = "medium", letterSpacing = 0)
                    }
                }
            } else {
                // Inactive state - show CTA
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                    StyledText(text = "No active membership", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    ActionButton(
                        variant = "primary",
                        text = "View Plans",
                        icon = "arrow-right",
                        onClick = onClick ?: {}
                    )
                }
            }
        }
    }
}

/**
 * TrainingSessionCardComposable - Today's session with goals
 * Reference: Home.tsx lines 124-148
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
        borderColor = "border", 
        borderWidth = 1, 
        borderRadius = 16, 
        padding = 20
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 16, padding = 0) {
            // Header row with label, focus, and chevron
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 16, padding = 0) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = label.uppercase(), style = "labelSmall", color = "secondary", fontWeight = "medium", letterSpacing = 1)
                    StyledText(text = focus, style = "titleLarge", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                }
                // Chevron with proper spacing
                StyledText(text = "›", style = "headlineMedium", color = "muted", fontWeight = "normal", letterSpacing = 0)
            }
            
            // Focus Areas section
            if (goals.isNotEmpty()) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                    StyledText(text = "Focus Areas", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    FlexRow(horizontalArrangement = "Wrap", verticalAlignment = "Top", spacing = 8, padding = 0) {
                        goals.forEach { goal ->
                            StyledBox(backgroundColor = "surfacevariant", borderRadius = 8, padding = 0, paddingHorizontal = 12, paddingVertical = 8, width = -1, height = -1, contentAlignment = "Center") {
                                StyledText(text = goal, style = "bodySmall", color = "primary", fontWeight = "normal", letterSpacing = 0)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * WeeklyAttendanceComposable - Training consistency tracker
 * Reference: Home.tsx lines 150-192 - uses box-based indicators
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
        borderRadius = 16, 
        padding = 20
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 16, padding = 0) {
            // Header with flame icon and streak count
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                StyledText(text = "🔥", style = "titleMedium", color = "accent", fontWeight = "normal", letterSpacing = 0)
                StyledText(text = "$streak-day streak", style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
            }
            
            // Weekly attendance grid - box-based indicators
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
                FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 0, padding = 0) {
                    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEachIndexed { index, status ->
                        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "CenterHorizontally", spacing = 6, padding = 0) {
                            // Day label
                            StyledText(text = dayLabels.getOrElse(index) { "?" }, style = "labelSmall", color = "muted", fontWeight = "normal", letterSpacing = 0)
                            // Box indicator
                            val (bgColor, dotColor) = when (status) {
                                "attended" -> "successmuted" to "success"
                                "today" -> "accent" to "background"
                                "missed" -> "surfacevariant" to "transparent"
                                else -> "surfacevariant" to "transparent"
                            }
                            StyledBox(
                                backgroundColor = bgColor, 
                                borderRadius = 8, 
                                padding = 0, 
                                paddingHorizontal = 0, 
                                paddingVertical = 0, 
                                width = 32, 
                                height = 32, 
                                contentAlignment = "Center"
                            ) {
                                if (status == "attended") {
                                    StyledText(text = "●", style = "labelSmall", color = dotColor, fontWeight = "bold", letterSpacing = 0)
                                } else if (status == "today") {
                                    StyledText(text = "●", style = "labelSmall", color = "background", fontWeight = "bold", letterSpacing = 0)
                                }
                            }
                        }
                    }
                }
                
                // Divider and summary
                Divider(color = "border")
                StyledText(text = summary, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
            }
        }
    }
}

/**
 * AnnouncementCardComposable - Coach updates
 * Reference: Home.tsx lines 194-203
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
        borderColor = "accentmuted", 
        borderWidth = 1, 
        borderRadius = 16, 
        padding = 20
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
            StyledText(text = "COACH UPDATE", style = "labelSmall", color = "accent", fontWeight = "medium", letterSpacing = 1)
            StyledText(text = title, style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
            StyledText(text = message, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
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
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "CenterHorizontally", spacing = 12, padding = 0) {
            AsyncImage(url = photoUrl, contentDescription = name, size = 64, circular = true)
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "CenterHorizontally", spacing = 4, padding = 0) {
                StyledText(text = name, style = "titleSmall", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                StyledText(text = role, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
            }
        }
    }
}
