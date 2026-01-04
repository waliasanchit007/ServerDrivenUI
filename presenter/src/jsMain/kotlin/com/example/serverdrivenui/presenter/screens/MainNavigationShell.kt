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
        
        // --- State Hoisting for Bottom Nav Tabs ---
        // We use 'remember' (or rememberSaveable) to hold state across tab switches.
        // This PREVENTS RELOADING when switching tabs.
        
        val homeUiState = remember { mutableStateOf<HomeUiState>(HomeUiState.Loading) }
        val trainingUiState = remember { mutableStateOf<TrainingUiState>(TrainingUiState.Loading) }
        val membershipUiState = remember { mutableStateOf<MembershipUiState>(MembershipUiState.Loading) }
        val profileUiState = remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }
        
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
        
        // Data Fetching Logic (Lazy Loading)
        // Fetches data for the current tab ONLY if it is still in Loading state.
        LaunchedEffect(currentTab, isLoggedIn) {
            if (isLoggedIn) {
                when (currentTab) {
                    "home" -> if (homeUiState.value is HomeUiState.Loading) {
                        println("MainNavigationShell: Fetching HOME data...")
                        homeUiState.value = fetchHomeData()
                    }
                    "training" -> if (trainingUiState.value is TrainingUiState.Loading) {
                        println("MainNavigationShell: Fetching TRAINING data...")
                        trainingUiState.value = fetchTrainingData()
                    }
                    "membership" -> if (membershipUiState.value is MembershipUiState.Loading) {
                        println("MainNavigationShell: Fetching MEMBERSHIP data...")
                        membershipUiState.value = fetchMembershipData()
                    }
                    "profile" -> if (profileUiState.value is ProfileUiState.Loading) {
                         println("MainNavigationShell: Fetching PROFILE data...")
                         profileUiState.value = fetchProfileData()
                    }
                }
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
                // Passing Hoisted State
                when (currentTab) {
                    "home" -> HomeScreenContent(
                        uiState = homeUiState.value,
                        onCoachClick = { name, role, bio, photoUrl, instagram ->
                            selectedCoachName = name
                            selectedCoachRole = role
                            selectedCoachBio = bio
                            selectedCoachPhotoUrl = photoUrl
                            selectedCoachInstagram = instagram
                            showingCoachSheet = true
                        }
                    )
                    "training" -> TrainingScreenContent(
                        uiState = trainingUiState.value
                    )
                    "membership" -> MembershipScreenContent(
                        uiState = membershipUiState.value
                    )
                    "profile" -> ProfileScreenContent(
                        uiState = profileUiState.value,
                        onLogout = {
                            isLoggedIn = false
                            currentTab = "home" // Reset tab
                            // Reset states on logout!
                            homeUiState.value = HomeUiState.Loading
                            trainingUiState.value = TrainingUiState.Loading
                            membershipUiState.value = MembershipUiState.Loading
                            profileUiState.value = ProfileUiState.Loading
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
