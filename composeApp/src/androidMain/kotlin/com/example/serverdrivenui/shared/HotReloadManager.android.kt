package com.example.serverdrivenui.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * Android implementation of HotReloadManager using OkHttp WebSocket.
 */
actual class HotReloadManager actual constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _refreshTrigger = MutableStateFlow(0L)
    actual val refreshTrigger: StateFlow<Long> = _refreshTrigger.asStateFlow()
    
    private var webSocket: WebSocket? = null
    private var isConnecting = false
    
    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    
    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("HotReload: Connected to dev server")
            isConnecting = false
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            println("HotReload: Received message: $text")
            if (text == "REFRESH") {
                val timestamp = System.currentTimeMillis()
                println("HotReload: Triggering refresh at $timestamp")
                _refreshTrigger.value = timestamp
            }
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("HotReload: Connection failed: ${t.message}")
            isConnecting = false
            // Attempt to reconnect after a delay
            scope.launch {
                kotlinx.coroutines.delay(3000)
                webSocket.request().url.toString().let { 
                    connect(it) 
                }
            }
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("HotReload: Connection closing: $reason")
            webSocket.close(1000, null)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            println("HotReload: Connection closed: $reason")
            isConnecting = false
        }
    }
    
    actual fun connect(url: String) {
        if (isConnecting || webSocket != null) {
            println("HotReload: Already connected or connecting")
            return
        }
        
        if (!DevConfig.HOT_RELOAD_ENABLED) {
            println("HotReload: Disabled by configuration")
            return
        }
        
        println("HotReload: Connecting to $url")
        isConnecting = true
        
        try {
            val request = Request.Builder()
                .url(url)
                .build()
            
            webSocket = client.newWebSocket(request, listener)
        } catch (e: Exception) {
            println("HotReload: Failed to connect: ${e.message}")
            isConnecting = false
        }
    }
    
    actual fun disconnect() {
        println("HotReload: Disconnecting")
        webSocket?.close(1000, "App closing")
        webSocket = null
        isConnecting = false
    }
}
