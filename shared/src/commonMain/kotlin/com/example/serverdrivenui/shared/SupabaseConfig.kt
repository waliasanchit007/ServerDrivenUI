package com.example.serverdrivenui.core.data

/**
 * Supabase configuration for Caliclan app.
 */
object SupabaseConfig {
    const val PROJECT_URL = "https://tumdkgcpikspixovmzrw.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR1bWRrZ2NwaWtzcGl4b3ZtenJ3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjcyNDkxNTEsImV4cCI6MjA4MjgyNTE1MX0.Qewg6JqydPboYKSkQ1wxuoHeD2fWMBez9XiO1CTcyA4"
    
    // API endpoints
    const val REST_URL = "$PROJECT_URL/rest/v1"
    const val AUTH_URL = "$PROJECT_URL/auth/v1"
}
