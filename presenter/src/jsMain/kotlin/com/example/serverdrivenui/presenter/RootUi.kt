package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.serverdrivenui.schema.compose.BackHandler
import com.example.serverdrivenui.schema.compose.ScreenStack
import com.example.serverdrivenui.presenter.screens.MainNavigationShell

/**
 * Root UI composable for the Caliclan app.
 * Entry point that sets up the navigation structure.
 * 
 * Uses MainNavigationShell for the main app experience with bottom navigation.
 * 
 * @param initialRoute The route to start on (for deep linking support)
 */
@Composable
fun RootUi(initialRoute: String = "main") {
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
 * 
 * Routes:
 * - "main" → MainNavigationShell (default, with bottom nav)
 * - "login" → MainNavigationShell (Login removed for now)
 */
fun routeToScreen(route: String): Screen {
    println("RootUi: routeToScreen($route)")
    return when (route) {
        "main", "home", "dashboard", "login" -> MainNavigationShell()
        else -> {
            println("RootUi: Unknown route '$route', defaulting to MainNavigationShell")
            MainNavigationShell()
        }
    }
}
