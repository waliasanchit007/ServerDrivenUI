# Known Konduit / Zipline bugs

Surfaced by the DevoStatus integration (real Android Compose app
consuming Konduit as a submodule + Maven library). Each entry lists
symptom, reproduction, current workaround, and what an upstream fix
would look like.

> Treat this file as the punch list. When a bug is fixed, move its
> section to `docs/CHANGELOG.md` with the fixing commit.

---

## 1. `suspend` `ZiplineService` methods returning `List<@Serializable T>` hang `bind<>()`

**Severity:** high (silent failure mode; integrators give up before
finding the workaround).

**Symptom.** Declaring a `ZiplineService` method as
`suspend fun foo(...): List<MySerializable>` causes the host's
`zipline.bind<MyService>("name", impl)` call to hang indefinitely. No
exception is thrown, no log line is emitted. The hang happens *before*
the guest ever calls the method — it's a bind-time problem.

**Reproduce.**
```kotlin
// shared/Protocol.kt
@Serializable
data class Quote(val id: String, val text: String)

interface HostQuotesProvider : ZiplineService {
    suspend fun getQuotes(filter: String?): List<Quote>  // ← hangs
}

// host
override suspend fun bindServices(treehouseApp: ..., zipline: Zipline) {
    zipline.bind<HostQuotesProvider>("quotes", impl)
    Log.d("…", "bound")  // ← never logged
}
```

Removing `suspend` (`fun getQuotes(...): List<Quote>`) resolves the
hang immediately.

**Empirically reproduced** on Konduit / Zipline 1.26 (commit
`0d18809` in this repo). Not investigated for whether the issue is in
Zipline's compiler plugin codegen or the host-side proxy construction.

**Workaround.** Keep `getQuotes` non-suspend; have the host pre-cache
the data before binding. See `HostQuotesProvider`'s kdoc and Step 4½ in
`USAGE.md`.

**Upstream fix.** Investigate the Zipline 1.26 compiler-plugin codegen
for `suspend fun … : List<@Serializable T>` signatures. A workaround
inside Zipline could be: detect the offending shape and either
(a) compile through it correctly, or (b) emit a build-time error so
integrators see a clear "this shape is unsupported" message instead of
a silent runtime hang.

**Workaround code paths to revert** once fixed:
- `shared/Protocol.kt#HostQuotesProvider.getQuotes`
- `presenter/screens/QuotesScreen.kt` (the snapshot-based fetch pattern)
- DevoStatus's `KonduitQuotesScreen.kt` load-gate (the
  `if (nativeQuotes.isEmpty()) { spinner } else { konduit }` wrapper)

---

## 2. Zipline Gradle plugin is mandatory on every module that calls `bind`/`take`, silently hangs otherwise

**Severity:** high (silent failure; same "give up" outcome).

**Symptom.** A module that calls `zipline.bind<Foo>(...)` or
`zipline.take<Foo>(...)` but doesn't apply the
`app.cash.zipline` Gradle plugin compiles successfully and links
successfully. At runtime, `bind` hangs forever and `take` throws
`"unexpected call to Zipline.take: is the Zipline plugin configured?"`.

**Reproduce.** New host module that depends on `:shared` but doesn't
add `alias(libs.plugins.zipline)` to its `plugins {}` block. Call
`zipline.bind<HostConsole>(...)` — never returns.

**Workaround.** Always apply the plugin:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)  // ← mandatory anywhere bind/take is called
}
```

Documented in `USAGE.md` Step 2 "⚠️ MANDATORY".

**Upstream fix.** The Zipline runtime could detect missing-plugin state
(no codegen artifacts on the classpath for the requested service) and
throw on `bind` instead of hanging. A lint/Detekt rule would also catch
this at compile time.

---

## 3. `kotlinx-serialization` plugin is required on every module that defines a `@Serializable` wire type used by a `ZiplineService`

**Severity:** medium (runtime error has good message, but error fires
late in integration).

**Symptom.** Defining `@Serializable data class Quote(...)` in a module
that doesn't apply `org.jetbrains.kotlin.plugin.serialization` compiles
OK, but `zipline.take<HostQuotesProvider>("quotes")` (or the
corresponding `bind` on the host) throws at runtime:

```
Serializer for class 'Quote' is not found.
Please ensure that class is marked as '@Serializable' and that the
serialization compiler plugin is applied.
```

**Reproduce.** Add `@Serializable` to a data class in a module that has
only `alias(libs.plugins.kotlinMultiplatform)` — no `kotlinSerialization`.

**Workaround.** Apply the plugin:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
    alias(libs.plugins.kotlinSerialization)  // ← required for @Serializable wire types
}
```

