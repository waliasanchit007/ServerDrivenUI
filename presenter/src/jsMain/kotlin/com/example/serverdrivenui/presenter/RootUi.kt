package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.serverdrivenui.schema.compose.BackHandler
import com.example.serverdrivenui.schema.compose.ScreenStack
import com.example.serverdrivenui.presenter.screens.HomeScreen

/**
 * Root UI composable for the SDUI app.
 * This is the entry point that sets up the navigation structure.
 * 
 * @param initialRoute The route to start on (for deep linking support)
 */
@Composable
fun RootUi(initialRoute: String = "home") {
    // Create and remember the Navigator
    val navigator = remember { 
        Navigator().apply {
            // Set initial screen based on route
            push(routeToScreen(initialRoute))
        }
    }
    
    println("RootUi: Rendering with stack size=${navigator.stackSize}, canGoBack=${navigator.canGoBack}")
    
    // Global back handler - intercepts system back button and iOS swipe gesture
    BackHandler(
        enabled = navigator.canGoBack,
        onBack = { 
            println("RootUi: BackHandler triggered, calling navigator.pop()")
            navigator.pop() 
        }
    )
    
    // Render the current screen in a ScreenStack container
    ScreenStack {
        val currentScreen = navigator.currentScreen
        if (currentScreen != null) {
            println("RootUi: Rendering screen ${currentScreen::class.simpleName}")
            currentScreen.Content(navigator)
        } else {
            println("RootUi: WARNING - No current screen!")
        }
    }
}

/**
 * Convert a route string to a Screen instance.
 * This is where deep linking routes are mapped to screens.
 */
fun routeToScreen(route: String): Screen {
    println("RootUi: routeToScreen($route)")
    return when (route) {
        "home", "dashboard" -> HomeScreen()
        "profile" -> com.example.serverdrivenui.presenter.screens.ProfileScreen()
        "settings" -> com.example.serverdrivenui.presenter.screens.SettingsScreen()
        else -> {
            println("RootUi: Unknown route '$route', defaulting to HomeScreen")
            HomeScreen()
        }
    }
}
