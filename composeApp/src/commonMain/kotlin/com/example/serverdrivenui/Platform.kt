package com.example.serverdrivenui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform