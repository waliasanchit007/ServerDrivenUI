package com.example.serverdrivenui.shared

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * RealGymService - Shared implementation connecting to Supabase via Ktor.
 * This same code runs on both Android (OkHttp engine) and iOS (Darwin engine).
 */
class RealGymService(
    private val httpClient: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String
) : GymService {
    
    private val restUrl = "$supabaseUrl/rest/v1"
    private val authUrl = "$supabaseUrl/auth/v1"
    
    private var currentUserId: String? = null
    private var currentAccessToken: String? = null
    
    // ============= Profile & Membership =============
    
    override suspend fun getProfile(): String {
        // Use demo user if not logged in
        val userId = currentUserId ?: demoUserId
        println("RealGymService: getProfile for userId=$userId")
        
        return try {
            val response = httpClient.get("$restUrl/profiles") {
                parameter("id", "eq.$userId")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val result = response.bodyAsText()
            println("RealGymService: getProfile result=$result")
            result
        } catch (e: Exception) {
            println("RealGymService: getProfile error=${e.message}")
            """{"error": "${e.message}"}"""
        }
    }
    
    // Demo user ID for anonymous access (must be valid UUID for Supabase)
    private val demoUserId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    
    // ============= Training =============
    
    override suspend fun getWeeklySchedule(weekStart: String): String {
        println("RealGymService: getWeeklySchedule called")
        return try {
            // Fetch all training schedule (no date filter for demo)
            val response = httpClient.get("$restUrl/training_schedule") {
                parameter("order", "date.asc")
                parameter("limit", "7")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val result = response.bodyAsText()
            println("RealGymService: getWeeklySchedule result=$result")
            result
        } catch (e: Exception) {
            println("RealGymService: getWeeklySchedule error=${e.message}")
            """[]"""
        }
    }
    
    override suspend fun getTodaySchedule(): String {
        val today = "2026-01-01" // TODO: Use actual date
        return try {
            val response = httpClient.get("$restUrl/training_schedule") {
                parameter("date", "eq.$today")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val result = response.bodyAsText()
            // Return first item or default
            if (result.startsWith("[") && result.length > 2) {
                val items = result.trimStart('[').trimEnd(']')
                if (items.contains("},")) {
                    items.split("},")[0] + "}"
                } else {
                    items
                }
            } else {
                """{"focus": "Rest Day", "description": "No training scheduled", "isRestDay": true}"""
            }
        } catch (e: Exception) {
            """{"focus": "Rest Day", "description": "No training scheduled", "isRestDay": true}"""
        }
    }
    
    // ============= Attendance & Consistency =============
    
    override suspend fun getAttendanceForWeek(weekStart: String): String {
        val userId = currentUserId ?: return """[]"""
        
        return try {
            val response = httpClient.get("$restUrl/attendance") {
                parameter("user_id", "eq.$userId")
                parameter("date", "gte.$weekStart")
                parameter("select", "date,status")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            response.bodyAsText()
        } catch (e: Exception) {
            """[]"""
        }
    }
    
    override suspend fun markAttendance(date: String): Boolean {
        val userId = currentUserId ?: return false
        
        return try {
            val response = httpClient.post("$restUrl/attendance") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                    append("Prefer", "return=minimal")
                }
                setBody("""{"user_id": "$userId", "date": "$date", "status": "present"}""")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("Error marking attendance: ${e.message}")
            false
        }
    }
    
    override suspend fun getStreak(): Int {
        val userId = currentUserId ?: return 0
        
        return try {
            val response = httpClient.get("$restUrl/attendance") {
                parameter("user_id", "eq.$userId")
                parameter("order", "date.desc")
                parameter("limit", "30")
                parameter("select", "date")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            
            val attendanceJson = response.bodyAsText()
            val entriesCount = attendanceJson.split("\"date\"").size - 1
            minOf(entriesCount, 7) // Cap at 7 for demo
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun getWeeklyAttendanceStatus(): String {
        val userId = currentUserId ?: demoUserId
        
        return try {
            val response = httpClient.get("$restUrl/attendance") {
                parameter("user_id", "eq.$userId")
                parameter("order", "date.asc")
                parameter("limit", "7")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val text = response.bodyAsText()
            // Parse attended dates
            val attendedDates = mutableSetOf<String>()
            val regex = """"date":"(\d{4}-\d{2}-\d{2})"""".toRegex()
            regex.findAll(text).forEach { match ->
                attendedDates.add(match.groupValues[1])
            }
            
            // Build status array for the week
            val today = "2026-01-02"
            val days = listOf(
                "2025-12-29" to "Mon",
                "2025-12-30" to "Tue",
                "2025-12-31" to "Wed",
                "2026-01-01" to "Thu",
                "2026-01-02" to "Fri",
                "2026-01-03" to "Sat",
                "2026-01-04" to "Sun"
            )
            
            val statuses = days.map { (date, _) ->
                when {
                    date == today -> "today"
                    attendedDates.contains(date) -> "attended"
                    date > today -> "future"
                    else -> "missed"
                }
            }
            """["${statuses.joinToString("\",\"")}"]"""
        } catch (e: Exception) {
            """["attended","attended","attended","attended","today","future","future"]"""
        }
    }
    
    // ============= Membership =============
    
    override suspend fun getMembershipPlans(): String {
        println("RealGymService: getMembershipPlans called")
        return try {
            val response = httpClient.get("$restUrl/membership_plans") {
                parameter("order", "sort_order.asc")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val result = response.bodyAsText()
            println("RealGymService: getMembershipPlans result=$result")
            result
        } catch (e: Exception) {
            println("RealGymService: getMembershipPlans error=${e.message}")
            """[]"""
        }
    }
    
    override suspend fun getMembershipHistory(): String {
        val userId = currentUserId ?: demoUserId
        println("RealGymService: getMembershipHistory for userId=$userId")
        
        return try {
            val response = httpClient.get("$restUrl/membership_history") {
                parameter("user_id", "eq.$userId")
                parameter("order", "start_date.desc")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val result = response.bodyAsText()
            println("RealGymService: getMembershipHistory result=$result")
            result
        } catch (e: Exception) {
            println("RealGymService: getMembershipHistory error=${e.message}")
            """[]"""
        }
    }
    
    override suspend fun getPaymentHistory(): String {
        val userId = currentUserId ?: demoUserId
        println("RealGymService: getPaymentHistory for userId=$userId")
        
        return try {
            val response = httpClient.get("$restUrl/payment_history") {
                parameter("user_id", "eq.$userId")
                parameter("order", "payment_date.desc")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            val result = response.bodyAsText()
            println("RealGymService: getPaymentHistory result=$result")
            result
        } catch (e: Exception) {
            println("RealGymService: getPaymentHistory error=${e.message}")
            """[]"""
        }
    }
    
    // ============= Community =============
    
    override suspend fun getCoaches(): String {
        return try {
            val response = httpClient.get("$restUrl/coaches") {
                parameter("order", "sort_order.asc")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            response.bodyAsText()
        } catch (e: Exception) {
            """[]"""
        }
    }
    
    override suspend fun getCoach(coachId: String): String {
        return try {
            val response = httpClient.get("$restUrl/coaches") {
                parameter("id", "eq.$coachId")
                parameter("select", "*")
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${currentAccessToken ?: supabaseKey}")
                }
            }
            response.bodyAsText()
        } catch (e: Exception) {
            """{}"""
        }
    }
    
    // ============= Auth =============
    
    override suspend fun isLoggedIn(): Boolean {
        return currentUserId != null
    }
    
    override suspend fun requestOtp(phone: String): Boolean {
        return try {
            val response = httpClient.post("$authUrl/otp") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                }
                setBody("""{"phone": "$phone"}""")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("OTP request failed: ${e.message}")
            false
        }
    }
    
    override suspend fun verifyOtp(phone: String, otp: String): Boolean {
        return try {
            val response = httpClient.post("$authUrl/verify") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                }
                setBody("""{"phone": "$phone", "token": "$otp", "type": "sms"}""")
            }
            
            if (response.status.isSuccess()) {
                val result = response.bodyAsText()
                // Parse access_token and user.id from response
                // For now, just mark as logged in
                currentUserId = "demo-user"
                currentAccessToken = supabaseKey
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("OTP verify failed: ${e.message}")
            false
        }
    }
    
    override suspend fun logout() {
        currentUserId = null
        currentAccessToken = null
    }
    
    // ============= Native Actions =============
    
    private var urlOpener: ((String) -> Unit)? = null
    private var toastShower: ((String) -> Unit)? = null
    
    fun setUrlOpener(opener: (String) -> Unit) {
        urlOpener = opener
    }
    
    fun setToastShower(shower: (String) -> Unit) {
        toastShower = shower
    }
    
    override suspend fun openUrl(url: String) {
        urlOpener?.invoke(url) ?: println("Would open URL: $url")
    }
    
    override suspend fun showToast(message: String) {
        toastShower?.invoke(message) ?: println("Toast: $message")
    }
    
    override fun close() {
        // HTTP client lifecycle managed externally
    }
}
