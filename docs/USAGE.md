# Using Caliclan in another Compose Multiplatform project

This guide is for "I have a separate Compose Multiplatform app and I want one
or more of its screens to be Server-Driven UI." It walks through vendoring
Caliclan, the minimum host-side boilerplate, defining a new screen on the
guest side, and the gotchas that bite.

If you're working on Caliclan itself, read `HANDOVER.md` and `KONDUIT_PLAN.md`
instead — this doc assumes you're a downstream consumer.

---

## TL;DR

```bash
# 1. Vendor Caliclan as a git submodule in your project root
git submodule add https://github.com/waliasanchit007/ServerDrivenUI third_party/caliclan

# 2. Include its Gradle modules from your settings.gradle.kts
includeBuild("third_party/caliclan")  # or include() individual modules

# 3. Add a KONDUIT_READ_TOKEN to your CI / local gradle.properties so the
#    Konduit Maven artifacts resolve (classic PAT, read:packages scope)

# 4. Set up a TreehouseApp.Spec in your activity / view controller (see below)

# 5. Write a guest .kt file that extends `Screen` and use the schema widgets
```

Production-readiness reality check before you commit to this path:

- **Android**: solid. End-to-end verified, hot reload works.
- **iOS**: solid as of 2026-05-12 (gotcha #12 fixed). Verified on iPhone 17 Pro sim.
- **Library distribution**: not yet on Maven Central. Git submodule is your only option today.
- **Compose Facade**: deferred. You'll import from 6+ modules (`shared`, `shared-widget`, `shared-protocol-host`, etc.). Lives with this for now.
- **Production load**: only showcase-level traffic tested. No 60 Hz Flow / large-list stress runs yet.

---

## Architecture, in 30 seconds

```
┌─────────────────┐    Zipline RPC over QuickJS    ┌─────────────────┐
│  Your host app  │ ◄────────────────────────────► │  Guest .zipline │
│  (Android/iOS)  │                                 │  (compiled JS)   │
│                 │                                 │                  │
│ TreehouseApp +  │     widget-tag protocol         │  Tier1ShowcaseS- │
│ M3 widgets +    │     (kotlinx-serialization)     │  creen, your     │
│ HostX services  │                                 │  screens, etc.   │
└─────────────────┘                                 └─────────────────┘
```

- **Host** holds the schema-host protocol, the M3 widget implementations, and
  any "HostX" services (HostConsole, HostSnackbar, anything you bind via
  `zipline.bind`).
- **Guest** is a Kotlin/JS module that emits a `.zipline` bundle. The guest
  uses generated `schema.compose.*` widgets which look exactly like normal
  Compose widgets but emit protocol messages instead of rendering directly.
- **Dev server** (in this repo's `dev-server/` module) serves the latest
  `.zipline` bundle over HTTP + hot-reload over WebSocket.

The host never imports guest code; the guest never imports host code. They
communicate through the schema (in `schema/`) and the generated protocol
modules.

---

## Step 1 — Vendor Caliclan

Easiest path is a git submodule:

```bash
git submodule add https://github.com/waliasanchit007/ServerDrivenUI third_party/caliclan
git submodule update --init --recursive
```

In your top-level `settings.gradle.kts`:

```kotlin
includeBuild("third_party/caliclan") {
    dependencySubstitution {
        substitute(module("com.example.serverdrivenui:shared"))
            .using(project(":shared"))
        substitute(module("com.example.serverdrivenui:shared-widget"))
            .using(project(":shared-widget"))
        // ...repeat for shared-protocol-host, schema-types, etc.
    }
}
```

Or, simpler, just include the modules directly:

```kotlin
include(":shared")
project(":shared").projectDir = file("third_party/caliclan/shared")
// ...repeat for every module you need
```

Modules you'll need on the host side:

| Module                    | Why                                                  |
|---------------------------|------------------------------------------------------|
| `shared`                  | HostConsole, HostSnackbar, SduiAppService            |
| `shared-widget`           | M3 widget implementations (Box, Column, Button, ...) |
| `shared-protocol-host`    | `SduiSchemaHostProtocol.Factory` for TreehouseApp    |
| `shared-modifier`         | Generated modifier interfaces                        |
| `schema-types`            | Color/typography enums + serializers module          |
| `composeApp` (optional)   | Use as-is if you want the full hosting plumbing      |

For the guest side, you'll need a fresh Kotlin/JS module similar to
`presenter/`. See Step 4.

### KONDUIT_READ_TOKEN

The host modules depend on `dev.konduit:konduit-*` artifacts hosted on
GitHub Packages in the private `waliasanchit007/konduit` repo. You need a
**classic** GitHub PAT (fine-grained PATs don't work — GitHub Packages
Maven only accepts classic) with `read:packages` scope.

Local: `~/.gradle/gradle.properties`:
```
gpr.user=your-github-username
gpr.token=ghp_your_classic_pat_here
```

CI: set `GITHUB_ACTOR` + `GITHUB_TOKEN` env vars on the runner. See this
repo's `.github/workflows/ci.yml` for an example token-probe step.

---

## Step 2 — Host boilerplate (Android)

Minimum `MainActivity.kt` to hoist a guest-driven screen:

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var treehouseApp: TreehouseApp<SduiAppService>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = TreehouseAppFactory(
            httpClient = OkHttpZiplineHttpClient(OkHttpClient()),
            manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
            embeddedFileSystem = null,
            embeddedDir = null,
            cacheName = "zipline",
            cacheMaxSizeInBytes = 50L * 1024L * 1024L,
            concurrentDownloads = 8,
            stateStore = MemoryStateStore(),
            leakDetector = LeakDetector.none(),
            hostProtocolFactory = SduiSchemaHostProtocol.Factory,
        )

        val spec = object : TreehouseApp.Spec<SduiAppService>() {
            override val name = "myapp"
            override val manifestUrl = MutableStateFlow("https://your-cdn/manifest.zipline.json")
            override val serializersModule = SduiSerializersModule

            // Hold strong refs to host services so they don't GC out from
            // under Zipline. See HANDOVER gotcha #6.
            private val hostConsole = AndroidRealHostConsole()
            private lateinit var hostSnackbar: RealHostSnackbar

            override suspend fun bindServices(
                treehouseApp: TreehouseApp<SduiAppService>,
                zipline: Zipline,
            ) {
                zipline.bind<HostConsole>("console", hostConsole)
                // Construct RealHostSnackbar with the zipline-confined
                // dispatcher — required, see HANDOVER gotcha #12.
                hostSnackbar = RealHostSnackbar(treehouseApp.dispatchers.zipline)
                zipline.bind<HostSnackbar>("snackbar", hostSnackbar)
            }

            override fun create(zipline: Zipline) = zipline.take<SduiAppService>("app")
        }

        treehouseApp = factory.create(lifecycleScope, spec)

        setContent {
            MaterialTheme {
                TreehouseContent(treehouseApp, source = SduiContentSource)
            }
        }
    }
}
```

iOS is structurally identical — see `composeApp/src/iosMain/.../MainViewController.kt`
in this repo for the iOS-flavored Spec.

---

## Step 3 — Wire the SnackbarHost (host side)

If you bind `HostSnackbar`, you also need to anchor a `SnackbarHost`
somewhere in your Compose tree to render the actual snackbars. The
`RealHostSnackbar` writes into a singleton `SnackbarHub.state`; anchor
once at the root:

```kotlin
@Composable
fun App(treehouseApp: TreehouseApp<SduiAppService>) {
    Box(modifier = Modifier.fillMaxSize()) {
        TreehouseContent(treehouseApp, source = SduiContentSource)
        SnackbarHost(
            hostState = SnackbarHub.state,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
```

---

## Step 4 — Define a guest screen

Spin up a new Kotlin/JS module modeled on `presenter/`:

```kotlin
// presenter/src/jsMain/kotlin/yourapp/screens/MyScreen.kt
class MyScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        Column(modifier = Modifier.padding(16, 16, 16, 16)) {
            Text(text = "Hello from the server", style = SchemaTextStyle.HeadlineMedium)
            Button(
                text = "Show snackbar",
                onClick = {
                    showHostSnackbar(
                        message = "Tapped from guest!",
                        actionLabel = "Undo",
                        onResult = { undone -> println("undone=$undone") },
                    )
                },
            )
        }
    }
}

// In your main.kt:
fun main() {
    val zipline = Zipline.get()
    val app = StandardAppLifecycle.create(
        json = Json { serializersModule = SduiSerializersModule },
        // ... screens registry
    )
    zipline.bind<SduiAppService>("app", app)
}
```

The guest-side ergonomics for snackbar / navigation / console are in
`presenter/src/jsMain/.../Main.kt` and `presenter/src/jsMain/.../Navigator.kt`.
Copy-paste the helpers you need.

---

## Step 5 — Dev loop

The dev server in this repo (`dev-server/`) serves the latest `.zipline`
bundle and pushes hot-reload notifications. To run:

```bash
./gradlew :dev-server:run
# or, for the full one-shot dev experience:
./gradlew konduitDev
```

Your host app's `manifestUrl` should point at the dev server (`http://10.0.2.2:8080/manifest.zipline.json`
for Android emulator, `http://127.0.0.1:8080/...` for iOS sim with adb
reverse, or an ngrok URL for physical devices).

