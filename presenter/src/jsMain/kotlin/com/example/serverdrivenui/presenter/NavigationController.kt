package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.compose.BackHandler

/**
 * Centralized Navigation Controller for handling navigation state and back-press.
 * 
 * This is a scalable, maintainable navigation solution that:
 * - Maintains internal route stack for sub-routes
 * - Delegates major navigation to native shell via NavigationService
 * - Handles back-press using Redwood's BackHandler
 * 
 * Usage:
 * ```
 * val navController = rememberNavigationController()
 * 
 * // Navigate internally
 * navController.navigateTo(Screen.Profile)
 * 
 * // Navigate via native shell
 * navController.navigateNative("settings")
 * 
 * // Go back
 * navController.goBack()
 * ```
 */
class NavigationController<T : Any>(
    private val initialScreen: T,
    private val onNativeNavigate: (String) -> Unit,
    private val onNativeBack: () -> Boolean
) {
    // Internal route stack
    private val _backStack = mutableListOf<T>()
    
    // Current screen state
    private val _currentScreen = mutableStateOf(initialScreen)
    val currentScreen: T get() = _currentScreen.value
    
    init {
        _backStack.add(initialScreen)
    }
    
    /**
     * Navigate to a screen within the internal stack.
     * This is for sub-flow navigation that doesn't require native transitions.
     */
    fun navigateTo(screen: T) {
        if (screen != _currentScreen.value) {
            _backStack.add(screen)
            _currentScreen.value = screen
            println("NavigationController: navigateTo($screen), stack size: ${_backStack.size}")
        }
    }
    
    /**
     * Navigate using native shell (for major transitions).
     * This triggers platform-specific navigation (Activities/ViewControllers).
     */
    fun navigateNative(route: String) {
        println("NavigationController: navigateNative($route)")
        onNativeNavigate(route)
    }
    
    /**
     * Go back in the navigation stack.
     * First pops internal stack, then delegates to native if at root.
     * @return true if navigation was handled, false if there's nowhere to go
     */
    fun goBack(): Boolean {
        println("NavigationController: goBack(), stack size: ${_backStack.size}")
        
        if (_backStack.size > 1) {
            // Pop internal stack
            _backStack.removeAt(_backStack.lastIndex)
            _currentScreen.value = _backStack.last()
            println("NavigationController: Popped to ${_currentScreen.value}")
            return true
        }
        
        // At root of internal stack, delegate to native
        return onNativeBack()
    }
    
    /**
     * Check if we can go back internally.
     */
    fun canGoBack(): Boolean = _backStack.size > 1
    
    /**
     * Get the current internal stack size.
     */
    val stackSize: Int get() = _backStack.size
}

/**
 * Remember and create a NavigationController.
 * Automatically handles back-press registration with Redwood's BackHandler.
 */
@Composable
inline fun <reified T : Any> rememberNavigationController(
    initialScreen: T,
    noinline onNativeNavigate: (String) -> Unit = { route ->
        navigationService?.navigateTo(route)
    },
    noinline onNativeBack: () -> Boolean = {
        navigationService?.goBack() ?: false
        true
    }
): NavigationController<T> {
    val controller = remember {
        NavigationController(
            initialScreen = initialScreen,
            onNativeNavigate = onNativeNavigate,
            onNativeBack = onNativeBack
        )
    }
    
    // Register BackHandler for hardware back button
    // Enabled when we have internal screens to pop OR when native can handle it
    BackHandler(enabled = true) {
        println("NavigationController: BackHandler triggered")
        controller.goBack()
    }
    
    return controller
}
