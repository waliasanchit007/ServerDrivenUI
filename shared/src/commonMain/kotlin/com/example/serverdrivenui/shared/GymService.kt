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
     * Returns JSON: { "id": "...", "fullName": "...", "membershipStatus": "active|expired|grace", "membershipExpiry": "2026-03-01", "avatarUrl": "..." }
     */
    suspend fun getProfile(): String
    
    // ============= Training =============
    
    /**
     * Get weekly training schedule.
     * @param weekStart ISO date string for Monday of the week (e.g., "2026-01-06")
     * Returns JSON array of schedule items.
     */
    suspend fun getWeeklySchedule(weekStart: String): String
    
    /**
     * Get today's training focus.
     * Returns JSON: { "focus": "...", "description": "...", "tags": [...], "isRestDay": false }
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
    suspend fun requestOtp(phone: String): Boolean
    
    /**
     * Verify OTP and login.
     */
    suspend fun verifyOtp(phone: String, otp: String): Boolean
    
    /**
     * Logout user.
     */
    suspend fun logout()
    
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
