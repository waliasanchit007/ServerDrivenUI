package com.example.serverdrivenui.data.repository

import com.example.serverdrivenui.core.data.dto.*
import com.example.serverdrivenui.shared.GymService
import com.example.serverdrivenui.shared.StorageService
import kotlinx.serialization.json.Json

/**
 * GymRepository - Guest-side repository for data access.
 * 
 * Uses the GymService bridge to get raw JSON from Host,
 * then parses it into type-safe DTOs using kotlinx.serialization.
 * 
 * Includes simple IN-MEMORY CACHING to prevent UI flicker/delays on tab switching.
 * Cache persists as long as the Zipline context is alive.
 * 
 * OFFLINE SUPPORT: Uses StorageService to persist data to disk.
 * Stale-While-Revalidate: Returns disk data immediately, then fetches fresh data.
 */
class GymRepository(
    private val service: GymService,
    private val storage: StorageService
) {
    
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
        
        // Step 1: Try Disk Cache if in-memory is empty
        if (cachedProfile == null) {
            try {
                val diskJson = storage.getString("profile")
                if (diskJson != null) {
                    val parsed = parseProfile(diskJson)
                    if (parsed != null) cachedProfile = parsed
                }
            } catch (e: Exception) {
                // Ignore disk read errors
            }
        }
        
        // Step 2: Fetch Live & Update Disk
        try {
            val response = service.getProfile()
            storage.setString("profile", response) // Persist
            
            val parsed = parseProfile(response)
            if (parsed != null) cachedProfile = parsed
            return parsed
        } catch (e: Exception) {
            println("GymRepository: Network failed for profile: ${e.message}")
            // Return cached/disk data if available
            return cachedProfile
        }
    }

    private fun parseProfile(jsonStr: String): ProfileDto? {
         return try {
             if (jsonStr.startsWith("[")) {
                 val profiles = json.decodeFromString<List<ProfileDto>>(jsonStr)
                 profiles.firstOrNull()
             } else if (jsonStr.startsWith("{") && !jsonStr.contains("error")) {
                 json.decodeFromString<ProfileDto>(jsonStr)
             } else {
                 null
             }
         } catch (e: Exception) {
             null
         }
    }
    
    // ============= Training =============
    
    suspend fun getWeeklySchedule(forceRefresh: Boolean = false): List<TrainingDayDto> {
        if (!forceRefresh && cachedWeeklySchedule != null) return cachedWeeklySchedule!!

        // Step 1: Disk
        if (cachedWeeklySchedule == null) {
            try {
                val diskJson = storage.getString("schedule")
                if (diskJson != null) {
                    cachedWeeklySchedule = json.decodeFromString<List<TrainingDayDto>>(diskJson)
                }
            } catch (e: Exception) { /* ignore */ }
        }

        // Step 2: Network
        return try {
            val response = service.getWeeklySchedule("") // Date param ignored by mock/demo
            storage.setString("schedule", response)
            val list = json.decodeFromString<List<TrainingDayDto>>(response)
            cachedWeeklySchedule = list
            list
        } catch (e: Exception) {
            println("GymRepository: Network failed for schedule: ${e.message}")
            cachedWeeklySchedule ?: emptyList()
        }
    }
    
    suspend fun getTodayTraining(forceRefresh: Boolean = false): TrainingDayDto? {
         if (!forceRefresh && cachedTodayTraining != null) return cachedTodayTraining

        // Step 1: Disk
        if (cachedTodayTraining == null) {
            try {
                val diskJson = storage.getString("today_training")
                if (diskJson != null) {
                    val parsed = parseToday(diskJson)
                    if (parsed != null) cachedTodayTraining = parsed
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val response = service.getTodaySchedule()
            storage.setString("today_training", response)
            
            val parsed = parseToday(response)
            if (parsed != null) cachedTodayTraining = parsed
            parsed
        } catch (e: Exception) {
            println("GymRepository: Network failed for today's training: ${e.message}")
            cachedTodayTraining
        }
    }

    private fun parseToday(jsonStr: String): TrainingDayDto? {
         return try {
             if (jsonStr.startsWith("{") && !jsonStr.contains("error")) {
                 json.decodeFromString<TrainingDayDto>(jsonStr)
             } else {
                 null
             }
         } catch (e: Exception) {
             null
         }
    }
    
    // ============= Coaches =============
    
    suspend fun getCoaches(forceRefresh: Boolean = false): List<CoachDto> {
        if (!forceRefresh && cachedCoaches != null) return cachedCoaches!!

        // Step 1: Disk
        if (cachedCoaches == null) {
            try {
                val diskJson = storage.getString("coaches")
                if (diskJson != null) {
                    cachedCoaches = json.decodeFromString<List<CoachDto>>(diskJson)
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val response = service.getCoaches()
             storage.setString("coaches", response)
            val list = json.decodeFromString<List<CoachDto>>(response)
            cachedCoaches = list
            list
        } catch (e: Exception) {
            println("GymRepository: Network failed for coaches: ${e.message}")
            cachedCoaches ?: emptyList()
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

        // Step 1: Disk
        if (cachedMembershipPlans == null) {
            try {
                val diskJson = storage.getString("plans")
                if (diskJson != null) {
                    cachedMembershipPlans = json.decodeFromString<List<MembershipPlanDto>>(diskJson)
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val response = service.getMembershipPlans()
            storage.setString("plans", response)
            val list = json.decodeFromString<List<MembershipPlanDto>>(response)
            cachedMembershipPlans = list
            list
        } catch (e: Exception) {
            println("GymRepository: Network failed for memberships: ${e.message}")
            cachedMembershipPlans ?: emptyList()
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

        // Step 1: Disk
        if (cachedMembershipHistory == null) {
            try {
                val diskJson = storage.getString("membership_history")
                if (diskJson != null) {
                    cachedMembershipHistory = json.decodeFromString<List<MembershipHistoryDto>>(diskJson)
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val response = service.getMembershipHistory()
            storage.setString("membership_history", response)
            val list = json.decodeFromString<List<MembershipHistoryDto>>(response)
            cachedMembershipHistory = list
            list
        } catch (e: Exception) {
            println("GymRepository: Network failed for membership history: ${e.message}")
            cachedMembershipHistory ?: emptyList()
        }
    }
    
    suspend fun getPaymentHistory(forceRefresh: Boolean = false): List<PaymentHistoryDto> {
        if (!forceRefresh && cachedPaymentHistory != null) return cachedPaymentHistory!!

        // Step 1: Disk
        if (cachedPaymentHistory == null) {
            try {
                val diskJson = storage.getString("payment_history")
                if (diskJson != null) {
                    cachedPaymentHistory = json.decodeFromString<List<PaymentHistoryDto>>(diskJson)
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val response = service.getPaymentHistory()
            storage.setString("payment_history", response)
            val list = json.decodeFromString<List<PaymentHistoryDto>>(response)
            cachedPaymentHistory = list
            list
        } catch (e: Exception) {
             println("GymRepository: Network failed for payments: ${e.message}")
            cachedPaymentHistory ?: emptyList()
        }
    }
    
    // ============= Attendance =============
    
    suspend fun getStreak(forceRefresh: Boolean = false): Int {
        if (!forceRefresh && cachedStreak != null) return cachedStreak!!
        
        // Step 1: Disk
        if (cachedStreak == null) {
            try {
                val diskStr = storage.getString("streak")
                if (diskStr != null) {
                    cachedStreak = diskStr.toIntOrNull()
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val streak = service.getStreak()
            storage.setString("streak", streak.toString())
            cachedStreak = streak
            streak
        } catch (e: Exception) {
            cachedStreak ?: 0
        }
    }
    
    suspend fun getWeeklyAttendanceStatus(forceRefresh: Boolean = false): List<String> {
        if (!forceRefresh && cachedWeeklyAttendance != null) return cachedWeeklyAttendance!!
        
        // Step 1: Disk
        if (cachedWeeklyAttendance == null) {
            try {
                val diskJson = storage.getString("weekly_attendance")
                if (diskJson != null) {
                    cachedWeeklyAttendance = json.decodeFromString<List<String>>(diskJson)
                }
            } catch (e: Exception) { /* ignore */ }
        }

        return try {
            val response = service.getWeeklyAttendanceStatus()
            storage.setString("weekly_attendance", response)
            val list = json.decodeFromString<List<String>>(response)
            cachedWeeklyAttendance = list
            list
        } catch (e: Exception) {
            println("GymRepository: Network failed for attendance status: ${e.message}")
            cachedWeeklyAttendance ?: listOf("attended", "attended", "attended", "attended", "today", "future", "future")
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
