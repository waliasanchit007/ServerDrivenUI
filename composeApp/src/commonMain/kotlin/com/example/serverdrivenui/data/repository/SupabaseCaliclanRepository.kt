package com.example.serverdrivenui.data.repository

import com.example.serverdrivenui.data.dto.*
import com.example.serverdrivenui.shared.SupabaseConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * SupabaseCaliclanRepository - Production implementation using Supabase REST API.
 * 
 * Architecture Notes:
 * - Uses Ktor HttpClient for cross-platform HTTP
 * - Uses kotlinx.serialization for JSON parsing
 * - Demo user ID hardcoded for testing (replace with auth in production)
 * - RLS policies handle row-level security on Supabase side
 */
class SupabaseCaliclanRepository(
    private val httpClient: HttpClient,
    private val supabaseUrl: String = SupabaseConfig.PROJECT_URL,
    private val supabaseKey: String = SupabaseConfig.ANON_KEY
) : CaliclanRepository {
    
    private val restUrl = "$supabaseUrl/rest/v1"
    
    // Demo user ID for testing (in production, get from auth)
    private val demoUserId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    
    // JSON parser with lenient settings
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    // ============= Common Request Builder =============
    
    private fun HttpRequestBuilder.supabaseHeaders() {
        headers {
            append("apikey", supabaseKey)
            append("Authorization", "Bearer $supabaseKey")
            append("Content-Type", "application/json")
        }
    }
    
    // ============= Home Screen =============
    
    override suspend fun getProfile(): ProfileDto? {
        return try {
            val response = httpClient.get("$restUrl/profiles") {
                supabaseHeaders()
                parameter("id", "eq.$demoUserId")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            val profiles = json.decodeFromString<List<ProfileDto>>(text)
            profiles.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching profile: ${e.message}")
            null
        }
    }
    
    override suspend fun getCoaches(): List<CoachDto> {
        return try {
            val response = httpClient.get("$restUrl/coaches") {
                supabaseHeaders()
                parameter("order", "sort_order.asc")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            json.decodeFromString<List<CoachDto>>(text)
        } catch (e: Exception) {
            println("Error fetching coaches: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getTodayTraining(): TrainingDayDto? {
        return try {
            val today = "2026-01-02" // In production, use actual date
            val response = httpClient.get("$restUrl/training_schedule") {
                supabaseHeaders()
                parameter("date", "eq.$today")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            val schedules = json.decodeFromString<List<TrainingDayDto>>(text)
            schedules.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching today's training: ${e.message}")
            null
        }
    }
    
    override suspend fun getStreak(): Int {
        return try {
            val response = httpClient.get("$restUrl/attendance") {
                supabaseHeaders()
                parameter("user_id", "eq.$demoUserId")
                parameter("order", "date.desc")
                parameter("limit", "30")
            }
            val text = response.bodyAsText()
            val attendance = json.decodeFromString<List<AttendanceDto>>(text)
            // Count consecutive days (simplified for demo)
            attendance.size.coerceAtMost(7)
        } catch (e: Exception) {
            println("Error fetching streak: ${e.message}")
            0
        }
    }
    
    override suspend fun getWeeklyAttendanceStatus(): List<String> {
        return try {
            val response = httpClient.get("$restUrl/attendance") {
                supabaseHeaders()
                parameter("user_id", "eq.$demoUserId")
                parameter("order", "date.asc")
                parameter("limit", "7")
            }
            val text = response.bodyAsText()
            val attendance = json.decodeFromString<List<AttendanceDto>>(text)
            val attendedDates = attendance.map { it.date }.toSet()
            
            // Generate 7-day status (simplified)
            val days = mutableListOf<String>()
            val baseDate = "2025-12-29" // Week start
            for (i in 0..6) {
                val dayDate = when (i) {
                    0 -> "2025-12-29"
                    1 -> "2025-12-30"
                    2 -> "2025-12-31"
                    3 -> "2026-01-01"
                    4 -> "2026-01-02"
                    5 -> "2026-01-03"
                    6 -> "2026-01-04"
                    else -> ""
                }
                days.add(when {
                    dayDate == "2026-01-02" -> "today"
                    attendedDates.contains(dayDate) -> "attended"
                    else -> "future"
                })
            }
            days
        } catch (e: Exception) {
            println("Error fetching weekly attendance: ${e.message}")
            listOf("attended", "attended", "attended", "attended", "today", "future", "future")
        }
    }
    
    // ============= Training Screen =============
    
    override suspend fun getWeeklySchedule(): List<TrainingDayDto> {
        return try {
            val response = httpClient.get("$restUrl/training_schedule") {
                supabaseHeaders()
                parameter("order", "date.asc")
                parameter("limit", "7")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            json.decodeFromString<List<TrainingDayDto>>(text)
        } catch (e: Exception) {
            println("Error fetching weekly schedule: ${e.message}")
            emptyList()
        }
    }
    
    // ============= Membership Screen =============
    
    override suspend fun getCurrentPlan(): MembershipPlanDto? {
        return try {
            val response = httpClient.get("$restUrl/membership_plans") {
                supabaseHeaders()
                parameter("order", "sort_order.asc")
                parameter("limit", "1")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            val plans = json.decodeFromString<List<MembershipPlanDto>>(text)
            plans.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching current plan: ${e.message}")
            null
        }
    }
    
    override suspend fun getUpgradePlans(): List<MembershipPlanDto> {
        return try {
            val response = httpClient.get("$restUrl/membership_plans") {
                supabaseHeaders()
                parameter("order", "sort_order.asc")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            val plans = json.decodeFromString<List<MembershipPlanDto>>(text)
            // Skip first (current), return rest as upgrade options
            plans.drop(1)
        } catch (e: Exception) {
            println("Error fetching upgrade plans: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getNextBillingDate(): String? {
        val profile = getProfile()
        return profile?.membershipExpiry
    }
    
    // ============= Profile Screen =============
    
    override suspend fun getMembershipHistory(): List<MembershipHistoryDto> {
        return try {
            val response = httpClient.get("$restUrl/membership_history") {
                supabaseHeaders()
                parameter("user_id", "eq.$demoUserId")
                parameter("order", "start_date.desc")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            json.decodeFromString<List<MembershipHistoryDto>>(text)
        } catch (e: Exception) {
            println("Error fetching membership history: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getPaymentHistory(): List<PaymentHistoryDto> {
        return try {
            val response = httpClient.get("$restUrl/payment_history") {
                supabaseHeaders()
                parameter("user_id", "eq.$demoUserId")
                parameter("order", "payment_date.desc")
                parameter("select", "*")
            }
            val text = response.bodyAsText()
            json.decodeFromString<List<PaymentHistoryDto>>(text)
        } catch (e: Exception) {
            println("Error fetching payment history: ${e.message}")
            emptyList()
        }
    }
}
