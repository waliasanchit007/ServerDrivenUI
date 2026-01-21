package com.example.serverdrivenui.presenter.components

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * ProfileInfoCardComposable - Built from primitives (Server-Driven UI compliant)
 */
@Composable
fun ProfileInfoCardComposable(
    name: String,
    email: String,
    phone: String,
    batch: String,
    memberSince: String
) {
    SduiCard(onClick = null, backgroundColor = "", borderColor = "", borderWidth = 0, borderRadius = 0, padding = 0) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
            // Avatar + Name row
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "Center", spacing = 0, padding = 0) {
                // Avatar (using initials chip)
                Chip(label = name.take(2).uppercase())
                Spacer(width = 16, height = 0)
                FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
                    HeaderText(text = name, size = "medium")
                    Spacer(width = 0, height = 4)
                    SecondaryText(text = "Member since $memberSince")
                }
            }
            
            Spacer(width = 0, height = 24)
            
            // Info rows
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center", spacing = 0, padding = 0) {
                SecondaryText(text = "Email")
                SecondaryText(text = email)
            }
            
            if (phone.isNotEmpty()) {
                Spacer(width = 0, height = 12)
                FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center", spacing = 0, padding = 0) {
                    SecondaryText(text = "Phone")
                    SecondaryText(text = phone)
                }
            }
            
            Spacer(width = 0, height = 12)
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center", spacing = 0, padding = 0) {
                SecondaryText(text = "Batch")
                SecondaryText(text = batch)
            }
        }
    }
}

/**
 * HistoryItemComposable - Built from primitives (Server-Driven UI compliant)
 */
@Composable
fun HistoryItemComposable(
    title: String,
    subtitle: String,
    status: String,
    amount: String
) {
    SduiCard(onClick = null, backgroundColor = "", borderColor = "", borderWidth = 0, borderRadius = 0, padding = 0) {
        FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center", spacing = 0, padding = 0) {
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
                HeaderText(text = title, size = "small")
                Spacer(width = 0, height = 4)
                SecondaryText(text = subtitle)
            }
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "End", spacing = 0, padding = 0) {
                if (amount.isNotEmpty()) {
                    HeaderText(text = amount, size = "small")
                }
                Chip(label = status.replaceFirstChar { it.uppercase() })
            }
        }
    }
}
