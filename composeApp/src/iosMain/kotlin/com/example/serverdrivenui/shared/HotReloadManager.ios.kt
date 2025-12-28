@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.example.serverdrivenui.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.Foundation.*
import platform.darwin.NSObject

/**
 * iOS implementation of HotReloadManager using NSURLSessionWebSocketTask.
 */
actual class HotReloadManager actual constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _refreshTrigger = MutableStateFlow(0L)
    actual val refreshTrigger: StateFlow<Long> = _refreshTrigger.asStateFlow()
    
    private var webSocketTask: NSURLSessionWebSocketTask? = null
    private var isConnecting = false
    private var currentUrl: String = ""
    
    private val urlSession: NSURLSession by lazy {
        val config = NSURLSessionConfiguration.defaultSessionConfiguration()
        NSURLSession.sessionWithConfiguration(config)
    }
    
    actual fun connect(url: String) {
        if (isConnecting || webSocketTask != null) {
            println("HotReload: Already connected or connecting")
            return
        }
        
        if (!DevConfig.HOT_RELOAD_ENABLED) {
            println("HotReload: Disabled by configuration")
            return
        }
        
        println("HotReload: Connecting to $url")
        isConnecting = true
        currentUrl = url
        
        try {
            val nsUrl = NSURL(string = url) ?: run {
                println("HotReload: Invalid URL: $url")
                isConnecting = false
                return
            }
            
            webSocketTask = urlSession.webSocketTaskWithURL(nsUrl).also { task ->
                task.resume()
                isConnecting = false
                println("HotReload: Connected to dev server")
                receiveMessage(task)
            }
        } catch (e: Exception) {
            println("HotReload: Failed to connect: ${e.message}")
            isConnecting = false
        }
    }
    
    private fun receiveMessage(task: NSURLSessionWebSocketTask) {
        task.receiveMessageWithCompletionHandler { message, error ->
            if (error != null) {
                println("HotReload: Receive error: ${error.localizedDescription}")
                webSocketTask = null
                isConnecting = false
                
                // Attempt to reconnect
                scope.launch {
                    delay(3000)
                    if (currentUrl.isNotEmpty()) {
                        connect(currentUrl)
                    }
                }
                return@receiveMessageWithCompletionHandler
            }
            
            message?.let { msg ->
                when (msg.type) {
                    NSURLSessionWebSocketMessageTypeString -> {
                        val text = msg.string ?: return@let
                        println("HotReload: Received message: $text")
                        
                        if (text == "REFRESH") {
                            val timestamp = NSDate().timeIntervalSince1970.toLong() * 1000
                            println("HotReload: Triggering refresh at $timestamp")
                            _refreshTrigger.value = timestamp
                        }
                    }
                    else -> {}
                }
            }
            
            // Continue receiving messages
            webSocketTask?.let { receiveMessage(it) }
        }
    }
    
    actual fun disconnect() {
        println("HotReload: Disconnecting")
        webSocketTask?.cancelWithCloseCode(
            closeCode = NSURLSessionWebSocketCloseCodeNormalClosure,
            reason = null
        )
        webSocketTask = null
        isConnecting = false
    }
}
