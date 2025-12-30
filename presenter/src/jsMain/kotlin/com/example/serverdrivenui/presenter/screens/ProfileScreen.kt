package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * Profile editing screen.
 */
class ProfileScreen : Screen {
    private var userName by mutableStateOf("John Doe")
    private var email by mutableStateOf("john.doe@example.com")
    
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "CenterHorizontally"
        ) {
            MyText(text = "Edit Profile")
            Spacer(width = 0, height = 16)
            
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
            
            FlexRow(
                horizontalArrangement = "SpaceEvenly",
                verticalAlignment = "CenterVertically"
            ) {
                MyButton(
                    text = "Cancel",
                    onClick = { navigator.pop() }
                )
                MyButton(
                    text = "Save",
                    onClick = { 
                        println("ProfileScreen: Saving profile - name=$userName, email=$email")
                        navigator.pop() 
                    }
                )
            }
        }
    }
}
