package com.example.serverdrivenui.shared

/**
 * Development configuration for connecting to the Zipline server.
 * 
 * Toggle USE_NGROK to switch between:
 * - true: Use Ngrok tunnel (works on any WiFi, no cable needed)
 * - false: Use local server (requires same network or adb reverse)
 */
object DevConfig {
    /**
     * Toggle between Ngrok (remote) and Local development.
     * Set to TRUE for wireless development without cable.
     * Set to FALSE for local USB/emulator development.
     */
    const val USE_NGROK = true
    
    /**
     * Ngrok free domain (permanent, doesn't expire).
     */
    const val NGROK_DOMAIN = "proximally-dialogic-priscila.ngrok-free.dev"
    
    /**
     * Local server IP address.
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
        get() = if (USE_NGROK) {
            "https://$NGROK_DOMAIN/manifest.zipline.json"
        } else {
            "http://$SERVER_IP:$SERVER_PORT/manifest.zipline.json"
        }
    
    /**
     * WebSocket URL for hot reload notifications
     */
    val hotReloadUrl: String
        get() = if (USE_NGROK) {
            "wss://$NGROK_DOMAIN/hot-reload"
        } else {
            "ws://$SERVER_IP:$SERVER_PORT/hot-reload"
        }
    
    /**
     * Whether hot reload is enabled (can be disabled for release builds)
     */
    const val HOT_RELOAD_ENABLED = true
}
