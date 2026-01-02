package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.data.dto.*
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

/**
 * MainNavigationShell - Root container with bottom navigation.
 * Uses AppScaffold for proper fixed bottom nav layout.
 */
class MainNavigationShell : Screen {
    private var currentTab by mutableStateOf("home")
    private var showingCoachSheet by mutableStateOf(false)
    private var selectedCoachName by mutableStateOf("")
    private var selectedCoachRole by mutableStateOf("")
    private var selectedCoachBio by mutableStateOf("")
    private var selectedCoachPhotoUrl by mutableStateOf("")
    private var selectedCoachInstagram by mutableStateOf("")
    
    @Composable
    override fun Content(navigator: Navigator) {
        // AppScaffold handles bottom nav layout properly with Compose Scaffold
        AppScaffold(
            showBottomBar = true,
            selectedTab = currentTab,
            onTabSelected = { tab ->
                currentTab = tab
            }
        ) {
            // Content based on selected tab
            when (currentTab) {
                "home" -> CaliclanHomeScreenContent(
                    onCoachClick = { name, role, bio, photoUrl, instagram ->
                        selectedCoachName = name
                        selectedCoachRole = role
                        selectedCoachBio = bio
                        selectedCoachPhotoUrl = photoUrl
                        selectedCoachInstagram = instagram
                        showingCoachSheet = true
                    }
                )
                "training" -> CaliclanTrainingScreenContent()
                "membership" -> CaliclanMembershipScreenContent()
                "profile" -> CaliclanProfileScreenContent()
            }
        }
        
        // Coach Profile Bottom Sheet (overlays on top)
        BottomSheet(
            isVisible = showingCoachSheet,
            onDismiss = { showingCoachSheet = false }
        ) {
            CoachProfileSheetContent(
                name = selectedCoachName,
                role = selectedCoachRole,
                bio = selectedCoachBio,
                photoUrl = selectedCoachPhotoUrl,
                instagram = selectedCoachInstagram,
                onClose = { showingCoachSheet = false }
            )
        }
    }
}

/**
 * Home Screen Content - matches web app exactly
 */
