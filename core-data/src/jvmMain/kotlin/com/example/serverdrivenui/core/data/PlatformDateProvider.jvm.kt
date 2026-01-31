package com.example.serverdrivenui.core.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.DayOfWeek

actual object PlatformDateProvider {
    actual fun today(): String {
        return LocalDate.now().toString()
    }
    
    actual fun now(): String {
        return LocalDateTime.now().toString()
    }
    
    actual fun addMonths(date: String, months: Int): String {
        return LocalDate.parse(date).plusMonths(months.toLong()).toString()
    }
    
    actual fun getDayOfWeek(dateStr: String): Int {
        // Returns 0=Sunday, 1=Monday, ..., 6=Saturday to match JS convention
        val dayOfWeek = LocalDate.parse(dateStr).dayOfWeek
        return when (dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
    }
}
