package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
            val firstScreen = if (HostQuotesProviderBridge.instance != null) {
                QuotesScreen()
            } else {
                Tier1ShowcaseScreen()
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
