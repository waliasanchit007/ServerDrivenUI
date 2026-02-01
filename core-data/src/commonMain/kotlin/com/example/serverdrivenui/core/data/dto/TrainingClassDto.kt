package com.example.serverdrivenui.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainingClassDto(
    val name: String = "",       // "Class 1", "Class 2"
    val focus: String = "",      // "Pull Strength/Static Skills"
    val exercises: List<String> = emptyList(), 
    val goals: List<String> = emptyList(),
    val muscles: List<String> = emptyList()
)
