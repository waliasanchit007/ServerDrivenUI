package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.compose.BackHandler

/**
 * Navigation strategy for choosing between approaches.
 */
enum class NavigationStrategy {
    /**
     * Option A: All navigation via internal stack + BackHandler.
     * - Simpler code, no native Activity/ViewController stacking
     * - All screens are SubRoutes in the internal stack
     * - BackHandler works uniformly across platforms
     */
    COMPOSE_ONLY,
    
    /**
     * Option B: Major transitions use native shell navigation.
     * - Platform-native animations (Activity/ViewController transitions)
     * - Requires NavigationService bridge and platform navigators
     * - Good for deep linking and complex native integrations
     */
    HYBRID_NATIVE
}

/**
 * Centralized Navigation Controller for handling navigation state and back-press.
 * 
 * Supports two navigation strategies:
 * - COMPOSE_ONLY: All navigation via internal stack (Option A)
 * - HYBRID_NATIVE: Major transitions via native shell (Option B)
 * 
 * Usage:
 * ```
 * val navController = rememberNavigationController(
 *     initialScreen = SubRoute.Dashboard,
 *     strategy = NavigationStrategy.COMPOSE_ONLY
 * )
 * 
 * // Navigate (always uses internal stack in COMPOSE_ONLY)
 * navController.navigateTo(SubRoute.Settings)
 * 
 * // Or navigate via native (only works in HYBRID_NATIVE)
 * navController.navigateNative("settings")
 * 
 * // Go back
 * navController.goBack()
 * ```
 */
class NavigationController<T : Any>(
    private val initialScreen: T,
    private val strategy: NavigationStrategy,
    private val onNativeNavigate: ((String) -> Unit)?,
    private val onNativeBack: (() -> Boolean)?
) {
    // Internal route stack
    private val _backStack = mutableListOf<T>()
    
    // Current screen state
    private val _currentScreen = mutableStateOf(initialScreen)
    val currentScreen: T get() = _currentScreen.value
    
    init {
        _backStack.add(initialScreen)
        println("NavigationController: Initialized with strategy=$strategy, screen=$initialScreen")
    }
    
    /**
     * Navigate to a screen within the internal stack.
     * Works in both COMPOSE_ONLY and HYBRID_NATIVE modes.
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
     * Only works in HYBRID_NATIVE mode. In COMPOSE_ONLY mode, logs a warning.
     */
    fun navigateNative(route: String) {
        when (strategy) {
            NavigationStrategy.HYBRID_NATIVE -> {
                println("NavigationController: navigateNative($route)")
                onNativeNavigate?.invoke(route)
            }
            NavigationStrategy.COMPOSE_ONLY -> {
                println("NavigationController: WARNING - navigateNative($route) called in COMPOSE_ONLY mode, ignored")
                // In COMPOSE_ONLY mode, native navigation is not available
                // Caller should use navigateTo() with a SubRoute instead
            }
        }
    }
    
    /**
     * Go back in the navigation stack.
     * - In COMPOSE_ONLY: Always pops internal stack
     * - In HYBRID_NATIVE: Pops internal stack first, then delegates to native if at root
     * @return true if navigation was handled, false if there's nowhere to go
     */
    fun goBack(): Boolean {
        println("NavigationController: goBack(), strategy=$strategy, stack size: ${_backStack.size}")
        
        if (_backStack.size > 1) {
            // Pop internal stack
            _backStack.removeAt(_backStack.lastIndex)
            _currentScreen.value = _backStack.last()
            println("NavigationController: Popped to ${_currentScreen.value}")
            return true
        }
        
        // At root of internal stack
        return when (strategy) {
            NavigationStrategy.HYBRID_NATIVE -> {
                // Delegate to native shell
                onNativeBack?.invoke() ?: false
            }
            NavigationStrategy.COMPOSE_ONLY -> {
                // No native fallback, we're at root
                println("NavigationController: At root, cannot go back")
                false
            }
        }
    }
    
    /**
     * Check if we can go back internally.
     */
    fun canGoBack(): Boolean = _backStack.size > 1
    
    /**
     * Get the current internal stack size.
     */
    val stackSize: Int get() = _backStack.size
    
    /**
     * Get the current strategy.
     */
    val currentStrategy: NavigationStrategy get() = strategy
}

/**
 * Remember and create a NavigationController.
 * Automatically handles back-press registration with Redwood's BackHandler.
 * 
 * @param initialScreen The initial screen to display
 * @param strategy The navigation strategy (COMPOSE_ONLY or HYBRID_NATIVE)
 * @param onNativeNavigate Callback for native navigation (only used in HYBRID_NATIVE)
 * @param onNativeBack Callback for native back (only used in HYBRID_NATIVE)
 */
@Composable
inline fun <reified T : Any> rememberNavigationController(
    initialScreen: T,
    strategy: NavigationStrategy = NavigationStrategy.COMPOSE_ONLY,
    noinline onNativeNavigate: ((String) -> Unit)? = if (strategy == NavigationStrategy.HYBRID_NATIVE) {
        { route -> navigationService?.navigateTo(route) }
    } else null,
    noinline onNativeBack: (() -> Boolean)? = if (strategy == NavigationStrategy.HYBRID_NATIVE) {
        {
            val canGo = navigationService?.canGoBack() == true
            if (canGo) navigationService?.goBack()
            canGo
        }
    } else null
): NavigationController<T> {
    val controller = remember {
        NavigationController(
            initialScreen = initialScreen,
            strategy = strategy,
            onNativeNavigate = onNativeNavigate,
            onNativeBack = onNativeBack
        )
    }
    
    // Register global callback for BackPressHandler service (iOS swipe gesture)
    // This allows the host to trigger back navigation in the guest
    onGlobalBackPress = { 
        println("NavigationController: onGlobalBackPress called")
        controller.goBack() 
    }
    
    // Register BackHandler for hardware back button
    // This works on Android; on iOS it depends on host providing the dispatcher
    BackHandler(enabled = true) {
        println("NavigationController: BackHandler triggered (strategy=$strategy)")
        controller.goBack()
    }
    
    return controller
}

