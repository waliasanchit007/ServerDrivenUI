package com.example.serverdrivenui.presenter

import com.example.serverdrivenui.shared.GymService
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * ZiplineProxyEngine - A Ktor Client Engine that proxies all requests
 * to the Host via the `GymService` interface.
 */
@OptIn(io.ktor.utils.io.InternalAPI::class)
class ZiplineProxyEngine(
    private val gymService: GymService,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : HttpClientEngine {

    override val config: HttpClientEngineConfig = HttpClientEngineConfig()
    override val coroutineContext: CoroutineContext = dispatcher + Job()

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        try {
            // 1. Convert Ktor Request to ProxyRequest args
            val url = data.url.toString()
            val method = data.method.value
            val headersMap = data.headers.entries().associate { it.key to it.value.joinToString(",") }
            
            var bodyString: String? = null
            val content = data.body
            if (content is io.ktor.http.content.TextContent) {
                bodyString = content.text
            } else if (content is io.ktor.http.content.OutgoingContent.ByteArrayContent) {
                // Convert to string assuming it's JSON/Text (limitations apply)
                bodyString = content.bytes().decodeToString()
            }

            // 2. Call GymService (Host)
            println("ZiplineProxyEngine: Proxying $method $url")
            val response = gymService.proxyRequest(url, method, headersMap, bodyString)
            println("ZiplineProxyEngine: Received response ${response.status} from Host")

            // Correct way to build HttpResponseData body for Ktor 2.x
            println("ZiplineProxyEngine: Encoding Body to ByteArray...")
            val bytes = try {
                response.body.encodeToByteArray()
            } catch (e: Throwable) {
                println("ZiplineProxyEngine: FATAL - Body encoding failed: ${e.message}")
                // Fallback to empty
                ByteArray(0)
            }
            println("ZiplineProxyEngine: Body Encoded (${bytes.size} bytes). Creating ByteReadChannel...")

            // 3. Convert ProxyResponses to Ktor Response
            val statusCode = HttpStatusCode.fromValue(response.status)
            
            // Aggressively strip headers to prevent Ktor/Netty mismatches in pure JS env
            val responseHeaders = HeadersBuilder().apply {
                response.headers.forEach { (k, v) -> 
                    if (k.equals(HttpHeaders.ContentType, ignoreCase = true)) {
                        append(k, v)
                    }
                }
                // Explicitly set correct Content-Length
                append(HttpHeaders.ContentLength, bytes.size.toString())
            }.build()
            
            val responseBody: Any = io.ktor.utils.io.ByteReadChannel(bytes)
            println("ZiplineProxyEngine: ByteReadChannel created. Returning HttpResponseData with Detached Job.")

            return HttpResponseData(
                statusCode = statusCode,
                requestTime = GMTDate(),
                headers = responseHeaders,
                version = HttpProtocolVersion.HTTP_1_1,
                body = responseBody,
                callContext = Job() // Completely detached job to avoid cancellation
            )
        } catch (e: Throwable) {
            println("ZiplineProxyEngine: TOP LEVEL ERROR: ${e.message}\n${e.stackTraceToString()}")
            throw e
        }
    }

    override fun close() {
        // No-op
    }
}
