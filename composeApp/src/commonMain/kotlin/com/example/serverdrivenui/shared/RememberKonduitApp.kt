@file:Suppress("OPT_IN_USAGE")

package com.example.serverdrivenui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.konduit.treehouse.AppService
import dev.konduit.treehouse.TreehouseApp

/**
 * Compose-aware factory for [TreehouseApp] that internalizes the stable-
 * key contract — and **warns the integrator at runtime if they break it.**
 *
 * Why this exists:
 *   The canonical Konduit host pattern holds a `TreehouseApp` via
 *   `remember(stableKeys…) { factory.create(spec) }`. If any of those
 *   keys has unstable identity across recompositions — most commonly
 *   anonymous lambdas, freshly-mapped flows, or anything constructed
 *   inline in the Composable — `remember` invalidates on EVERY
 *   recomposition and rebuilds a brand-new `TreehouseApp`. The old one
 *   is still alive (nobody closes it), the two instances race for the
 *   same Zipline runtime, and the second `Spec.bindServices` call
 *   silently fails. See `docs/KNOWN_BUGS.md` gotcha #9 for the full
 *   pathology — DevoStatus burned about a day on it.
 *
 * What [rememberKonduitApp] does differently:
 *   - It runs `remember(*stableKeys) { create() }` like you would by
 *     hand — same caching semantics, same lifetime.
 *   - It also tracks how many times `create` has run for this call
 *     site. If `create` runs more than once, that proves at least one
 *     key was unstable. The helper logs a loud warning the FIRST time
 *     this is detected (subsequent rebuilds are silent so we don't
 *     spam logcat under genuine recomposition pressure).
 *
 * Intended usage:
 *
 * ```kotlin
 * // Unstable lambdas wrapped in rememberUpdatedState:
 * val currentSource by rememberUpdatedState(sourceLambda)
 * val currentCallback by rememberUpdatedState(callbackLambda)
 *
 * // Only stable refs in the key list:
 * val app = rememberKonduitApp(activity, stableFlow) {
 *     factory.create(
 *         appScope = activity.lifecycleScope,
 *         spec = MySpec(
 *             // Adapter lambdas with stable identity, delegating to the
 *             // always-current State via rememberUpdatedState above.
 *             source = { x -> currentSource(x) },
 *             callback = { r -> currentCallback(r) },
 *         ),
 *         eventListenerFactory = listenerFactory,
 *     )
 * }
 * ```
 *
 * **Warning, not enforcement.** The helper can't physically prevent
 * the integrator from passing unstable keys (it has no way to inspect
 * lambda identity). The warning is your only signal in the logs that
 * something's wrong — read it.
 *
 * @param stableKeys Cache keys. MUST be stable across recompositions
 *   for the same logical TreehouseApp instance. Typical: the hosting
 *   Activity, plus any stably-remembered Flow / object refs the spec
 *   depends on.
 * @param create Factory block. Called exactly once for a stable key set.
 */
@Composable
fun <A : AppService> rememberKonduitApp(
    vararg stableKeys: Any?,
    create: () -> TreehouseApp<A>,
): TreehouseApp<A> {
    // The counter lives in a separate `remember` with NO keys, so it
    // persists across stableKeys changes. Each rebuild of the
    // TreehouseApp increments it.
    val counter = remember { intArrayOf(0) }
    return remember(*stableKeys) {
        counter[0]++
        if (counter[0] == 2) {
            // Only fire on the SECOND build — at this point we have
            // enough evidence to claim the keys are unstable. First
            // build is just normal initialization. Subsequent builds
            // beyond 2 are silent: the integrator is presumed to have
            // seen the warning by then.
            println(
                "⚠️  rememberKonduitApp: built a SECOND TreehouseApp for this " +
                    "call site. Your `stableKeys` list contains at least one " +
                    "unstable reference (anonymous lambda? Flow.map { ... } " +
                    "inline? object instantiated in the Composable body?). " +
                    "Wrap unstable lambdas in `rememberUpdatedState` and pass " +
                    "stable adapters to your Spec. See Konduit USAGE.md Step 2 " +
                    "'Remember stability' and `docs/KNOWN_BUGS.md` gotcha #9 " +
                    "for the full pattern."
            )
        }
        create()
    }
}
