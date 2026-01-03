package com.example.serverdrivenui.presenter

import app.cash.zipline.Zipline
import com.example.serverdrivenui.shared.GymService
import com.example.serverdrivenui.data.repository.GymRepository

/**
 * GymServiceProvider - Singleton accessor for GymService and GymRepository in presenter.
 * 
 * Uses LAZY INITIALIZATION: The service is taken from Zipline on first access,
 * not at startup. This is necessary because the presenter's main() runs before
 * the host's bindServices() binds the GymService.
 */
object GymServiceProvider {
    
    private var _service: GymService? = null
    private var _repository: GymRepository? = null
    private var initialized = false
    
    /**
     * Lazy initialization - takes GymService from Zipline on first access.
     */
    private fun ensureInitialized() {
        if (initialized) return
        initialized = true
        
        try {
            val zipline = Zipline.get()
            _service = zipline.take<GymService>("gym")
            val storage = zipline.take<com.example.serverdrivenui.shared.StorageService>("storage")
            
            _repository = if (_service != null) {
                GymRepository(_service!!, storage)
            } else {
                null
            }
            println("GymServiceProvider: LAZY INIT SUCCESS - GymService and Repository initialized")
        } catch (e: Throwable) {
            println("GymServiceProvider: LAZY INIT FAILED - ${e.message}")
            _service = null
            _repository = null
        }
    }
    
    /**
     * Get the GymService instance (raw JSON methods).
     * Initializes lazily on first access.
     */
    val service: GymService?
        get() {
            ensureInitialized()
            return _service
        }
    
    /**
     * Get the GymRepository instance (parsed DTOs).
     * Use this in screens for type-safe data access.
     * Initializes lazily on first access.
     */
    val repository: GymRepository?
        get() {
            ensureInitialized()
            return _repository
        }
    
    /**
     * Check if services are available.
     */
    val isAvailable: Boolean
        get() {
            ensureInitialized()
            return _service != null && _repository != null
        }
    
    /**
     * Force re-initialization (useful if first attempt failed).
     */
    fun reset() {
        initialized = false
        _service = null
        _repository = null
    }
}
