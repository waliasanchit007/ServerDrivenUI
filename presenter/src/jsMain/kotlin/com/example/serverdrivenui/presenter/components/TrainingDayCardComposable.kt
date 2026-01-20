package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * TrainingDayCard - Composable built from primitives (Server-Driven UI compliant)
 * 
 * This card is built entirely using schema primitives (SduiCard, FlexColumn, FlexRow, 
 * HeaderText, SecondaryText, Chip, etc.) so the layout can be updated via Zipline
 * without requiring an app release.
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
    // Card with conditional styling based on isToday
    // Since SduiCard doesn't support dynamic styling, we use the default card
    // The Host will handle the border/background based on content context
    SduiCard(onClick = null) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
            // Day Header Row
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top") {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                    // Day name with optional badges
                    FlexRow(horizontalArrangement = "Start", verticalAlignment = "Center") {
                        if (isToday) {
                            // Highlighted day for today
                            HeaderText(text = day, size = "small")
                            Spacer(width = 8, height = 0)
                            Chip(label = "TODAY")
                        } else if (attended) {
                            SecondaryText(text = day)
                            Spacer(width = 8, height = 0)
                            Chip(label = "✓")
                        } else {
                            SecondaryText(text = day)
                        }
                    }
                    Spacer(width = 0, height = 4)
                    HeaderText(text = focus, size = "medium")
                }
                SecondaryText(text = date)
            }
            
            Spacer(width = 0, height = 16)
            
            // Primary Goals Section
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "Center") {
                SecondaryText(text = "🎯 Primary Goals")
            }
            Spacer(width = 0, height = 8)
            
            // Goals as chips
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "Center") {
                goals.forEachIndexed { index, goal ->
                    Chip(label = goal)
                    if (index < goals.size - 1) {
                        Spacer(width = 8, height = 0)
                    }
                }
            }
            
            // Supporting Section (if not empty)
            if (supporting.isNotEmpty()) {
                Spacer(width = 0, height = 16)
                FlexRow(horizontalArrangement = "Start", verticalAlignment = "Center") {
                    SecondaryText(text = "🕐 Supporting")
                }
                Spacer(width = 0, height = 4)
                SecondaryText(text = supporting.joinToString(" • "))
            }
        }
    }
}
