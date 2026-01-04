package ui

// Temporary Mock to unblock UI development
class MockGymService {
    suspend fun getAllUsers(): String = """
        [
            {"id": "1", "full_name": "Alice Smith", "email": "alice@example.com", "status": "active"},
            {"id": "2", "full_name": "Bob Jones", "email": "bob@example.com", "status": "inactive"},
            {"id": "3", "full_name": "Charlie Brown", "email": "charlie@example.com", "status": "active"}
        ]
    """.trimIndent()

    suspend fun getWeeklySchedule(date: String): String = """
        [
            {"date": "2026-01-01", "focus": "Push Day", "description": "Chest and Triceps"},
            {"date": "2026-01-02", "focus": "Pull Day", "description": "Back and Biceps"},
            {"date": "2026-01-03", "focus": "Leg Day", "description": "Quads and Hamstrings"},
            {"date": "2026-01-04", "focus": "Rest", "description": "Active Recovery"}
        ]
    """.trimIndent()

    suspend fun getMembershipPlans(): String = """
        [
            {"id": "p1", "name": "Basic", "price": 29.99, "duration_months": 1},
            {"id": "p2", "name": "Pro", "price": 49.99, "duration_months": 3},
            {"id": "p3", "name": "Elite", "price": 99.99, "duration_months": 12}
        ]
    """.trimIndent()

    suspend fun createTrainingDay(json: String): Boolean {
        println("Mock: Created training day: $json")
        return true
    }

    suspend fun updateMembershipPlan(json: String): Boolean {
        println("Mock: Updated plan: $json")
        return true
    }

    suspend fun checkInUser(userId: String): Boolean {
        println("Mock: Checked in user $userId")
        return true
    }
}
