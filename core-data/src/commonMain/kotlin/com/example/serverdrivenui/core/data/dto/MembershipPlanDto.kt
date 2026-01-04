package com.example.serverdrivenui.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Membership Plan Data Transfer Object - matches Supabase membership_plans table
 */
@Serializable
data class MembershipPlanDto(
    val id: String = "",
    val name: String = "",
    val duration: String = "",
    val price: String = "",
    @SerialName("price_label") val priceLabel: String = "",
    val features: List<String> = emptyList(),
    @SerialName("is_recommended") val isRecommended: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)
