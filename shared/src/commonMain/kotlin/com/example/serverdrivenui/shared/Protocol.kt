package com.example.serverdrivenui.shared

import dev.konduit.treehouse.AppService
import dev.konduit.treehouse.ZiplineTreehouseUi
import app.cash.zipline.ZiplineService

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

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
 * Callback `ZiplineService` for [HostSnackbar.showWithResult]. The guest
 * implements this, passes the implementation into `showWithResult(...)`,
 * and the host invokes [onResult] when the snackbar resolves.
 *
 * Why a service instead of a `(Boolean) -> Unit` lambda parameter: Zipline
 * can only marshal across the QuickJS boundary values that are either
 * (a) `@Serializable`, or (b) `ZiplineService` proxies. A raw function-typed
 * parameter is neither — the codegen accepts the signature at compile time
 * but the runtime proxy fails to construct, which is the bug that broke
 * snackbars in commit 62ccff1.
 *
 * Lifecycle: the host calls [close] after invoking [onResult] exactly once,
 * so the guest impl doesn't have to manage its own ref. The guest is free
 * to share a single instance across calls — Zipline scopes the proxy.
 */
interface SnackbarResultCallback : ZiplineService {
    /**
     * @param actionPerformed true if the user tapped the action button
     *   (M3 `ActionPerformed`); false if the snackbar timed out, was
     *   swiped away, or was dismissed by a subsequent show().
     */
    fun onResult(actionPerformed: Boolean)
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
 * [actionLabel] is the optional trailing action button label. The host
 * dismisses the snackbar on press. The fire-and-forget [show] discards
 * the M3 SnackbarResult; use [showWithResult] to learn whether the user
 * tapped the action.
 */
interface HostSnackbar : ZiplineService {
    fun show(message: String, actionLabel: String?, durationMillis: Long)

    /**
     * Same as [show], but [onResult] fires when the snackbar is
     * dismissed:
     *   - true  → user tapped the action button (M3 `ActionPerformed`).
     *   - false → snackbar timed out, was swiped away, or was dismissed
     *             by a subsequent show() (M3 `Dismissed`).
     *
     * If [actionLabel] is null the action button is hidden, in which
     * case [onResult] always receives false.
     *
     * Why a separate method rather than adding an `onResult` parameter
     * to [show]: ZiplineService method signatures are wire format —
     * adding a parameter to [show] would break old guests calling new
     * hosts and vice versa. Adding a new method is additive.
     */
    fun showWithResult(
        message: String,
        actionLabel: String?,
        durationMillis: Long,
        callback: SnackbarResultCallback,
    )
}

/**
 * A single quote shown in a feed-style list. Cross-platform value
 * shape used by both host (Android / iOS) and guest (Kotlin/JS).
 *
 * Konduit-the-library is not opinionated about what a quote is — this
 * is a deliberately generic shape that maps to common content cards:
 * `text` is the headline, `language` is an optional locale tag,
 * `tag` is an optional grouping (e.g. an author, deity, category id)
 * that the host can use for downstream navigation.
 */
@Serializable
data class Quote(
    val id: String,
    val text: String,
    val language: String = "en",
    val tag: String? = null,
)

/**
 * Host-side data provider for a quote feed. The host implements this
 * to feed quotes (from a database, network, cache, etc.) into a
 * guest-rendered quote-feed screen.
 *
 * Why a service: the guest doesn't know how to fetch from your app's
 * data source. Bind one of these from the host's
 * `Spec.bindServices(...)` and the guest takes it as
 * `zipline.take<HostQuotesProvider>("quotes")`.
 *
 * If the guest's `take("quotes")` fails (no provider bound), the
 * presenter falls back to its default Tier 1 showcase screen.
 *
 * @see HostQuoteNavigator for the companion callback service that
 *   the guest uses to notify the host when a quote is tapped.
 */
interface HostQuotesProvider : ZiplineService {
    /**
     * Fetch the current quotes list. Suspends so the host can return
     * cached data or kick off a network fetch.
     *
     * The host should respect [languageFilter] when non-null: "en",
     * "hi", "sa" (extensible). Pass null to mean "all languages".
     */
    suspend fun getQuotes(languageFilter: String?): List<Quote>
}

/**
 * Companion to [HostQuotesProvider]: the guest calls [onQuoteSelected]
 * when the user taps a quote card. The host typically responds by
 * navigating to its own creation flow.
 */
interface HostQuoteNavigator : ZiplineService {
    fun onQuoteSelected(quoteId: String, tag: String?)
}
