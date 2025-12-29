package com.example.serverdrivenui

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.serverdrivenui.shared.NativeNavigator

/**
 * Android implementation of NativeNavigator.
 * Uses Intent-based Activity navigation with singleTop launch mode.
 * Maintains a route history stack for proper back navigation.
 */
class AndroidNativeNavigator(
    private val activity: Activity
) : NativeNavigator {
    
    // Route history stack for tracking navigation
    private val routeHistory = mutableListOf<String>()
    
    init {
        // Initialize with current route
        val initialRoute = activity.intent.getStringExtra(EXTRA_ROUTE) ?: DEFAULT_ROUTE
        routeHistory.add(initialRoute)
        Log.d("AndroidNativeNavigator", "Initialized with route: $initialRoute")
    }
    
    override val currentRoute: String
        get() = routeHistory.lastOrNull() ?: DEFAULT_ROUTE
    
    override fun navigateTo(route: String, params: Map<String, String>) {
        Log.d("AndroidNativeNavigator", "navigateTo($route), history before: $routeHistory")
        
        // Add to history unless we're navigating to the same route
        if (route != currentRoute) {
            routeHistory.add(route)
        }
        
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
        
        Log.d("AndroidNativeNavigator", "navigateTo complete, history after: $routeHistory")
    }
    
    override fun goBack(): Boolean {
        Log.d("AndroidNativeNavigator", "goBack(), history: $routeHistory")
        
        if (routeHistory.size <= 1) {
            // At root, let Activity handle back (close app)
            Log.d("AndroidNativeNavigator", "Cannot go back, at root")
            return false
        }
        
        // Pop current route
        routeHistory.removeAt(routeHistory.lastIndex)
        val previousRoute = routeHistory.lastOrNull() ?: DEFAULT_ROUTE
        
        Log.d("AndroidNativeNavigator", "Going back to: $previousRoute")
        
        // Navigate to previous route
        val intent = Intent(activity, MainActivity::class.java).apply {
            putExtra(EXTRA_ROUTE, previousRoute)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
        
        return true
    }
    
    override fun canGoBack(): Boolean {
        return routeHistory.size > 1
    }
    
    /**
     * Call this when the Activity receives onNewIntent to sync route history.
     */
    fun onNewIntent(route: String) {
        // Only add if different from current (in case goBack triggered this)
        if (routeHistory.lastOrNull() != route && !routeHistory.contains(route)) {
            routeHistory.add(route)
        } else if (routeHistory.lastOrNull() == route) {
            // Same route, no change needed
        }
        Log.d("AndroidNativeNavigator", "onNewIntent($route), history: $routeHistory")
    }
    
    companion object {
        const val EXTRA_ROUTE = "sdui_route"
        const val DEFAULT_ROUTE = "dashboard"
    }
}

