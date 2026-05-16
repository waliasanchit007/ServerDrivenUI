package com.example.serverdrivenui.shared

import coil3.ComponentRegistry
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

/**
 * Android `installCoilNetworkFetcher` — OkHttp-backed.
 *
 * Why OkHttp: Zipline's HTTP loader already requires OkHttp at runtime
 * on Android, so this adds zero transitive weight while keeping Ktor 2
 * out of consumer apps' classpaths. See `docs/KNOWN_BUGS.md` #6.
 */
internal actual fun ComponentRegistry.Builder.installCoilNetworkFetcher() {
    add(OkHttpNetworkFetcherFactory())
}
