package com.example.serverdrivenui.data.repository

import com.example.serverdrivenui.data.dto.*

/**
 * CaliclanRepository - Abstraction layer for Caliclan gym data access.
 * 
 * This interface defines the contract for data operations across all screens.
 * Implementation can be swapped for testing or different backends.
 */
interface CaliclanRepository {
    
    // ============= Home Screen =============
    
    /**
     * Get current user profile
     */
    suspend fun getProfile(): ProfileDto?
    
    /**
     * Get all coaches ordered by sort_order
     */
    suspend fun getCoaches(): List<CoachDto>
    
    /**
     * Get today's training schedule
     */
    suspend fun getTodayTraining(): TrainingDayDto?
    
    /**
     * Get current attendance streak (consecutive days)
     */
    suspend fun getStreak(): Int
    
    /**
     * Get weekly attendance status for visual display
     * Returns list of 7 items: ["attended", "attended", "today", "future", "future", "future", "future"]
     */
    suspend fun getWeeklyAttendanceStatus(): List<String>
    
    // ============= Training Screen =============
    
    /**
     * Get full weekly training schedule (7 days)
     */
    suspend fun getWeeklySchedule(): List<TrainingDayDto>
    
    // ============= Membership Screen =============
    
    /**
     * Get the user's current active membership plan
     */
    suspend fun getCurrentPlan(): MembershipPlanDto?
    
    /**
     * Get available upgrade/renewal plans
     */
    suspend fun getUpgradePlans(): List<MembershipPlanDto>
    
    /**
     * Get next billing date for current plan
     */
    suspend fun getNextBillingDate(): String?
    
    // ============= Profile Screen =============
    
    /**
     * Get user's membership history
     */
    suspend fun getMembershipHistory(): List<MembershipHistoryDto>
    
    /**
     * Get user's payment history
     */
    suspend fun getPaymentHistory(): List<PaymentHistoryDto>
}
