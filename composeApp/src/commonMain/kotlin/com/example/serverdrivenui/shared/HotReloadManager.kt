package com.example.serverdrivenui.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages WebSocket connection to the dev server for hot reload notifications.
 * Platform-specific implementations handle the actual WebSocket connection.
 */
expect class HotReloadManager() {
    /**
     * Connect to the hot reload WebSocket endpoint.
     * Call this when the app starts.
     */
    fun connect(url: String)
    
    /**
     * Disconnect from the hot reload endpoint.
     * Call this when the app goes to background or terminates.
     */
    fun disconnect()
    
    /**
     * Flow that emits timestamps when a refresh is needed.
     * Collectors should update their manifestUrl when this emits.
     */
    val refreshTrigger: StateFlow<Long>
}
