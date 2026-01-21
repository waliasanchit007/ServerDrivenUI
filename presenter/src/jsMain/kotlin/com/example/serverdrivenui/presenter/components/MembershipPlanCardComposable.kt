package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * MembershipPlanCardComposable - Built from styled primitives
 * 
 * Uses StyledText, StyledBox with CaliclanTheme semantic colors for proper UI.
 */
@Composable
fun MembershipPlanCardComposable(
    name: String,
    duration: String,
    price: String,
    priceLabel: String,
    features: List<String>,
    isCurrent: Boolean,
    isRecommended: Boolean,
    billingDate: String,
    onSelect: (() -> Unit)?
) {
    // Card styling based on state
    val borderColor = when {
        isCurrent -> "success"
        isRecommended -> "accent"
        else -> "border"
    }
    val borderWidth = if (isCurrent || isRecommended) 2 else 1
    
    SduiCard(
        onClick = if (!isCurrent && onSelect != null) onSelect else null, 
        backgroundColor = "surface", 
        borderColor = borderColor, 
        borderWidth = borderWidth, 
        borderRadius = 12, 
        padding = 16
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
            // Label badge
            if (isCurrent) {
                StyledBox(backgroundColor = "successbg", borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                    StyledText(text = "CURRENT PLAN", style = "labelSmall", color = "success", fontWeight = "bold", letterSpacing = 1)
                }
            }
            if (isRecommended && !isCurrent) {
                StyledBox(backgroundColor = "accentmuted", borderRadius = 4, padding = 0, paddingHorizontal = 8, paddingVertical = 4, width = -1, height = -1, contentAlignment = "Center") {
                    StyledText(text = "RECOMMENDED", style = "labelSmall", color = "accent", fontWeight = "bold", letterSpacing = 1)
                }
            }
            
            // Header: Name/Duration + Price
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 0, padding = 0) {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = name, style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                    StyledText(text = duration, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End", spacing = 2, padding = 0) {
                    StyledText(text = price, style = "titleMedium", color = "primary", fontWeight = "bold", letterSpacing = 0)
                    StyledText(text = priceLabel, style = "labelSmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
            }
            
            // Divider
            Divider(color = "border")
            
            // Features list
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                features.forEach { feature ->
                    FlexRow(horizontalArrangement = "Start", verticalAlignment = "Top", spacing = 8, padding = 0) {
                        StyledText(text = "✓", style = "body", color = "success", fontWeight = "bold", letterSpacing = 0)
                        StyledText(text = feature, style = "bodySmall", color = "primary", fontWeight = "normal", letterSpacing = 0)
                    }
                }
            }
            
            // Billing date (current plan only)
            if (isCurrent && billingDate.isNotEmpty()) {
                Divider(color = "border")
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = "Next billing date", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    StyledText(text = billingDate, style = "titleSmall", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                }
            }
            
            // Select button (non-current plans)
            if (!isCurrent && onSelect != null) {
                ActionButton(
                    variant = if (isRecommended) "primary" else "secondary",
                    text = "Select Plan",
                    icon = "arrow_right",
                    onClick = onSelect
                )
            }
        }
    }
}
