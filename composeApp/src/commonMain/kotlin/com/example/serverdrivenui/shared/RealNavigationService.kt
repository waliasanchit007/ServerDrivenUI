package com.example.serverdrivenui.shared

/**
 * Host-side implementation of NavigationService that delegates to NativeNavigator.
 * This is bound to Zipline in bindServices() so the Presenter can call it.
 */
class RealNavigationService(
    private val navigator: NativeNavigator
) : NavigationService {
    
    override fun navigateTo(route: String, params: Map<String, String>) {
        println("NavigationService: navigateTo($route, $params)")
        navigator.navigateTo(route, params)
    }
    
    override fun goBack() {
        println("NavigationService: goBack()")
        navigator.goBack()
    }
    
    override fun canGoBack(): Boolean {
        return navigator.canGoBack()
    }
}
