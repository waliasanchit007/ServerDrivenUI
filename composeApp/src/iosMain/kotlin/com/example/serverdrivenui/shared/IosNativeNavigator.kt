package com.example.serverdrivenui.shared

import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

/**
 * iOS implementation of NativeNavigator.
 * Uses UINavigationController for navigation.
 * 
 * Note: This requires the root view controller to be wrapped in a UINavigationController.
 */
class IosNativeNavigator(
    private val getNavigationController: () -> UINavigationController?,
    private val createViewController: (String) -> UIViewController
) : NativeNavigator {
    
    private var _currentRoute: String = "dashboard"
    
    override val currentRoute: String
        get() = _currentRoute
    
    override fun navigateTo(route: String, params: Map<String, String>) {
        println("IosNativeNavigator: navigateTo($route)")
        _currentRoute = route
        
        getNavigationController()?.let { navController ->
            val viewController = createViewController(route)
            navController.pushViewController(viewController, animated = true)
        } ?: run {
            println("IosNativeNavigator: No navigation controller available")
        }
    }
    
    override fun goBack(): Boolean {
        println("IosNativeNavigator: goBack()")
        val navController = getNavigationController()
        
        return if (navController != null && navController.viewControllers.size > 1) {
            navController.popViewControllerAnimated(true)
            // Update current route based on new top view controller
            val viewControllers = navController.viewControllers
            if (viewControllers.isNotEmpty()) {
                // Could track routes via view controller tags or associated objects
                println("IosNativeNavigator: Popped to previous screen")
            }
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
}
