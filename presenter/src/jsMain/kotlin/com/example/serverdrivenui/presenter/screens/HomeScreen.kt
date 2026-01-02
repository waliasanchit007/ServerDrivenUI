package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.schema.compose.*

/**
 * Home Screen Content - matches web app exactly
 */
@Composable
fun HomeScreenContent(
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
