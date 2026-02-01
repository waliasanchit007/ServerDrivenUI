package com.example.serverdrivenui.presenter.screens

import com.example.serverdrivenui.core.data.dto.*
import com.example.serverdrivenui.presenter.GymServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Moved from HomeScreen.kt
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

/**
 * HomePresenter - Singleton State Holder for Home Screen.
 * Persists state across navigation back-stack operations to prevent reloading/state loss.
 */
object HomePresenter {
    
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state = _state.asStateFlow()
    
    private var lastLoadTime = 0L
    private const val CACHE_DURATION_MS = 60000 // 1 minute cache to avoid spamming calls on quick nav
    
    fun reset() {
        _state.value = HomeUiState.Loading
        lastLoadTime = 0
    }
    
    suspend fun loadData(forceRefresh: Boolean = false) {
        val currentTime = kotlin.js.Date().getTime().toLong()
        
        // Simple cache check: If success and not forced, skip
        if (!forceRefresh && _state.value is HomeUiState.Success) {
            println("HomePresenter: Using cached data.")
            return
        }

        println("HomePresenter: Loading Data...")
        // _state.value = HomeUiState.Loading // Optional: Show loading spinner on refresh? Or keep showing content until new data arrives. 
        // Better UX: Keep old content (Success) while fetching new? 
        // For now, if it's explicitly Loading (initial), it shows spinner. If refreshing, silent update.
        
        try {
            val repo = GymServiceProvider.getRepository()
            if (repo == null) {
                println("HomePresenter: GymService/Repo is null")
                _state.value = HomeUiState.Error("GymService not available")
                return
            }
            
            // Fetch Data
            var profile = repo.getProfile()
            if (profile == null) {
                 println("HomePresenter: Profile fetch failed/missing. Using fallback.")
                 profile = ProfileDto(
                    id = "temp",
                    fullName = "Member",
                    email = "",
                    membershipStatus = "inactive",
                    membershipExpiry = null
                 )
            }
            
            val profileName = profile.fullName.split(" ").firstOrNull() ?: "Member"
            
            // Membership Logic
            val membershipHistory = repo.getMembershipHistory()
            val activePlan = membershipHistory.firstOrNull { it.status == "active" }
            
            val rawStatus = activePlan?.status ?: profile.membershipStatus
            val rawExpiry = activePlan?.endDate ?: profile.membershipExpiry
            
            val daysLeft = calculateDaysLeft(rawExpiry)
            
            val status = if (rawStatus == "active") {
                if (activePlan != null) "active"
                else if (daysLeft > 0) "active"
                else "expired"
            } else {
                "inactive"
            }
            val expiry = rawExpiry
            
            // Training
            val todayTraining = repo.getTodaySchedule()
            
            // Consistency
            val streak = repo.getStreak()
            val attendanceDays = repo.getWeeklyAttendanceStatus()
            
            val warningMessage = when {
                status == "inactive" -> "Your membership is inactive. Join a plan today!"
                status == "active" && daysLeft <= 5 -> "Your membership expires in $daysLeft days. Renew soon!"
                else -> null
            }
            
            _state.value = HomeUiState.Success(
                userName = profileName,
                membershipStatus = status,
                membershipExpiry = expiry,
                daysLeft = daysLeft,
                warningMessage = warningMessage,
                todayTraining = todayTraining,
                streak = streak,
                attendanceDays = attendanceDays
            )
            println("HomePresenter: Data Loaded Successfully")
            
        } catch (e: Exception) {
            println("HomePresenter: Error loading data: ${e.message}")
            _state.value = HomeUiState.Error("Failed to load: ${e.message}")
        }
    }
}
