package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import com.example.serverdrivenui.schema.compose.*
import com.example.serverdrivenui.shared.NavigationService
import com.example.serverdrivenui.shared.RouteService

/**
 * Internal sub-routes for the Presenter.
 * These are handled without native navigation (fast internal transitions).
 */
sealed class SubRoute {
    object Dashboard : SubRoute()
    object ProfileEdit : SubRoute()
    object Settings : SubRoute()
    data class FormStep(val step: Int) : SubRoute()
    
    companion object {
        fun fromString(route: String): SubRoute = when (route) {
            "dashboard", "home" -> Dashboard
            "profile", "profileEdit" -> ProfileEdit
            "settings" -> Settings
            else -> Dashboard
        }
    }
}

/**
 * Global reference to NavigationService.
 * Set from Main.kt when the Presenter starts.
 */
var navigationService: NavigationService? = null

/**
 * Global reference to RouteService and initial route.
 */
var routeService: RouteService? = null
var initialRoute: String = "dashboard"

@Composable
fun SduiPresenter() {
    // Internal sub-route state, initialized based on host-provided route
    var subRoute by remember { mutableStateOf(SubRoute.fromString(initialRoute)) }
    
    // State for the dashboard
    var userName by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var counter by remember { mutableStateOf(0) }

    // Render based on current sub-route
    when (subRoute) {
        is SubRoute.Dashboard -> DashboardScreen(
            userName = userName,
            counter = counter,
            notificationsEnabled = notificationsEnabled,
            darkModeEnabled = darkModeEnabled,
            onEditProfile = { subRoute = SubRoute.ProfileEdit },
            onOpenSettings = { 
                // Major navigation - use native shell
                navigationService?.navigateTo("settings") 
            },
            onCounterTap = { counter++ },
            onNotificationsToggle = { notificationsEnabled = it },
            onDarkModeToggle = { darkModeEnabled = it }
        )
        
        is SubRoute.ProfileEdit -> ProfileEditScreen(
            userName = userName,
            email = email,
            onUserNameChange = { userName = it },
            onEmailChange = { email = it },
            onBack = { subRoute = SubRoute.Dashboard },
            onSave = { 
                // Save and go back
                subRoute = SubRoute.Dashboard 
            }
        )
        
        is SubRoute.Settings -> SettingsScreen(
            onBack = { subRoute = SubRoute.Dashboard }
        )
        
        is SubRoute.FormStep -> FormStepScreen(
            step = (subRoute as SubRoute.FormStep).step,
            onNext = { 
                subRoute = SubRoute.FormStep((subRoute as SubRoute.FormStep).step + 1)
            },
            onBack = {
                val currentStep = (subRoute as SubRoute.FormStep).step
                if (currentStep > 1) {
                    subRoute = SubRoute.FormStep(currentStep - 1)
                } else {
                    subRoute = SubRoute.Dashboard
                }
            }
        )
    }
}

@Composable
private fun DashboardScreen(
    userName: String,
    counter: Int,
    notificationsEnabled: Boolean,
    darkModeEnabled: Boolean,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onCounterTap: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit
) {
    FlexColumn(
        verticalArrangement = "Top",
        horizontalAlignment = "CenterHorizontally"
    ) {
        // Profile Header Card
        SduiCard(onClick = onEditProfile) {
            FlexRow(
                horizontalArrangement = "Start",
                verticalAlignment = "CenterVertically"
            ) {
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
                    MyText(text = "Tap to edit profile")
                }
            }
        }
        
        Spacer(width = 0, height = 16)
        
        // Quick Settings Toggles
        MyText(text = "Quick Settings")
        
        Spacer(width = 0, height = 8)
        
        FlexRow(
            horizontalArrangement = "SpaceBetween",
            verticalAlignment = "CenterVertically"
        ) {
            MyText(text = "Notifications")
            SduiSwitch(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsToggle
            )
        }
        
        FlexRow(
            horizontalArrangement = "SpaceBetween",
            verticalAlignment = "CenterVertically"
        ) {
            MyText(text = "Dark Mode")
            SduiSwitch(
                checked = darkModeEnabled,
                onCheckedChange = onDarkModeToggle
            )
        }
        
        Spacer(width = 0, height = 16)
        
        // Interactive Counter
        SduiCard(onClick = onCounterTap) {
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
                text = "Edit Profile",
                onClick = onEditProfile
            )
            MyButton(
                text = "Settings",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun ProfileEditScreen(
    userName: String,
    email: String,
    onUserNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
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
            onValueChange = onUserNameChange
        )
        
        Spacer(width = 0, height = 8)
        
        SduiTextField(
            value = email,
            label = "Email",
            placeholder = "Enter your email",
            onValueChange = onEmailChange
        )
        
        Spacer(width = 0, height = 16)
        
        FlexRow(
            horizontalArrangement = "SpaceEvenly",
            verticalAlignment = "CenterVertically"
        ) {
            MyButton(text = "Cancel", onClick = onBack)
            MyButton(text = "Save", onClick = onSave)
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    FlexColumn(
        verticalArrangement = "Top",
        horizontalAlignment = "CenterHorizontally"
    ) {
        MyText(text = "Settings Screen")
        MyText(text = "(Navigated via Native Shell)")
        
        Spacer(width = 0, height = 16)
        
        MyButton(text = "Go Back", onClick = { 
            navigationService?.goBack()
        })
    }
}

@Composable
private fun FormStepScreen(
    step: Int,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    FlexColumn(
        verticalArrangement = "Center",
        horizontalAlignment = "CenterHorizontally"
    ) {
        MyText(text = "Form Step $step of 3")
        MyText(text = "(Internal sub-route)")
        
        Spacer(width = 0, height = 16)
        
        FlexRow(
            horizontalArrangement = "SpaceEvenly",
            verticalAlignment = "CenterVertically"
        ) {
            MyButton(text = "Back", onClick = onBack)
            if (step < 3) {
                MyButton(text = "Next", onClick = onNext)
            } else {
                MyButton(text = "Done", onClick = onBack)
            }
        }
    }
}

