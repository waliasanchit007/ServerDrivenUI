package com.example.serverdrivenui.core.data

import kotlin.js.Date

actual object PlatformDateProvider {
    actual fun today(): String {
        val d = Date()
        val year = d.getFullYear()
        val month = (d.getMonth() + 1).toString().padStart(2, '0')
        val day = d.getDate().toString().padStart(2, '0')
        return "$year-$month-$day"
    }
    
    actual fun now(): String {
        return Date().toString() // Use local representation or keep ISO if specifically needed for APIs? 
        // Keeping ISO for 'now' is usually safer for storing/API timestamps, 
        // but 'today' MUST be local for UI logic.
        // Let's stick to ISO for 'now' as it's typically for timestamps.
        // But user asked for "Real time". Ideally usually ISO is best for backend.
        // I will only change 'today' and day logic for now.
        return Date().toISOString()
    }
    
    actual fun addMonths(date: String, months: Int): String {
        val parts = date.split("-")
        // Date constructor uses Local time: new Date(year, monthIndex, day)
        // JS Date handles overflow (e.g. month+1 when month is 11 -> next year)
        val d = Date(parts[0].toInt(), parts[1].toInt() - 1 + months, parts[2].toInt())
        
        val year = d.getFullYear()
        val month = (d.getMonth() + 1).toString().padStart(2, '0')
        val day = d.getDate().toString().padStart(2, '0')
        return "$year-$month-$day"
    }
    
    actual fun addDays(date: String, days: Int): String {
        val parts = date.split("-")
        val d = Date(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt() + days)
        
        val year = d.getFullYear()
        val month = (d.getMonth() + 1).toString().padStart(2, '0')
        val day = d.getDate().toString().padStart(2, '0')
        return "$year-$month-$day"
    }
    
    actual fun getDayOfWeek(dateStr: String): Int {
        // Returns 0=Sunday, 1=Monday, ..., 6=Saturday
        val parts = dateStr.split("-")
        // Parse as Local time to ensure getDay() aligns with the date string
        val d = Date(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        return d.getDay()
    }
}
