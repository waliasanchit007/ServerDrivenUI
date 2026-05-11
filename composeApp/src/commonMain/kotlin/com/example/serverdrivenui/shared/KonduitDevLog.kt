package com.example.serverdrivenui.shared

import kotlin.time.TimeSource

/**
 * High-level Konduit lifecycle formatter (Phase 5c — see KONDUIT_PLAN.md).
 *
 * The platform EventListeners (`SDUIZiplineEventListener` on Android,
 * `IosKonduitEventListener` on iOS) emit one debug line per low-level
 * Zipline event — that's useful for diagnostics (we used it to find
 * the snackbar bind-site bug). For normal dev, you want a curated
 * stream that summarizes each lifecycle phase: download started, code
 * loaded with timing, reload-on-file-change, errors.
 *
 * Usage: each platform constructs a single instance with a `sink`
 * callback that routes to its native log API (logcat on Android,
 * `println` → stderr on iOS). The platform EventListener then calls
 * the appropriate hooks alongside its existing low-level logging:
 *
 *   private val konduitLog = KonduitDevLog(sink = ::logToLogcat)
 *
 *   override fun downloadStart(url: String): Any? {
 *       Log.d("SDUI-Zipline", "downloadStart: $url")     // raw debug
 *       konduitLog.manifestDownloadStart(url)             // curated
 *       return null
 *   }
 *
 * State (timing, load count, etc.) is held internally so the platform
 * EventListener stays a thin pass-through.
 */
class KonduitDevLog(
    private val sink: (level: KonduitLogLevel, message: String) -> Unit,
) {
    private val timeSource = TimeSource.Monotonic

    /** Stamp of the most recent manifest download start; null between cycles. */
    private var downloadStartMark: TimeSource.Monotonic.ValueTimeMark? = null

    /**
     * Cumulative count of successful code loads. 0 = first load (use
     * "Loaded"); 1+ = subsequent loads (use "Reloaded" — these only
     * happen via Konduit hot-reload triggers).
     */
    private var loadCount: Int = 0

    fun manifestDownloadStart(url: String) {
        downloadStartMark = timeSource.markNow()
        sink(KonduitLogLevel.D, "⬇ Downloading manifest from ${url.shortenForDisplay()}")
    }

    fun manifestReady(moduleCount: Int) {
        sink(KonduitLogLevel.D, "📦 Manifest ready — $moduleCount modules")
    }

    fun codeLoadSuccess(applicationName: String) {
        val ms = downloadStartMark?.elapsedNow()?.inWholeMilliseconds
        val verb = if (loadCount == 0) "✓ Loaded" else "🔄 Reloaded"
        loadCount += 1
        val timing = ms?.let { " (${it}ms)" } ?: ""
        sink(KonduitLogLevel.D, "$verb $applicationName$timing")
        downloadStartMark = null
    }

    fun codeLoadFailed(message: String?) {
        sink(KonduitLogLevel.E, "✕ Code load failed: ${message ?: "(no message)"}")
        downloadStartMark = null
    }

    /**
     * Download failure: most often a network/manifest issue. We surface
     * the URL on a separate line because logcat truncates long lines.
     */
    fun downloadFailed(url: String, message: String?) {
        sink(KonduitLogLevel.E, "✕ Download failed")
        sink(KonduitLogLevel.E, "  url: ${url.shortenForDisplay()}")
        sink(KonduitLogLevel.E, "  cause: ${message ?: "(no message)"}")
        downloadStartMark = null
    }

    fun manifestParseFailed(message: String?) {
        sink(KonduitLogLevel.E, "✕ Manifest parse failed: ${message ?: "(no message)"}")
        downloadStartMark = null
    }

    fun uncaughtException(message: String?) {
        sink(KonduitLogLevel.E, "💥 Guest threw: ${message ?: "(no message)"}")
    }

    /**
     * Konduit's own warning that a bound service was GC'd without
     * close() — we hit this with the snackbar bind-site bug. Surfacing
     * it loudly here saves the next person from having to dig through
     * raw Zipline traces.
     */
    fun serviceLeaked(name: String) {
        sink(
            KonduitLogLevel.W,
            "⚠ Service leaked: '$name' was garbage-collected without close(). " +
                "Hold a strong reference on the host side (val field, not anonymous arg).",
        )
    }

    /**
     * Trim long URLs for log readability. Keeps the host + last path
     * segment so the line is still useful for diagnosis.
     */
    private fun String.shortenForDisplay(): String {
        if (length <= 80) return this
        // Hand-roll a "host…/lastSegment" so we don't pull in URL
        // parsing on Kotlin/Native (no java.net.URL there).
        val schemeEnd = indexOf("://").takeIf { it >= 0 }?.plus(3) ?: 0
        val firstSlash = indexOf('/', startIndex = schemeEnd)
        val lastSlash = lastIndexOf('/')
        return if (firstSlash < 0 || lastSlash <= firstSlash) {
            take(40) + "…" + takeLast(35)
        } else {
            "${substring(0, firstSlash)}/…${substring(lastSlash)}"
        }
    }
}

/**
 * Three levels are enough for Konduit's curated stream — they map
 * cleanly to logcat priorities (D/W/E) and to print-stream prefixes
 * on iOS where there's no level concept.
 */
enum class KonduitLogLevel { D, W, E }
