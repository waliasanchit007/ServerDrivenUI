package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.serverdrivenui.presenter.screens.ExploreScreen
import com.example.serverdrivenui.presenter.screens.QuotesScreen
import com.example.serverdrivenui.presenter.screens.Tier1ShowcaseScreen
import com.example.serverdrivenui.schema.compose.BackHandler
import com.example.serverdrivenui.schema.compose.ScreenStack

/**
 * Root UI composable. Sets up navigation + renders the current screen.
 *
 * Initial-screen selection is driven by which host services were bound
 * at startup:
 *  - HostQuotesProvider bound → QuotesScreen (DevoStatus-style quote feed)
 *  - else → Tier1ShowcaseScreen (Konduit widget gallery)
 *
 * The choice is host-driven: a host that calls
 * `zipline.bind<HostQuotesProvider>("quotes", ...)` in its Spec gets
 * the quote feed without modifying guest code; a host that doesn't,
 * gets the showcase.
 *
 * @param initialRoute Reserved for future deep-linking. Currently
 *   only consulted as a tie-breaker if no host service is bound.
 */
@Composable
fun RootUi(@Suppress("UNUSED_PARAMETER") initialRoute: String = "auto") {
    val navigator = remember {
        // Route by validating the proxies via a real method call. The
        // bridges in Main.kt are populated with DEFERRED proxies that
        // are non-null regardless of whether the host actually bound
        // the service (see Main.kt comment). Calling the getter is the
        // only reliable signal of "this service is wired through to a
        // live host implementation." By RootUi composition time, the
        // host's bindServices has finished, so the call either returns
        // data or throws "no such service" cleanly.
        //
        // Routing priority: Explore wiring beats Quotes, beats default
        // showcase. The host binds whichever services its current
        // screen needs; the guest mounts the matching screen. If both
        // are bound, Explore wins — DevoStatus uses two separate
        // TreehouseApps so this conflict can't occur in practice.
        val isWallpapersLive = try {
            HostWallpapersProviderBridge.instance?.also { it.getWallpapers(null) } != null
        } catch (_: Throwable) {
            false
        }
        val isQuotesLive = try {
            HostQuotesProviderBridge.instance?.also { it.getQuotes(null) } != null
        } catch (_: Throwable) {
            false
        }
        println("Zipline JS: Routing — wallpapersLive=$isWallpapersLive quotesLive=$isQuotesLive")
        // Clear bridges that proxy unbound services so downstream code
        // (LaunchedEffects in QuotesScreen / ExploreScreen) doesn't try
        // to call them and re-trigger "no such service" errors.
        //
        // .close() the deferred proxy before nulling — otherwise Konduit's
        // leak detector logs `serviceLeaked name=quotes/wallpapers` because
        // the proxy goes out of scope without being closed. Cosmetic but
        // noisy, and adopters following this routing pattern will see the
        // warnings for any service their host doesn't bind.
        if (!isWallpapersLive) {
            try { HostWallpapersProviderBridge.instance?.close() } catch (_: Throwable) {}
            HostWallpapersProviderBridge.instance = null
        }
        if (!isQuotesLive) {
            try { HostQuotesProviderBridge.instance?.close() } catch (_: Throwable) {}
            HostQuotesProviderBridge.instance = null
        }

        Navigator().apply {
            val firstScreen = when {
                isWallpapersLive -> ExploreScreen()
                isQuotesLive -> QuotesScreen()
                else -> Tier1ShowcaseScreen()
            }
            push(firstScreen)
        }
    }

    BackHandler(
        enabled = navigator.canGoBack,
        onBack = { navigator.pop() },
    )

    ScreenStack {
        navigator.currentScreen?.Content(navigator)
    }
}
