package com.example.serverdrivenui

import android.content.Context
import android.content.SharedPreferences
import com.example.serverdrivenui.shared.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of StorageService using SharedPreferences.
 */
class AndroidStorageService(private val context: Context) : StorageService {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("sdui_storage", Context.MODE_PRIVATE)
    }

    override suspend fun getString(key: String): String? {
        return withContext(Dispatchers.IO) {
            prefs.getString(key, null)
        }
    }

    override suspend fun setString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(key, value).apply()
        }
    }
}
