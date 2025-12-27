package com.example.serverdrivenui.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform