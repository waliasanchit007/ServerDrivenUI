package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService

/**
 * GymService - The Bridge between Host and Guest for Caliclan app.
 * Guest logic NEVER calls Supabase directly; all data flows through here.
 */
interface GymService : ZiplineService {
    // ============= Profile & Membership =============
    
    /**
     * Get current user's profile.
     * Returns JSON: { "id": "...", "fullName": "...", "membershipStatus": "active|expired|grace", "membershipExpiry": "2026-03-01", "avatarUrl": "...", "email": "...", "phone": "...", "batch": "..." }
     */
    suspend fun getProfile(): String
    
    /**
     * Get all membership plans.
     * Returns JSON array of plans with id, name, duration, price, priceLabel, features, isRecommended.
     */
    suspend fun getMembershipPlans(): String
    
    /**
     * Get current user's membership history.
     * Returns JSON array of { planName, startDate, endDate, status }.
     */
    suspend fun getMembershipHistory(): String
    
    /**
     * Get current user's payment history.
     * Returns JSON array of { amount, paymentDate, method, status }.
     */
    suspend fun getPaymentHistory(): String
    
    // ============= Training =============
    
    /**
     * Get weekly training schedule.
     * @param weekStart ISO date string for Monday of the week (e.g., "2026-01-06")
     * Returns JSON array of schedule items with goals and supporting.
     */
    suspend fun getWeeklySchedule(weekStart: String): String
    
    /**
     * Get today's training focus.
     * Returns JSON: { "focus": "...", "description": "...", "goals": [...], "supporting": [...], "isRestDay": false }
     */
    suspend fun getTodaySchedule(): String
    
    // ============= Attendance & Consistency =============
    
    /**
     * Get attendance history for a week.
     * @param weekStart ISO date string for Monday
     * Returns JSON array of attended dates.
     */
    suspend fun getAttendanceForWeek(weekStart: String): String
    
    /**
     * Get weekly attendance status as array.
     * Returns JSON array: ["attended", "attended", "today", "future", ...]
     */
    suspend fun getWeeklyAttendanceStatus(): String
    
    /**
     * Mark attendance for a specific date.
     * @param date ISO date string (e.g., "2026-01-01")
     */
    suspend fun markAttendance(date: String): Boolean
    
    /**
     * Calculate current streak.
     * Returns streak count (rest days don't break streak).
     */
    suspend fun getStreak(): Int
    
    // ============= Community =============
    
    /**
     * Get list of coaches.
     * Returns JSON array of coach objects.
     */
    suspend fun getCoaches(): String
    
    /**
     * Get single coach details.
     */
    suspend fun getCoach(coachId: String): String
    
    // ============= Auth =============
    
    /**
     * Check if user is logged in.
     */
    suspend fun isLoggedIn(): Boolean
    
    /**
     * Request OTP for phone number.
     */
    /**
     * Request OTP for email.
     */
    suspend fun requestOtp(email: String): Boolean
    
    /**
     * Verify OTP and login.
     */
    suspend fun verifyOtp(email: String, otp: String): Boolean
    
    /**
     * Update user profile (e.g. set Name).
     */
    suspend fun updateProfile(name: String, email: String): Boolean
    
    /**
     * Logout user.
     */
    suspend fun logout()
    
    // ============= Admin Operations =============
    
    /**
     * Get all users.
     * Returns JSON list of ProfileDto.
     */
    suspend fun getAllUsers(): String
    
    /**
     * Create a training day.
     * @param trainingDayJson JSON string of TrainingDayDto.
     */
    suspend fun createTrainingDay(trainingDayJson: String): Boolean
    
    /**
     * Update a membership plan.
     * @param planJson JSON string of MembershipPlanDto.
     */
    suspend fun updateMembershipPlan(planJson: String): Boolean
    
    /**
     * Check in a user manually.
     */
    suspend fun checkInUser(userId: String): Boolean

    // ============= Native Actions =============
    
    /**
     * Open URL (WhatsApp, Instagram, etc.)
     */
    suspend fun openUrl(url: String)
    
    /**
     * Show native toast message.
     */
    suspend fun showToast(message: String)
}

