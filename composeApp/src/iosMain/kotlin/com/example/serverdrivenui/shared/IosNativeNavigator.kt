@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.example.serverdrivenui.shared

import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UINavigationController
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIGestureRecognizer
import platform.darwin.NSObject

/**
 * iOS implementation of NativeNavigator.
 * Uses UINavigationController for navigation with swipe-back gesture detection.
 * 
 * Features:
 * - Maintains route stack synced with view controller stack
 * - Detects swipe-back gestures via UINavigationControllerDelegate
 * - Updates current route when user navigates back via gesture
 */
class IosNativeNavigator(
    private val getNavigationController: () -> UINavigationController?,
    private val createViewController: (String) -> UIViewController,
    private val onRouteChanged: ((String) -> Unit)? = null
) : NativeNavigator {
    
    // Route stack - mirrors the view controller stack
    private val routeStack = mutableListOf("dashboard")
    
    // Navigation controller delegate for detecting back navigation
    private val navigationDelegate = NavigationDelegate(
        onDidShow = { viewController, animated ->
            // Sync route when any navigation completes (including swipe-back)
            syncRouteWithViewControllerStack()
        }
    )
    
    override val currentRoute: String
        get() = routeStack.lastOrNull() ?: "dashboard"
    
    /**
     * Set up the navigation controller with our delegate.
     * Call this after setting the navigation controller.
     */
    fun setupNavigationDelegate() {
        getNavigationController()?.let { navController ->
            navController.delegate = navigationDelegate
            println("IosNativeNavigator: Navigation delegate set up")
        }
    }
    
    override fun navigateTo(route: String, params: Map<String, String>) {
        println("IosNativeNavigator: navigateTo($route)")
        
        getNavigationController()?.let { navController ->
            routeStack.add(route)
            val viewController = createViewController(route)
            // Store route in view controller for later identification
            viewController.title = route
            navController.pushViewController(viewController, animated = true)
            println("IosNativeNavigator: Pushed $route, stack: $routeStack")
            onRouteChanged?.invoke(route)
        } ?: run {
            println("IosNativeNavigator: No navigation controller available")
        }
    }
    
    override fun goBack(): Boolean {
        println("IosNativeNavigator: goBack()")
        val navController = getNavigationController()
        
        return if (navController != null && navController.viewControllers.size > 1) {
            navController.popViewControllerAnimated(true)
            // Route will be synced in delegate callback
            true
        } else {
            println("IosNativeNavigator: Cannot go back, at root")
            false
        }
    }
    
    override fun canGoBack(): Boolean {
        val navController = getNavigationController()
        return navController != null && navController.viewControllers.size > 1
    }
    
    /**
     * Sync route stack with view controller stack.
     * Called when view controller stack changes (push, pop, swipe-back).
     */
    private fun syncRouteWithViewControllerStack() {
        val navController = getNavigationController() ?: return
        val vcCount = navController.viewControllers.size
        
        // If view controller stack is smaller than route stack, user went back
        while (routeStack.size > vcCount && routeStack.size > 1) {
            val poppedRoute = routeStack.removeAt(routeStack.lastIndex)
            println("IosNativeNavigator: Route popped (swipe-back): $poppedRoute")
        }
        
        val newCurrentRoute = routeStack.lastOrNull() ?: "dashboard"
        println("IosNativeNavigator: Route synced, current: $newCurrentRoute, stack: $routeStack")
        onRouteChanged?.invoke(newCurrentRoute)
    }
    
    /**
     * Get the full route stack for debugging.
     */
    fun getRouteStack(): List<String> = routeStack.toList()
}

/**
 * UINavigationControllerDelegate implementation for detecting navigation events.
 * Note: We only implement didShowViewController to avoid conflicting overloads
 * issue in Kotlin/Native ObjC interop.
 */
private class NavigationDelegate(
    private val onDidShow: (UIViewController, Boolean) -> Unit
) : NSObject(), UINavigationControllerDelegateProtocol {
    
    override fun navigationController(
        navigationController: UINavigationController,
        didShowViewController: UIViewController,
        animated: Boolean
    ) {
        println("NavigationDelegate: didShowViewController: ${didShowViewController.title}")
        onDidShow(didShowViewController, animated)
    }
}


