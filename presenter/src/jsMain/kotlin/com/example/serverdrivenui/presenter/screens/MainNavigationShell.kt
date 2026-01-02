package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.presenter.GymServiceProvider
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
 * Training Screen Content - PIXEL PERFECT match to web app
 */
@Composable
private fun CaliclanTrainingScreenContent() {
    ScrollableColumn(padding = 24) {
        // Header - matches web app exactly
        HeaderText(text = "This Week's Training", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Structured calisthenics program")
        
        Spacer(width = 0, height = 24)
        
        // Day 1: Monday - Attended
        TrainingDayCard(
            day = "Monday",
            date = "Dec 30",
            focus = "Pull Strength & Skills",
            goals = listOf("Muscle-ups", "Front Lever Progressions", "Wide Grip Pull-ups"),
            supporting = listOf("Core Stability", "Shoulder Mobility"),
            isToday = false,
            attended = true
        )
        
        Spacer(width = 0, height = 16)
        
        // Day 2: Tuesday - Attended
        TrainingDayCard(
            day = "Tuesday",
            date = "Dec 31",
            focus = "Push Strength & Balance",
            goals = listOf("Handstand Push-ups", "Planche Leans", "Ring Dips"),
            supporting = listOf("Wrist Conditioning", "Scapular Control"),
            isToday = false,
            attended = true
        )
        
        Spacer(width = 0, height = 16)
        
        // Day 3: Wednesday - TODAY (amber highlight)
        TrainingDayCard(
            day = "Wednesday",
            date = "Jan 1",
            focus = "Legs & Core",
            goals = listOf("Pistol Squats", "L-Sits", "Dragon Flags"),
            supporting = listOf("Hip Mobility", "Ankle Strength"),
            isToday = true,
            attended = false
        )
        
        Spacer(width = 0, height = 16)
        
        // Day 4: Thursday
        TrainingDayCard(
            day = "Thursday",
            date = "Jan 2",
            focus = "Skills & Flow",
            goals = listOf("Bar Muscle-up", "Handstand Holds", "Bar Flow Combinations"),
            supporting = listOf("Flexibility", "Movement Coordination"),
            isToday = false,
            attended = false
        )
        
        Spacer(width = 0, height = 16)
        
        // Day 5: Friday
        TrainingDayCard(
            day = "Friday",
            date = "Jan 3",
            focus = "Mobility & Recovery",
            goals = listOf("Deep Stretching", "Active Flexibility", "Joint Preparation"),
            supporting = listOf("Breath Work", "Light Conditioning"),
            isToday = false,
            attended = false
        )
        
        Spacer(width = 0, height = 16)
        
        // Day 6: Saturday
        TrainingDayCard(
            day = "Saturday",
            date = "Jan 4",
            focus = "Full Body Strength",
            goals = listOf("Weighted Pull-ups", "Ring Muscle-ups", "Squat Variations"),
            supporting = listOf("Core Compression", "Endurance"),
            isToday = false,
            attended = false
        )
        
        Spacer(width = 0, height = 16)
        
        // Day 7: Sunday - Active Rest
        TrainingDayCard(
            day = "Sunday",
            date = "Jan 5",
            focus = "Active Rest",
            goals = listOf("Light Movement", "Yoga Flow", "Mobility Work"),
            supporting = listOf("Recovery", "Mindfulness"),
            isToday = false,
            attended = false
        )
        
        Spacer(width = 0, height = 24)
        
        // Program Notes
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                SecondaryText(text = "PROGRAM NOTES")
                Spacer(width = 0, height = 8)
                SecondaryText(text = "This module focuses on building foundational strength and mastering key calisthenics skills. Progress at your own pace and prioritize form over speed.")
            }
        }
        
        Spacer(width = 0, height = 32)
    }
}

/**
 * Membership Screen Content - PIXEL PERFECT match to web app
 */
