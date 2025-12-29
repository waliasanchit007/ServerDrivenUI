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

/**
 * Get the current route, reading lazily from RouteService if available.
 * This ensures we get the correct route even if main() ran before bindServices().
 */
fun getCurrentInitialRoute(): String {
    // Try to get route from service (available after bindServices)
    val routeFromService = try {
        routeService?.getCurrentRoute()
    } catch (e: Throwable) {
        println("SduiPresenter: Failed to get route from service: ${e.message}")
        null
    }
    
    return routeFromService ?: initialRoute
}

@Composable
fun SduiPresenter() {
    // Read route lazily - by the time Show() runs, bindServices() has completed
    val currentRoute = remember { getCurrentInitialRoute() }
    println("SduiPresenter: Starting with route=$currentRoute")
    
    // Use centralized NavigationController with BackHandler integration
    val navController = rememberNavigationController(
        initialScreen = SubRoute.fromString(currentRoute),
        onNativeNavigate = { route ->
            navigationService?.navigateTo(route)
        },
        onNativeBack = {
            val canGo = navigationService?.canGoBack() == true
            if (canGo) navigationService?.goBack()
            canGo
        }
    )
    
    // State for the dashboard
    var userName by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var counter by remember { mutableStateOf(0) }

    // Render based on current screen from NavController
    when (val screen = navController.currentScreen) {
        is SubRoute.Dashboard -> DashboardScreen(
            userName = userName,
            counter = counter,
            notificationsEnabled = notificationsEnabled,
            darkModeEnabled = darkModeEnabled,
            onEditProfile = { navController.navigateTo(SubRoute.ProfileEdit) },
            onOpenSettings = { 
                // Major navigation - use native shell
                navController.navigateNative("settings")
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
            onBack = { navController.goBack() },
            onSave = { 
                // Save and go back
                navController.goBack()
            }
        )
        
        is SubRoute.Settings -> SettingsScreen(
            onBack = { navController.goBack() }
        )
        
        is SubRoute.FormStep -> FormStepScreen(
            step = (screen as SubRoute.FormStep).step,
            onNext = { 
                navController.navigateTo(SubRoute.FormStep(screen.step + 1))
            },
            onBack = {
                if (screen.step > 1) {
                    navController.navigateTo(SubRoute.FormStep(screen.step - 1))
                } else {
                    navController.goBack()
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

