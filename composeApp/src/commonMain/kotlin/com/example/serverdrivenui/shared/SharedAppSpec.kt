package com.example.serverdrivenui.shared

import app.cash.redwood.treehouse.TreehouseApp
import app.cash.zipline.Zipline
import io.ktor.client.*
import kotlinx.coroutines.flow.Flow

/**
 * SharedAppSpec - Platform-agnostic TreehouseApp.Spec implementation.
 * Used by both Android and iOS with platform-specific HttpClient engines.
 * 
 * This is placed in composeApp because it needs access to TreehouseApp.Spec
 * from redwood-treehouse-host.
 */
class SharedAppSpec(
    override val manifestUrl: Flow<String>,
    private val httpClient: HttpClient,
    private val hostApi: HostApiConfig,
    private val hostConsole: HostConsole
) : TreehouseApp.Spec<SduiAppService>() {
    
    override val name: String = "sdui"
    
    override suspend fun bindServices(
        treehouseApp: TreehouseApp<SduiAppService>,
        zipline: Zipline
    ) {
        println("SharedAppSpec: bindServices called")
        
        // Bind console for logging
        zipline.bind<HostConsole>("console", hostConsole)
        println("SharedAppSpec: console bound")
        
        // Bind GymService for Supabase data access
        val gymService = RealGymService(
            httpClient = httpClient,
            supabaseUrl = hostApi.supabaseUrl,
            supabaseKey = hostApi.supabaseKey
        )
        zipline.bind<GymService>("gym", gymService)
        println("SharedAppSpec: gym service bound")
    }
    
    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}
