package com.example.serverdrivenui.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.serverdrivenui.presenter.screens.Tier1ShowcaseScreen
import com.example.serverdrivenui.schema.compose.BackHandler
import com.example.serverdrivenui.schema.compose.ScreenStack

/**
 * Root UI composable. Sets up navigation + renders the current screen.
 *
 * Phase 3 Tier 1: only one screen exists (the widget showcase). Navigator
 * stays in place so future screens can be added without re-architecting.
 *
 * @param initialRoute Reserved for future deep-linking. Currently ignored.
 */
@Composable
fun RootUi(@Suppress("UNUSED_PARAMETER") initialRoute: String = "showcase") {
    val navigator = remember {
        Navigator().apply { push(Tier1ShowcaseScreen()) }
    }

    BackHandler(
        enabled = navigator.canGoBack,
        onBack = { navigator.pop() },
    )

    ScreenStack {
        navigator.currentScreen?.Content(navigator)
    }
}