On each guest source change, recompile:
```bash
./gradlew :presenter:compileDevelopmentExecutableKotlinJsZipline
```
…and the dev server pushes a hot-reload event over WebSocket. The host
re-fetches and swaps in the new bundle without restarting.

---

## Gotchas you should internalize before writing your first screen

These are pulled from `HANDOVER.md`'s gotcha list — read that doc for full
detail. The TL;DR:

1. **Add new enums to `SduiSerializersModule`** if you use them as a field
   on a `@Modifier`. Otherwise the whole protocol batch silently fails and
   you get a white screen on the host.
2. **No lambda-typed properties on `@Modifier` classes.** Click handlers
   live on widgets, never modifiers. The codegen will accept a lambda
   modifier property at compile time and silently fail at runtime on JS.
3. **Outbound calls to `ZiplineService` proxies MUST happen on
   `treehouseApp.dispatchers.zipline`.** This includes `callback.onResult`,
   `callback.close()`, `flow.value`. Wire the dispatcher into your HostX
   service constructor (see `RealHostSnackbar`'s ctor) and `withContext`
   around proxy touches. JVM tolerates the wrong thread by luck; iOS K/N
   crashes with `QuickJsException: stack overflow`.
4. **Hold strong refs to bound host services** as `lateinit var` properties
   of your `TreehouseApp.Spec`. Anonymous inline arguments to `zipline.bind`
   GC out from under Zipline and the next guest call errors with "no such
   service".
5. **Use the right manifest URL per platform.** Android emulator → `10.0.2.2`,
   iOS sim → `127.0.0.1` with `adb reverse` (or ngrok HTTPS for physical
   devices). iOS additionally needs `NSExceptionDomains` in `Info.plist`
   for IP-literal URLs.

The full list of 12 gotchas is in `HANDOVER.md` §"Gotchas / requirements
that aren't obvious". Read it before debugging anything mysterious.

---

## When something breaks

1. **Blank screen on host, no exception**: 95% of the time it's gotcha #10
   (an enum on a `@Modifier` not registered in `SduiSerializersModule`).
   Check the guest console (host → `HostConsole.log`) for SerializationException.
2. **"no such service (service closed?)"**: gotcha #11. You're binding
   inline; hold a strong ref in your Spec.
3. **`QuickJsException: stack overflow` on iOS**: gotcha #12. An outbound
   call to a ZiplineService proxy happened off the zipline dispatcher.
4. **App builds but `take<T>("foo")` returns a null bridge**: your guest
   uses a raw lambda-typed parameter on a ZiplineService method. Wrap it
   in a `ZiplineService` callback type (see `SnackbarResultCallback`).

For anything else, search `HANDOVER.md`'s gotchas list with the failure
mode keywords (the section was written to be greppable).

---

## What's NOT yet in this distribution

If you need any of these, expect to either help land them or work around
them:

- **No Compose Facade**: you import from 6+ modules. Phase 4 in
  `KONDUIT_PLAN.md` — deferred until onboarding pain justifies it.
- **No Maven Central publishing**: submodule only for now.
- **No production load testing**: only showcase-level traffic verified.
- **iOS verified only on sim**: physical iOS devices not yet tested
  end-to-end on this branch.
- **No StateFlow / Flow patterns demonstrated**: would need the same
  dispatcher pattern as gotcha #12. Pattern works but no example yet.
