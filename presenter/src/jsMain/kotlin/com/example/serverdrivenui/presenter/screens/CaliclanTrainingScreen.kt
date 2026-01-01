package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * TrainingScreen - Weekly training schedule.
 * Shows Mon-Sun with today highlighted and attendance markers.
 */
class CaliclanTrainingScreen : Screen {
    
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "Start"
        ) {
            Spacer(width = 0, height = 16)
            MyText(text = "This Week")
            Spacer(width = 0, height = 16)
            
            // Monday
            ScheduleItem(
                dayName = "Monday",
                date = "Dec 29",
                focus = "Pull Strength",
                isToday = false,
                isAttended = true,
                isRestDay = false,
                onClick = {}
            )
            
            Spacer(width = 0, height = 8)
            
            // Tuesday
            ScheduleItem(
                dayName = "Tuesday",
                date = "Dec 30",
                focus = "Push Strength",
                isToday = false,
                isAttended = true,
                isRestDay = false,
                onClick = {}
            )
            
            Spacer(width = 0, height = 8)
            
            // Wednesday
            ScheduleItem(
                dayName = "Wednesday",
                date = "Dec 31",
                focus = "Legs & Core",
                isToday = false,
                isAttended = true,
                isRestDay = false,
                onClick = {}
            )
            
            Spacer(width = 0, height = 8)
            
            // Thursday (Today)
            ScheduleItem(
                dayName = "Thursday",
                date = "Jan 1",
                focus = "Skills Training",
                isToday = true,
                isAttended = false,
                isRestDay = false,
                onClick = {}
            )
            
            Spacer(width = 0, height = 8)
            
            // Friday
            ScheduleItem(
                dayName = "Friday",
                date = "Jan 2",
                focus = "Full Body",
                isToday = false,
                isAttended = false,
                isRestDay = false,
                onClick = {}
            )
            
            Spacer(width = 0, height = 8)
            
            // Saturday
            ScheduleItem(
                dayName = "Saturday",
                date = "Jan 3",
                focus = "Open Gym",
                isToday = false,
                isAttended = false,
                isRestDay = false,
                onClick = {}
            )
            
            Spacer(width = 0, height = 8)
            
            // Sunday (Rest)
            ScheduleItem(
                dayName = "Sunday",
                date = "Jan 4",
                focus = "Rest & Recovery",
                isToday = false,
                isAttended = false,
                isRestDay = true,
                onClick = {}
            )
            
            Spacer(width = 0, height = 24)
            
            MyButton(
                text = "Back to Home",
                onClick = { navigator.pop() }
            )
        }
    }
}
