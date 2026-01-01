package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * LoginScreen - Phone OTP authentication.
 * Premium dark design following Caliclan guidelines.
 */
class CaliclanLoginScreen : Screen {
    
    @Composable
    override fun Content(navigator: Navigator) {
        var phone by remember { mutableStateOf("") }
        var otp by remember { mutableStateOf("") }
        var step by remember { mutableStateOf("phone") } // "phone" or "otp"
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        
        FlexColumn(
            verticalArrangement = "Center",
            horizontalAlignment = "CenterHorizontally"
        ) {
            Spacer(width = 0, height = 80)
            
            // Logo/Brand
            MyText(text = "CALICLAN")
            Spacer(width = 0, height = 8)
            MyText(text = "Calisthenics Training")
            
            Spacer(width = 0, height = 60)
            
            when (step) {
                "phone" -> {
                    // Phone input step
                    MyText(text = "Enter your phone number")
                    Spacer(width = 0, height = 16)
                    
                    SduiTextField(
                        value = phone,
                        label = "Phone",
                        placeholder = "+91 98765 43210",
                        onValueChange = { phone = it }
                    )
                    
                    Spacer(width = 0, height = 24)
                    
                    MyButton(
                        text = if (isLoading) "Sending..." else "Send OTP",
                        onClick = {
                            if (phone.isNotEmpty()) {
                                isLoading = true
                                // In real app: gymService.requestOtp(phone)
                                // For demo, just proceed
                                isLoading = false
                                step = "otp"
                            } else {
                                errorMessage = "Please enter phone number"
                            }
                        }
                    )
                }
                
                "otp" -> {
                    // OTP verification step
                    MyText(text = "Enter OTP sent to")
                    MyText(text = phone)
                    Spacer(width = 0, height = 16)
                    
                    SduiTextField(
                        value = otp,
                        label = "OTP",
                        placeholder = "123456",
                        onValueChange = { otp = it }
                    )
                    
                    Spacer(width = 0, height = 24)
                    
                    MyButton(
                        text = if (isLoading) "Verifying..." else "Verify & Login",
                        onClick = {
                            if (otp.isNotEmpty()) {
                                isLoading = true
                                // In real app: gymService.verifyOtp(phone, otp)
                                // For demo, accept any 6-digit OTP
                                if (otp.length == 6) {
                                    isLoading = false
                                    navigator.replaceAll(CaliclanHomeScreen())
                                } else {
                                    isLoading = false
                                    errorMessage = "Invalid OTP"
                                }
                            } else {
                                errorMessage = "Please enter OTP"
                            }
                        }
                    )
                    
                    Spacer(width = 0, height = 16)
                    
                    MyButton(
                        text = "Change Phone",
                        onClick = {
                            step = "phone"
                            otp = ""
                            errorMessage = ""
                        }
                    )
                }
            }
            
            // Error message
            if (errorMessage.isNotEmpty()) {
                Spacer(width = 0, height = 16)
                MyText(text = errorMessage)
            }
            
            Spacer(width = 0, height = 40)
            
            // Skip login for demo
            MyButton(
                text = "Skip (Demo Mode)",
                onClick = { navigator.replaceAll(CaliclanHomeScreen()) }
            )
        }
    }
}
