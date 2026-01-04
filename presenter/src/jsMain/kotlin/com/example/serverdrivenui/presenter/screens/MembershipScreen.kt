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
private sealed class MembershipUiState {
    object Loading : MembershipUiState()
    data class Success(
        val plans: List<MembershipPlanDto>,
        val profile: ProfileDto?
    ) : MembershipUiState()
    data class Error(val message: String) : MembershipUiState()
}

/**
 * Membership Screen Content - REAL API CALLS ONLY
 */
@Composable
fun MembershipScreenContent() {
    // State
    // State
    var uiState by remember { mutableStateOf<MembershipUiState>(MembershipUiState.Loading) }
    
    val scope = rememberCoroutineScope()
    
    // Fetch data on mount
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val repo = GymServiceProvider.getRepository()
                
                if (repo == null) {
                    uiState = MembershipUiState.Error("GymService not available")
                    return@launch
                }
                
                val plans = repo.getMembershipPlans()
                val profile = repo.getProfile()
                
                uiState = if (plans.isNotEmpty()) {
                    MembershipUiState.Success(plans, profile)
                } else {
                    MembershipUiState.Error("No membership plans found")
                }
            } catch (e: Exception) {
                uiState = MembershipUiState.Error("Failed to load: ${e.message}")
            }
        }
    }
    
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Membership", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Manage your gym access")
        
        Spacer(width = 0, height = 32)
        
        when (val state = uiState) {
            is MembershipUiState.Loading -> {
                SduiCard(onClick = null) {
                    SecondaryText(text = "Loading membership plans...")
                }
            }
            is MembershipUiState.Error -> {
                SduiCard(onClick = null) {
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                        SecondaryText(text = "⚠️ Error")
                        Spacer(width = 0, height = 8)
                        SecondaryText(text = state.message)
                    }
                }
            }
            is MembershipUiState.Success -> {
                // Current Plan (first plan)
                val currentPlan = state.plans.firstOrNull()
                if (currentPlan != null) {
                    MembershipPlanCard(
                        name = currentPlan.name,
                        duration = currentPlan.duration,
                        price = currentPlan.price,
                        priceLabel = currentPlan.priceLabel,
                        features = currentPlan.features,
                        isCurrent = true,
                        isRecommended = false,
                        billingDate = state.profile?.membershipExpiry?.let { formatDateDisplay(it) } ?: "",
                        onSelect = null
                    )
                }
                
                Spacer(width = 0, height = 32)
                
                // Upgrade or Renew section
                HeaderText(text = "Upgrade or Renew", size = "medium")
                Spacer(width = 0, height = 16)
                
                // Show upgrade plans (skip first/current)
                state.plans.drop(1).forEachIndexed { index, plan ->
                    MembershipPlanCard(
                        name = plan.name,
                        duration = plan.duration,
                        price = plan.price,
                        priceLabel = plan.priceLabel,
                        features = plan.features,
                        isCurrent = false,
                        isRecommended = plan.isRecommended,
                        billingDate = "",
                        onSelect = { /* Select plan */ }
                    )
                    
                    if (index < state.plans.size - 2) {
                        Spacer(width = 0, height = 16)
                    }
                }
            }
        }
        
        Spacer(width = 0, height = 24)
        
        // Contact Note (always show)
        SduiCard(onClick = null) {
            SecondaryText(text = "Need a custom plan or have questions? Contact us via WhatsApp.")
        }
        
        Spacer(width = 0, height = 32)
    }
}
