package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.core.data.dto.*
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

// Sealed class for UI state (Top level)
sealed class TrainingUiState {
    object Loading : TrainingUiState()
    data class Success(
        val schedule: List<TrainingDayDto>,
        val attendanceStatus: List<String>,
        val currentDate: String
    ) : TrainingUiState()
    data class Error(val message: String) : TrainingUiState()
}

suspend fun fetchTrainingData(): TrainingUiState {
    val weekStart = "2026-01-01" 
    return try {
        val repo = GymServiceProvider.getRepository()
        
        if (repo == null) {
            return TrainingUiState.Error("GymService not available")
        }
        
        val schedule = repo.getWeeklySchedule(weekStart)
        val attendance = repo.getWeeklyAttendanceStatus()
        val today = repo.getTodayDate()
        
        if (schedule.isNotEmpty()) {
            TrainingUiState.Success(schedule, attendance, today)
        } else {
            TrainingUiState.Error("No training schedule found")
        }
    } catch (e: Exception) {
        TrainingUiState.Error("Failed to load: ${e.message}")
    }
}

/**
 * Training Screen Content - REAL API CALLS ONLY
 */
@Composable
fun TrainingScreenContent(
    uiState: TrainingUiState
) {
    // val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "This Week's Training", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Structured calisthenics program")
        
        Spacer(width = 0, height = 24)
        
        // Render based on UI state
        when (val state = uiState) {
            is TrainingUiState.Loading -> {
                SduiCard(onClick = null) {
                    SecondaryText(text = "Loading training schedule...")
                }
            }
            is TrainingUiState.Error -> {
                SduiCard(onClick = null) {
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                        SecondaryText(text = "⚠️ Error")
                        Spacer(width = 0, height = 8)
                        SecondaryText(text = state.message)
                    }
                }
            }
            is TrainingUiState.Success -> {
                // Render each day from live data
                state.schedule.forEachIndexed { index, day ->
                    val isToday = day.date == state.currentDate
                    val attended = index < state.attendanceStatus.size && state.attendanceStatus[index] == "attended"
                    val dateDisplay = formatDateDisplay(day.date)
                    
                    TrainingDayCard(
                        day = day.dayName,
                        date = dateDisplay,
                        focus = day.focus,
                        goals = day.goals,
                        supporting = day.supporting,
                        isToday = isToday,
                        attended = attended
                    )
                    
                    if (index < state.schedule.size - 1) {
                        Spacer(width = 0, height = 16)
                    }
                }
            }
        }
        
        Spacer(width = 0, height = 24)
        
        // Program Notes (always show)
        SduiCard(onClick = null) {
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                SecondaryText(text = "PROGRAM NOTES")
                Spacer(width = 0, height = 8)
                SecondaryText(text = "This module focuses on building foundational strength and mastering key calisthenics skills.")
            }
        }
        
        Spacer(width = 0, height = 32)
    }
}

/**
 * Format date string from ISO to display format (e.g., "Jan 2")
 */
internal fun formatDateDisplay(isoDate: String): String {
    return try {
        val parts = isoDate.split("-")
        if (parts.size == 3) {
            val month = when (parts[1]) {
                "01" -> "Jan"
                "02" -> "Feb"
                "03" -> "Mar"
                "04" -> "Apr"
                "05" -> "May"
                "06" -> "Jun"
                "07" -> "Jul"
                "08" -> "Aug"
                "09" -> "Sep"
                "10" -> "Oct"
                "11" -> "Nov"
                "12" -> "Dec"
                else -> parts[1]
            }
            val day = parts[2].toIntOrNull() ?: parts[2]
            "$month $day"
        } else {
            isoDate
        }
    } catch (e: Exception) {
        isoDate
    }
}
