package com.example.serverdrivenui.shared

import androidx.compose.ui.test.junit4.createComposeRule
import org.jetbrains.compose.resources.painterResource
import org.junit.Rule
import org.junit.Test
import serverdrivenui.composeapp.generated.resources.Res
import serverdrivenui.composeapp.generated.resources.compose_multiplatform

class ResourceAvailabilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testResourceLoading() {
        composeTestRule.setContent {
            // Attempt to load the resource
            painterResource(Res.drawable.compose_multiplatform)
        }
        
        // If it hasn't crashed by here, the test passes
    }
}
