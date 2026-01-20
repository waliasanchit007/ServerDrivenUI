package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * MembershipPlanCardComposable - Built from primitives (Server-Driven UI compliant)
 * 
 * This card is built entirely using schema primitives so the layout can be updated
 * via Zipline without requiring an app release.
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
    SduiCard(onClick = null) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
            // Label badge
            if (isCurrent) {
                Chip(label = "CURRENT PLAN")
                Spacer(width = 0, height = 8)
            }
            if (isRecommended && !isCurrent) {
                Chip(label = "RECOMMENDED")
                Spacer(width = 0, height = 12)
            }
            
            // Header: Name/Duration + Price
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Top") {
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                    HeaderText(text = name, size = "small")
                    Spacer(width = 0, height = 4)
                    SecondaryText(text = duration)
                }
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End") {
                    HeaderText(text = price, size = "small")
                    SecondaryText(text = priceLabel)
                }
            }
            
            Spacer(width = 0, height = 16)
            
            // Features list
            features.forEach { feature ->
                FlexRow(horizontalArrangement = "Start", verticalAlignment = "Top") {
                    SecondaryText(text = "✓")
                    Spacer(width = 8, height = 0)
                    SecondaryText(text = feature)
                }
                Spacer(width = 0, height = 8)
            }
            
            // Billing date (current plan only)
            if (isCurrent && billingDate.isNotEmpty()) {
                Spacer(width = 0, height = 8)
                SecondaryText(text = "Next billing date")
                Spacer(width = 0, height = 4)
                HeaderText(text = billingDate, size = "small")
            }
            
            // Select button (non-current plans)
            if (!isCurrent && onSelect != null) {
                Spacer(width = 0, height = 16)
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
