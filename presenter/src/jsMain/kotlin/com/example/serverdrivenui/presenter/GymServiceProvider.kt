package com.example.serverdrivenui.presenter

import app.cash.zipline.Zipline
import com.example.serverdrivenui.shared.GymService

/**
 * GymServiceProvider - Singleton accessor for GymService in presenter.
 * 
 * The presenter runs as a Zipline guest. GymService is bound by the host
 * and taken here for use across all screens.
 */
object GymServiceProvider {
    
    private var _service: GymService? = null
    
    /**
     * Initialize the service provider by taking GymService from Zipline.
     * Should be called once in Main.kt after Zipline is set up.
     */
    fun initialize() {
        try {
            val zipline = Zipline.get()
            _service = zipline.take<GymService>("gym")
            println("GymServiceProvider: GymService bound successfully")
        } catch (e: Throwable) {
            println("GymServiceProvider: Failed to take GymService: ${e.message}")
            _service = null
        }
    }
    
    /**
     * Get the GymService instance.
     * Returns null if not initialized or binding failed.
     */
    val service: GymService?
        get() = _service
    
    /**
     * Check if GymService is available.
     */
    val isAvailable: Boolean
        get() = _service != null
}
