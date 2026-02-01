package com.example.serverdrivenui.presenter

import app.cash.zipline.Zipline
import com.example.serverdrivenui.shared.GymService
import com.example.serverdrivenui.core.data.SupabaseGymRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * GymServiceProvider - Singleton accessor for GymService and SupabaseGymRepository.
 * 
 * Host provides Config via GymService.
 * Guest instantiates SupabaseGymRepository locally.
 */
object GymServiceProvider {
    
    private var _service: GymService? = null
    private var _repository: SupabaseGymRepository? = null
    private var initialized = false
    
    /**
     * Lazy initialization.
     */
    private suspend fun ensureInitialized() {
        if (initialized) return
        
        try {
            val zipline = Zipline.get()
            _service = zipline.take<GymService>("gym")
            
            // Initialize Repository using Host Config
            if (_service != null) {
                initializeRepository(_service!!)
            }
            
            initialized = true
            println("GymServiceProvider: Initialized")
        } catch (e: Throwable) {
            println("GymServiceProvider: INIT FAILED - ${e.message}")
            _service = null
            _repository = null
        }
    }
    
    private suspend fun initializeRepository(service: GymService) {
        try {
            println("GymServiceProvider: Fetching Host Config...")
            val config = service.getHostConfig()
            println("GymServiceProvider: Config received: ${config.supabaseUrl}")
            
            val token = service.getSessionToken()
            val userId = service.getSessionUserId()
            println("GymServiceProvider: Host session - userId=$userId, hasToken=${token != null}")
            
            println("GymServiceProvider: Creating Local Repository...")
            
            // USE PROXY ENGINE
            val engine = ZiplineProxyEngine(service)
            val client = HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(io.ktor.client.plugins.logging.Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            println("NETWORK: $message")
                        }
                    }
                    level = io.ktor.client.plugins.logging.LogLevel.ALL
                }
                // Custom Request Logging via hook not easily available in config block without plugin
                // We rely on response validator to log completion.
            }
            val repo = SupabaseGymRepository(
                httpClient = client,
                supabaseUrl = config.supabaseUrl,
                supabaseKey = config.supabaseKey
            )
            
            // Restore session
            // IMPORTANT: We check if userId starts with "user-" which was the legacy fake ID format.
            // If so, we ignore it to force re-login with real UUID.
            if (token != null && token.isNotEmpty() && userId != null && userId.isNotEmpty() && !userId.startsWith("user-")) {
                println("GymServiceProvider: Restoring Session (User: $userId)")
                repo.setSession(userId, token) 
            } else {
                println("GymServiceProvider: WARNING - No valid persisted session found! userId=$userId (Legacy=${userId?.startsWith("user-")})")
                // If it was legacy, clear it so we don't keep warning
                if (userId?.startsWith("user-") == true) {
                     service.clearSession()
                }
            }
            
            _repository = repo
            println("GymServiceProvider: Repository initialized successfully")
        } catch (e: Exception) {
            println("GymServiceProvider: Repo Init Failed: ${e.message}")
            e.printStackTrace()
            _repository = null
        }
    }
    
    /**
     * Get the GymService instance (Access to Config/Native Actions).
     * Initializes lazily on first access.
     */
    suspend fun getService(): GymService? {
        ensureInitialized()
        return _service
    }
    
    /**
     * Get the Local Supabase Repository.
     * Call this from Screens to get data.
     */
    suspend fun getRepository(): SupabaseGymRepository? {
        ensureInitialized()
        return _repository
    }
    
    /**
     * Force re-initialization.
     */
    fun reset() {
        initialized = false
        _service = null
        _repository = null
    }

    suspend fun saveSession(userId: String, token: String) {
        val service = getService()
        if (service != null) {
            println("GymServiceProvider: Saving session to Host...")
            service.saveSession(userId, token)
        }
    }

    suspend fun clearSession() {
        val service = getService()
        if (service != null) {
            println("GymServiceProvider: Clearing session on Host...")
            service.clearSession()
        }
        _repository?.logout()
    }
}