DevoStatus hit this in `:shared` initially — fixed in upstream commit
`c73b04c` (see also the comment at the top of `shared/build.gradle.kts`).

**Upstream fix.** The Zipline Gradle plugin could check whether
`@Serializable` types appear in the module's classfiles and warn if the
kotlinSerialization plugin isn't also applied.

---

## 4. Schema lacks `alpha`, `offset`, `border`, and custom-font modifiers

**Severity:** medium (visual fidelity gap; integrators ship subpar UX or
fall back to native screens).

**Symptom.** Common visual treatments — semi-transparent watermarks,
absolutely-positioned decorative glyphs, custom borders on cards,
serif/custom-typeface text — cannot be expressed in the schema. The
result is "close but not pixel-identical" SDUI screens vs the native
Compose originals they replace.

**Reproduce.** Port any production Compose screen with:
- `Modifier.alpha(0.1f)` → no schema equivalent
- `Modifier.offset(x = 12.dp, y = -12.dp)` → no schema equivalent
- `BorderStroke(1.dp, Color.X.copy(alpha = 0.2f))` on a `Card` → no
  `border` param on `schema.compose.Card`
- `TextStyle(fontFamily = FontFamily.Serif, color = Color(0xFF7A1F1F))`
  → enum-based `SchemaTextStyle` + `SchemaColor` can't carry custom
  hex or family

DevoStatus's `QuotesScreen` is the case study — see the comparison
table in `KONDUIT_INTEGRATION_REPORT.md` (DevoStatus side).

**Workaround.** Approximate the look using available widgets (smaller
inline icons in place of corner watermarks, default fonts, theme
colors). Accept ~80% visual fidelity. Or fall back to a native screen
for the highest-fidelity surfaces.

**Upstream fix.** Add to the schema:
1. `SchemaModifier.Alpha(value: Float)` — directly maps to
   `Modifier.alpha`. Cheapest change with highest visual return.
2. `SchemaModifier.Offset(x: Int, y: Int)` — maps to
   `Modifier.offset(x.dp, y.dp)`. Unlocks decorative absolute placement.
3. `Card.border: SchemaBorderStroke?` — new optional param. Already
   trivially mapped (M3 `Card` accepts `border`).
4. Optional: `SchemaFontFamily` enum (Default, Serif, Monospace) or a
   `SchemaTypography` schema-derived from M3's typography slots. Carries
   custom typefaces through theme rather than per-call.

---

## 5. Coil 3's singleton `ImageLoader` has no network fetcher by default

**Severity:** low (already documented in USAGE.md), but listing here
because the failure mode is silent.

**Symptom.** A schema `AsyncImage` with an `http://…` URL renders blank.
No exception, no log line. Looks like the schema widget is broken.

**Cause.** Coil 3 ships with an empty default `ImageLoader`. The
`coil-network-okhttp` (or `coil-network-ktor3` / `coil-network-ktor2`)
artifact adds a network fetcher, but the integrator has to call
`setSingletonImageLoaderFactory { … }` before any `AsyncImage` is
composed.

**Workaround.** See `USAGE.md` "⚠️ If you use AsyncImage" callout.

**Upstream fix.** Konduit could supply a default
`setSingletonImageLoaderFactory` call from within
`TreehouseApp`'s composeui wrapper, with an `okhttp` fetcher when the
integrator's classpath has OkHttp (detect at build time, or fall back
to a no-op + clear warning). Or document this in a way that's
impossible to miss — a startup-time println if the singleton hasn't
been initialized would help.

---

## 6. `coil-network-ktor2` conflicts at runtime with apps that use Ktor 3

**Severity:** low (DevoStatus-specific, but generalizable).

**Symptom.** App that uses Supabase 3.x (which pulls in Ktor 3) +
Konduit (which historically pulls in `coil-network-ktor2`) crashes at
first `AsyncImage` load with `NoClassDefFoundError` on
`io.ktor.utils.io.jvm.nio.WritingKt`.

**Workaround.** Switch to `coil-network-okhttp` on Android (already done
in `:konduit-host`). For iOS, switch to `coil-network-ktor3`.

**Upstream fix.** Konduit's `:composeApp` should prefer
`coil-network-okhttp` over the ktor2 variant — OkHttp is already a
required Android-side dep for the Zipline HTTP loader, so it adds no
new transitive weight. For iOS, switch to `ktor3` matching the rest of
the iOS-side networking story.

