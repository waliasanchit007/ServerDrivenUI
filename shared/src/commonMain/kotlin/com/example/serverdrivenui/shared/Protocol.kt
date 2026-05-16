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
     * Fetch the current quotes list synchronously from the host's
     * in-memory cache. The host should have its data ready before
     * navigating to a QuotesScreen — typically by kicking off the
     * network fetch in `LaunchedEffect(Unit)` of the route's
     * composable before binding this service.
     *
     * Why non-suspend: Konduit-Zipline 1.26's compiler plugin causes
     * the host's `bind<>()` to hang silently when this method is
     * declared `suspend`. Empirically reproducible with
     * `List<@Serializable Quote>` return type. A future Konduit
     * release may lift this restriction.
     *
     * The host should respect [languageFilter] when non-null: "en",
     * "hi", "sa" (extensible). Pass null to mean "all languages".
     */
    fun getQuotes(languageFilter: String?): List<Quote>

    /**
     * Register a [HostQuotesObserver] so the host can push
     * change-notifications when its underlying data updates (a refresh
     * lands, a new quote is added, a user-driven filter change happens
     * out-of-band, etc.). The guest is expected to re-fetch via
     * [getQuotes] when [HostQuotesObserver.onQuotesChanged] fires.
     *
     * Calling [observe] a second time replaces the previous observer —
     * the host must drop its reference to the old one. The guest is
     * free to pass the same [HostQuotesObserver] instance across calls;
     * Zipline scopes the proxy.
     *
     * Why a separate method rather than embedding the data in the
     * callback: keeping `getQuotes()` as the single source of truth
     * means the host doesn't have to know about the guest's current
     * language filter when notifying. The guest re-asks with the right
     * filter, and the host serves the same in-memory cache.
     *
     * Why additive on the existing service rather than a new
     * `HostQuotesObservable` service: ZiplineService method addition is
     * wire-additive (per [HostSnackbar.showWithResult]'s rationale),
     * and integrators are more likely to remember to wire one service
     * than to wire two. Old guests that never call [observe] keep
     * working unchanged. Old hosts that don't override [observe] would
     * surface as a "no such method" runtime error on the guest side —
     * the guest should wrap [observe] in try/catch for graceful
     * degradation against older host binaries.
     */
    fun observe(observer: HostQuotesObserver)
}

/**
 * Companion to [HostQuotesProvider]: the guest calls [onQuoteSelected]
 * when the user taps a quote card. The host typically responds by
 * navigating to its own creation flow.
 */
interface HostQuoteNavigator : ZiplineService {
    fun onQuoteSelected(quoteId: String, tag: String?)
}

// ─── Explore-feed shapes (Wallpaper + provider + navigator) ─────────────

/**
 * A wallpaper image surfaced to the guest as part of an explore /
 * gallery feed. Like [Quote], this is a deliberately minimal value
 * shape — `imageUrl` is enough for an `AsyncImage`, [tag] gives the
 * host a way to group / filter (e.g. by deity, mood, category) when
 * the user picks one.
 */
@Serializable
data class Wallpaper(
    val id: String,
    val imageUrl: String,
    val tag: String? = null,
)

/**
 * Host-side provider for a wallpaper feed. Same shape as
 * [HostQuotesProvider]:
 *   - Non-suspend `getWallpapers(tagFilter)` for the same reason
 *     (Konduit-Zipline 1.26 suspend-bind hang).
 *   - Optional [HostWallpapersObserver] for reactive pushes when the
 *     host's cache updates.
 *
 * The DevoStatus Explore screen pairs `getQuotes(...)` with a random
 * matching wallpaper per card; in that pattern the guest fetches both
 * lists once on mount and zips them in the presenter. For screens
 * where wallpapers are the primary feed (a pure gallery), provider +
 * observer give the full reactive contract.
 */
interface HostWallpapersProvider : ZiplineService {
    fun getWallpapers(tagFilter: String?): List<Wallpaper>
    fun observe(observer: HostWallpapersObserver)
}

interface HostWallpapersObserver : ZiplineService {
    fun onWallpapersChanged()
}

/**
 * Companion navigator for an explore-feed tap. The guest passes the
 * selected quote id + wallpaper id; the host typically navigates to a
 * preview / creation screen pre-populated with that pair.
 *
 * [wallpaperId] is nullable because the screen may render quote-only
 * cards when no wallpaper is available (gradient fallback).
 */
interface HostExploreNavigator : ZiplineService {
    fun onExploreItemSelected(quoteId: String, wallpaperId: String?)
}

/**
 * Reactive push channel for a quote feed. The guest implements this
 * service, passes it to [HostQuotesProvider.observe], and the host calls
 * [onQuotesChanged] whenever its in-memory data changes.
 *
 * The guest is expected to respond by re-calling
 * [HostQuotesProvider.getQuotes] with its current language filter — the
 * callback intentionally doesn't carry the new data because the host
 * doesn't know the guest's filter state.
 *
 * Lifecycle: Zipline's leak detector will surface a `serviceLeaked`
 * event if the host stops holding a reference to the observer before
 * calling [close], so hosts should null out their stored reference (or
 * call [close]) when the guest screen unmounts. The guest doesn't need
 * to manage anything — its LaunchedEffect scope handles teardown.
 */
interface HostQuotesObserver : ZiplineService {
    /**
     * Fires when host data may have changed. Guest should re-fetch via
     * [HostQuotesProvider.getQuotes] with its current filter.
     *
     * Non-suspend for the same reason [HostQuotesProvider.getQuotes] is
     * non-suspend (Konduit-Zipline 1.26 suspend-bind hang). A future
     * release may lift this.
     */
    fun onQuotesChanged()
}
