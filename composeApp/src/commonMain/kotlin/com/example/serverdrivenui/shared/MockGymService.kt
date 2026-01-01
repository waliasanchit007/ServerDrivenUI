package com.example.serverdrivenui.shared

/**
 * MockGymService - Returns sample data for development and testing.
 * Used when RealGymService (Ktor) isn't available.
 */
class MockGymService : GymService {
    
    override suspend fun getProfile(): String = """
        {
            "id": "mock-user-123",
            "full_name": "Rahul Sharma",
            "membership_status": "active",
            "membership_expiry": "2026-03-15",
            "avatar_url": null
        }
    """.trimIndent()
    
    override suspend fun getWeeklySchedule(weekStart: String): String = """
        [
            {"date": "2025-12-29", "day_name": "Monday", "focus": "Pull Strength", "description": "Focus on pulling movements. Row progressions and chin-up work.", "tags": ["Pull", "Back", "Biceps"], "is_rest_day": false},
            {"date": "2025-12-30", "day_name": "Tuesday", "focus": "Push Strength", "description": "Horizontal and vertical pushing. Dips and push-up variations.", "tags": ["Push", "Chest", "Triceps"], "is_rest_day": false},
            {"date": "2025-12-31", "day_name": "Wednesday", "focus": "Legs & Core", "description": "Lower body strength and core stability work.", "tags": ["Legs", "Core", "Squats"], "is_rest_day": false},
            {"date": "2026-01-01", "day_name": "Thursday", "focus": "Skills Training", "description": "Handstand practice and skill-specific drills.", "tags": ["Skills", "Balance"], "is_rest_day": false},
            {"date": "2026-01-02", "day_name": "Friday", "focus": "Full Body", "description": "Circuit training combining all movement patterns.", "tags": ["Full Body", "Endurance"], "is_rest_day": false},
            {"date": "2026-01-03", "day_name": "Saturday", "focus": "Open Gym", "description": "Practice your weak points. Coaches available.", "tags": ["Open", "Practice"], "is_rest_day": false},
            {"date": "2026-01-04", "day_name": "Sunday", "focus": "Rest & Recovery", "description": "Active recovery. Mobility work optional.", "tags": ["Rest", "Recovery"], "is_rest_day": true}
        ]
    """.trimIndent()
    
    override suspend fun getTodaySchedule(): String = """
        {
            "date": "2026-01-01",
            "day_name": "Thursday",
            "focus": "Skills Training",
            "description": "Handstand practice and skill-specific drills.",
            "tags": ["Skills", "Balance"],
            "is_rest_day": false
        }
    """.trimIndent()
    
    override suspend fun getAttendanceForWeek(weekStart: String): String = """
        [
            {"date": "2025-12-29", "status": "present"},
            {"date": "2025-12-30", "status": "present"},
            {"date": "2025-12-31", "status": "present"}
        ]
    """.trimIndent()
    
    override suspend fun markAttendance(date: String): Boolean {
        println("MockGymService: Marked attendance for $date")
        return true
    }
    
    override suspend fun getStreak(): Int = 4
    
    override suspend fun getCoaches(): String = """
        [
            {"id": "1", "name": "Hemant Singh", "role": "Head Coach", "bio": "Founder of Caliclan. Specializing in statics and front lever mechanics.", "instagram_handle": "hemant_caliclan", "photo_url": "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400", "sort_order": 1},
            {"id": "2", "name": "Arjun Verma", "role": "Strength Coach", "bio": "Expert in progressive overload and weighted calisthenics.", "instagram_handle": "arjun_strength", "photo_url": "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400", "sort_order": 2},
            {"id": "3", "name": "Priya Sharma", "role": "Mobility Coach", "bio": "Focus on flexibility, mobility flows, and injury prevention.", "instagram_handle": "priya_mobility", "photo_url": "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400", "sort_order": 3}
        ]
    """.trimIndent()
    
    override suspend fun getCoach(coachId: String): String = when (coachId) {
        "1" -> """{"id": "1", "name": "Hemant Singh", "role": "Head Coach", "bio": "Founder of Caliclan. Specializing in statics and front lever mechanics.", "instagram_handle": "hemant_caliclan", "photo_url": "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400"}"""
        "2" -> """{"id": "2", "name": "Arjun Verma", "role": "Strength Coach", "bio": "Expert in progressive overload and weighted calisthenics.", "instagram_handle": "arjun_strength", "photo_url": "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400"}"""
        "3" -> """{"id": "3", "name": "Priya Sharma", "role": "Mobility Coach", "bio": "Focus on flexibility, mobility flows, and injury prevention.", "instagram_handle": "priya_mobility", "photo_url": "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400"}"""
        else -> """{}"""
    }
    
    override suspend fun isLoggedIn(): Boolean = true
    
    override suspend fun requestOtp(phone: String): Boolean {
        println("MockGymService: OTP requested for $phone")
        return true
    }
    
    override suspend fun verifyOtp(phone: String, otp: String): Boolean {
        println("MockGymService: OTP $otp verified for $phone")
        return otp == "123456" // Demo OTP
    }
    
    override suspend fun logout() {
        println("MockGymService: User logged out")
    }
    
    override suspend fun openUrl(url: String) {
        println("MockGymService: Would open URL: $url")
    }
    
    override suspend fun showToast(message: String) {
        println("MockGymService: Toast: $message")
    }
    
    override fun close() {
        // No cleanup needed
    }
}
