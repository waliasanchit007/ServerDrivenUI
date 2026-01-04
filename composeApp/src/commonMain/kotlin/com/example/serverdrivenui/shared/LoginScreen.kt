package com.example.serverdrivenui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    gymService: GymService,
    onLoginSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // UI Logic
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Caliclan",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(32.dp))
        
        if (!isOtpSent) {
            // Step 1: Name and Email
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        if (email.isNotBlank() && name.isNotBlank()) {
                            isLoading = true
                            error = null
                            val success = gymService.requestOtp(email)
                            isLoading = false
                            if (success) {
                                isOtpSent = true
                            } else {
                                error = "Failed to send OTP. Check email."
                            }
                        } else {
                            error = "Please enter name and email."
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Continue")
                }
            }
        } else {
            // Step 2: OTP
            Text("Enter OTP sent to $email")
            Spacer(Modifier.height(16.dp))
            
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("OTP Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        if (otp.isNotBlank()) {
                            isLoading = true
                            error = null
                            val success = gymService.verifyOtp(email, otp)
                            if (success) {
                                // Update Profile with Name
                                gymService.updateProfile(name, email)
                                isLoading = false
                                onLoginSuccess()
                            } else {
                                isLoading = false
                                error = "Invalid OTP."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                 if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Verify & Login")
                }
            }
            
            TextButton(onClick = { isOtpSent = false }) {
                Text("Back to Email")
            }
        }
        
        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