@Composable
private fun CaliclanHomeScreenContent(
    onCoachClick: (String, String, String, String, String) -> Unit
) {
    ScrollableColumn(padding = 24) {
        // 1. Greeting (two-line like web app)
        GreetingHeader(
            subtitle = "Welcome back,",
            title = "Alex"
        )
        
        Spacer(width = 0, height = 32)
        
        // 2. Membership Status Card
        StatusCard(
            status = "active",
            title = "Active",
            subtitle = "Expires on February 15, 2025",
            daysLeft = 45,
            onClick = null
        )
        
        Spacer(width = 0, height = 32)
        
        // 3. Today's Session Card
        TrainingSessionCard(
            label = "Today's Session",
            focus = "Legs & Core",
            goals = listOf("Pistol Squats", "L-Sits", "Dragon Flags"),
            onClick = null
        )
        
        Spacer(width = 0, height = 32)
        
        // 4. Training Consistency (streak + weekly visual)
        WeeklyAttendance(
            streak = 4,
            days = listOf("attended", "attended", "today", "future", "future", "future", "future"),
            summary = "Trained 2 days this week"
        )
        
        Spacer(width = 0, height = 32)
        
        // 5. Coach Announcement
        AnnouncementCard(
            label = "Coach Update",
            title = "Advanced Skills Workshop",
            message = "Join us this Saturday at 10 AM for a special muscle-up workshop."
        )
        
        Spacer(width = 0, height = 32)
        
        // 6. WhatsApp CTA Button
        ActionButton(
            icon = "whatsapp",
            text = "Contact Gym via WhatsApp",
            variant = "secondary",
            onClick = { /* Open WhatsApp */ }
        )
        
        Spacer(width = 0, height = 32)
        
        // 7. Meet Your Coaches (2-column grid with 6 coaches)
        HeaderText(text = "Meet Your Coaches", size = "medium")
        Spacer(width = 0, height = 16)
        
        CoachGrid {
            CoachCard(
                name = "Hemant",
                role = "Founder, Master Coach",
                photoUrl = "",
                onClick = {
                    onCoachClick(
                        "Hemant",
                        "Founder, Master Coach",
                        "Specializing in statics and front lever mechanics. 8+ years of calisthenics experience.",
                        "",
                        "hemant_caliclan"
                    )
                }
            )
            CoachCard(
                name = "Ankit",
                role = "Head Coach",
                photoUrl = "",
                onClick = {
                    onCoachClick(
                        "Ankit",
                        "Head Coach",
                        "Expert in progressive training and muscle-up progressions.",
                        "",
                        "ankit_coach"
                    )
                }
            )
            CoachCard(
                name = "Shoaib",
                role = "Coach",
                photoUrl = "",
                onClick = {
                    onCoachClick(
                        "Shoaib",
                        "Coach",
                        "Specializing in handstand and balance work.",
                        "",
                        "shoaib_coach"
                    )
                }
            )
            CoachCard(
                name = "Tejas",
                role = "Coach",
                photoUrl = "",
                onClick = {
                    onCoachClick(
                        "Tejas",
                        "Coach",
                        "Focus on strength and endurance training.",
                        "",
                        "tejas_coach"
                    )
                }
            )
            CoachCard(
                name = "Mayank",
                role = "Coach",
                photoUrl = "",
                onClick = {
                    onCoachClick(
                        "Mayank",
                        "Coach",
                        "Mobility and flexibility specialist.",
                        "",
                        "mayank_coach"
                    )
                }
            )
            CoachCard(
                name = "Gupta Ji",
                role = "Coach",
                photoUrl = "",
                onClick = {
                    onCoachClick(
                        "Gupta Ji",
                        "Coach",
                        "Senior coach with years of experience.",
                        "",
                        "guptaji_coach"
                    )
                }
            )
        }
        
        // Bottom padding for scrolling room
        Spacer(width = 0, height = 32)
    }
}

/**
 * Training Screen Content - LIVE DATA from Supabase via GymRepository
 */
