package com.example.serverdrivenui.shared

/**
 * Development configuration for connecting to the Zipline server.
 * 
 * Update SERVER_IP to match your development machine's IP address.
 * Find it with: `ipconfig getifaddr en0` (macOS) or `hostname -I` (Linux)
 */
object DevConfig {
    /**
     * Development server IP address.
     * - Use "10.0.2.2" for Android emulator
     * - Use "127.0.0.1" with `adb reverse tcp:8080 tcp:8080` for USB debugging
     * - Use your machine's actual IP for wireless debugging
     */
    const val SERVER_IP = "127.0.0.1"
    
    /**
     * Server port (default 8080)
     */
    const val SERVER_PORT = 8080
    
    /**
     * Base URL for the Zipline manifest
     */
    val manifestUrl: String
        get() = "http://$SERVER_IP:$SERVER_PORT/manifest.zipline.json"
    
    /**
     * WebSocket URL for hot reload notifications
     */
    val hotReloadUrl: String
        get() = "ws://$SERVER_IP:$SERVER_PORT/hot-reload"
    
    /**
     * Whether hot reload is enabled (can be disabled for release builds)
     */
    const val HOT_RELOAD_ENABLED = true
}
