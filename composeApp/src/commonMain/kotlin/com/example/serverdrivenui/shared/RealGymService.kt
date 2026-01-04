package com.example.serverdrivenui.shared

import com.example.serverdrivenui.core.data.SupabaseGymRepository
import com.example.serverdrivenui.core.data.dto.*
import io.ktor.client.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * RealGymService - Adapter that implements GymService (Zipline)
 * but delegates logic to Core Data (SupabaseGymRepository).
 */
class RealGymService(
    private val repository: SupabaseGymRepository
) : GymService {

    constructor(httpClient: HttpClient, supabaseUrl: String, supabaseKey: String) : this(
        SupabaseGymRepository(httpClient, supabaseUrl, supabaseKey)
    )
    
    // Auth & Utilities
    override suspend fun showToast(message: String) {
        toastShower?.invoke(message)
    }
    
    private var urlOpener: ((String) -> Unit)? = null
    private var toastShower: ((String) -> Unit)? = null
    
    fun setUrlOpener(opener: (String) -> Unit) {
        urlOpener = opener
    }
    
    fun setToastShower(shower: (String) -> Unit) {
        toastShower = shower
    }
    
    override suspend fun openUrl(url: String) {
        urlOpener?.invoke(url)
    }

    // ============= User Methods =============
    override suspend fun getProfile(): String = 
        repository.getProfile()?.let { Json.encodeToString(it) } ?: "null"

    override suspend fun getMembershipPlans(): String = 
        Json.encodeToString(repository.getMembershipPlans())

    override suspend fun getMembershipHistory(): String = 
        Json.encodeToString(repository.getMembershipHistory())

    override suspend fun getPaymentHistory(): String = 
        Json.encodeToString(repository.getPaymentHistory())

    override suspend fun getWeeklySchedule(weekStart: String): String = 
        Json.encodeToString(repository.getWeeklySchedule(weekStart))

    override suspend fun getTodaySchedule(): String = 
        repository.getTodaySchedule()?.let { Json.encodeToString(it) } ?: "null"

    override suspend fun getAttendanceForWeek(weekStart: String): String = 
        Json.encodeToString(repository.getAttendanceForWeek(weekStart))

    override suspend fun getWeeklyAttendanceStatus(): String = 
        Json.encodeToString(repository.getWeeklyAttendanceStatus())

    override suspend fun markAttendance(date: String): Boolean = 
        repository.markAttendance(date)

    override suspend fun getStreak(): Int = 
        repository.getStreak()

    override suspend fun getCoaches(): String = 
        Json.encodeToString(repository.getCoaches())

    override suspend fun getCoach(coachId: String): String = 
        repository.getCoach(coachId)?.let { Json.encodeToString(it) } ?: "{}"

    override suspend fun isLoggedIn(): Boolean = 
        repository.isLoggedIn()

    override suspend fun auth_logout() = 
        repository.logout()

    override suspend fun requestOtp(email: String): Boolean = 
        repository.requestOtp(email)

    override suspend fun verifyOtp(email: String, otp: String): Boolean = 
        repository.verifyOtp(email, otp)
        
    override suspend fun updateProfile(name: String, email: String): Boolean {
        // Todo: Actually update generic profile. Using updateUser for now.
        // We need to fetch current profile ID first? 
        // Or assume repository handles it contextually.
        // For MVP: We assume we are updating the current user.
        return repository.updateUser(ProfileDto(full_name = name, email = email))
    }

    override suspend fun logout() = 
        repository.logout()
        
    // ============= Admin Methods =============
    override suspend fun getAllUsers(): String = 
        Json.encodeToString(repository.getAllUsers())
        
    override suspend fun createTrainingDay(trainingDayJson: String): Boolean {
        // Deserialize JSON to DTO then call repo
        val dto = Json.decodeFromString<TrainingDayDto>(trainingDayJson)
        return repository.createTrainingDay(dto)
    }
        
    override suspend fun updateMembershipPlan(planJson: String): Boolean {
        val dto = Json.decodeFromString<MembershipPlanDto>(planJson)
        return repository.updateMembershipPlan(dto)
    }
        
    override suspend fun checkInUser(userId: String): Boolean =
         repository.checkInUser(userId)

    override fun close() {
        // HTTP client lifecycle managed externally
    }
}
