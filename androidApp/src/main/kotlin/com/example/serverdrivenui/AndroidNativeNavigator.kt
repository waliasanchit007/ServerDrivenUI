package com.example.serverdrivenui

import android.app.Activity
import android.content.Intent
import com.example.serverdrivenui.shared.NativeNavigator

/**
 * Android implementation of NativeNavigator.
 * Uses Intent-based Activity navigation with singleTop launch mode.
 */
class AndroidNativeNavigator(
    private val activity: Activity
) : NativeNavigator {
    
    override val currentRoute: String
        get() = activity.intent.getStringExtra(EXTRA_ROUTE) ?: DEFAULT_ROUTE
    
    override fun navigateTo(route: String, params: Map<String, String>) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            putExtra(EXTRA_ROUTE, route)
            // Pass params as extras
            params.forEach { (key, value) ->
                putExtra("param_$key", value)
            }
            // Use singleTop to reuse existing instance if at top
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
    }
    
    override fun goBack(): Boolean {
        // Check if we can go back (not at the root Activity)
        if (activity.isTaskRoot) {
            return false
        }
        activity.finish()
        return true
    }
    
    override fun canGoBack(): Boolean {
        return !activity.isTaskRoot
    }
    
    companion object {
        const val EXTRA_ROUTE = "sdui_route"
        const val DEFAULT_ROUTE = "dashboard"
    }
}
