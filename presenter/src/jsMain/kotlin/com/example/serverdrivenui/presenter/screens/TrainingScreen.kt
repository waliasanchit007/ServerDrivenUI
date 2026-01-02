package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.data.dto.*
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

// Sealed class for UI state (Top level)
private sealed class TrainingUiState {
    object Loading : TrainingUiState()
    data class Success(
        val schedule: List<TrainingDayDto>,
        val attendanceStatus: List<String>
    ) : TrainingUiState()
    data class Error(val message: String) : TrainingUiState()
}

/**
 * Training Screen Content - REAL API CALLS ONLY
 */
@Composable
fun TrainingScreenContent() {
    // State
    var uiState by remember { 
        val repo = GymServiceProvider.repository
        
        // Initial synchronous check - if data is cached, use it immediately
        val initialState = if (repo != null && repo.cachedWeeklySchedule != null) {
            val schedule = repo.cachedWeeklySchedule ?: emptyList()
            val attendance = repo.cachedWeeklyAttendance ?: emptyList()
            if (schedule.isNotEmpty()) {
                TrainingUiState.Success(schedule, attendance)
            } else {
                TrainingUiState.Loading // Maybe error? but Loading is safer fallback
            }
        } else {
            TrainingUiState.Loading
        }
        mutableStateOf(initialState) 
    }
    
    val scope = rememberCoroutineScope()
    val today = "2026-01-02"
    
    // Fetch data on mount - REAL API CALLS ONLY
    LaunchedEffect(Unit) {
        println("TrainingScreen: LaunchedEffect triggered")
        scope.launch {
            println("TrainingScreen: Coroutine launched, accessing repository...")
            try {
                val repo = GymServiceProvider.repository
                println("TrainingScreen: Repository = ${repo != null}")
                
                if (repo == null) {
                    uiState = TrainingUiState.Error("GymService not available - check host binding")
                    return@launch
                }
                
                // If we already have success state, we might skip or just refresh
                
                println("TrainingScreen: Calling getWeeklySchedule()...")
                val schedule = repo.getWeeklySchedule()
                println("TrainingScreen: Got ${schedule.size} days")
                
                val attendance = repo.getWeeklyAttendanceStatus()
                println("TrainingScreen: Got ${attendance.size} attendance records")
                
                uiState = if (schedule.isNotEmpty()) {
                    TrainingUiState.Success(schedule, attendance)
                } else {
                    TrainingUiState.Error("No training schedule found")
                }
            } catch (e: Exception) {
                println("TrainingScreen: Exception: ${e.message}")
                uiState = TrainingUiState.Error("Failed to load: ${e.message}")
            }
        }
    }

    
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
                    val isToday = day.date == today
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
