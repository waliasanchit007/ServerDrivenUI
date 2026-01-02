package com.example.serverdrivenui.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Training Day Data Transfer Object - matches Supabase training_schedule table
 */
@Serializable
data class TrainingDayDto(
    val id: String,
    val date: String,
    @SerialName("day_name") val dayName: String,
    val focus: String,
    val description: String? = null,
    val goals: List<String> = emptyList(),
    val supporting: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @SerialName("is_rest_day") val isRestDay: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
