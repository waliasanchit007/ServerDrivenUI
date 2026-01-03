package com.example.serverdrivenui.shared

import platform.Foundation.NSUserDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * iOS implementation of StorageService using NSUserDefaults.
 */
class IosStorageService : StorageService {
    
    override suspend fun getString(key: String): String? {
        return withContext(Dispatchers.IO) {
            NSUserDefaults.standardUserDefaults.stringForKey(key)
        }
    }

    override suspend fun setString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
        }
    }
}
