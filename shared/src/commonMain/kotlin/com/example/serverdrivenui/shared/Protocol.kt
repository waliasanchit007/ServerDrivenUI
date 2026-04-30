package com.example.serverdrivenui.shared

import dev.konduit.treehouse.AppService
import dev.konduit.treehouse.ZiplineTreehouseUi
import app.cash.zipline.ZiplineService

import kotlinx.serialization.KSerializer

interface SduiAppService : AppService {
    fun launch(): ZiplineTreehouseUi

    companion object {
        internal class Adapter(
            serializers: List<KSerializer<*>>,
            serialName: String
        ) : ManualSduiAppServiceAdapter(serializers, serialName)
    }
}

interface HostConsole : ZiplineService {
    fun log(message: String)
}
