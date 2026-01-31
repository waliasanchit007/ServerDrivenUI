package com.example.serverdrivenui.core.data

expect object PlatformDateProvider {
    fun today(): String
    fun now(): String
    fun addMonths(date: String, months: Int): String
    fun getDayOfWeek(dateStr: String): Int  // 0=Sunday, 1=Monday, ..., 6=Saturday
}
