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
sealed class MembershipUiState {
    object Loading : MembershipUiState()
    data class Success(
        val plans: List<MembershipPlanDto>,
        val profile: ProfileDto?
    ) : MembershipUiState()
    data class Error(val message: String) : MembershipUiState()
}

suspend fun fetchMembershipData(): MembershipUiState {
    return try {
        val repo = GymServiceProvider.getRepository()
        
        if (repo == null) {
            return MembershipUiState.Error("GymService not available")
        }
        
        val plans = repo.getMembershipPlans()
        val profile = repo.getProfile()
        
        if (plans.isNotEmpty()) {
            MembershipUiState.Success(plans, profile)
        } else {
            MembershipUiState.Error("No membership plans found")
        }
    } catch (e: Exception) {
        MembershipUiState.Error("Failed to load: ${e.message}")
    }
}

/**
 * Membership Screen Content - REAL API CALLS ONLY
 */
@Composable
fun MembershipScreenContent(
    uiState: MembershipUiState
) {
    ScrollableColumn(padding = 24) {
        // Payment State
        var selectedPlanForPayment by remember { mutableStateOf<MembershipPlanDto?>(null) }
        val scope = rememberCoroutineScope()
        
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
                        onSelect = { selectedPlanForPayment = plan }
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
        if (selectedPlanForPayment != null) {
            val plan = selectedPlanForPayment!!
            PaymentSheet(
                isVisible = true,
                plan = plan,
                onDismiss = { selectedPlanForPayment = null },
                onSuccess = {
                    scope.launch {
                        val service = GymServiceProvider.getService() ?: return@launch
                        val repo = GymServiceProvider.getRepository() ?: return@launch
                        val userId = service.getSessionUserId() ?: return@launch
                        
                        // 1. Record Payment
                        repo.recordPayment(userId, plan.price, plan.id)
                        
                        // 2. Assign Membership
                        repo.assignMembership(userId, plan.id)
                        
                        // 3. Show Success & Close
                        service.showToast("Membership Activated! Welcome to the clan.")
                        selectedPlanForPayment = null
                    }
                },
                onFailure = {
                    scope.launch {
                        val service = GymServiceProvider.getService()
                        service?.showToast("Payment Failed. Please try again.")
                        selectedPlanForPayment = null
                    }
                }
            )
        }
    }
}

@Composable
fun PaymentSheet(
    isVisible: Boolean,
    plan: MembershipPlanDto,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    onDismiss: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    
    BottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss
    ) {
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Center") {
             HeaderText(text = "Complete Payment", size = "medium")
             Spacer(width = 0, height = 16)
             
             SduiCard(onClick = null) {
                 FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start") {
                     SecondaryText(text = "Membership Plan")
                     HeaderText(text = plan.name, size = "small")
                     Spacer(width = 0, height = 8)
                     // Use FlexRow for layout
                     FlexRow(horizontalArrangement = "SpaceBetween", verticalAlignment = "Center") {
                        SecondaryText(text = "Amount to Pay")
                        HeaderText(text = plan.priceLabel, size = "medium")
                     }
                 }
             }
             
             Spacer(width = 0, height = 24)
             
             if (isProcessing) {
                 SecondaryText(text = "Processing Payment...")
                 Spacer(width = 0, height = 16)
             } else {
                 ActionButton(
                     variant = "primary",
                     text = "Simulate Success (UPI)",
                     icon = "check",
                     onClick = {
                         isProcessing = true
                         onSuccess()
                     }
                 )
                 
                 Spacer(width = 0, height = 12)
                 
                 ActionButton(
                     variant = "secondary",
                     text = "Simulate Failure",
                     icon = "close",
                     onClick = {
                         onFailure()
                     }
                 )
             }
        }
    }
}
