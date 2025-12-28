package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import com.example.serverdrivenui.schema.compose.*

@Composable
fun SduiPresenter() {
    // State for the dashboard
    var userName by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var counter by remember { mutableStateOf(0) }

    FlexColumn(
        verticalArrangement = "Top",
        horizontalAlignment = "CenterHorizontally"
    ) {
        // Profile Header Card
        SduiCard(onClick = null) {
            FlexRow(
                horizontalArrangement = "Start",
                verticalAlignment = "CenterVertically"
            ) {
                // Profile Image
                SduiImage(
                    url = "https://picsum.photos/80/80",
                    contentDescription = "Profile Picture"
                )
                
                Spacer(width = 16, height = 0)
                
                FlexColumn(
                    verticalArrangement = "Center",
                    horizontalAlignment = "Start"
                ) {
                    MyText(text = "Welcome back!")
                    MyText(text = userName)
                }
            }
        }
        
        Spacer(width = 0, height = 16)
        
        // User Info Section
        MyText(text = "Profile Settings")
        
        Spacer(width = 0, height = 8)
        
        SduiTextField(
            value = userName,
            label = "Name",
            placeholder = "Enter your name",
            onValueChange = { userName = it }
        )
        
        Spacer(width = 0, height = 8)
        
        SduiTextField(
            value = email,
            label = "Email",
            placeholder = "Enter your email",
            onValueChange = { email = it }
        )
        
        Spacer(width = 0, height = 16)
        
        // Settings Toggles
        MyText(text = "Preferences")
        
        Spacer(width = 0, height = 8)
        
        FlexRow(
            horizontalArrangement = "SpaceBetween",
            verticalAlignment = "CenterVertically"
        ) {
            MyText(text = "Enable Notifications")
            SduiSwitch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }
        
        Spacer(width = 0, height = 8)
        
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
        
        // Interactive Counter Demo
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
        
        // Action Buttons
        FlexRow(
            horizontalArrangement = "SpaceEvenly",
            verticalAlignment = "CenterVertically"
        ) {
            MyButton(
                text = "Save Profile",
                onClick = { /* Would save profile */ }
            )
            MyButton(
                text = "Reset",
                onClick = {
                    userName = "John Doe"
                    email = "john.doe@example.com"
                    counter = 0
                }
            )
        }
    }
}
