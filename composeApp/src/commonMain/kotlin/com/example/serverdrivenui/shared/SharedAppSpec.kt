package com.example.serverdrivenui.shared

import app.cash.redwood.treehouse.TreehouseApp
import app.cash.zipline.Zipline
import io.ktor.client.*
import kotlinx.coroutines.flow.Flow
import com.example.serverdrivenui.shared.dto.HostApiConfig

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
    private val hostConsole: HostConsole,
    private val storage: StorageService
) : TreehouseApp.Spec<SduiAppService>() {
    
    override val name: String = "sdui"
    
    // Expose GymService so Host Activity can observe session changes
    val gymService: GymService by lazy {
        RealGymService(
            supabaseUrl = hostApi.supabaseUrl,
            supabaseKey = hostApi.supabaseKey,
            storage = storage,
            toastShower = { msg -> println("HOST TOAST: $msg") },
            urlOpener = { url -> println("HOST OPEN URL: $url") }
        )
    }

    override suspend fun bindServices(
        treehouseApp: TreehouseApp<SduiAppService>,
        zipline: Zipline
    ) {
        println("SharedAppSpec: bindServices called")
        
        // Bind console for logging
        zipline.bind<HostConsole>("console", hostConsole)
        println("SharedAppSpec: console bound")
        
        // Bind Storage for offline support
        zipline.bind<StorageService>("storage", storage)
        println("SharedAppSpec: storage bound")
        
        // Bind GymService for Supabase data access
        zipline.bind<GymService>("gym", gymService)
        println("SharedAppSpec: gym service bound")
    }
    
    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}
