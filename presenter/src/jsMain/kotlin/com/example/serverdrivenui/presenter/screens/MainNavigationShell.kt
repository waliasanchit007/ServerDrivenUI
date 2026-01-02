package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

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
 * Training Screen Content - matches web app exactly
 */
@Composable
private fun CaliclanTrainingScreenContent() {
    ScrollableColumn(padding = 24) {
        // Header matching web app
        GreetingHeader(
            subtitle = "This Week's Training",
            title = "Structured calisthenics program"
        )
        
        Spacer(width = 0, height = 24)
        
        // Weekly Schedule
        ScheduleItem(
            dayName = "Monday",
            date = "Jan 27",
            focus = "Pull Strength",
            isToday = false,
            isAttended = true,
            isRestDay = false,
            onClick = {}
        )
        
        Spacer(width = 0, height = 8)
        
        ScheduleItem(
            dayName = "Tuesday",
            date = "Jan 28",
            focus = "Push Strength",
            isToday = false,
            isAttended = true,
            isRestDay = false,
            onClick = {}
        )
        
        Spacer(width = 0, height = 8)
        
        ScheduleItem(
            dayName = "Wednesday",
            date = "Jan 29",
            focus = "Legs & Core",
            isToday = false,
            isAttended = true,
            isRestDay = false,
            onClick = {}
        )
        
        Spacer(width = 0, height = 8)
        
        ScheduleItem(
            dayName = "Thursday",
            date = "Jan 30",
            focus = "Skills Training",
            isToday = true,
            isAttended = false,
            isRestDay = false,
            onClick = {}
        )
        
        Spacer(width = 0, height = 8)
        
        ScheduleItem(
            dayName = "Friday",
            date = "Jan 31",
            focus = "Full Body",
            isToday = false,
            isAttended = false,
            isRestDay = false,
            onClick = {}
        )
        
        Spacer(width = 0, height = 8)
        
        ScheduleItem(
            dayName = "Saturday",
            date = "Feb 1",
            focus = "Mobility & Flexibility",
            isToday = false,
            isAttended = false,
            isRestDay = false,
            onClick = {}
        )
        
        Spacer(width = 0, height = 8)
        
        ScheduleItem(
            dayName = "Sunday",
            date = "Feb 2",
            focus = "",
            isToday = false,
            isAttended = false,
            isRestDay = true,
            onClick = {}
        )
        
        Spacer(width = 0, height = 24)
    }
}

/**
 * Membership Screen Content
 */
@Composable
private fun CaliclanMembershipScreenContent() {
    ScrollableColumn(padding = 16) {
        HeaderText(text = "Membership", size = "large")
        
        Spacer(width = 0, height = 24)
        
        // Current Plan Card
        StatusCard(
            status = "active",
            title = "Monthly Plan",
            subtitle = "Active until March 1, 2026",
            daysLeft = 45,
            onClick = null
        )
        
        Spacer(width = 0, height = 32)
        
        // Renewal Options
        HeaderText(text = "Renewal Options", size = "medium")
        Spacer(width = 0, height = 16)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                HeaderText(text = "1 Month", size = "small")
                SecondaryText(text = "Continue your journey • ₹4,000")
            }
        }
        
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                HeaderText(text = "3 Months", size = "small")
                SecondaryText(text = "Commit to consistency • ₹10,500")
                Spacer(width = 0, height = 8)
                Chip(label = "Save 12%")
            }
        }
        
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                HeaderText(text = "6 Months", size = "small")
                SecondaryText(text = "Best value • ₹18,000")
                Spacer(width = 0, height = 8)
                Chip(label = "Save 25%")
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // Renew CTA
        MyButton(
            text = "Renew via WhatsApp",
            onClick = { /* Open WhatsApp */ }
        )
        
        Spacer(width = 0, height = 24)
    }
}

/**
 * Profile Screen Content - matches web app exactly
 */
