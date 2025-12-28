package com.example.serverdrivenui.shared

import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.zipline.ZiplineService

import kotlinx.serialization.KSerializer

interface SduiAppService : AppService {
    fun launch(): ZiplineTreehouseUi

    companion object {
        class Adapter(
            serializers: List<KSerializer<*>>,
            serialName: String
        ) : ManualSduiAppServiceAdapter(serializers, serialName)
    }
}

interface HostConsole : ZiplineService {
    fun log(message: String)
}
