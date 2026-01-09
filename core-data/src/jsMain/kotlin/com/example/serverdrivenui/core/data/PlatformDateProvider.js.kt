package com.example.serverdrivenui.core.data

import kotlin.js.Date

actual object PlatformDateProvider {
    actual fun today(): String {
        return Date().toISOString().split("T")[0]
    }
    
    actual fun now(): String {
        return Date().toISOString()
    }
    
    actual fun addMonths(date: String, months: Int): String {
        val d = Date(date)
        val newDate = Date(d.getFullYear(), d.getMonth() + months, d.getDate())
        return newDate.toISOString().split("T")[0]
    }
}
