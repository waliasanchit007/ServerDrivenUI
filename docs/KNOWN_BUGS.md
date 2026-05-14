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

## Process

When you fix one of these:

1. Write a regression test in `:shared` (or wherever the bug surfaces)
   that fails on master and passes with the fix.
2. Move the section to `docs/CHANGELOG.md` under the next-released
   version with the fixing commit hash.
3. Open a corresponding GitHub issue + link to the commit so external
   integrators can find it via search.
