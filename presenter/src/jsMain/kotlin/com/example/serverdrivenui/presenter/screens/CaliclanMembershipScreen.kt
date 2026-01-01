package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * MembershipScreen - Current plan and renewal options.
 */
class CaliclanMembershipScreen : Screen {
    
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "CenterHorizontally"
        ) {
            Spacer(width = 0, height = 16)
            MyText(text = "Membership")
            Spacer(width = 0, height = 24)
            
            // Current Plan Card
            StatusCard(
                status = "active",
                title = "Monthly Plan",
                subtitle = "Started: Dec 1, 2025 • Ends: Jan 1, 2026",
                daysLeft = 0,
                onClick = null
            )
            
            Spacer(width = 0, height = 32)
            
            MyText(text = "Renewal Options")
            Spacer(width = 0, height = 16)
            
            // 1 Month Option
            SduiCard(onClick = {}) {
                FlexColumn(
                    verticalArrangement = "Center",
                    horizontalAlignment = "CenterHorizontally"
                ) {
                    MyText(text = "1 Month")
                    MyText(text = "Continue your journey")
                }
            }
            
            Spacer(width = 0, height = 12)
            
            // 3 Month Option
            SduiCard(onClick = {}) {
                FlexColumn(
                    verticalArrangement = "Center",
                    horizontalAlignment = "CenterHorizontally"
                ) {
                    MyText(text = "3 Months")
                    MyText(text = "Commit to consistency")
                }
            }
            
            Spacer(width = 0, height = 12)
            
            // 6 Month Option
            SduiCard(onClick = {}) {
                FlexColumn(
                    verticalArrangement = "Center",
                    horizontalAlignment = "CenterHorizontally"
                ) {
                    MyText(text = "6 Months")
                    MyText(text = "Best value for serious training")
                }
            }
            
            Spacer(width = 0, height = 24)
            
            MyButton(
                text = "Renew via WhatsApp",
                onClick = {
                    // Would call: gymService.openUrl("whatsapp://send?text=...")
                    println("Opening WhatsApp for renewal")
                }
            )
            
            Spacer(width = 0, height = 16)
            
            MyButton(
                text = "Back",
                onClick = { navigator.pop() }
            )
        }
    }
}