@Composable
private fun CaliclanMembershipScreenContent() {
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Membership", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Manage your gym access")
        
        Spacer(width = 0, height = 32)
        
        // Current Plan Card (amber gradient border, prominent)
        MembershipPlanCard(
            name = "Monthly Unlimited",
            duration = "1 Month",
            price = "₹2,500",
            priceLabel = "per month",
            features = listOf(
                "Unlimited access to all sessions",
                "Weekly structured training program",
                "Community support",
                "Coach guidance"
            ),
            isCurrent = true,
            isRecommended = false,
            billingDate = "February 15, 2025",
            onSelect = null
        )
        
        Spacer(width = 0, height = 32)
        
        // Upgrade or Renew section
        HeaderText(text = "Upgrade or Renew", size = "medium")
        Spacer(width = 0, height = 16)
        
        // Quarterly - Recommended
        MembershipPlanCard(
            name = "Quarterly Unlimited",
            duration = "3 Months",
            price = "₹6,500",
            priceLabel = "total",
            features = listOf(
                "Unlimited access to all sessions",
                "Weekly structured training program",
                "Community support",
                "Coach guidance",
                "Save 13% vs monthly"
            ),
            isCurrent = false,
            isRecommended = true,
            billingDate = "",
            onSelect = { /* Select plan */ }
        )
        
        Spacer(width = 0, height = 16)
        
        // Annual
        MembershipPlanCard(
            name = "Annual Unlimited",
            duration = "12 Months",
            price = "₹24,000",
            priceLabel = "total",
            features = listOf(
                "Unlimited access to all sessions",
                "Weekly structured training program",
                "Community support",
                "Coach guidance",
                "Priority workshop access",
                "Save 20% vs monthly"
            ),
            isCurrent = false,
            isRecommended = false,
            billingDate = "",
            onSelect = { /* Select plan */ }
        )
        
        Spacer(width = 0, height = 24)
        
        // Contact Note
        SduiCard(onClick = null) {
            SecondaryText(text = "Need a custom plan or have questions? Contact us via WhatsApp for personalized membership options.")
        }
        
        Spacer(width = 0, height = 32)
    }
}

/**
 * Profile Screen Content - PIXEL PERFECT match to web app
 */
@Composable
private fun CaliclanProfileScreenContent() {
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Profile", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Your account details")
        
        Spacer(width = 0, height = 32)
        
        // Member Info Card
        ProfileInfoCard(
            name = "Alex Martinez",
            email = "alex.martinez@email.com",
            phone = "+91 98765 43210",
            batch = "Adult Batch - Evening",
            memberSince = "Aug 2024"
        )
        
        Spacer(width = 0, height = 32)
        
        // Membership History
        HeaderText(text = "📅 Membership History", size = "medium")
        Spacer(width = 0, height = 16)
        
        HistoryItem(
            title = "Monthly Unlimited",
            subtitle = "Dec 15, 2024 - Jan 15, 2025",
            status = "active",
            amount = ""
        )
        
        Spacer(width = 0, height = 12)
        
        HistoryItem(
            title = "Quarterly Unlimited",
            subtitle = "Aug 15, 2024 - Nov 15, 2024",
            status = "completed",
            amount = ""
        )
        
        Spacer(width = 0, height = 32)
        
        // Payment History
        HeaderText(text = "💳 Payment History", size = "medium")
        Spacer(width = 0, height = 16)
        
        HistoryItem(
            title = "₹2,500",
            subtitle = "Dec 15, 2024 • UPI",
            status = "completed",
            amount = ""
        )
        
        Spacer(width = 0, height = 12)
        
        HistoryItem(
            title = "₹6,500",
            subtitle = "Aug 15, 2024 • UPI",
            status = "completed",
            amount = ""
        )
        
        Spacer(width = 0, height = 32)
        
        // Support
        HeaderText(text = "Support", size = "medium")
        Spacer(width = 0, height = 16)
        
        ActionButton(
            icon = "whatsapp",
            text = "Contact Gym Support",
            variant = "secondary",
            onClick = { /* Open WhatsApp */ }
        )
        
        Spacer(width = 0, height = 16)
        
        // Sign out (red text)
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
