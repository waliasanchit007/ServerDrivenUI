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
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

// Sealed class for UI state
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val userName: String,
        val membershipStatus: String, // "active", "expired", etc
        val membershipExpiry: String?, // Formatted date
        val daysLeft: Int,
        val todayTraining: TrainingDayDto?,
        val streak: Int,
        val attendanceDays: List<String>,
        val warningMessage: String?
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

suspend fun fetchHomeData(): HomeUiState {
    return try {
        println("HomeScreen: Getting Repository...")
        val repo = GymServiceProvider.getRepository()
        if (repo == null) {
            println("HomeScreen: GymService/Repo is null")
            return HomeUiState.Error("GymService not available")
        }
        println("HomeScreen: Repository obtained. Fetching Data...")
        
        // Fetch Data
        val profile = repo.getProfile()
        if (profile == null) {
            println("HomeScreen: No Profile Found")
            return HomeUiState.Error("No Profile Found")
        }
        
        println("HomeScreen: Profile Fetched: ${profile.fullName}")
        val profileName = profile.fullName.split(" ").firstOrNull() ?: "Member"
        
        // Membership
        val membershipHistory = repo.getMembershipHistory()
        val activePlan = membershipHistory.firstOrNull { it.status == "active" }
        val status = activePlan?.status ?: "inactive"
        val expiry = activePlan?.endDate 
        val daysLeft = if (status == "active") 30 else 0 
        
        // Training
        val todayTraining = repo.getTodaySchedule()
        
        // Consistency
        val streak = repo.getStreak()
        val attendanceDays = repo.getWeeklyAttendanceStatus()
        
        // Logic for warnings
        val warningMessage = when {
            status == "inactive" -> "Your membership is inactive. Join a plan today!"
            status == "active" && daysLeft <= 5 -> "Your membership expires in $daysLeft days. Renew soon!"
            else -> null
        }
        
        HomeUiState.Success(
            userName = profileName,
            membershipStatus = status,
            membershipExpiry = expiry,
            daysLeft = daysLeft,
            warningMessage = warningMessage, // New field needed in State
            todayTraining = todayTraining,
            streak = streak,
            attendanceDays = attendanceDays
        )
    } catch (e: Exception) {
        println("HomeScreen: Error loading data: ${e.message}")
        HomeUiState.Error("Failed to load: ${e.message}")
    }
}

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
    onCoachClick: (String, String, String, String, String) -> Unit
) {
    ScrollableColumn(padding = 24) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                GreetingHeader(subtitle = "Welcome back,", title = "Loading...")
                Spacer(width = 0, height = 32)
                SduiCard(onClick = null) {
                   SecondaryText(text = "Loading your dashboard...")
                }
            }
            is HomeUiState.Error -> {
                GreetingHeader(subtitle = "Welcome back,", title = "Member")
                Spacer(width = 0, height = 32)
                SduiCard(onClick = null) {
                    SecondaryText(text = "⚠️ ${state.message}")
                }
            }
            is HomeUiState.Success -> {
                // 1. Greeting
                GreetingHeader(
                    subtitle = "Welcome back,",
                    title = state.userName
                )
                
                Spacer(width = 0, height = 32)
                
                if (state.warningMessage != null) {
                    SduiCard(onClick = null) { // Could be clickable to Membership
                        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                            HeaderText(text = "⚠️ Membership Alert", size = "small")
                            Spacer(width = 0, height = 8)
                            SecondaryText(text = state.warningMessage)
                        }
                    }
                    Spacer(width = 0, height = 32)
                }
                
                // 2. Membership Status Card
                StatusCard(
                    status = state.membershipStatus,
                    title = state.membershipStatus.replaceFirstChar { it.uppercase() },
                    subtitle = state.membershipExpiry?.let { "Expires on $it" } ?: "No active plan",
                    daysLeft = state.daysLeft,
                    onClick = null
                )
                
                Spacer(width = 0, height = 32)
                
                // 3. Today's Session Card
                val focus = state.todayTraining?.focus ?: "Rest Day"
                val goals = state.todayTraining?.goals ?: listOf("Rest", "Recover", "Hydrate")
                
                TrainingSessionCard(
                    label = "Today's Session",
                    focus = focus,
                    goals = goals,
                    onClick = null 
                )
                
                Spacer(width = 0, height = 32)
                
                // 4. Training Consistency
                WeeklyAttendance(
                    streak = state.streak,
                    days = state.attendanceDays,
                    summary = "Trained ${state.streak} days this week"
                )
                
                Spacer(width = 0, height = 32)

                 // 5. Coach Announcement
                AnnouncementCard(
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
                     CoachCard(
                        name = "Hemant",
                        role = "Founder",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Hemant", "Founder", "Master Coach", "", "hemant")
                        }
                    )
                     CoachCard(
                        name = "Ankit",
                        role = "Head Coach",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Ankit", "Head Coach", "Skills", "", "ankit")
                        }
                    )
                     CoachCard(
                        name = "Gaurav",
                        role = "Senior Coach",
                        photoUrl = "",
                        onClick = {
                            onCoachClick("Gaurav", "Senior Coach", "Strength", "", "gaurav")
                        }
                    )
                     CoachCard(
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

