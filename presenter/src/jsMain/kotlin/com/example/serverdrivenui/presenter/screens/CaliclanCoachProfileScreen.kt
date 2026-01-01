package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * CoachProfileScreen - Individual coach detail view.
 */
class CaliclanCoachProfileScreen(
    private val coachId: String,
    private val name: String,
    private val role: String,
    private val bio: String,
    private val photoUrl: String,
    private val instagram: String
) : Screen {
    
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "CenterHorizontally"
        ) {
            Spacer(width = 0, height = 16)
            
            // Hero Image
            AsyncImage(
                url = photoUrl,
                contentDescription = name,
                size = 200,
                circular = false
            )
            
            Spacer(width = 0, height = 24)
            
            // Name
            MyText(text = name)
            
            // Role (in amber)
            MyText(text = role)
            
            Spacer(width = 0, height = 16)
            
            // Bio
            MyText(text = bio)
            
            Spacer(width = 0, height = 32)
            
            // Instagram CTA
            MyButton(
                text = "Follow @$instagram",
                onClick = {
                    // Would call: gymService.openUrl("https://instagram.com/$instagram")
                    println("Opening Instagram: $instagram")
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
