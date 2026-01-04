package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreenContent(onLoginSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    // Using FlexColumn for layout
    FlexColumn(
        verticalArrangement = "Center",
        horizontalAlignment = "CenterHorizontally"
    ) {
        
        HeaderText(text = "Welcome to Caliclan", size = "large")
        Spacer(width = 0, height = 32)
        
        // Name Input
        SduiTextField(
            value = name,
            label = "Full Name",
            onValueChange = { name = it },
            placeholder = "Enter your name"
        )
        
        Spacer(width = 0, height = 16)
        
        // Email Input
        SduiTextField(
            value = email,
            label = "Email Address",
            onValueChange = { email = it },
            placeholder = "Enter your email"
        )
        
        Spacer(width = 0, height = 24)
        
        // Action Button
        MyButton(
            text = if (isLoading) "Loading..." else "Start Training",
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && !isLoading) {
                     scope.launch {
                         isLoading = true
                         error = ""
                         try {
                             val repo = GymServiceProvider.getRepository()
                             val success = repo?.onboardUser(name, email) ?: false
                             isLoading = false
                             if (success) {
                                 println("LoginScreen: Login Successful! Saving session and calling onLoginSuccess()")
                                 // Persist session
                                 val uid = repo?.currentUserId ?: ""
                                 val token = repo?.currentAccessToken ?: ""
                                 if (uid.isNotEmpty() && token.isNotEmpty()) {
                                     GymServiceProvider.saveSession(uid, token)
                                 }
                                 
                                 onLoginSuccess()
                             } else {
                                 error = "Failed to sign in. Please try again."
                             }
                         } catch (e: Exception) {
                             isLoading = false
                             error = "Error: ${e.message}"
                         }
                     }
                } else if (!isLoading) {
                    error = "Please enter name and email"
                }
            }
        )
        
        if (error.isNotEmpty()) {
            Spacer(width = 0, height = 16)
            SecondaryText(text = error) // Using SecondaryText as we don't have explicit ErrorText
        }
    }
}
