package com.example.serverdrivenui.shared

/**
 * Configuration for Host API endpoints.
 * Used by SharedAppSpec to initialize services.
 */
data class HostApiConfig(
    val supabaseUrl: String,
    val supabaseKey: String
)
