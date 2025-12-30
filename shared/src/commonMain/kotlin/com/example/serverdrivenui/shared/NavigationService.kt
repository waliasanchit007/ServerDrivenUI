package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService

/**
 * Navigation service that allows the Presenter (guest) to request 
 * native navigation transitions from the Host.
 * 
 * Major transitions (Home -> Settings) trigger native Activity/ViewController navigation.
 * Internal sub-flows are handled within the Presenter state.
 */
interface NavigationService : ZiplineService {
    /**
     * Navigate to a new route using native navigation.
     * @param route The route identifier (e.g., "home", "settings", "profile")
     * @param params Optional parameters to pass to the destination
     */
    fun navigateTo(route: String, params: Map<String, String> = emptyMap())
    
    /**
     * Request the native host to go back.
     * This will pop the current Activity/ViewController.
     */
    fun goBack()
    
    /**
     * Check if native navigation can go back.
     * @return true if there's a screen to go back to
     */
    fun canGoBack(): Boolean
    
    /**
     * Set the guest back handler.
     * This allows the guest to register a listener for back events (e.g. iOS swipe).
     */
    fun setGuestBackHandler(handler: BackPressHandler)
}
