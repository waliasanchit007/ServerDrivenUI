package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * GreetingHeaderComposable - Built from primitives (Server-Driven UI compliant)
 */
@Composable
fun GreetingHeaderComposable(
    subtitle: String,
    title: String
) {
    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
        SecondaryText(text = subtitle)
        Spacer(width = 0, height = 4)
        HeaderText(text = title, size = "large")
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
    SduiCard(onClick = onClick) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
            // Header
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top") {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                    Chip(label = label.uppercase())
                    Spacer(width = 0, height = 4)
                    HeaderText(text = focus, size = "medium")
                }
                SecondaryText(text = "›")
            }
            
            // Goals
            if (goals.isNotEmpty()) {
                Spacer(width = 0, height = 16)
                SecondaryText(text = "Focus Areas")
                Spacer(width = 0, height = 8)
                FlexRow(horizontalArrangement = "Start", verticalAlignment = "Center") {
                    goals.forEachIndexed { index, goal ->
                        Chip(label = goal)
                        if (index < goals.size - 1) {
                            Spacer(width = 8, height = 0)
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
    SduiCard(onClick = null) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
            Chip(label = label.uppercase())
            Spacer(width = 0, height = 8)
            HeaderText(text = title, size = "small")
            Spacer(width = 0, height = 8)
            SecondaryText(text = message)
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
    SduiCard(onClick = onClick) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center") {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                    HeaderText(text = title, size = "small")
                    Spacer(width = 0, height = 4)
                    SecondaryText(text = subtitle)
                }
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End") {
                    HeaderText(text = "$daysLeft", size = "medium")
                    SecondaryText(text = "days left")
                }
            }
            Spacer(width = 0, height = 8)
            Chip(label = status.uppercase())
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
    SduiCard(onClick = null) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center") {
                SecondaryText(text = "This Week")
                HeaderText(text = "🔥 $streak", size = "small")
            }
            Spacer(width = 0, height = 12)
            FlexRow(horizontalArrangement = "SpaceEvenly", verticalAlignment = "Center") {
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, status ->
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Center") {
                        SecondaryText(text = dayLabels.getOrElse(index) { "?" })
                        Spacer(width = 0, height = 4)
                        val icon = when (status) {
                            "attended" -> "✓"
                            "today" -> "●"
                            "missed" -> "✗"
                            else -> "○"
                        }
                        SecondaryText(text = icon)
                    }
                }
            }
            Spacer(width = 0, height = 12)
            SecondaryText(text = summary)
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
    SduiCard(onClick = onClick) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Center") {
            AsyncImage(url = photoUrl, contentDescription = name, size = 64, circular = true)
            Spacer(width = 0, height = 8)
            HeaderText(text = name, size = "small")
            SecondaryText(text = role)
        }
    }
}
