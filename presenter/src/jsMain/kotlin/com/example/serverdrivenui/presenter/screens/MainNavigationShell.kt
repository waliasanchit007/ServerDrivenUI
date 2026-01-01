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
 * 
 * Features:
 * - Bottom navigation bar with 3 tabs (Home, Training, Membership)
 * - Screens switch based on selected tab
 * - Bottom sheet for coach profiles
 */
class MainNavigationShell : Screen {
    private var currentTab by mutableStateOf("home")
    private var showingCoachSheet by mutableStateOf(false)
    private var selectedCoachId by mutableStateOf<String?>(null)
    private var selectedCoachName by mutableStateOf("")
    private var selectedCoachRole by mutableStateOf("")
    private var selectedCoachBio by mutableStateOf("")
    private var selectedCoachPhotoUrl by mutableStateOf("")
    private var selectedCoachInstagram by mutableStateOf("")
    
    @Composable
    override fun Content(navigator: Navigator) {
        // Main layout: Content + Bottom Nav
        FlexColumn(
            verticalArrangement = "SpaceBetween",
            horizontalAlignment = "Start"
        ) {
            // Screen content area (takes remaining space)
            Box {
                when (currentTab) {
                    "home" -> CaliclanHomeScreenContent(
                        onCoachClick = { id, name, role, bio, photoUrl, instagram ->
                            selectedCoachId = id
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
            
            // Bottom Navigation Bar
            BottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            )
            
            // Coach Profile Bottom Sheet
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
}

/**
 * Home Screen Content (used within MainNavigationShell)
 */
@Composable
private fun CaliclanHomeScreenContent(
    onCoachClick: (String, String, String, String, String, String) -> Unit
) {
    ScrollableColumn(padding = 16) {
        // Greeting
        HeaderText(text = "Good Morning, Rahul", size = "large")
        
        Spacer(width = 0, height = 8)
        
        // Membership Card
        StatusCard(
            status = "active",
            title = "Active Member",
            subtitle = "Monthly Plan • Auto-renews Mar 1",
            daysLeft = 45,
            onClick = null
        )
        
        Spacer(width = 0, height = 16)
        
        // Today's Training
        HeaderText(text = "Today's Training", size = "medium")
        Spacer(width = 0, height = 8)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                MyText(text = "Skills Training")
                SecondaryText(text = "Handstand practice and skill-specific drills")
                Spacer(width = 0, height = 8)
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
        
        Spacer(width = 0, height = 16)
        
        // Consistency Section
        HeaderText(text = "Consistency", size = "medium")
        Spacer(width = 0, height = 8)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                MyText(text = "🔥 4-day streak")
                Spacer(width = 0, height = 8)
                ConsistencyStrip(
                    monday = "attended",
                    tuesday = "attended",
                    wednesday = "attended",
                    thursday = "today",
                    friday = "future",
                    saturday = "future",
                    sunday = "rest"
                )
                Spacer(width = 0, height = 8)
                SecondaryText(text = "You trained yesterday. Keep it up!")
            }
        }
        
        Spacer(width = 0, height = 16)
        
        // Meet Your Coaches
        HeaderText(text = "Meet Your Coaches", size = "medium")
        Spacer(width = 0, height = 8)
        
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
                        "1",
                        "Hemant Singh",
                        "Head Coach",
                        "Founder of Caliclan. Specializing in statics and front lever mechanics.",
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
                        "2",
                        "Arjun Verma",
                        "Strength Coach",
                        "Expert in progressive overload and weighted calisthenics.",
                        "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400",
                        "arjun_strength"
                    )
                }
            )
        }
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
        
        Spacer(width = 0, height = 16)
        
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
        
        ScheduleItem(
            dayName = "Tuesday",
            date = "Jan 28",
            focus = "Push Strength",
            isToday = false,
            isAttended = true,
            isRestDay = false,
            onClick = {}
        )
        
        ScheduleItem(
            dayName = "Wednesday",
            date = "Jan 29",
            focus = "Legs & Core",
            isToday = false,
            isAttended = true,
            isRestDay = false,
            onClick = {}
        )
        
        ScheduleItem(
            dayName = "Thursday",
            date = "Jan 30",
            focus = "Skills Training",
            isToday = true,
            isAttended = false,
            isRestDay = false,
            onClick = {}
        )
        
        ScheduleItem(
            dayName = "Friday",
            date = "Jan 31",
            focus = "Full Body",
            isToday = false,
            isAttended = false,
            isRestDay = false,
            onClick = {}
        )
        
        ScheduleItem(
            dayName = "Saturday",
            date = "Feb 1",
            focus = "Mobility & Flexibility",
            isToday = false,
            isAttended = false,
            isRestDay = false,
            onClick = {}
        )
        
        ScheduleItem(
            dayName = "Sunday",
            date = "Feb 2",
            focus = "",
            isToday = false,
            isAttended = false,
            isRestDay = true,
            onClick = {}
        )
    }
}

/**
 * Membership Screen Content
 */
@Composable
private fun CaliclanMembershipScreenContent() {
    ScrollableColumn(padding = 16) {
        HeaderText(text = "Membership", size = "large")
        
        Spacer(width = 0, height = 16)
        
        // Current Plan Card
        StatusCard(
            status = "active",
            title = "Monthly Plan",
            subtitle = "Active until March 1, 2026",
            daysLeft = 45,
            onClick = null
        )
        
        Spacer(width = 0, height = 24)
        
        // Renewal Options
        HeaderText(text = "Renewal Options", size = "medium")
        Spacer(width = 0, height = 12)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                MyText(text = "1 Month")
                SecondaryText(text = "Continue your journey • ₹4,000")
            }
        }
        
        Spacer(width = 0, height = 8)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                MyText(text = "3 Months")
                SecondaryText(text = "Commit to consistency • ₹10,500")
                Spacer(width = 0, height = 4)
                Chip(label = "Save 12%")
            }
        }
        
        Spacer(width = 0, height = 8)
        
        SduiCard(onClick = null) {
            FlexColumn(
                verticalArrangement = "Top",
                horizontalAlignment = "Start"
            ) {
                MyText(text = "6 Months")
                SecondaryText(text = "Best value • ₹18,000")
                Spacer(width = 0, height = 4)
                Chip(label = "Save 25%")
            }
        }
        
        Spacer(width = 0, height = 24)
        
        // Renew CTA
        MyButton(
            text = "Renew via WhatsApp",
            onClick = { /* Open WhatsApp */ }
        )
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
        
        Spacer(width = 0, height = 16)
        
        // Name & Role
        HeaderText(text = name, size = "large")
        SecondaryText(text = role)
        
        Spacer(width = 0, height = 16)
        
        // Bio
        MyText(text = bio)
        
        Spacer(width = 0, height = 16)
        
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
        
        Spacer(width = 0, height = 24)
        
        // Close Button
        MyButton(
            text = "Close",
            onClick = onClose
        )
    }
}
