package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*

/**
 * HomeScreen - Main dashboard for Caliclan app.
 * Layout:
 * 1. Greeting header
 * 2. Membership StatusCard
 * 3. Today's Training card
 * 4. Consistency section (streak + strip)
 * 5. Meet Your Coaches
 */
class CaliclanHomeScreen : Screen {
    
    @Composable
    override fun Content(navigator: Navigator) {
        FlexColumn(
            verticalArrangement = "Top",
            horizontalAlignment = "Start"
        ) {
            // 1. Greeting
            Spacer(width = 0, height = 16)
            MyText(text = "Good Morning, Member")
            Spacer(width = 0, height = 24)
            
            // 2. Membership Status
            StatusCard(
                status = "active",
                title = "Active Member",
                subtitle = "Your membership is active",
                daysLeft = 45,
                onClick = { navigator.push(CaliclanMembershipScreen()) }
            )
            
            Spacer(width = 0, height = 24)
            
            // 3. Today's Training
            MyText(text = "Today's Training")
            Spacer(width = 0, height = 8)
            SduiCard(onClick = { navigator.push(CaliclanTrainingScreen()) }) {
                FlexColumn(
                    verticalArrangement = "Top",
                    horizontalAlignment = "Start"
                ) {
                    MyText(text = "Skills Training")
                    MyText(text = "Handstand practice and skill-specific drills")
                }
            }
            
            Spacer(width = 0, height = 24)
            
            // 4. Consistency Section
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
            MyText(text = "You trained yesterday. Keep it up!")
            
            Spacer(width = 0, height = 24)
            
            // 5. Meet Your Coaches
            MyText(text = "Meet Your Coaches")
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
                        navigator.push(CaliclanCoachProfileScreen(
                            coachId = "1",
                            name = "Hemant Singh",
                            role = "Head Coach",
                            bio = "Founder of Caliclan. Specializing in statics and front lever mechanics.",
                            photoUrl = "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400",
                            instagram = "hemant_caliclan"
                        ))
                    }
                )
                Spacer(width = 12, height = 0)
                CoachCard(
                    name = "Arjun",
                    role = "Strength",
                    photoUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400",
                    onClick = { 
                        navigator.push(CaliclanCoachProfileScreen(
                            coachId = "2",
                            name = "Arjun Verma",
                            role = "Strength Coach",
                            bio = "Expert in progressive overload and weighted calisthenics.",
                            photoUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400",
                            instagram = "arjun_strength"
                        ))
                    }
                )
            }
            
            Spacer(width = 0, height = 24)
            
            // Navigation buttons
            FlexRow(
                horizontalArrangement = "SpaceEvenly",
                verticalAlignment = "CenterVertically"
            ) {
                MyButton(
                    text = "Training",
                    onClick = { navigator.push(CaliclanTrainingScreen()) }
                )
                MyButton(
                    text = "Membership",
                    onClick = { navigator.push(CaliclanMembershipScreen()) }
                )
            }
        }
    }
}
