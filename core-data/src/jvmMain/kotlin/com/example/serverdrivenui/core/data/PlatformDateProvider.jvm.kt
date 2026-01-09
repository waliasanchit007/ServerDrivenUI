package com.example.serverdrivenui.core.data

import java.time.LocalDate
import java.time.LocalDateTime

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
}
