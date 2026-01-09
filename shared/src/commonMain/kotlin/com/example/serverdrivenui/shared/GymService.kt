package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService
import com.example.serverdrivenui.shared.dto.HostApiConfig

/**
 * GymService - The Bridge between Host and Guest for Caliclan app.
 * Guest logic NEVER calls Supabase directly; all data flows through here.
 */
interface GymService : ZiplineService {
    // ============= Dynamic Config =============
    
    /**
     * Get the Host API configuration (Url, Key).
     */
    suspend fun getHostConfig(): HostApiConfig
    
    /**
     * Get the current session access token.
     * Use this to create a local Supabase client in Guest logic.
     */
    suspend fun getSessionToken(): String?

    /**
     * Get the current session user ID.
     */
    suspend fun getSessionUserId(): String?

    /**
     * Persist session credentials on the Host.
     */
    suspend fun saveSession(userId: String, accessToken: String)

    /**
     * Clear session credentials (Logout).
     */
    suspend fun clearSession()
    
    // ============= Native Actions =============
    
    /**
     * Open URL (WhatsApp, Instagram, etc.)
     */
    suspend fun openUrl(url: String)
    
    /**
     * Show native toast message.
     */
    /**
     * Show native toast message.
     */
    suspend fun showToast(message: String)
    
    // ============= Network Proxy =============
    
    /**
     * Execute a network request on behalf of the Guest.
     * This bypasses the Guest's lack of fetch/XHR support.
     */
    suspend fun proxyRequest(
        url: String, 
        method: String, 
        headers: Map<String, String>, 
        body: String?
    ): ProxyResponse

}

@kotlinx.serialization.Serializable
data class ProxyResponse(
    val status: Int,
    val body: String,
    val headers: Map<String, String>
)
