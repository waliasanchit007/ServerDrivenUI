package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * ProfileInfoCardComposable - Built from styled primitives
 */
@Composable
fun ProfileInfoCardComposable(
    name: String,
    email: String,
    phone: String,
    batch: String,
    memberSince: String
) {
    SduiCard(
        onClick = null, 
        backgroundColor = "surface", 
        borderColor = "border", 
        borderWidth = 1, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
            // Avatar + Name row
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 16, padding = 0) {
                // Avatar (circular with initials)
                StyledBox(backgroundColor = "accent", borderRadius = 24, padding = 0, paddingHorizontal = 0, paddingVertical = 0, width = 48, height = 48, contentAlignment = "Center") {
                    StyledText(text = name.take(2).uppercase(), style = "titleMedium", color = "surface", fontWeight = "bold", letterSpacing = 0)
                }
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = name, style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                    if (memberSince.isNotEmpty()) {
                        StyledText(text = "Member since $memberSince", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    }
                }
            }
            
            Spacer(width = 0, height = 24)
            Divider(color = "border")
            Spacer(width = 0, height = 16)
            
            // Info rows with proper spacing using FlexColumn for label-value pairs
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
                // Email row
                FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 16, padding = 0) {
                    StyledText(text = "Email", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    Spacer(width = 8, height = 0)
                    StyledText(
                        text = if (email.isNotEmpty()) email else "Not set", 
                        style = "bodySmall", 
                        color = if (email.isNotEmpty()) "primary" else "muted", 
                        fontWeight = "normal", 
                        letterSpacing = 0
                    )
                }
                
                // Batch row  
                FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 16, padding = 0) {
                    StyledText(text = "Batch", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    Spacer(width = 8, height = 0)
                    StyledText(
                        text = if (batch.isNotEmpty()) batch else "Not assigned", 
                        style = "bodySmall", 
                        color = if (batch.isNotEmpty()) "primary" else "muted", 
                        fontWeight = "normal", 
                        letterSpacing = 0
                    )
                }
            }
        }
    }
}

/**
 * HistoryItemComposable - Built from styled primitives
 */
@Composable
fun HistoryItemComposable(
    title: String,
    subtitle: String,
    status: String,
    amount: String
) {
    // Status colors
    val statusColor = when (status.lowercase()) {
        "active" -> "success"
        "completed" -> "success"
        else -> "secondary"
    }
    val statusBgColor = when (status.lowercase()) {
        "active" -> "successbg"
        else -> "surfacevariant"
    }
    
    SduiCard(
        onClick = null, 
        backgroundColor = "surface", 
        borderColor = "border", 
        borderWidth = 1, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 16, padding = 0) {
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                StyledText(text = title, style = "body", color = "primary", fontWeight = "medium", letterSpacing = 0)
                StyledText(text = subtitle, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
            }
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End", spacing = 4, padding = 0) {
                if (amount.isNotEmpty()) {
                    StyledText(text = amount, style = "body", color = "primary", fontWeight = "medium", letterSpacing = 0)
                }
                StyledBox(backgroundColor = statusBgColor, borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                    StyledText(text = status.replaceFirstChar { it.uppercase() }, style = "labelSmall", color = statusColor, fontWeight = "semibold", letterSpacing = 0)
                }
            }
        }
    }
}