@Composable
private fun CaliclanTrainingScreenContent() {
    // Sealed class for UI state
    sealed class TrainingUiState {
        object Loading : TrainingUiState()
        data class Success(
            val schedule: List<TrainingDayDto>,
            val attendanceStatus: List<String>
        ) : TrainingUiState()
        data class Error(val message: String) : TrainingUiState()
    }
    
    // State
    var uiState by remember { mutableStateOf<TrainingUiState>(TrainingUiState.Loading) }
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
private fun formatDateDisplay(isoDate: String): String {
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

/**
 * Membership Screen Content - REAL API CALLS ONLY
 */
@Composable
private fun CaliclanMembershipScreenContent() {
    // Sealed class for UI state
    sealed class MembershipUiState {
        object Loading : MembershipUiState()
        data class Success(
            val plans: List<MembershipPlanDto>,
            val profile: ProfileDto?
        ) : MembershipUiState()
        data class Error(val message: String) : MembershipUiState()
    }
    
    // State
    var uiState by remember { mutableStateOf<MembershipUiState>(MembershipUiState.Loading) }
    val scope = rememberCoroutineScope()
    
    // Fetch data on mount
    LaunchedEffect(Unit) {
        println("MembershipScreen: LaunchedEffect triggered")
        scope.launch {
            try {
                val repo = GymServiceProvider.repository
                println("MembershipScreen: Repository = ${repo != null}")
                
                if (repo == null) {
                    uiState = MembershipUiState.Error("GymService not available")
                    return@launch
                }
                
                val plans = repo.getMembershipPlans()
                val profile = repo.getProfile()
                println("MembershipScreen: Fetched ${plans.size} plans")
                
                uiState = if (plans.isNotEmpty()) {
                    MembershipUiState.Success(plans, profile)
                } else {
                    MembershipUiState.Error("No membership plans found")
                }
            } catch (e: Exception) {
                println("MembershipScreen: Exception: ${e.message}")
                uiState = MembershipUiState.Error("Failed to load: ${e.message}")
            }
        }
    }
    
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Membership", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Manage your gym access")
        
        Spacer(width = 0, height = 32)
        
        when (val state = uiState) {
            is MembershipUiState.Loading -> {
                SduiCard(onClick = null) {
                    SecondaryText(text = "Loading membership plans...")
                }
            }
            is MembershipUiState.Error -> {
                SduiCard(onClick = null) {
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                        SecondaryText(text = "⚠️ Error")
                        Spacer(width = 0, height = 8)
                        SecondaryText(text = state.message)
                    }
                }
            }
            is MembershipUiState.Success -> {
                // Current Plan (first plan)
                val currentPlan = state.plans.firstOrNull()
                if (currentPlan != null) {
                    MembershipPlanCard(
                        name = currentPlan.name,
                        duration = currentPlan.duration,
                        price = currentPlan.price,
                        priceLabel = currentPlan.priceLabel,
                        features = currentPlan.features,
                        isCurrent = true,
                        isRecommended = false,
                        billingDate = state.profile?.membershipExpiry?.let { formatDateDisplay(it) } ?: "",
                        onSelect = null
                    )
                }
                
                Spacer(width = 0, height = 32)
                
                // Upgrade or Renew section
                HeaderText(text = "Upgrade or Renew", size = "medium")
                Spacer(width = 0, height = 16)
                
                // Show upgrade plans (skip first/current)
                state.plans.drop(1).forEachIndexed { index, plan ->
                    MembershipPlanCard(
                        name = plan.name,
                        duration = plan.duration,
                        price = plan.price,
                        priceLabel = plan.priceLabel,
                        features = plan.features,
                        isCurrent = false,
                        isRecommended = plan.isRecommended,
                        billingDate = "",
                        onSelect = { /* Select plan */ }
                    )
                    
                    if (index < state.plans.size - 2) {
                        Spacer(width = 0, height = 16)
                    }
                }
            }
        }
        
        Spacer(width = 0, height = 24)
        
        // Contact Note (always show)
        SduiCard(onClick = null) {
            SecondaryText(text = "Need a custom plan or have questions? Contact us via WhatsApp.")
        }
        
        Spacer(width = 0, height = 32)
    }
}

/**
 * Profile Screen Content - REAL API CALLS ONLY
 */
