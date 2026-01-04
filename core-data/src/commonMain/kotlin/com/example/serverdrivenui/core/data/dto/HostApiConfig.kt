package com.example.serverdrivenui.core.data.dto

import kotlinx.serialization.Serializable

/**
 * Configuration for Host API endpoints.
 * Shared between Host and Guest via GymService.
 */
@Serializable
data class HostApiConfig(
    val supabaseUrl: String,
    val supabaseKey: String
)
