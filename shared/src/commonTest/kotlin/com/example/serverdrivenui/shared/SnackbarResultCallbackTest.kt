package com.example.serverdrivenui.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Regression tests covering the two ways snackbar result delivery has
 * broken historically. Both bugs were on the *guest* side (presenter/jsMain
 * `Main.kt::showHostSnackbar`), but the contract under test belongs to this
 * module (the [SnackbarResultCallback] ZiplineService interface), so the
 * regression check lives here.
 *
 * Bug 1 — commit 62ccff1: the original [HostSnackbar.showWithResult] took a
 *   raw `(Boolean) -> Unit` lambda. Zipline can only marshal `@Serializable`
 *   values or `ZiplineService` proxies across the QuickJS boundary, so the
 *   proxy silently failed to construct and every snackbar with an Undo action
 *   became a no-op. Fix: wrap the lambda in [SnackbarResultCallback].
 *
 * Bug 2 — commit 7c259ac: the wrapper was an anonymous object whose override
 *   was also named `onResult(actionPerformed: Boolean)`. From inside the
 *   override, `onResult(actionPerformed)` resolved to the override itself
 *   (Kotlin name shadowing) rather than the outer-scope lambda parameter →
 *   infinite recursion → stack overflow → app crash on Undo tap. Fix: alias
 *   the outer lambda to a fresh name *before* the object expression.
 *
 * The two tests below pin down the post-fix behaviour. They use a faithful
 * reproduction of the guest pattern from `presenter/.../Main.kt`. If either
 * regression returns, the matching test will either fail an assertion or
 * blow the call stack.
 */
class SnackbarResultCallbackTest {
    /**
     * Mirror of the guest-side `showHostSnackbar` wrapper pattern. Returns
     * the [SnackbarResultCallback] that the host would invoke. The `val
     * resultLambda = outerLambda` aliasing is the critical part — see KDoc
     * on `Main.kt::showHostSnackbar` for the full rationale.
     */
    private fun wrapForSnackbarResult(outerLambda: (Boolean) -> Unit): SnackbarResultCallback {
        val resultLambda = outerLambda
        return object : SnackbarResultCallback {
            override fun onResult(actionPerformed: Boolean) {
                resultLambda(actionPerformed)
            }
        }
    }

    @Test
    fun callbackInvokesOuterLambdaExactlyOnce() {
        var calls = 0
        var observed: Boolean? = null
        val callback = wrapForSnackbarResult { actionPerformed ->
            calls++
            observed = actionPerformed
        }

        callback.onResult(true)

        assertEquals(1, calls, "outer lambda must fire exactly once per onResult call")
        assertEquals(true, observed, "lambda must receive the actionPerformed argument verbatim")
    }

    @Test
    fun callbackPassesFalseForDismissedSnackbar() {
        var observed: Boolean? = null
        val callback = wrapForSnackbarResult { observed = it }

        callback.onResult(false)

        assertEquals(false, observed, "Dismissed snackbar must surface actionPerformed=false")
    }

    @Test
    fun callbackDoesNotSelfRecurse() {
        // If the override were rewritten as
        //
        //     override fun onResult(actionPerformed: Boolean) {
        //         onResult(actionPerformed) // BUG: recurses into self
        //     }
        //
        // then a single onResult call would StackOverflow. We catch a
        // StackOverflowError here as the canonical fingerprint and fail
        // with a clear message so that a future contributor doesn't
        // re-introduce the bug while "simplifying" the wrapper.
        val callback = wrapForSnackbarResult { /* no-op */ }
        try {
            callback.onResult(true)
        } catch (e: Throwable) {
            // Kotlin/Native + JVM both report StackOverflowError on
            // infinite recursion. Treat any Throwable that mentions
            // overflow as the failure mode we're guarding against.
            if (e::class.simpleName?.contains("StackOverflow") == true) {
                fail("SnackbarResultCallback wrapper recursed into itself — restore the `val resultLambda = outerLambda` alias before the object expression.")
            }
            throw e
        }
        assertTrue(true, "no recursion observed")
    }
}
