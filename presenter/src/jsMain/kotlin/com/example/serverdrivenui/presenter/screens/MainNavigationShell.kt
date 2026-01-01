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
 * Home Screen Content
 */
@Composable
private fun CaliclanHomeScreenContent(
    onCoachClick: (String, String, String, String, String) -> Unit
) {
    ScrollableColumn(padding = 16) {
        // Greeting
        HeaderText(text = "Good Morning, Rahul", size = "large")
        
        Spacer(width = 0, height = 16)
        
        // Membership Card
        StatusCard(
            status = "active",
            title = "Active Member",
            subtitle = "Monthly Plan · Auto-renews Mar 1",
            daysLeft = 45,
            onClick = null
        )
        
        Spacer(width = 0, height = 32)
        
        // TODAY'S TRAINING - PRIMARY HERO CARD
        HeaderText(text = "TODAY'S TRAINING", size = "small")
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                // Large dominant title
                HeaderText(text = "Skills Training", size = "large")
                Spacer(width = 0, height = 8)
                SecondaryText(text = "Handstand practice and skill-specific drills")
                Spacer(width = 0, height = 16)
                FlexRow(
                    horizontalArrangement = "Start",
                    verticalAlignment = "CenterVertically"
                ) {
                    Chip(label = "Skills")
                    Spacer(width = 8, height = 0)
                    Chip(label = "Balance")
                }
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // CONSISTENCY - Quiet Accountability
        HeaderText(text = "Consistency", size = "medium")
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                HeaderText(text = "🔥 4-day streak", size = "small")
                Spacer(width = 0, height = 12)
                ConsistencyStrip(
                    monday = "attended",
                    tuesday = "attended",
                    wednesday = "attended",
                    thursday = "today",
                    friday = "future",
                    saturday = "future",
                    sunday = "rest"
                )
                Spacer(width = 0, height = 12)
                // NEUTRAL COPY - no motivational coaching tone
                SecondaryText(text = "You trained yesterday")
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // MEET YOUR COACHES - Bottom section, visually present
        HeaderText(text = "Meet Your Coaches", size = "medium")
        Spacer(width = 0, height = 12)
        
        FlexRow(
            horizontalArrangement = "Start",
            verticalAlignment = "Top"
        ) {
            CoachCard(
                name = "Hemant",
                role = "Head Coach",
                photoUrl = "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400",
                onClick = {
                    onCoachClick(
                        "Hemant Singh",
                        "Head Coach",
                        "Founder of Caliclan. Specializing in statics and front lever mechanics. 8+ years of calisthenics experience.",
                        "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400",
                        "hemant_caliclan"
                    )
                }
            )
            Spacer(width = 12, height = 0)
            CoachCard(
                name = "Arjun",
                role = "Strength",
                photoUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400",
                onClick = {
                    onCoachClick(
                        "Arjun Verma",
                        "Strength Coach",
                        "Expert in progressive overload and weighted calisthenics. Certified fitness trainer.",
                        "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400",
                        "arjun_strength"
                    )
                }
            )
        }
        
        // Bottom padding for scrolling room
        Spacer(width = 0, height = 24)
    }
}

/**
 * Training Screen Content
 */
@Composable
private fun CaliclanTrainingScreenContent() {
    ScrollableColumn(padding = 16) {
        HeaderText(text = "This Week", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "January 2026")
        
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
