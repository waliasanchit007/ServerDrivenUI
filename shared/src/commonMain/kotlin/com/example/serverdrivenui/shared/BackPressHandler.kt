package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService

/**
 * Service that allows the Host to trigger back navigation in the Guest.
 * 
 * This is the reverse of NavigationService:
 * - NavigationService: Guest calls → Host implements (for native nav)
 * - BackPressHandler: Host calls → Guest implements (for back press from iOS gesture)
 * 
 * The Guest binds an implementation that calls NavigationController.goBack().
 */
interface BackPressHandler : ZiplineService {
    /**
     * Handle a back press event from the host.
     * This is called when iOS detects a swipe-back gesture.
     */
    suspend fun handleBackPress()
}
