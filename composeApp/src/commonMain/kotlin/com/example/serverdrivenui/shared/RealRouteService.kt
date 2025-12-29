package com.example.serverdrivenui.shared

/**
 * Host-side implementation of RouteService.
 * Provides the current route to the presenter.
 */
class RealRouteService(
    private val getCurrentRouteProvider: () -> String
) : RouteService {
    override fun getCurrentRoute(): String {
        val route = getCurrentRouteProvider()
        println("RouteService: getCurrentRoute() = $route")
        return route
    }
}
