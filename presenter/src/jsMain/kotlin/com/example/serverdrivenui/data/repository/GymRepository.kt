package com.example.serverdrivenui.data.repository

import com.example.serverdrivenui.data.dto.*
import com.example.serverdrivenui.shared.GymService
import kotlinx.serialization.json.Json

/**
 * GymRepository - Guest-side repository for data access.
 * 
 * Uses the GymService bridge to get raw JSON from Host,
 * then parses it into type-safe DTOs using kotlinx.serialization.
 * 
 * Includes simple IN-MEMORY CACHING to prevent UI flicker/delays on tab switching.
 * Cache persists as long as the Zipline context is alive.
 */
class GymRepository(private val service: GymService) {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
    }
    
    // ============= Cache (Public Read-Only for Synchronous UI Init) =============
    var cachedProfile: ProfileDto? = null
        private set
    var cachedWeeklySchedule: List<TrainingDayDto>? = null
        private set
    var cachedTodayTraining: TrainingDayDto? = null
        private set
    var cachedCoaches: List<CoachDto>? = null
        private set
    var cachedMembershipPlans: List<MembershipPlanDto>? = null
        private set
    var cachedMembershipHistory: List<MembershipHistoryDto>? = null
        private set
    var cachedPaymentHistory: List<PaymentHistoryDto>? = null
        private set
    var cachedStreak: Int? = null
        private set
    var cachedWeeklyAttendance: List<String>? = null
        private set

    // ============= Profile =============
    
    suspend fun getProfile(forceRefresh: Boolean = false): ProfileDto? {
        if (!forceRefresh && cachedProfile != null) return cachedProfile
        
        return try {
            val response = service.getProfile()
            val parsed: ProfileDto? = if (response.startsWith("[")) {
                // Array response - take first item
                val profiles = json.decodeFromString<List<ProfileDto>>(response)
                profiles.firstOrNull()
            } else if (response.startsWith("{") && !response.contains("error")) {
                json.decodeFromString<ProfileDto>(response)
            } else {
                null
            }
            if (parsed != null) cachedProfile = parsed
            parsed
        } catch (e: Exception) {
            println("GymRepository: Error parsing profile: ${e.message}")
            null
        }
    }
    
    // ============= Training =============
    
    suspend fun getWeeklySchedule(forceRefresh: Boolean = false): List<TrainingDayDto> {
        if (!forceRefresh && cachedWeeklySchedule != null) return cachedWeeklySchedule!!

        return try {
            val response = service.getWeeklySchedule("") // Date param ignored by mock/demo
            val list = json.decodeFromString<List<TrainingDayDto>>(response)
            cachedWeeklySchedule = list
            list
        } catch (e: Exception) {
            println("GymRepository: Error parsing schedule: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getTodayTraining(forceRefresh: Boolean = false): TrainingDayDto? {
         if (!forceRefresh && cachedTodayTraining != null) return cachedTodayTraining

        return try {
            val response = service.getTodaySchedule()
            val parsed: TrainingDayDto? = if (response.startsWith("{") && !response.contains("error")) {
                json.decodeFromString<TrainingDayDto>(response)
            } else {
                null
            }
            if (parsed != null) cachedTodayTraining = parsed
            parsed
        } catch (e: Exception) {
            println("GymRepository: Error parsing today's training: ${e.message}")
            null
        }
    }
    
    // ============= Coaches =============
    
    suspend fun getCoaches(forceRefresh: Boolean = false): List<CoachDto> {
        if (!forceRefresh && cachedCoaches != null) return cachedCoaches!!

        return try {
            val response = service.getCoaches()
            val list = json.decodeFromString<List<CoachDto>>(response)
            cachedCoaches = list
            list
        } catch (e: Exception) {
            println("GymRepository: Error parsing coaches: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getCoach(coachId: String, forceRefresh: Boolean = false): CoachDto? {
        // Try to find in list cache first
        if (!forceRefresh && cachedCoaches != null) {
            val found = cachedCoaches?.find { it.id == coachId }
            if (found != null) return found
        }

        return try {
            val response = service.getCoach(coachId)
            if (response.startsWith("[")) {
                val coaches = json.decodeFromString<List<CoachDto>>(response)
                coaches.firstOrNull()
            } else if (response.startsWith("{")) {
                json.decodeFromString<CoachDto>(response)
            } else {
                null
            }
        } catch (e: Exception) {
            println("GymRepository: Error parsing coach: ${e.message}")
            null
        }
    }
    
    // ============= Membership =============
    
    suspend fun getMembershipPlans(forceRefresh: Boolean = false): List<MembershipPlanDto> {
        if (!forceRefresh && cachedMembershipPlans != null) return cachedMembershipPlans!!

        return try {
            val response = service.getMembershipPlans()
            val list = json.decodeFromString<List<MembershipPlanDto>>(response)
            cachedMembershipPlans = list
            list
        } catch (e: Exception) {
            println("GymRepository: Error parsing membership plans: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getCurrentPlan(): MembershipPlanDto? {
        // For convenience, just calls getMembershipPlans which uses cache
        return getMembershipPlans().firstOrNull()
    }
    
    suspend fun getUpgradePlans(): List<MembershipPlanDto> {
        return getMembershipPlans().drop(1)
    }
    
    // ============= History =============
    
    suspend fun getMembershipHistory(forceRefresh: Boolean = false): List<MembershipHistoryDto> {
        if (!forceRefresh && cachedMembershipHistory != null) return cachedMembershipHistory!!

        return try {
            val response = service.getMembershipHistory()
            val list = json.decodeFromString<List<MembershipHistoryDto>>(response)
            cachedMembershipHistory = list
            list
        } catch (e: Exception) {
            println("GymRepository: Error parsing membership history: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getPaymentHistory(forceRefresh: Boolean = false): List<PaymentHistoryDto> {
        if (!forceRefresh && cachedPaymentHistory != null) return cachedPaymentHistory!!

        return try {
            val response = service.getPaymentHistory()
            val list = json.decodeFromString<List<PaymentHistoryDto>>(response)
            cachedPaymentHistory = list
            list
        } catch (e: Exception) {
            println("GymRepository: Error parsing payment history: ${e.message}")
            emptyList()
        }
    }
    
    // ============= Attendance =============
    
    suspend fun getStreak(forceRefresh: Boolean = false): Int {
        if (!forceRefresh && cachedStreak != null) return cachedStreak!!
        
        return try {
            val streak = service.getStreak()
            cachedStreak = streak
            streak
        } catch (e: Exception) {
            0
        }
    }
    
    suspend fun getWeeklyAttendanceStatus(forceRefresh: Boolean = false): List<String> {
        if (!forceRefresh && cachedWeeklyAttendance != null) return cachedWeeklyAttendance!!
        
        return try {
            val response = service.getWeeklyAttendanceStatus()
            val list = json.decodeFromString<List<String>>(response)
            cachedWeeklyAttendance = list
            list
        } catch (e: Exception) {
            println("GymRepository: Error parsing attendance status: ${e.message}")
            listOf("attended", "attended", "attended", "attended", "today", "future", "future")
        }
    }
    
    suspend fun markAttendance(date: String): Boolean {
        // Invalidate attendance caches when marking attendance
        cachedStreak = null
        cachedWeeklyAttendance = null
        
        return try {
            service.markAttendance(date)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear all caches (e.g. on logout)
     */
    fun clearCache() {
        cachedProfile = null
        cachedWeeklySchedule = null
        cachedTodayTraining = null
        cachedCoaches = null
        cachedMembershipPlans = null
        cachedMembershipHistory = null
        cachedPaymentHistory = null
        cachedStreak = null
        cachedWeeklyAttendance = null
    }
}
