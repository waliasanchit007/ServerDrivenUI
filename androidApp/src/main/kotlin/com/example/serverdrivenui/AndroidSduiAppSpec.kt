package com.example.serverdrivenui

import android.util.Log
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.zipline.Zipline
import com.example.serverdrivenui.shared.HostConsole
import com.example.serverdrivenui.shared.SduiAppService
import kotlinx.coroutines.flow.Flow

class AndroidRealHostConsole : HostConsole {
    init {
        Log.d("SDUI-Host", "AndroidRealHostConsole initialized")
    }
    override fun log(message: String) {
        Log.d("SDUI-JS", message)
    }
}

class AndroidSduiAppSpec(
    override val manifestUrl: Flow<String>,
    override val name: String = "sdui",
) : TreehouseApp.Spec<SduiAppService>() {
    init {
        Log.d("SDUI-Host", "AndroidSduiAppSpec initialized")
    }

    override suspend fun bindServices(treehouseApp: TreehouseApp<SduiAppService>, zipline: Zipline) {
        Log.d("SDUI-Host", "bindServices called")
        zipline.bind<HostConsole>("console", AndroidRealHostConsole())
        Log.d("SDUI-Host", "console service bound")
    }
    
    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}
