package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.core.data.dto.*
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

// Sealed class for UI state (Top level)
private sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val profile: ProfileDto,
        val membershipHistory: List<MembershipHistoryDto>,
        val paymentHistory: List<PaymentHistoryDto>
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/**
 * Profile Screen Content - REAL API CALLS ONLY
 */
@Composable
fun ProfileScreenContent(
    onLogout: () -> Unit
) {
    // State
    // State
    var uiState by remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }
    
    val scope = rememberCoroutineScope()
    
    // Fetch data on mount
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val repo = GymServiceProvider.getRepository()
                
                if (repo == null) {
                    uiState = ProfileUiState.Error("GymService not available")
                    return@launch
                }
                
                val profile = repo.getProfile()
                
                if (profile == null) {
                    uiState = ProfileUiState.Error("Profile not found")
                    return@launch
                }
                
                val membershipHistory = repo.getMembershipHistory()
                val paymentHistory = repo.getPaymentHistory()
                
                uiState = ProfileUiState.Success(profile, membershipHistory, paymentHistory)
            } catch (e: Exception) {
                uiState = ProfileUiState.Error("Failed to load: ${e.message}")
            }
        }
    }
    
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Profile", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Your account details")
        
        Spacer(width = 0, height = 32)
        
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                SduiCard(onClick = null) {
                    SecondaryText(text = "Loading profile...")
                }
            }
            is ProfileUiState.Error -> {
                SduiCard(onClick = null) {
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                        SecondaryText(text = "⚠️ Error")
                        Spacer(width = 0, height = 8)
                        SecondaryText(text = state.message)
                    }
                }
            }
            is ProfileUiState.Success -> {
                // Member Info Card
                ProfileInfoCard(
                    name = state.profile.fullName,
                    email = state.profile.email ?: "",
                    phone = state.profile.phone ?: "",
                    batch = state.profile.batch ?: "",
                    memberSince = state.profile.createdAt?.let { formatDateDisplay(it) } ?: ""
                )
                
                Spacer(width = 0, height = 32)
                
                // Membership History
                HeaderText(text = "📅 Membership History", size = "medium")
                Spacer(width = 0, height = 16)
                
                if (state.membershipHistory.isEmpty()) {
                    SecondaryText(text = "No membership history")
                } else {
                    state.membershipHistory.forEachIndexed { index, item ->
                        HistoryItem(
                            title = item.planName,
                            subtitle = "${formatDateDisplay(item.startDate)} - ${formatDateDisplay(item.endDate)}",
                            status = item.status,
                            amount = ""
                        )
                        if (index < state.membershipHistory.size - 1) {
                            Spacer(width = 0, height = 12)
                        }
                    }
                }
                
                Spacer(width = 0, height = 32)
                
                // Payment History
                HeaderText(text = "💳 Payment History", size = "medium")
                Spacer(width = 0, height = 16)
                
                if (state.paymentHistory.isEmpty()) {
                    SecondaryText(text = "No payment history")
                } else {
                    state.paymentHistory.forEachIndexed { index, item ->
                        HistoryItem(
                            title = item.amount,
                            subtitle = "${formatDateDisplay(item.paymentDate)} • ${item.method}",
                            status = item.status,
                            amount = ""
                        )
                        if (index < state.paymentHistory.size - 1) {
                            Spacer(width = 0, height = 12)
                        }
                    }
                }
            }
        }
        
        Spacer(width = 0, height = 32)
        
        // Support (always show)
        HeaderText(text = "Support", size = "medium")
        Spacer(width = 0, height = 16)
        
        ActionButton(
            icon = "whatsapp",
            text = "Contact Gym Support",
            variant = "secondary",
            onClick = { /* Open WhatsApp */ }
        )
        
        Spacer(width = 0, height = 16)
        
        ActionButton(
            icon = "logout",
            text = "Sign Out",
            variant = "ghost",
            onClick = { 
                scope.launch {
                    GymServiceProvider.clearSession()
                    onLogout()
                }
            }
        )
        
        Spacer(width = 0, height = 32)
    }
}
