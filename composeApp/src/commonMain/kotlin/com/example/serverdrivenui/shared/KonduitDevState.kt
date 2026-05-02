package com.example.serverdrivenui.shared

import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 3a — dev tooling.
 *
 * Aggregates the state of guest .zipline loading so the host can:
 *   1. Render an error fallback (with retry) when the guest fails to load.
 *   2. Show a debug-only overlay banner during reload cycles.
 *
 * Driven by the Treehouse `EventListener` on each platform — see
 * `SDUIZiplineEventListener` (Android) and the iOS equivalent in
 * `MainViewController.kt`. Each platform also registers a retry
 * callback so the Error state can re-trigger a manifest fetch.
 *
 * NOT for production telemetry. Use a real observability hook for that.
 */
sealed class KonduitDevState {

    /** Initial state, before any load attempt. */
    object Idle : KonduitDevState()

    /** A guest manifest fetch is in progress. */
    object Downloading : KonduitDevState()

    /**
     * Guest loaded successfully. Stays in this state for ~1.5s before the
     * overlay auto-dismisses; underlying TreehouseContent renders normally.
     *
     * @param duration how long the load took (download + compile)
     * @param fresh true if this load fetched a new manifest, false if cached
     */
    data class LoadSuccess(
        val duration: Duration,
        val fresh: Boolean,
    ) : KonduitDevState()

    /**
     * Hot-reload triggered by dev-server WebSocket. Shows a brief
     * "reloading" indicator while the new manifest is fetched.
     */
    object Reloading : KonduitDevState()

    /**
     * Guest failed to load. Host renders fallback UI with a retry button.
     * The TreehouseContent is NOT mounted in this state — guest crashes
     * never propagate to the host's compose tree.
     *
     * @param message short, user-facing description (e.g. "Manifest 404")
     * @param detail optional file:line or stack snippet (debug builds)
     * @param onRetry invoke to trigger another manifest fetch
     */
    data class Error(
        val message: String,
        val detail: String? = null,
        val onRetry: () -> Unit,
    ) : KonduitDevState()
}

/**
 * Singleton sink for dev-state events. Both platforms emit into this from
 * their EventListener; the App composable subscribes to render fallback.
 */
object KonduitDevController {

    private val _state = MutableStateFlow<KonduitDevState>(KonduitDevState.Idle)
    val state: StateFlow<KonduitDevState> = _state.asStateFlow()

    private var downloadStartMark: TimeSource.Monotonic.ValueTimeMark? = null
    private var retryCallback: (() -> Unit)? = null

    /**
     * Each platform registers its retry hook (e.g. bumping manifestUrlFlow
     * to force Treehouse to re-fetch). Called once during host init.
     */
    fun registerRetryCallback(callback: () -> Unit) {
        retryCallback = callback
    }

    fun reportDownloadStart() {
        downloadStartMark = TimeSource.Monotonic.markNow()
        _state.value = KonduitDevState.Downloading
    }

    fun reportLoadSuccess(fresh: Boolean = true) {
        val duration = downloadStartMark?.elapsedNow() ?: Duration.ZERO
        _state.value = KonduitDevState.LoadSuccess(duration, fresh)
    }

    fun reportError(message: String, detail: String? = null) {
        _state.value = KonduitDevState.Error(
            message = message,
            detail = detail,
            onRetry = {
                _state.value = KonduitDevState.Downloading
                retryCallback?.invoke()
            },
        )
    }

    fun reportReloading() {
        downloadStartMark = TimeSource.Monotonic.markNow()
        _state.value = KonduitDevState.Reloading
    }
}
