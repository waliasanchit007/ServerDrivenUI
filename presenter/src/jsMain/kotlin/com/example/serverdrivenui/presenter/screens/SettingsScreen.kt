package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * Settings screen.
 */
class SettingsScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "CenterHorizontally"
        ) {
            MyText(text = "Settings")
            Spacer(width = 0, height = 16)
            
            MyText(text = "This is the Settings screen.")
            MyText(text = "Use back gesture or button to return.")
            
            Spacer(width = 0, height = 16)
            
            MyButton(
                text = "Go Back",
                onClick = { navigator.pop() }
            )
        }
    }
}
