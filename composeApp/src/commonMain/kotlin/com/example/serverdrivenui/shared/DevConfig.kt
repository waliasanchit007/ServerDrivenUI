package com.example.serverdrivenui.shared

/**
 * Development configuration for connecting to the Zipline dev-server.
 *
 * Set SERVER_BASE_URL to one of:
 *   - LAN IP:        "http://192.168.1.6:8080"
 *   - Android emul:  "http://10.0.2.2:8080"
 *   - adb reverse:   "http://127.0.0.1:8080"  (after `adb reverse tcp:8080 tcp:8080`)
 *   - ngrok tunnel:  "https://your-subdomain.ngrok-free.dev"
 *
 * Tip on local IP: `ipconfig getifaddr en0` (macOS) / `hostname -I` (Linux).
 */
object DevConfig {
    /**
     * Full base URL of the dev-server. Include scheme; omit trailing slash.
     * For ngrok, no port is needed — it terminates TLS on 443.
     */
    const val SERVER_BASE_URL = "https://proximally-dialogic-priscila.ngrok-free.dev"

    /**
     * Base URL for the Zipline manifest.
     */
    val manifestUrl: String
        get() = "$SERVER_BASE_URL/manifest.zipline.json"

    /**
     * WebSocket URL for hot reload notifications.
     * Derived from SERVER_BASE_URL: http -> ws, https -> wss.
     */
    val hotReloadUrl: String
        get() = SERVER_BASE_URL
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://") + "/hot-reload"

    /**
     * Whether hot reload is enabled (can be disabled for release builds).
     */
    const val HOT_RELOAD_ENABLED = true
}