---

## 7. `TreehouseApp.Spec` services held as anonymous inline references get GC'd

**Severity:** medium (documented in code as "gotcha #6", but trips
every new integrator on the first try).

**Symptom.** Inline `bind` such as
`zipline.bind<HostConsole>("console", object : HostConsole { … })`
binds successfully, then the first guest call to the service errors
with `no such service (service closed?)`.

**Cause.** Konduit's leak detector logs `serviceLeaked` events
("invoked when a service is garbage collected without being closed").
The anonymous instance becomes GC-eligible the moment `bindServices`
returns; the host's underlying weak reference gets cleared before the
guest's first call.

**Workaround.** Hold each service as a `val` or `lateinit var` property
of the `Spec` (its lifetime survives the GC pressure that anon
instances don't):

```kotlin
val spec = object : TreehouseApp.Spec<…>() {
    private val hostConsole = MyHostConsole()   // ← strong ref
    private lateinit var hostSnackbar: RealHostSnackbar

    override suspend fun bindServices(...) {
        zipline.bind<HostConsole>("console", hostConsole)
        hostSnackbar = RealHostSnackbar(...)
        zipline.bind<HostSnackbar>("snackbar", hostSnackbar)
    }
}
```

**Upstream fix.** `TreehouseApp.Spec` could hold a strong-ref map of
bound services internally — the API user shouldn't have to know about
the leak detector's quirks. Or `Zipline.bind` could itself hold a
strong ref until `Zipline.close()`.

---

## 8. Host `ZiplineService` method bodies execute on the Zipline dispatcher, not Main — UI touches silently no-op

**Severity:** high (silent failure on Android, possible crash on iOS K/N).
**Counterpart to gotcha #12** in `docs/HANDOVER.md`, which documents the
*outbound* direction. This entry covers the *inbound* direction.

**Symptom.** A guest call to a host `ZiplineService` method routes correctly
(host log line fires with the expected arguments) but the host-side side
effect — `NavController.navigate(...)`, `viewModel.someState = ...`, etc. —
never takes effect. The UI just doesn't react. There's no exception, no
warning. The handler runs to completion and exits silently.

**Reproduce.** Define any guest→host service whose method body touches the
UI or Compose state:

```kotlin
interface HostNavigator : ZiplineService {
    fun onItemSelected(id: String)
}

private class RealHostNavigator(
    private val navController: NavController,
) : HostNavigator {
    override fun onItemSelected(id: String) {
        Log.d("MyApp", "onItemSelected($id)")  // ← fires
        navController.navigate("detail/$id")    // ← silent no-op
    }
}
```

The log line appears. The navigation doesn't happen. The user sees nothing.

**Cause.** Zipline runs the host method body on its own thread-confined
dispatcher (the QuickJS thread, `treehouseApp.dispatchers.zipline`).
`NavController.navigate` requires the Main thread, as does any Compose
`MutableState` mutation. Calling them off-Main is a known Android
silent-failure pattern.

JVM-backed Zipline (Android) tolerates the wrong-thread state mutation
quietly. iOS Kotlin/Native may crash (we haven't reproduced this one on
iOS yet, but the symmetric outbound case in gotcha #12 does).

**Workaround.** Take a `CoroutineScope` in the service's constructor,
launch the side effect onto Main:

```kotlin
private class RealHostNavigator(
    private val scope: CoroutineScope,
    private val navController: NavController,
) : HostNavigator {
    override fun onItemSelected(id: String) {
        Log.d("MyApp", "onItemSelected($id)")
        scope.launch(Dispatchers.Main) {
            navController.navigate("detail/$id")
        }
    }
}
```

Pass `activity.lifecycleScope` (Android) or a Main-dispatcher scope
(iOS) at construction. This is the *symmetric pair* to gotcha #12: the
inbound host method needs Main, the outbound guest-proxy call needs
the Zipline dispatcher.

**Upstream fix.** Three options, ranked by impact:

1. **`@MainThread` annotation honored by Zipline codegen** — let the
   integrator mark a service method as needing Main, and the generated
   host stub does the dispatch hop. Lowest-friction.
2. **`TreehouseApp.dispatchers.main`** — expose a Main dispatcher
   alongside `dispatchers.zipline` so integrators have a sanctioned way
   to switch, rather than reaching for `Dispatchers.Main` directly.
3. **Doc + lint** — at minimum, document this in `USAGE.md` next to
   gotcha #12 so the two directions appear as a pair. The current docs
   only cover outbound.

**Real-world incidence.** Bit DevoStatus's "Tap to create status"
button on the Quotes tab — `HostQuoteNavigator.onQuoteSelected` fired
on every tap, the host's callback received the right ID, but
`navController.navigate(...)` did nothing. Took a logcat audit to
confirm the call was reaching the host before realizing it was a
threading bug rather than a wiring bug.

---

## 9. ~~`lateinit var` services inside `bindServices` silently skip binding on 2nd `TreehouseApp` mount~~ — NOT a Konduit bug. Root cause: integrator's `remember(...)` keying on unstable lambdas.

**Severity:** high when present (silent failure of host-side service
binding).
**Status:** root cause PINNED — this was never a Konduit bug. The fix
lives entirely in the integrator's Composable.

**The real story.** When this gotcha was first observed, every recompo-
sition was creating a new `TreehouseApp` instance, and the "second
mount" with the lateinit-var pattern silently dropped service binding.
Both directions — "Konduit is caching" and "lateinit var inside
bindServices is bad" — were hypotheses; both were wrong.

Actual root cause: the integrator's Composable held the TreehouseApp
in a `remember(activity, quotesSource, onQuoteSelected, quotesFlow) {
createTreehouseApp(...) }`. `quotesSource` and `onQuoteSelected` were
anonymous lambdas at the call site, which means they got a new
identity on every recomposition. `remember`'s key list saw "new keys"
and invalidated, calling the factory block again — building a brand
new TreehouseApp with a brand new Spec. The OLD TreehouseApp was
still alive (no one closed it) and shared the same `appScope` and
`manifestUrlFlow`. The two specs raced for the Zipline runtime; the
later spec's bindServices either no-op'd or its log statements went
to a thread whose stdout never made it to logcat.

**The fix (integrator-side).** Make the `remember` key list stable by
wrapping unstable lambdas in `rememberUpdatedState`, then pass thin
adapter lambdas (declared once inside `remember`) that delegate to the
always-current State:

```kotlin
@Composable
fun MyScreen(
    sourceLambda: (Filter) -> Data,         // unstable identity per recomposition
    callbackLambda: (Result) -> Unit,       // same
    stableFlow: Flow<…>,                    // stable (caller used remember)
) {
    val currentSource by rememberUpdatedState(sourceLambda)
    val currentCallback by rememberUpdatedState(callbackLambda)
    val treehouseApp = remember(activity, stableFlow) {           // ← stable keys only
        createTreehouseApp(
            source = { filter -> currentSource(filter) },          // ← stable adapter
            callback = { result -> currentCallback(result) },      // ← stable adapter
            flow = stableFlow,
        )
    }
    // … TreehouseContent(treehouseApp, …)
}
```

Net effect: exactly **one** `TreehouseApp` per Composable lifetime,
even across hundreds of recompositions. The lateinit-var-in-bindServices
pattern (per gotcha #12 outbound dispatch) then works fine — verified
on DevoStatus commit `<bump>` after the fix.

**Why this is gotcha-list-worthy even though it's not a Konduit bug.**
The two related gotchas (lateinit-var-in-bindServices for #12 outbound
dispatch + unstable remember keys for screen wiring) form a pair where
either alone is fine but TOGETHER they create a silent failure mode
that's nearly impossible to debug from logs. New integrators following
RealHostSnackbar's lateinit-var pattern get tripped if they also keyed
their `remember` on unstable lambdas. The right fix is documenting the
pairing, not changing the pattern.

**Where Konduit could help (still worth doing).**

1. **`TreehouseApp.Spec.bindServices` log instrumentation.** When an
   integrator's bindServices is called the second time on the same
   process, Konduit could detect it (or detect a previous-Spec leak
   in the same appScope) and log a warning. Right now the failure is
   diagnostically silent.
2. **`USAGE.md` Step 2 callout.** Add a "remember stability"
   subsection right after the Spec example — link forward to
   `rememberUpdatedState` and provide the stable-adapter pattern as
   the canonical shape.
3. **A `rememberTreehouseApp { ... }` Compose helper.** Internalize
   the stable-key contract in the API surface itself, so integrators
   can't get it wrong.

---

## Process

When you fix one of these:

1. Write a regression test in `:shared` (or wherever the bug surfaces)
   that fails on master and passes with the fix.
2. Move the section to `docs/CHANGELOG.md` under the next-released
   version with the fixing commit hash.
3. Open a corresponding GitHub issue + link to the commit so external
   integrators can find it via search.