@Composable
private fun CaliclanProfileScreenContent() {
    // Sealed class for UI state
    sealed class ProfileUiState {
        object Loading : ProfileUiState()
        data class Success(
            val profile: ProfileDto,
            val membershipHistory: List<MembershipHistoryDto>,
            val paymentHistory: List<PaymentHistoryDto>
        ) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }
    
    // State
    var uiState by remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }
    val scope = rememberCoroutineScope()
    
    // Fetch data on mount
    LaunchedEffect(Unit) {
        println("ProfileScreen: LaunchedEffect triggered")
        scope.launch {
            try {
                val repo = GymServiceProvider.repository
                println("ProfileScreen: Repository = ${repo != null}")
                
                if (repo == null) {
                    uiState = ProfileUiState.Error("GymService not available")
                    return@launch
                }
                
                val profile = repo.getProfile()
                println("ProfileScreen: Profile = ${profile?.fullName}")
                
                if (profile == null) {
                    uiState = ProfileUiState.Error("Profile not found")
                    return@launch
                }
                
                val membershipHistory = repo.getMembershipHistory()
                val paymentHistory = repo.getPaymentHistory()
                
                uiState = ProfileUiState.Success(profile, membershipHistory, paymentHistory)
            } catch (e: Exception) {
                println("ProfileScreen: Exception: ${e.message}")
                uiState = ProfileUiState.Error("Failed to load: ${e.message}")
            }
        }
    }
    
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Profile", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Your account details")
        
        Spacer(width = 0, height = 32)
        
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                SduiCard(onClick = null) {
                    SecondaryText(text = "Loading profile...")
                }
            }
            is ProfileUiState.Error -> {
                SduiCard(onClick = null) {
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                        SecondaryText(text = "⚠️ Error")
                        Spacer(width = 0, height = 8)
                        SecondaryText(text = state.message)
                    }
                }
            }
            is ProfileUiState.Success -> {
                // Member Info Card
                ProfileInfoCard(
                    name = state.profile.fullName,
                    email = state.profile.email ?: "",
                    phone = state.profile.phone ?: "",
                    batch = state.profile.batch ?: "",
                    memberSince = state.profile.createdAt?.let { formatDateDisplay(it) } ?: ""
                )
                
                Spacer(width = 0, height = 32)
                
                // Membership History
                HeaderText(text = "📅 Membership History", size = "medium")
                Spacer(width = 0, height = 16)
                
                if (state.membershipHistory.isEmpty()) {
                    SecondaryText(text = "No membership history")
                } else {
                    state.membershipHistory.forEachIndexed { index, item ->
                        HistoryItem(
                            title = item.planName,
                            subtitle = "${formatDateDisplay(item.startDate)} - ${formatDateDisplay(item.endDate)}",
                            status = item.status,
                            amount = ""
                        )
                        if (index < state.membershipHistory.size - 1) {
                            Spacer(width = 0, height = 12)
                        }
                    }
                }
                
                Spacer(width = 0, height = 32)
                
                // Payment History
                HeaderText(text = "💳 Payment History", size = "medium")
                Spacer(width = 0, height = 16)
                
                if (state.paymentHistory.isEmpty()) {
                    SecondaryText(text = "No payment history")
                } else {
                    state.paymentHistory.forEachIndexed { index, item ->
                        HistoryItem(
                            title = item.amount,
                            subtitle = "${formatDateDisplay(item.paymentDate)} • ${item.method}",
                            status = item.status,
                            amount = ""
                        )
                        if (index < state.paymentHistory.size - 1) {
                            Spacer(width = 0, height = 12)
                        }
                    }
                }
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // Support (always show)
        HeaderText(text = "Support", size = "medium")
        Spacer(width = 0, height = 16)
        
        ActionButton(
            icon = "whatsapp",
            text = "Contact Gym Support",
            variant = "secondary",
            onClick = { /* Open WhatsApp */ }
        )
        
        Spacer(width = 0, height = 16)
        
        ActionButton(
            icon = "logout",
            text = "Sign Out",
            variant = "ghost",
            onClick = { /* Sign out */ }
        )
        
        Spacer(width = 0, height = 32)
    }
}


/**
 * Coach Profile Sheet Content
 */
@Composable
private fun CoachProfileSheetContent(
    name: String,
    role: String,
    bio: String,
    photoUrl: String,
    instagram: String,
    onClose: () -> Unit
) {
    FlexColumn(
        verticalArrangement = "Top",
        horizontalAlignment = "CenterHorizontally"
    ) {
        // Coach Photo
        AsyncImage(
            url = photoUrl,
            contentDescription = name,
            size = 120,
            circular = true
        )
        
        Spacer(width = 0, height = 20)
        
        // Name & Role
        HeaderText(text = name, size = "large")
        Spacer(width = 0, height = 4)
        SecondaryText(text = role)
        
        Spacer(width = 0, height = 20)
        
        // Bio
        SecondaryText(text = bio)
        
        Spacer(width = 0, height = 20)
        
        // Instagram
        FlexRow(
            horizontalArrangement = "Center",
            verticalAlignment = "CenterVertically"
        ) {
            IconButton(
                icon = "instagram",
                onClick = { /* Open Instagram */ },
                isSelected = false
            )
            MyText(text = "@$instagram")
        }
        
        Spacer(width = 0, height = 32)
        
        // Close Button
        MyButton(
            text = "Close",
            onClick = onClose
        )
    }
}

