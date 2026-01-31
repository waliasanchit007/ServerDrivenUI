package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * MembershipPlanCardComposable - Membership plan card with proper alignment
 * Reference: Membership.tsx lines 68-148
 * 
 * Current Plan Layout:
 * ┌─────────────────────────────────────────┐
 * │ CURRENT PLAN                            │
 * ├─────────────────────────────────────────┤
 * │ Monthly Unlimited            ₹2,500     │
 * │ 1 Month                      per month  │
 * ├─────────────────────────────────────────┤
 * │ ✓ Unlimited access to all sessions      │
 * │ ✓ Weekly structured training program    │
 * │ ✓ Community support                     │
 * │ ✓ Coach guidance                        │
 * ├─────────────────────────────────────────┤
 * │ Next billing date                       │
 * │ February 15, 2025                       │
 * └─────────────────────────────────────────┘
 * 
 * Available Plan Layout:
 * ┌─────────────────────────────────────────┐
 * │ RECOMMENDED                             │
 * ├─────────────────────────────────────────┤
 * │ Quarterly Unlimited          ₹6,500     │
 * │ 3 Months                        total   │
 * ├─────────────────────────────────────────┤
 * │ ✓ Feature 1                             │
 * │ ✓ Feature 2                             │
 * ├─────────────────────────────────────────┤
 * │ [       Select Plan    →       ]        │
 * └─────────────────────────────────────────┘
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
        isCurrent -> "accent"
        isRecommended -> "accent"
        else -> "border"
    }
    val borderWidth = if (isCurrent || isRecommended) 2 else 1
    
    SduiCard(
        onClick = if (!isCurrent && onSelect != null) onSelect else null, 
        backgroundColor = "surface", 
        borderColor = borderColor, 
        borderWidth = borderWidth, 
        borderRadius = 16, 
        padding = 20
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 16, padding = 0) {
            // Label badge
            if (isCurrent) {
                StyledText(text = "CURRENT PLAN", style = "labelSmall", color = "accent", fontWeight = "medium", letterSpacing = 1)
            } else if (isRecommended) {
                StyledText(text = "RECOMMENDED", style = "labelSmall", color = "accent", fontWeight = "medium", letterSpacing = 1)
            }
            
            // Header: Name/Duration on left, Price on right
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top", spacing = 16, padding = 0) {
                // Left side: Name and Duration
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = name, style = "titleLarge", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                    StyledText(text = duration, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
                // Right side: Price and unit
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End", spacing = 4, padding = 0) {
                    StyledText(text = priceLabel, style = "titleLarge", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                    StyledText(text = if (isCurrent) "per month" else "total", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                }
            }
            
            // Features list with checkmarks
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                features.forEach { feature ->
                    FlexRow(horizontalArrangement = "Start", verticalAlignment = "Top", spacing = 8, padding = 0) {
                        val checkColor = if (isCurrent) "accent" else "muted"
                        StyledText(text = "✓", style = "body", color = checkColor, fontWeight = "medium", letterSpacing = 0)
                        StyledText(text = feature, style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    }
                }
            }
            
            // Expiry date (current plan only) - with divider
            if (isCurrent && billingDate.isNotEmpty()) {
                Divider(color = "border")
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                    StyledText(text = "Valid Until", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                    StyledText(text = billingDate, style = "titleMedium", color = "primary", fontWeight = "medium", letterSpacing = 0)
                }
            }
            
            // Select button for non-current plans
            if (!isCurrent && onSelect != null) {
                ActionButton(
                    variant = if (isRecommended) "primary" else "secondary",
                    text = "Select Plan",
                    icon = "arrow-right",
                    onClick = onSelect
                )
            }
        }
    }
}
