package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.core.data.dto.*
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.presenter.components.*
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

// Helper function to format date for display (e.g., "2026-02-15" -> "February 15, 2026")
fun formatDateForDisplay(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "January"
                "02" -> "February"
                "03" -> "March"
                "04" -> "April"
                "05" -> "May"
                "06" -> "June"
                "07" -> "July"
                "08" -> "August"
                "09" -> "September"
                "10" -> "October"
                "11" -> "November"
                "12" -> "December"
                else -> parts[1]
            }
            val day = parts[2].toIntOrNull() ?: parts[2]
            "$month $day, $year"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

// Calculate days left until expiry date
fun calculateDaysLeft(expiryDateStr: String?): Int {
    if (expiryDateStr == null) return 0
    return try {
        val parts = expiryDateStr.split("-")
        if (parts.size != 3) {
            println("HomeScreen: Invalid expiry format: $expiryDateStr")
            return 0
        }
        
        val expiryYear = parts[0].toIntOrNull() ?: return 0
        val expiryMonth = parts[1].toIntOrNull() ?: return 0
        val expiryDay = parts[2].toIntOrNull() ?: return 0
        
        // Use today() which returns YYYY-MM-DD format
        val today = com.example.serverdrivenui.core.data.PlatformDateProvider.today()
        println("HomeScreen: Today is $today, Expiry is $expiryDateStr")
        
        val todayParts = today.split("-")
        if (todayParts.size != 3) {
            println("HomeScreen: Invalid today format: $today")
            return 0
        }
        
        val todayYear = todayParts[0].toIntOrNull() ?: return 0
        val todayMonth = todayParts[1].toIntOrNull() ?: return 0
        val todayDay = todayParts[2].toIntOrNull() ?: return 0
        
        // Calculate approximate difference in days
        // More accurate formula considering months
        val expiryTotalDays = expiryYear * 365 + expiryMonth * 30 + expiryDay
        val todayTotalDays = todayYear * 365 + todayMonth * 30 + todayDay
        val diff = expiryTotalDays - todayTotalDays
        
        println("HomeScreen: Days left = $diff")
        if (diff < 0) 0 else diff
    } catch (e: Exception) {
        println("HomeScreen: Error calculating days left: ${e.message}")
        0
    }
}

// HomeUiState and fetchHomeData moved to HomePresenter.kt to fix state persistence issues.

// Update State sealed class to include warningMessage
// (Doing this via replace_content below might be tricky if I don't target the class definition again.
// actually I'll just update the whole file logic block or allow the compiler to guide me?
// I'll update the class definition in a separate chunk or include it here if contiguous.)
// I will split this into two chunks/tools or careful editing.
// This replacement is for the `fetchHomeData` body mostly.

/**
 * Home Screen Content - Real Data
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onCoachClick: (String, String, String, String, String) -> Unit,
    onNavigateToMembership: () -> Unit,
    onNavigateToTrainingDetail: (TrainingDayDto) -> Unit,
    onNavigateToStreak: () -> Unit
) {
    ScrollableColumn(padding = 24) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                GreetingHeaderComposable(subtitle = "Welcome back,", title = "Loading...")
                Spacer(width = 0, height = 32)
                SduiCard(onClick = null, backgroundColor = "", borderColor = "", borderWidth = 0, borderRadius = 0, padding = 0) {
                   SecondaryText(text = "Loading your dashboard...")
                }
            }
            is HomeUiState.Error -> {
                GreetingHeaderComposable(subtitle = "Welcome back,", title = "Member")
                Spacer(width = 0, height = 32)
                SduiCard(onClick = null, backgroundColor = "", borderColor = "", borderWidth = 0, borderRadius = 0, padding = 0) {
                    SecondaryText(text = "⚠️ ${state.message}")
                }
            }
            is HomeUiState.Success -> {
                // ... (lines 210-238 unchanged)
                // 1. Greeting
                GreetingHeaderComposable(
                    subtitle = "Welcome back,",
                    title = state.userName
                )
                
                Spacer(width = 0, height = 32)
                
                if (state.warningMessage != null) {
                    SduiCard(onClick = onNavigateToMembership, backgroundColor = "", borderColor = "", borderWidth = 0, borderRadius = 0, padding = 0) { // Clickable to Membership
                        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
                            HeaderText(text = "⚠️ Membership Alert", size = "small")
                            Spacer(width = 0, height = 8)
                            SecondaryText(text = state.warningMessage)
                        }
                    }
                    Spacer(width = 0, height = 32)
                }
                
                // 2. Membership Status Card - uses new unified design
                StatusCardComposable(
                    status = state.membershipStatus,
                    expiryDate = state.membershipExpiry?.let { formatDateForDisplay(it) },
                    daysLeft = state.daysLeft,
                    onClick = onNavigateToMembership 
                )
                
                Spacer(width = 0, height = 32)
                
                // 3. Today's Session Card
                val focus = state.todayTraining?.focus ?: "Rest Day"
                val goals = state.todayTraining?.goals ?: listOf("Rest", "Recover", "Hydrate")
                
                TrainingSessionCardComposable(
                    label = "Today's Session",
                    focus = focus,
                    goals = goals,
                    onClick = {
                        if (state.todayTraining != null) {
                            onNavigateToTrainingDetail(state.todayTraining)
                        }
                    } 
                )
                
                Spacer(width = 0, height = 32)
                
                // 4. Training Consistency - derive count from attendance data for consistency
                WeeklyAttendanceComposable(
                    streak = state.streak,  // Use the actual streak from state
                    days = state.attendanceDays,
                    summary = "Trained ${state.attendanceDays.count { it == "attended" }} days this week",
                    onClick = onNavigateToStreak
                )
                
                Spacer(width = 0, height = 32)

                 // 5. Coach Announcement
                AnnouncementCardComposable(
                    label = "Coach Update",
                    title = "Advanced Skills Workshop",
                    message = "Join us this Saturday at 10 AM for a special muscle-up workshop."
                )
                
                Spacer(width = 0, height = 32)
                
                // 6. WhatsApp CTA
                ActionButton(
                    icon = "whatsapp",
                    text = "Contact Gym via WhatsApp",
                    variant = "secondary",
                    onClick = { /* Open WhatsApp */ }
                )
                
                Spacer(width = 0, height = 32)
                
                // 7. Meet Your Coaches
                HeaderText(text = "Meet Your Coaches", size = "medium")
                Spacer(width = 0, height = 16)
                
                CoachGrid {
                     CoachCardComposable(
                        name = "Hemant",
                        role = "Founder",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Hemant", "Founder", "Master Coach", "", "hemant")
                        }
                    )
                     CoachCardComposable(
                        name = "Ankit",
                        role = "Head Coach",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Ankit", "Head Coach", "Skills", "", "ankit")
                        }
                    )
                     CoachCardComposable(
                        name = "Gaurav",
                        role = "Senior Coach",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Gaurav", "Senior Coach", "Strength", "", "gaurav")
                        }
                    )
                     CoachCardComposable(
                        name = "Jatin",
                        role = "Mobility Coach",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Jatin", "Mobility", "Flexibility", "", "jatin")
                        }
                    )
                }
            }
        }
        
        Spacer(width = 0, height = 100) // Extra padding for bottom nav
    }
}

