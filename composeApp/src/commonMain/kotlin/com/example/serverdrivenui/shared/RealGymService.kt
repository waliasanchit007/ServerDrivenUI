package com.example.serverdrivenui.shared

import com.example.serverdrivenui.core.data.SupabaseGymRepository
import com.example.serverdrivenui.shared.dto.HostApiConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * RealGymService - Adapter that implements GymService (Zipline)
 * but delegates logic to Core Data (SupabaseGymRepository).
 */
/**
 * RealGymService - Adapter that implements GymService (Zipline)
 * but delegates logic to Core Data (SupabaseGymRepository) or Storage.
 */
class RealGymService(
    private val repository: SupabaseGymRepository,
    private val storage: StorageService?, // Optional for backward compat, but needed for proper persistence
    private val toastShower: ((String) -> Unit)? = null,
    private val urlOpener: ((String) -> Unit)? = null
) : GymService {

    constructor(
        httpClient: HttpClient, 
        supabaseUrl: String, 
        supabaseKey: String,
        storage: StorageService?,
        toastShower: ((String) -> Unit)? = null,
        urlOpener: ((String) -> Unit)? = null
    ) : this(
        SupabaseGymRepository(httpClient, supabaseUrl, supabaseKey),
        storage,
        toastShower,
        urlOpener
    )
    
    // Auth & Utilities
    
    override suspend fun getHostConfig(): HostApiConfig {
        return HostApiConfig(repository.supabaseUrl, repository.supabaseKey)
    }

    override suspend fun getSessionToken(): String? {
        // Prefer storage, fallback to repo memory
        return storage?.getString("auth_token") ?: repository.currentAccessToken
    }

    override suspend fun getSessionUserId(): String? {
        return storage?.getString("user_id") ?: repository.currentUserId
    }

    override suspend fun saveSession(userId: String, accessToken: String) {
        storage?.setString("user_id", userId)
        storage?.setString("auth_token", accessToken)
        // Also update local repo just in case (though Guest has its own repo)
        repository.setSession(userId, accessToken)
    }

    override suspend fun clearSession() {
        storage?.setString("user_id", "")
        storage?.setString("auth_token", "")
        repository.setSession(null, null)
    }

    override suspend fun showToast(message: String) {
        toastShower?.invoke(message)
    }
    
    override suspend fun openUrl(url: String) {
        urlOpener?.invoke(url)
    }

    // ============= Network Proxy =============
    
    private val proxyClient = HttpClient() {
        // Basic config
    }

    override suspend fun proxyRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): ProxyResponse {
        try {
            val response = proxyClient.request(url) {
                this.method = io.ktor.http.HttpMethod.parse(method)
                headers.forEach { (k, v) ->
                    this.headers.append(k, v)
                }
                if (body != null) {
                    setBody(body)
                }
            }
            
            val responseBody = response.bodyAsText()
            val responseHeaders = response.headers.entries().associate { it.key to it.value.joinToString(",") }
            
            println("RealGymService: Proxy success: ${response.status} (Body: ${responseBody.length} chars)")
            
            return ProxyResponse(
                status = response.status.value,
                body = responseBody,
                headers = responseHeaders
            )
        } catch (e: Exception) {
            println("RealGymService: Proxy request failed: $e")
            return ProxyResponse(500, "{\"error\": \"${e.message}\"}", emptyMap())
        }
    }

    override fun close() {
        proxyClient.close()
    }
}
