@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER", "EXPOSED_PARAMETER_TYPE", "EXPOSED_SUPER_CLASS", "EXPOSED_FUNCTION_RETURN_TYPE", "EXPOSED_PROPERTY_TYPE")

package com.example.serverdrivenui.shared

import app.cash.zipline.ZiplineService
import app.cash.zipline.internal.bridge.OutboundCallHandler
import app.cash.zipline.internal.bridge.OutboundService
import app.cash.zipline.internal.bridge.ReturningZiplineFunction
import app.cash.zipline.ZiplineFunction
import app.cash.zipline.internal.bridge.ZiplineServiceAdapter
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.AppLifecycle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

internal open class ManualSduiAppServiceAdapter(
    override val serializers: List<KSerializer<*>>,
    override val serialName: String = "com.example.serverdrivenui.shared.SduiAppService"
) : ZiplineServiceAdapter<SduiAppService>() {

    override val simpleName: String = "SduiAppService"

    override fun ziplineFunctions(serializersModule: SerializersModule): List<ZiplineFunction<SduiAppService>> {
        val launchFunction = object : ReturningZiplineFunction<SduiAppService>(
            id = "launch", 
            signature = "fun launch(): app.cash.redwood.treehouse.ZiplineTreehouseUi",
            argSerializers = emptyList(),
            resultSerializer = app.cash.zipline.ziplineServiceSerializer<ZiplineTreehouseUi>()
        ) {
            override fun call(service: SduiAppService, args: List<*>): Any? {
                return service.launch()
            }
        }
        val appLifecycleFunction = object : ReturningZiplineFunction<SduiAppService>(
            id = "appLifecycle",
            signature = "fun appLifecycle(): app.cash.redwood.treehouse.AppLifecycle",
            argSerializers = emptyList(),
            resultSerializer = app.cash.zipline.ziplineServiceSerializer<AppLifecycle>()
        ) {
             override fun call(service: SduiAppService, args: List<*>): Any? {
                 return service.appLifecycle
             }
        }
        val closeFunction = object : ReturningZiplineFunction<SduiAppService>(
             id = "close",
             signature = "fun close(): kotlin.Unit",
             argSerializers = emptyList(),
             resultSerializer = serializersModule.serializer<Unit>()
        ) {
             override fun call(service: SduiAppService, args: List<*>): Any? {
                 service.close()
                 return Unit
             }
        }
        return listOf(launchFunction, appLifecycleFunction, closeFunction)
    }

    override fun outboundService(callHandler: OutboundCallHandler): SduiAppService {
        return object : SduiAppService, OutboundService {
            override val callHandler: OutboundCallHandler = callHandler

            override val appLifecycle: AppLifecycle
                get() = callHandler.call(this, 1) as AppLifecycle

            override fun launch(): ZiplineTreehouseUi {
                return callHandler.call(this, 0) as ZiplineTreehouseUi
            }
            
            override fun close() {
                callHandler.call(this, 2)
            }
        }
    }
}
