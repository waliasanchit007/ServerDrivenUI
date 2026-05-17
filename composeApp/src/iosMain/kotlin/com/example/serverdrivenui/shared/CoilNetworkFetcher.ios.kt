package com.example.serverdrivenui.shared

import coil3.ComponentRegistry
import coil3.network.ktor2.KtorNetworkFetcherFactory

/**
 * iOS `installCoilNetworkFetcher` — Ktor 2-backed.
 *
 * Why Ktor 2 (not Ktor 3): the iOS source set already pulls in
 * `ktor-client-darwin` at the project-wide ktor version (2.x). Swap to
 * `coil-network-ktor3` when the project-wide ktor version bumps.
 */
internal actual fun ComponentRegistry.Builder.installCoilNetworkFetcher() {
    add(KtorNetworkFetcherFactory())
}
