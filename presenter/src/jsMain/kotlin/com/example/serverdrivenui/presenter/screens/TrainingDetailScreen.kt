package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*
import com.example.serverdrivenui.core.data.dto.TrainingDayDto
import com.example.serverdrivenui.core.data.dto.TrainingClassDto

data class TrainingDetailScreen(val day: TrainingDayDto) : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        ScrollableColumn(padding = 24) {
            // 1. Navigation Header
            FlexRow(horizontalArrangement = "Start", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                 ActionButton(
                     icon = "arrow_back", // Native implementations map this
                     text = "Back",
                     variant = "ghost",
                     onClick = { navigator.pop() }
                 )
            }
            
            Spacer(width = 0, height = 16)
            
            // 2. Day & Date Header (Big & Clear)
            FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                HeaderText(text = day.dayName, size = "large")
                SecondaryText(text = formatDateDisplay(day.date))
            }
            
            Spacer(width = 0, height = 32)
            
            // 3. Focus Summary / Badge (Optional)
            if (day.isRestDay) {
                SduiCard(onClick = null, backgroundColor = "surface", borderColor = "primary", borderWidth = 1, borderRadius = 16, padding = 24) {
                     FlexColumn(verticalArrangement = "Center", horizontalAlignment = "CenterHorizontally", spacing = 16, padding = 0) {
                         StyledText(text = "🧘‍♂️ Rest & Recovery", style = "titleMedium", color = "primary", fontWeight = "bold", letterSpacing = 0)
                         SecondaryText(text = "Take time to recover or join our Yoga session.")
                     }
                }
            } else {
                // 4. Classes List
                // We iterate cleanly to ensure order Class 1 -> Class 2 -> Class 3
                day.classes.forEach { gymClass ->
                    ClassDetailCard(gymClass)
                    Spacer(width = 0, height = 24)
                }
            }
            
            // Bottom spacing
            Spacer(width = 0, height = 48)
        }
    }
}


@Composable
fun ClassDetailCard(gymClass: TrainingClassDto) {
    SduiCard(onClick = null, backgroundColor = "surface", borderColor = "border", borderWidth = 1, borderRadius = 16, padding = 24) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 0, padding = 0) {
            // Header Row: Name (Class 1) and Badge
            FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "CenterVertically", spacing = 0, padding = 0) {
                 StyledText(text = gymClass.name.uppercase(), style = "titleSmall", color = "primary", fontWeight = "bold", letterSpacing = 1)
            }
            
            Spacer(width = 0, height = 12)
            
            // Focus Title
            if (gymClass.focus.isNotEmpty()) {
                StyledText(text = gymClass.focus, style = "titleMedium", color = "onSurface", fontWeight = "semibold", letterSpacing = 0)
                Spacer(width = 0, height = 20)
            }
            
            // Divider
            Divider(color = "border")
            Spacer(width = 0, height = 20)
            
            // Exercises / Breakdown
            if (gymClass.exercises.isNotEmpty()) {
                SecondaryText(text = "WORKOUT MODULE")
                Spacer(width = 0, height = 8)
                gymClass.exercises.forEach { ex ->
                     FlexRow(horizontalArrangement = "Start", verticalAlignment = "Top", spacing = 8, padding = 0) {
                         StyledText(text = "•", style = "bodyMedium", color = "primary", fontWeight = "bold", letterSpacing = 0)
                         StyledText(text = ex, style = "bodyMedium", color = "onSurface", fontWeight = "normal", letterSpacing = 0)
                     }
                     Spacer(width = 0, height = 6)
                }
                Spacer(width = 0, height = 24)
            }
            
            // Goals
            if (gymClass.goals.isNotEmpty()) {
                SecondaryText(text = "GOALS")
                Spacer(width = 0, height = 8)
                 FlexRow(horizontalArrangement = "Wrap", verticalAlignment = "Top", spacing = 8, padding = 0) {
                     gymClass.goals.forEach { goal ->
                         StyledBox(backgroundColor = "surface", borderRadius = 8, padding = 0, paddingHorizontal = 12, paddingVertical = 6, width = -1, height = -1, contentAlignment = "Center") {
                             StyledText(text = goal, style = "labelSmall", color = "onSurface", fontWeight = "medium", letterSpacing = 0)
                         }
                     }
                 }
                Spacer(width = 0, height = 12)
            }
            
             // Muscles
            if (gymClass.muscles.isNotEmpty()) {
                SecondaryText(text = "MUSCLES")
                Spacer(width = 0, height = 4)
                StyledText(text = gymClass.muscles.joinToString(", "), style = "labelSmall", color = "onSurfaceVariant", fontWeight = "normal", letterSpacing = 0)
            }
        }
    }
}
