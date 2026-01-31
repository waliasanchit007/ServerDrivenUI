package com.example.serverdrivenui.shared

import com.example.serverdrivenui.shared.dto.HostApiConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * RealGymService - "Dumb" Host adapter that implements GymService (Zipline).
 * 
 * This class has NO knowledge of SupabaseGymRepository or any business logic.
 * It only provides:
 * - Config values (URL, Key as strings)
 * - Storage access
 * - Native capabilities (toast, URL opening)
 * - Network proxy
 * 
 * All actual API logic lives in the Guest (presenter module via Zipline).
 */
class RealGymService(
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val storage: StorageService,
    private val toastShower: ((String) -> Unit)? = null,
    private val urlOpener: ((String) -> Unit)? = null
) : GymService {
    
    // ============= Config =============
    
    override suspend fun getHostConfig(): HostApiConfig {
        return HostApiConfig(supabaseUrl, supabaseKey)
    }

    // ============= Session (Storage Only) =============

    override suspend fun getSessionToken(): String? {
        return storage.getString("auth_token")?.takeIf { it.isNotEmpty() }
    }

    override suspend fun getSessionUserId(): String? {
        return storage.getString("user_id")?.takeIf { it.isNotEmpty() }
    }

    override suspend fun saveSession(userId: String, accessToken: String) {
        storage.setString("user_id", userId)
        storage.setString("auth_token", accessToken)
    }

    override suspend fun clearSession() {
        storage.setString("user_id", "")
        storage.setString("auth_token", "")
    }

    // ============= Native Actions =============

    override suspend fun showToast(message: String) {
        toastShower?.invoke(message)
    }
    
    override suspend fun openUrl(url: String) {
        urlOpener?.invoke(url)
    }

    // ============= Network Proxy =============
    
    private val proxyClient = HttpClient()

    override suspend fun proxyRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): ProxyResponse {
        try {
            val response = proxyClient.request(url) {
                this.method = HttpMethod.parse(method)
                
                // Get Content-Type from headers (default to text/plain if not specified)
                val contentTypeHeader = headers.entries.find { 
                    it.key.equals("Content-Type", ignoreCase = true) 
                }?.value ?: "text/plain"
                val contentType = try {
                    ContentType.parse(contentTypeHeader)
                } catch (e: Exception) {
                    ContentType.Text.Plain
                }
                
                // Add all headers except Content-Type (we'll set it via TextContent)
                headers.forEach { (k, v) ->
                    if (!k.equals("Content-Type", ignoreCase = true)) {
                        this.headers.append(k, v)
                    }
                }
                
                // Set body with explicit Content-Type
                if (body != null) {
                    setBody(io.ktor.http.content.TextContent(body, contentType))
                }
            }
            
            val responseBody = response.bodyAsText()
            val responseHeaders = response.headers.entries().associate { it.key to it.value.joinToString(",") }
            
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

