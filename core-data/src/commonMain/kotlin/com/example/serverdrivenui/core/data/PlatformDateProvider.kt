package com.example.serverdrivenui.core.data

expect object PlatformDateProvider {
    fun today(): String
    fun now(): String
    fun addMonths(date: String, months: Int): String
}
