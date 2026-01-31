package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.serverdrivenui.presenter.GymServiceProvider
import com.example.serverdrivenui.presenter.components.MembershipPlanCardComposable
import com.example.serverdrivenui.core.data.dto.*
import com.example.serverdrivenui.schema.compose.*
import kotlinx.coroutines.launch

// Sealed class for UI state (Top level)
sealed class MembershipUiState {
    object Loading : MembershipUiState()
    data class Success(
        val availablePlans: List<MembershipPlanDto>,  // All available plans from API
        val currentPlanName: String?,                   // User's actual current plan name
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
        
        // Get user's actual current plan from membership_history
        val membershipHistory = repo.getMembershipHistory()
        val activeMembership = membershipHistory.firstOrNull { it.status == "active" }
        val currentPlanName = activeMembership?.planName
        
        if (plans.isNotEmpty()) {
            MembershipUiState.Success(
                availablePlans = plans,
                currentPlanName = currentPlanName,
                profile = profile
            )
        } else {
            MembershipUiState.Error("No membership plans found")
        }
    } catch (e: Exception) {
        MembershipUiState.Error("Failed to load: ${e.message}")
    }
}

/**
 * Membership Screen Content - with state refresh capability
 */
@Composable
fun MembershipScreenContent(
    uiState: MembershipUiState
) {
    // Payment State
    var selectedPlanForPayment by remember { mutableStateOf<MembershipPlanDto?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // Mutable UI state for refresh after payment
    var currentState by remember { mutableStateOf(uiState) }
    
    // Refresh when trigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            currentState = MembershipUiState.Loading
            currentState = fetchMembershipData()
        }
    }
    
    // Update current state when initial uiState changes
    LaunchedEffect(uiState) {
        currentState = uiState
    }
    
    // Main content
    ScrollableColumn(padding = 24) {
        // Header
        HeaderText(text = "Membership", size = "large")
        Spacer(width = 0, height = 8)
        SecondaryText(text = "Manage your gym access")
        
        Spacer(width = 0, height = 32)
        
        when (val state = currentState) {
            is MembershipUiState.Loading -> {
                SduiCard(onClick = null, backgroundColor = "surface", borderColor = "border", borderWidth = 1, borderRadius = 16, padding = 20) {
                    SecondaryText(text = "Loading membership plans...")
                }
            }
            is MembershipUiState.Error -> {
                SduiCard(onClick = null, backgroundColor = "surface", borderColor = "error", borderWidth = 1, borderRadius = 16, padding = 20) {
                    FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                        StyledText(text = "⚠️ Error", style = "titleSmall", color = "error", fontWeight = "semibold", letterSpacing = 0)
                        SecondaryText(text = state.message)
                    }
                }
            }
            is MembershipUiState.Success -> {
                // Find user's current plan from available plans
                val currentPlan = if (state.currentPlanName != null) {
                    state.availablePlans.find { cleanPlanName(it.name) == cleanPlanName(state.currentPlanName) }
                        ?: state.availablePlans.firstOrNull()
                } else {
                    null // No active plan
                }
                
                // Current Plan Card
                if (currentPlan != null) {
                    MembershipPlanCardComposable(
                        name = cleanPlanName(currentPlan.name),
                        duration = currentPlan.duration,
                        price = currentPlan.price,
                        priceLabel = currentPlan.priceLabel,
                        features = currentPlan.features,
                        isCurrent = true,
                        isRecommended = false,
                        billingDate = state.profile?.membershipExpiry?.let { formatMembershipDate(it) } ?: "",
                        onSelect = null
                    )
                } else {
                    // No current plan - show message
                    SduiCard(onClick = null, backgroundColor = "surface", borderColor = "accentmuted", borderWidth = 1, borderRadius = 16, padding = 20) {
                        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 8, padding = 0) {
                            StyledText(text = "No Active Plan", style = "titleMedium", color = "secondary", fontWeight = "medium", letterSpacing = 0)
                            SecondaryText(text = "Select a plan below to get started")
                        }
                    }
                }
                
                Spacer(width = 0, height = 32)
                
                // Available Plans section
                HeaderText(text = if (currentPlan != null) "Upgrade or Renew" else "Available Plans", size = "medium")
                Spacer(width = 0, height = 16)
                
                // Show all plans except current
                state.availablePlans.forEachIndexed { index, plan ->
                    val isCurrentPlan = currentPlan != null && 
                        cleanPlanName(plan.name) == cleanPlanName(currentPlan.name)
                    
                    if (!isCurrentPlan) {
                        MembershipPlanCardComposable(
                            name = cleanPlanName(plan.name),
                            duration = plan.duration,
                            price = plan.price,
                            priceLabel = plan.priceLabel,
                            features = plan.features,
                            isCurrent = false,
                            isRecommended = plan.isRecommended,
                            billingDate = "",
                            onSelect = { selectedPlanForPayment = plan }
                        )
                        
                        if (index < state.availablePlans.size - 1) {
                            Spacer(width = 0, height = 16)
                        }
                    }
                }
            }
        }
        
        Spacer(width = 0, height = 24)
        
        // Contact Note
        SduiCard(onClick = null, backgroundColor = "surface", borderColor = "border", borderWidth = 1, borderRadius = 16, padding = 20) {
            SecondaryText(text = "Need a custom plan or have questions? Contact us via WhatsApp.")
        }
    }
    
    // PaymentSheet
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
                    
                    // 4. Trigger refresh to update UI with new plan
                    refreshTrigger++
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
        FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 24, padding = 0) {
             HeaderText(text = "Complete Payment", size = "medium")
             
             SduiCard(onClick = null, backgroundColor = "surfacevariant", borderColor = "", borderWidth = 0, borderRadius = 12, padding = 16) {
                 FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
                     FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                         StyledText(text = "Membership Plan", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                         StyledText(text = cleanPlanName(plan.name), style = "titleMedium", color = "primary", fontWeight = "semibold", letterSpacing = 0)
                     }
                     FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 4, padding = 0) {
                         StyledText(text = "Amount to Pay", style = "bodySmall", color = "secondary", fontWeight = "normal", letterSpacing = 0)
                         StyledText(text = plan.priceLabel, style = "headlineMedium", color = "accent", fontWeight = "bold", letterSpacing = 0)
                     }
                 }
             }
             
             if (isProcessing) {
                 SecondaryText(text = "Processing Payment...")
             } else {
                 FlexColumn(verticalArrangement = "Top", horizontalAlignment = "Start", spacing = 12, padding = 0) {
                     ActionButton(
                         variant = "primary",
                         text = "Simulate Success (UPI)",
                         icon = "check",
                         onClick = {
                             isProcessing = true
                             onSuccess()
                         }
                     )
                     
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
}

/**
 * Clean plan name by removing price suffix
 */
private fun cleanPlanName(name: String): String {
    // Pattern 1: "Name - duration price" -> strip after last hyphen with duration+price
    if (name.contains(" - ")) {
        val parts = name.split(" - ")
        if (parts.size > 1) {
            val lastPart = parts.last()
            val looksLikeDurationPrice = lastPart.matches(Regex("^\\d+[DMWY]\\s+\\d+$"))
            if (looksLikeDurationPrice) {
                return parts.dropLast(1).joinToString(" - ")
            }
        }
    }
    
    // Pattern 2: "Name 525" -> strip trailing price at end
    val trailingPricePattern = Regex("\\s+\\d{3,}$")
    if (trailingPricePattern.containsMatchIn(name)) {
        return name.replace(trailingPricePattern, "").trim()
    }
    
    return name
}

/**
 * Format date for display (e.g., "2026-02-15" -> "February 15, 2026")
 */
private fun formatMembershipDate(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "January"
                "02" -> "February"
                "03" -> "March"
                "04" -> "April"
                "05" -> "May"
                "06" -> "June"
                "07" -> "July"
                "08" -> "August"
                "09" -> "September"
                "10" -> "October"
                "11" -> "November"
                "12" -> "December"
                else -> parts[1]
            }
            val day = parts[2].toIntOrNull() ?: parts[2]
            "$month $day, $year"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}
