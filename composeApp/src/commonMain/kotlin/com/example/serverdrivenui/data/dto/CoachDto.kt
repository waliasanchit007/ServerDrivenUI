package com.example.serverdrivenui.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Coach Data Transfer Object - matches Supabase coaches table
 */
@Serializable
data class CoachDto(
    val id: String,
    val name: String,
    val role: String,
    val bio: String? = null,
    @SerialName("instagram_handle") val instagramHandle: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)
