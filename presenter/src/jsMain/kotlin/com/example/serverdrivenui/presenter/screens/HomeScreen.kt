package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * Home/Dashboard screen - the main entry point of the app.
 */
class HomeScreen : Screen {
    // Local state for this screen
    private var counter by mutableStateOf(0)
    private var notificationsEnabled by mutableStateOf(true)
    private var darkModeEnabled by mutableStateOf(false)
    
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "CenterHorizontally"
        ) {
            // Header
            MyText(text = "Welcome to SDUI!")
            Spacer(width = 0, height = 16)
            
            // Profile Card
            SduiCard(onClick = { navigator.push(ProfileScreen()) }) {
                FlexColumn(
                    verticalArrangement = "Center",
                    horizontalAlignment = "Start"
                ) {
                    MyText(text = "John Doe")
                    MyText(text = "Tap to edit profile")
                }
            }
            
            Spacer(width = 0, height = 16)
            
            // Quick Settings
            MyText(text = "Quick Settings")
            Spacer(width = 0, height = 8)
            
            FlexRow(
                horizontalArrangement = "SpaceBetween",
                verticalAlignment = "CenterVertically"
            ) {
                MyText(text = "Notifications")
                SduiSwitch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
            
            FlexRow(
                horizontalArrangement = "SpaceBetween",
                verticalAlignment = "CenterVertically"
            ) {
                MyText(text = "Dark Mode")
                SduiSwitch(
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
            
            Spacer(width = 0, height = 16)
            
            // Counter Card
            SduiCard(onClick = { counter++ }) {
                FlexColumn(
                    verticalArrangement = "Center",
                    horizontalAlignment = "CenterHorizontally"
                ) {
                    MyText(text = "Tap Counter")
                    MyText(text = "Count: $counter")
                }
            }
            
            Spacer(width = 0, height = 16)
            
            // Navigation Buttons
            FlexRow(
                horizontalArrangement = "SpaceEvenly",
                verticalAlignment = "CenterVertically"
            ) {
                MyButton(
                    text = "Profile",
                    onClick = { navigator.push(ProfileScreen()) }
                )
                MyButton(
                    text = "Settings",
                    onClick = { navigator.push(SettingsScreen()) }
                )
            }
        }
    }
}
