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

/**
 * Host-side snackbar queue. The guest calls [show] to enqueue a message;
 * the host displays it via M3's `SnackbarHostState` (FIFO queue, dismissed
 * by the next show or after [durationMillis]).
 *
 * Why a service rather than a widget: a snackbar is fundamentally an
 * EVENT, not a piece of layout. Modelling it as a widget forces the
 * guest to track "is the snackbar visible right now?" state and own a
 * timer; modelling it as a service lets the guest just say "tell the
 * user X happened" and forget.
 *
 * [durationMillis] semantics:
 *   - <= 0  → Indefinite (host displays until user dismisses or another
 *             show() supersedes it).
 *   - 1..6000  → Short  (~4 s — M3 default).
 *   - > 6000  → Long   (~10 s).
 *
 * [actionLabel] is the optional trailing action button label. Today the
 * guest can't react to action presses (no callback wire-up); the host
 * dismisses the snackbar on press. Adding a result callback is an
 * additive change for later.
 */
interface HostSnackbar : ZiplineService {
    fun show(message: String, actionLabel: String?, durationMillis: Long)
}
