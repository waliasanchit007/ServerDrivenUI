package com.example.serverdrivenui.devserver

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.nio.file.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.pathString

/**
 * Development server for Zipline hot reload.
 * 
 * - Serves static Zipline files from presenter/build/zipline/Development/
 * - Provides WebSocket endpoint at /hot-reload for clients to receive refresh notifications
 * - Watches manifest.zipline.json for changes and broadcasts REFRESH to all connected clients
 */

private val connections = CopyOnWriteArrayList<WebSocketSession>()
private val refreshChannel = Channel<Unit>(Channel.CONFLATED)

fun main() {
    val projectRoot = findProjectRoot()
    val ziplineDir = File(projectRoot, "presenter/build/zipline/Development")
    
    println("╔══════════════════════════════════════════════════════════════╗")
    println("║           Zipline Development Server Starting...             ║")
    println("╠══════════════════════════════════════════════════════════════╣")
    println("║  Static files: ${ziplineDir.absolutePath.take(45).padEnd(45)} ║")
    println("║  Server URL:   http://0.0.0.0:8080                           ║")
    println("║  WebSocket:    ws://0.0.0.0:8080/hot-reload                  ║")
    println("╚══════════════════════════════════════════════════════════════╝")
    
    // Start file watcher in background
    GlobalScope.launch {
        watchForChanges(ziplineDir)
    }
    
    // Start refresh broadcaster
    GlobalScope.launch {
        for (unit in refreshChannel) {
            broadcastRefresh()
        }
    }
    
    embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriodMillis = 15000
            timeoutMillis = 15000
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        
        routing {
            // WebSocket endpoint for hot reload notifications
            webSocket("/hot-reload") {
                println("🔌 Hot-reload client connected")
                connections.add(this)
                
                try {
                    for (frame in incoming) {
                        // Just keep the connection alive, we're only sending from server
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("📥 Received from client: $text")
                        }
                    }
                } finally {
                    connections.remove(this)
                    println("🔌 Hot-reload client disconnected")
                }
            }
            
            // Serve static Zipline files
            staticFiles("/", ziplineDir) {
                default("manifest.zipline.json")
            }
            
            // Health check endpoint
            get("/health") {
                call.respondText("OK", ContentType.Text.Plain)
            }
            
            // Navigation test endpoint
            // Usage: GET /test-nav?route=settings
            get("/test-nav") {
                val route = call.request.queryParameters["route"] ?: "home"
                val message = "NAVIGATE:$route"
                
                println("🧭 Test navigation requested: $route")
                
                // Broadcast navigation command to all connected clients
                connections.forEach { session ->
                    try {
                        session.send(Frame.Text(message))
                    } catch (e: Exception) {
                        println("❌ Failed to send nav to client: ${e.message}")
                    }
                }
                
                call.respondText("Navigation broadcast: $route to ${connections.size} client(s)", ContentType.Text.Plain)
            }
        }
    }.start(wait = true)
}

private fun findProjectRoot(): File {
    var current = File(System.getProperty("user.dir"))
    while (current.parentFile != null) {
        if (File(current, "settings.gradle.kts").exists()) {
            return current
        }
        current = current.parentFile
    }
    // Fallback to current directory
    return File(System.getProperty("user.dir"))
}

private suspend fun watchForChanges(ziplineDir: File) {
    if (!ziplineDir.exists()) {
        println("⚠️  Zipline directory not found: ${ziplineDir.absolutePath}")
        println("    Run: ./gradlew :presenter:compileDevelopmentExecutableKotlinJs")
        return
    }
    
    val watchService = FileSystems.getDefault().newWatchService()
    val path = ziplineDir.toPath()
    
    path.register(
        watchService,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.ENTRY_CREATE
    )
    
    println("👀 Watching for changes in: ${ziplineDir.absolutePath}")
    
    while (true) {
        val key = watchService.take()
        var shouldRefresh = false
        
        for (event in key.pollEvents()) {
            val kind = event.kind()
            val filename = (event.context() as? Path)?.pathString ?: continue
            
            // Only refresh on manifest changes (this indicates a full rebuild)
            if (filename == "manifest.zipline.json" && 
                (kind == StandardWatchEventKinds.ENTRY_MODIFY || 
                 kind == StandardWatchEventKinds.ENTRY_CREATE)) {
                println("📦 Detected change: $filename")
                shouldRefresh = true
            }
        }
        
        if (shouldRefresh) {
            // Small delay to ensure file is fully written
            delay(100)
            refreshChannel.trySend(Unit)
        }
        
        key.reset()
    }
}

private suspend fun broadcastRefresh() {
    val message = "REFRESH"
    val timestamp = System.currentTimeMillis()
    
    println("📢 Broadcasting REFRESH to ${connections.size} client(s) at $timestamp")
    
    connections.forEach { session ->
        try {
            session.send(Frame.Text(message))
        } catch (e: Exception) {
            println("❌ Failed to send to client: ${e.message}")
        }
    }
}
