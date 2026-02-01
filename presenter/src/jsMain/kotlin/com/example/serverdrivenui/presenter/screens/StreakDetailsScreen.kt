package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.*
import app.cash.redwood.Modifier
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.schema.compose.*
import com.example.serverdrivenui.core.data.dto.AttendanceDto
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import kotlinx.coroutines.launch
import kotlin.js.Date

// --- UI State ---
sealed class StreakUiState {
    object Loading : StreakUiState()
    data class Success(
        val currentStreak: Int,
        val totalSessions: Int,
        val attendanceMap: Map<String, Boolean>, // Key: "YYYY-MM-DD", Value: Present
        val months: List<YearMonth> // For rendering calendar structure
    ) : StreakUiState()
    data class Error(val message: String) : StreakUiState()
}

data class YearMonth(val year: Int, val month: Int, val name: String, val daysInMonth: Int, val startDayOffset: Int)

// --- Screen Class ---
class StreakDetailsScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        val scope = rememberCoroutineScope()
        var uiState by remember { mutableStateOf<StreakUiState>(StreakUiState.Loading) }

        LaunchedEffect(Unit) {
            try {
                val repo = GymServiceProvider.getRepository() ?: throw Exception("Service unavailable")
                val streak = repo.getStreak()
                val history = repo.getAllAttendanceHistory()
                val totalSessions = history.size
                val attendanceMap = history.associate { it.date to (it.status == "present") }
                
                // Build Calendar (Last 12 Months)
                val today = Date()
                val monthList = mutableListOf<YearMonth>()
                val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                
                for (i in 0 until 12) {
                    val d = Date(today.getFullYear(), today.getMonth() - i, 1)
                    val y = d.getFullYear()
                    val m = d.getMonth()
                    val monthName = "${monthNames[m]} $y"
                    val daysInMonth = Date(y, m + 1, 0).getDate()
                    val startDay = d.getDay() // 0=Sunday
                    
                    monthList.add(YearMonth(y, m, monthName, daysInMonth, startDay))
                }
                
                uiState = StreakUiState.Success(streak, totalSessions, attendanceMap, monthList)
            } catch (e: Exception) {
                uiState = StreakUiState.Error("Failed to load streak: ${e.message}")
            }
        }

        // --- UI Content ---
        ScrollableColumn(padding = 16) {
             // Header - Simplified to reduce "weirdness"
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 16, padding = 0) {
                 HeaderText(text = "My Consistency", size = "medium")
                 // Simple Text Button for Close to avoid bulky button UI
                 SduiCard(onClick = { navigator.pop() }, backgroundColor = "transparent", borderColor = "transparent", borderWidth = 0, borderRadius = 0, padding = 8) {
                     StyledText(text = "Close", style = "labelLarge", color = "primary", fontWeight = "bold", letterSpacing = 0)
                 }
            }
            
            SecondaryText(text = "Track your fitness journey")            
            when (val state = uiState) {
                is StreakUiState.Loading -> SecondaryText(text = "Loading stats...")
                is StreakUiState.Error -> StyledText(text = state.message, color = "error", style = "bodyMedium", fontWeight = "normal", letterSpacing = 0)
                is StreakUiState.Success -> {
                    // Stats
                    FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 12, padding = 0) {
                        StatsCard("Current Streak", "${state.currentStreak} 🔥")
                        StatsCard("Sessions", "${state.totalSessions} 💪")
                    }
                                        
                    // Calendar List with Pagination
                    var visibleMonths by remember { mutableStateOf(2) }
                    
                    state.months.take(visibleMonths).forEach { month ->
                        MonthCalendar(month, state.attendanceMap)
                        Spacer(width = 0, height = 16)
                    }
                    
                    // Show More Button
                    if (visibleMonths < state.months.size) {
                        SduiCard(onClick = { visibleMonths += 4 }, backgroundColor = "surface", borderColor = "border", borderWidth = 1, borderRadius = 50, padding = 12) {
                             FlexRow(horizontalArrangement = "Center", verticalAlignment = "CenterVertically", spacing = 8, padding = 0) {
                                  StyledText(text = "Show More History", style = "labelLarge", color = "primary", fontWeight = "bold", letterSpacing = 0)
                                  StyledText(text = "↓", style = "labelLarge", color = "primary", fontWeight = "bold", letterSpacing = 0)
                             }
                        }
                        Spacer(width = 0, height = 24)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(label: String, value: String) {
    SduiCard(onClick = null, backgroundColor = "surface", borderColor = "border", borderWidth = 1, borderRadius = 16, padding = 16) {
        FlexColumn(verticalArrangement = "Center", horizontalAlignment = "Start", spacing = 4, padding = 0) {
             SecondaryText(text = label)
             StyledText(text = value, style = "headlineSmall", fontWeight = "bold", color = "primary", letterSpacing = 0)
        }
    }
}

@Composable
fun MonthCalendar(month: YearMonth, attendanceMap: Map<String, Boolean>) {
    SduiCard(onClick = null, backgroundColor = "surface", borderColor = "border", borderWidth = 1, borderRadius = 16, padding = 16) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
             // Month Header
             StyledText(text = month.name, style = "titleMedium", fontWeight = "bold", color = "primary", letterSpacing = 0)
             Spacer(width = 0, height = 16)
             
             // Week Headers (Fixed Width Cells)
             FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                 listOf("S", "M", "T", "W", "T", "F", "S").forEach { 
                     // Use fixed width container for alignment
                     StyledBox(width = 36, height = 24, padding = 0, backgroundColor = "transparent", borderRadius = 0, paddingHorizontal = 0, paddingVertical = 0, contentAlignment = "Center") {
                        StyledText(text = it, style = "labelSmall", color = "tertiary", fontWeight = "bold", letterSpacing = 0)
                     }
                 }
             }
             Spacer(width = 0, height = 8)
             
             // Calendar Grid
             val days = mutableListOf<String?>()
             repeat(month.startDayOffset) { days.add(null) }
             for (d in 1..month.daysInMonth) {
                 val mStr = (month.month + 1).toString().padStart(2, '0')
                 val dStr = d.toString().padStart(2, '0')
                 days.add("${month.year}-$mStr-$dStr")
             }
             
             days.chunked(7).forEach { week ->
                 FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                     val normalizedWeek = week + List(7 - week.size) { null }
                     normalizedWeek.forEachIndexed { index, dateStr ->
                         // Derive day number from dateStr or use null
                         val dayNum = if (dateStr != null) dateStr.split("-").last().toInt() else null
                         
                         val isPresent = if (dateStr != null) attendanceMap[dateStr] == true else false
                         
                         DayCell(dayNum = dayNum, isPresent = isPresent)
                     }
                 }
                 Spacer(width = 0, height = 4) // Row spacing
             }
        }
    }
}

@Composable
fun DayCell(dayNum: Int?, isPresent: Boolean) {
    // Fixed Size Cell (36x36)
    // If Present: Green Circle Background
    // If Absent: Transparent
    
    val bgColor = if (isPresent) "success" else "transparent"
    val textColor = if (isPresent) "background" else "secondary"
    val fontWeight = if (isPresent) "bold" else "normal"
    
    StyledBox(
        width = 36, 
        height = 36, 
        padding = 0, 
        paddingHorizontal = 0, 
        paddingVertical = 0, 
        backgroundColor = bgColor, 
        borderRadius = 18, // Circle
        contentAlignment = "Center"
    ) {
        if (dayNum != null) {
            StyledText(
                text = dayNum.toString(), 
                style = "bodySmall", 
                color = textColor, 
                fontWeight = fontWeight, 
                letterSpacing = 0
            )
        }
    }
}