@Composable
private fun CaliclanProfileScreenContent() {
    ScrollableColumn(padding = 24) {
        // Header
        GreetingHeader(
            subtitle = "Profile",
            title = "Your account details"
        )
        
        Spacer(width = 0, height = 32)
        
        // Member Info Card
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                // Avatar + Name row
                FlexRow(
                    horizontalArrangement = "Start",
                    verticalAlignment = "CenterVertically"
                ) {
                    // Avatar placeholder with initial
                    Chip(label = "AM")
                    Spacer(width = 16, height = 0)
                    FlexColumn(
                        verticalArrangement = "Top",
                        horizontalAlignment = "Start"
                    ) {
                        HeaderText(text = "Alex Martinez", size = "medium")
                        SecondaryText(text = "Member since Aug 2024")
                    }
                }
                
                Spacer(width = 0, height = 16)
                
                // Info rows
                FlexRow(
                    horizontalArrangement = "SpaceBetween",
                    verticalAlignment = "CenterVertically"
                ) {
                    SecondaryText(text = "Email")
                    MyText(text = "alex.martinez@email.com")
                }
                Spacer(width = 0, height = 8)
                FlexRow(
                    horizontalArrangement = "SpaceBetween",
                    verticalAlignment = "CenterVertically"
                ) {
                    SecondaryText(text = "Phone")
                    MyText(text = "+91 98765 43210")
                }
                Spacer(width = 0, height = 8)
                FlexRow(
                    horizontalArrangement = "SpaceBetween",
                    verticalAlignment = "CenterVertically"
                ) {
                    SecondaryText(text = "Batch")
                    MyText(text = "Adult Batch - Evening")
                }
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // Membership History
        HeaderText(text = "📅 Membership History", size = "medium")
        Spacer(width = 0, height = 16)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                FlexRow(
                    horizontalArrangement = "SpaceBetween",
                    verticalAlignment = "Top"
                ) {
                    FlexColumn(
                        verticalArrangement = "Top",
                        horizontalAlignment = "Start"
                    ) {
                        MyText(text = "Monthly Unlimited")
                        SecondaryText(text = "Dec 15, 2024 - Jan 15, 2025")
                    }
                    Chip(label = "Active")
                }
            }
        }
        
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                FlexRow(
                    horizontalArrangement = "SpaceBetween",
                    verticalAlignment = "Top"
                ) {
                    FlexColumn(
                        verticalArrangement = "Top",
                        horizontalAlignment = "Start"
                    ) {
                        MyText(text = "Quarterly Unlimited")
                        SecondaryText(text = "Aug 15, 2024 - Nov 15, 2024")
                    }
                    SecondaryText(text = "Completed")
                }
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // Payment History
        HeaderText(text = "💳 Payment History", size = "medium")
        Spacer(width = 0, height = 16)
        
        SduiCard(onClick = null) {
            FlexRow(
                horizontalArrangement = "SpaceBetween",
                verticalAlignment = "CenterVertically"
            ) {
                FlexColumn(
                    verticalArrangement = "Top",
                    horizontalAlignment = "Start"
                ) {
                    MyText(text = "₹2,500")
                    SecondaryText(text = "Dec 15, 2024 • UPI")
                }
                MyText(text = "✓ Completed")
            }
        }
        
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexRow(
                horizontalArrangement = "SpaceBetween",
                verticalAlignment = "CenterVertically"
            ) {
                FlexColumn(
                    verticalArrangement = "Top",
                    horizontalAlignment = "Start"
                ) {
                    MyText(text = "₹6,500")
                    SecondaryText(text = "Aug 15, 2024 • UPI")
                }
                MyText(text = "✓ Completed")
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // Support section
        HeaderText(text = "Support", size = "medium")
        Spacer(width = 0, height = 16)
        
        ActionButton(
            icon = "whatsapp",
            text = "Contact Gym Support",
            variant = "secondary",
            onClick = { /* Open WhatsApp */ }
        )
        
        Spacer(width = 0, height = 16)
        
        // Sign out
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
