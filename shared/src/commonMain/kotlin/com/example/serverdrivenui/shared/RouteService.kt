package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService

/**
 * Service to provide the current route from host to presenter.
 * This allows the presenter to know which screen to show on initialization.
 */
interface RouteService : ZiplineService {
    /**
     * Get the current route that the presenter should display.
     */
    fun getCurrentRoute(): String
}
