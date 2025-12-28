package com.example.serverdrivenui.shared

/**
 * Interface for platform-specific native navigation.
 * Implemented by Android (Intent-based) and iOS (UINavigationController-based).
 */
interface NativeNavigator {
    /**
     * Navigate to a route using native platform navigation.
     * Android: Starts a new Activity with route in Intent extras.
     * iOS: Pushes a new ViewController onto the navigation stack.
     * 
     * @param route The route identifier
     * @param params Optional parameters for the destination
     */
    fun navigateTo(route: String, params: Map<String, String> = emptyMap())
    
    /**
     * Navigate back using native platform navigation.
     * Android: finish() the current Activity
     * iOS: popViewController()
     * 
     * @return true if navigation was handled, false if at root
     */
    fun goBack(): Boolean
    
    /**
     * Check if there's a screen to go back to.
     * @return true if native back navigation is possible
     */
    fun canGoBack(): Boolean
    
    /**
     * Get the current route.
     */
    val currentRoute: String
}
