package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.compose.*
import com.example.serverdrivenui.presenter.GymServiceProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember

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
        var isChecking by remember { mutableStateOf(true) }
        var isLoggedIn by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        
        // Check Auth on Mount
        LaunchedEffect(Unit) {
            println("MainNavigationShell: Checking Auth...")
            try {
                // Use local repo which restores session from Host
                val repo = GymServiceProvider.getRepository()
                isLoggedIn = repo?.isLoggedIn() ?: false
                println("MainNavigationShell: Auth Check Result: isLoggedIn=$isLoggedIn")
            } catch (e: Exception) {
                println("MainNavigationShell: Auth check failed: $e")
            } finally {
                isChecking = false
            }
        }
        
        println("MainNavigationShell: Recompose. isChecking=$isChecking, isLoggedIn=$isLoggedIn")
        
        if (isChecking) {
             FlexColumn(verticalArrangement = "Center", horizontalAlignment = "CenterHorizontally") {
                 SecondaryText(text = "Loading...")
             }
        } else if (!isLoggedIn) {
            LoginScreenContent(onLoginSuccess = { isLoggedIn = true })
        } else {
            // AppScaffold handles bottom nav layout properly with Compose Scaffold
            AppScaffold(
                showBottomBar = true,
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            ) {
                // Content based on selected tab - delegate to separate screen files
                when (currentTab) {
                    "home" -> HomeScreenContent(
                        onCoachClick = { name, role, bio, photoUrl, instagram ->
                            selectedCoachName = name
                            selectedCoachRole = role
                            selectedCoachBio = bio
                            selectedCoachPhotoUrl = photoUrl
                            selectedCoachInstagram = instagram
                            showingCoachSheet = true
                        }
                    )
                    "training" -> TrainingScreenContent()
                    "membership" -> MembershipScreenContent()
                    "profile" -> ProfileScreenContent(
                        onLogout = {
                            isLoggedIn = false
                            currentTab = "home" // Reset tab
                        }
                    )
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
