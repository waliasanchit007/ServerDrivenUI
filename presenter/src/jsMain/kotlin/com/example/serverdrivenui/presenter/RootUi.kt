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
        Navigator().apply {
            // Routing priority: explicit Explore wiring beats Quotes,
            // beats default showcase. The host binds whichever services
            // its current screen needs; the guest mounts the matching
            // screen. If both are bound (e.g. the host pre-warms BOTH
            // before user navigates), Explore wins — DevoStatus mounts
            // them via two separate TreehouseApps so this conflict
            // can't occur in practice, but the deterministic ordering
            // is documented for any future single-app integrators.
            val firstScreen = when {
                HostWallpapersProviderBridge.instance != null -> ExploreScreen()
                HostQuotesProviderBridge.instance != null -> QuotesScreen()
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
