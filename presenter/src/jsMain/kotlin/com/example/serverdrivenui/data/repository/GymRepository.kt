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
 * This lives in the Guest (presenter) module, NOT the Host.
 */
class GymRepository(private val service: GymService) {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
    }
    
    // ============= Profile =============
    
    /**
     * Get current user profile from Supabase via GymService bridge.
     * Returns null if parsing fails or response is empty.
     */
    suspend fun getProfile(): ProfileDto? {
        return try {
            val response = service.getProfile()
            if (response.startsWith("[")) {
                // Array response - take first item
                val profiles = json.decodeFromString<List<ProfileDto>>(response)
                profiles.firstOrNull()
            } else if (response.startsWith("{") && !response.contains("error")) {
                json.decodeFromString<ProfileDto>(response)
            } else {
                null
            }
        } catch (e: Exception) {
            println("GymRepository: Error parsing profile: ${e.message}")
            null
        }
    }
    
    // ============= Training =============
    
    /**
     * Get weekly training schedule.
     */
    suspend fun getWeeklySchedule(): List<TrainingDayDto> {
        return try {
            val response = service.getWeeklySchedule("")
            json.decodeFromString<List<TrainingDayDto>>(response)
        } catch (e: Exception) {
            println("GymRepository: Error parsing schedule: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get today's training.
     */
    suspend fun getTodayTraining(): TrainingDayDto? {
        return try {
            val response = service.getTodaySchedule()
            if (response.startsWith("{") && !response.contains("error")) {
                json.decodeFromString<TrainingDayDto>(response)
            } else {
                null
            }
        } catch (e: Exception) {
            println("GymRepository: Error parsing today's training: ${e.message}")
            null
        }
    }
    
    // ============= Coaches =============
    
    /**
     * Get all coaches.
     */
    suspend fun getCoaches(): List<CoachDto> {
        return try {
            val response = service.getCoaches()
            json.decodeFromString<List<CoachDto>>(response)
        } catch (e: Exception) {
            println("GymRepository: Error parsing coaches: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get single coach by ID.
     */
    suspend fun getCoach(coachId: String): CoachDto? {
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
    
    /**
     * Get all membership plans.
     */
    suspend fun getMembershipPlans(): List<MembershipPlanDto> {
        return try {
            val response = service.getMembershipPlans()
            json.decodeFromString<List<MembershipPlanDto>>(response)
        } catch (e: Exception) {
            println("GymRepository: Error parsing membership plans: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get current plan (first plan - typically the one user has).
     */
    suspend fun getCurrentPlan(): MembershipPlanDto? {
        return getMembershipPlans().firstOrNull()
    }
    
    /**
     * Get upgrade plans (all plans except first).
     */
    suspend fun getUpgradePlans(): List<MembershipPlanDto> {
        return getMembershipPlans().drop(1)
    }
    
    // ============= History =============
    
    /**
     * Get membership history.
     */
    suspend fun getMembershipHistory(): List<MembershipHistoryDto> {
        return try {
            val response = service.getMembershipHistory()
            json.decodeFromString<List<MembershipHistoryDto>>(response)
        } catch (e: Exception) {
            println("GymRepository: Error parsing membership history: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get payment history.
     */
    suspend fun getPaymentHistory(): List<PaymentHistoryDto> {
        return try {
            val response = service.getPaymentHistory()
            json.decodeFromString<List<PaymentHistoryDto>>(response)
        } catch (e: Exception) {
            println("GymRepository: Error parsing payment history: ${e.message}")
            emptyList()
        }
    }
    
    // ============= Attendance =============
    
    /**
     * Get streak count.
     */
    suspend fun getStreak(): Int {
        return try {
            service.getStreak()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get weekly attendance status.
     * Returns list like ["attended", "attended", "today", "future", ...]
     */
    suspend fun getWeeklyAttendanceStatus(): List<String> {
        return try {
            val response = service.getWeeklyAttendanceStatus()
            json.decodeFromString<List<String>>(response)
        } catch (e: Exception) {
            println("GymRepository: Error parsing attendance status: ${e.message}")
            listOf("attended", "attended", "attended", "attended", "today", "future", "future")
        }
    }
    
    /**
     * Mark attendance for a date.
     */
    suspend fun markAttendance(date: String): Boolean {
        return try {
            service.markAttendance(date)
        } catch (e: Exception) {
            false
        }
    }
}
