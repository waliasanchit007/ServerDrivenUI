package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService

/**
 * StorageService - Interface for key-value storage.
 * Used for persisting data across app sessions (offline support).
 */
interface StorageService : ZiplineService {
    suspend fun getString(key: String): String?
    suspend fun setString(key: String, value: String)
}
