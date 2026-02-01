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
        
        // --- State Hoisting for Bottom Nav Tabs ---
        // Home State is now managed by Singleton HomePresenter to persist across navigation
        val homeUiState = remember { mutableStateOf(HomePresenter.state.value) }
        
        // Subscription to HomePresenter Updates
        LaunchedEffect(Unit) {
            HomePresenter.state.collect { 
                homeUiState.value = it 
            }
        }
        
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
        // We include the states in the key so that if we manually set them to Loading (e.g. after update), it re-triggers.
        LaunchedEffect(currentTab, isLoggedIn, trainingUiState.value, membershipUiState.value, profileUiState.value) {
            if (isLoggedIn) {
                when (currentTab) {
                    "home" -> {
                        // HomePresenter manages its own caching/loading state logic
                        println("MainNavigationShell: Triggering HomePresenter load...")
                        HomePresenter.loadData()
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
             FlexColumn(verticalArrangement = "Center", horizontalAlignment = "CenterHorizontally", spacing = 0, padding = 0) {
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
                        },
                        onNavigateToMembership = {
                            currentTab = "membership"
                        },
                        onNavigateToTrainingDetail = { trainingDay ->
                            navigator.push(TrainingDetailScreen(trainingDay))
                        },
                        onNavigateToStreak = {
                            navigator.push(StreakDetailsScreen())
                        }
                    )
                    "training" -> TrainingScreenContent(
                        uiState = trainingUiState.value,
                        navigator = navigator
                    )
                    "membership" -> MembershipScreenContent(
                        uiState = membershipUiState.value,
                        onMembershipUpdate = {
                            // Invalidate ALL tabs to ensure fresh data
                            // For Home, we MUST reset the cache to force a refresh on next visit
                            HomePresenter.reset()
                            
                            // For others, set to Loading so they re-fetch on next tab visit
                            membershipUiState.value = MembershipUiState.Loading 
                            profileUiState.value = ProfileUiState.Loading
                        }
                    )
                    "profile" -> ProfileScreenContent(
                        uiState = profileUiState.value,
                        onLogout = {
                            isLoggedIn = false
                            currentTab = "home" // Reset tab
                            // Reset states on logout!
                            HomePresenter.reset() // Reset Singleton State
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
        horizontalAlignment = "CenterHorizontally",
        spacing = 0,
        padding = 0
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
            verticalAlignment = "CenterVertically",
            spacing = 0,
            padding = 0
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
