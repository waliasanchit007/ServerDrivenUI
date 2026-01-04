package com.example.serverdrivenui.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Profile Data Transfer Object - matches Supabase profiles table
 */
@Serializable
data class ProfileDto(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val email: String? = null,
    val phone: String? = null,
    val batch: String? = null,
    @SerialName("membership_status") val membershipStatus: String = "active",
    @SerialName("membership_expiry") val membershipExpiry: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
