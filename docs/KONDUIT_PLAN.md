# Konduit — Plan v2

> Single source of truth for the Konduit library and the Caliclan migration.
> Replaces the v1 plan at `~/Downloads/KONDUIT_PLAN.md`.
>
> No time estimates. Phases are gated by deliverables, not deadlines.

---

## 1. Mission

Konduit is Caliclan's private fork of CashApp Redwood 0.18.0 + Zipline. It
ships executable Kotlin screen logic to a Compose Multiplatform app without
an app-store update. Konduit is the "browser" (host primitives + Zipline
runtime); the **guest** is per-screen Kotlin compiled to `.zipline` and
served from a CDN.

**The boundary that matters:**
- **Guest** is platform-agnostic Kotlin/JS — produces UI trees, never calls
  platform APIs.
- **Host** is per-platform (Compose Multiplatform today; native iOS/Android
  later). Implements `Cmp*` widgets in the local toolkit.

The contract between them is Konduit's schema (immutable widget IDs, additive
property changes only within a major version).

---

## 2. Locked-in decisions

| Decision | Choice | Rationale |
|---|---|---|
| **Fork base** | CashApp Redwood `0.18.0` | Final stable release |
| **Fork posture** | Soft fork in name only — upstream is dormant | We will not actively track upstream; merges only if a critical Zipline-related fix lands |
| **Maven group** | `dev.konduit` | Already published, working |
| **Version scheme** | `MAJOR.MINOR.PATCH-caliclan.N` | `1.0.0-caliclan.1` is the current floor |
| **Distribution** | GitHub Packages (pre-release + stable). mavenLocal for dev | Private repo, simple auth via PAT |
| **Wire-format promise** | Host `vMAJOR.x.y-caliclan.N` runs all guests built against the same MAJOR | Major bump = no compat promise |
| **Manifest signing** | Optional, not mandatory in production | Per Caliclan choice; security risk documented |
| **Guest pinning per app** | Yes — manifest URLs include version (`/manifests/v23/manifest.zipline.json`) | Old app binaries lock to known-good guest versions |
| **Test policy** | No new tests written. Upstream snapshot tests preserved (1880 LFS PNGs); may be re-enabled later without code changes | User decision |
| **Plan ordering** | 0 → 1 → 1.5 → 2 → 5a/5d → 3 → 4 → 5b/5c → 6-as-principles | Cleanup before CI; dev tooling slices before the schema slog |

### Deferred — explicitly scheduled in this plan

- **Strip non-CMP modules** (`konduit-widget-view/uiview/dom`,
  `konduit-layout-view/uiview/dom`, equivalent lazylayout/UI-basic variants)
  → Phase 1.5
- **Class-name rename** (`RedwoodPlugin → KonduitPlugin`, ~79 identifiers)
  → Phase 1.5 (or never; cosmetic only, no functional impact)
- **GitHub Packages publishing pipeline** → Phase 2
- **CHANGELOG cadence** → Phase 2

### Out of scope (forever, unless reversed)

- Maven Central publishing (only if Konduit is open-sourced)
- Native iOS / native Android hosts (architecture preserved per
  `WIDGET_CONTRACT.md`, but no host implementation planned)
- WASM migration (Zipline 2.0) — wait for upstream Zipline to ship it

---

## 3. Engineering principles

These apply across all phases. Treat them as invariants.

### 3.1 Wire-format compatibility

**Promise:** Within a given MAJOR version, the host is forward-compatible
with all older guests built against that same MAJOR.

Concretely: host `1.0.0-caliclan.5` runs any guest built against any
`1.0.0-caliclan.[1..5]`. A guest built against `caliclan.7` running on a
host at `caliclan.5` is undefined behavior — the host should fail gracefully
(see §3.5 missing-widget policy) but is not required to render.

**To preserve this within MAJOR:**
- Widget IDs are immutable. Once assigned, never reused, never renumbered.
- New widgets only appear at unused IDs.
- Properties on existing widgets can only be **added**, never removed,
  retyped, or renamed. Added properties must have defaults the host
  applies when the guest doesn't set them.
- Events on existing widgets can only be added, never removed.
- Generated code in `konduit-protocol-host` and `konduit-protocol-guest`
  must default-skip unknown properties (Redwood's protocol already does
  this; preserve it).

**Major bump (`2.0.0-caliclan.1`)** is the only release type that may
break wire format. Triggered by: removing a widget, removing a property,
retyping a property, changing event signatures, changing modifier
serialization.

### 3.2 Versioning

| Bump | Trigger |
|---|---|
| `1.0.0-caliclan.N → caliclan.N+1` | Any non-breaking change: new widget at unused ID, new property with default, new modifier, internal refactor, dependency bump within compatible range, doc change |
| `1.0.0 → 1.1.0-caliclan.1` | Reserved — currently unused; minor bumps fold into `caliclan.N` for now |
| `1.x → 2.0.0-caliclan.1` | Any breaking change (see §3.1) |

**Hotfix branches:** From any released tag, branch `hotfix/X.Y.Z-caliclan.N`,
fix, tag `X.Y.Z-caliclan.N+0.1` style is overkill — just bump `caliclan.N+1`
and merge back to main. We are not on main 90% of the time, so this is fine.

**CHANGELOG.md** at repo root. Every release tag has a section. Format:
```
## 1.0.0-caliclan.2 — YYYY-MM-DD
### Added
- Widget(46) RangeSlider
### Changed
- ...
### Fixed
- ...
```

### 3.3 Schema governance

**Widget ID registry:** `docs/WIDGET_REGISTRY.md` is the single source of
truth. Maintained as a numbered table. Every PR that adds a widget MUST
update the registry in the same commit.

**ID ranges:**
| Range | Owner | Purpose |
|---|---|---|
| 1–99 | Konduit | Tier 1 + 2 widgets (foundation + core M3) |
| 100–199 | Konduit | Tier 3 widgets (extended M3) |
| 200–999 | Reserved | Future Konduit growth |
| 1000+ | Caliclan / consumers | App-specific domain widgets |

We claim 1000+ (instead of v1's 200+) so an unused upstream Redwood ID
collision is impossible — Redwood never went past ~30 widgets in 0.18.0.

**Deprecation:** `@Widget(45) @Deprecated("Use Widget(46) instead")` keeps
the ID forever. Renderer keeps working. Eventual removal only at major bump.

**Caliclan vs Konduit boundary:**
- Konduit ships **schema-defined widgets** (IDs 1–999) but Caliclan declares
  them in its own `schema/Schema.kt`. This means Caliclan currently owns
  widget definitions; Konduit only ships the gradle plugin + protocol +
  facade. **Decision:** keep this. Konduit becoming a self-contained
  primitive library is a Phase 6 north-star, not a deliverable.

### 3.4 Distribution + publishing

**Where artifacts live:**
| Repo | Purpose | Auth |
|---|---|---|
| `~/.m2/repository/dev/konduit/` (mavenLocal) | Active dev | None |
| `https://maven.pkg.github.com/waliasanchit007/konduit` | All published versions (pre-release + stable) | GitHub PAT with `read:packages` for consumers, `write:packages` for publishers |

**Publishing trigger (Phase 2):** publish on **git tag matching `v*`**, NOT
on every push to main. WIP merges to main don't ship.

**Tag → version mapping:** `git tag v1.0.0-caliclan.2` publishes
`dev.konduit:konduit-*:1.0.0-caliclan.2` (strip the `v` prefix).

**Local dev workflow stays:** every consumer (Caliclan today, future
projects) has `mavenLocal()` first in repo order. Active Konduit changes
go to mavenLocal via `./gradlew publishToMavenLocal -x test
-DRELEASE_SIGNING_ENABLED=false` and Caliclan picks them up immediately.

### 3.5 Missing-widget / unknown-property policy (host-side)

When the host sees a widget ID it doesn't know (guest built against newer
host), it logs an error and renders a placeholder Box (debug builds: red
"unknown widget N" box; release: empty Box). Never crashes.

When the host receives a property it doesn't recognize, it ignores the
property silently. Already Redwood's default — preserve.

When the host receives an event from the guest it can't dispatch (widget
gone), drop the event. Log in debug.

### 3.6 Forking ops

**Upstream remote:** keep `upstream =
https://github.com/cashapp/redwood.git` configured locally for ad-hoc LFS
fetches and reference. **Do not auto-merge upstream.** No periodic sync
ritual.

**If upstream ships a critical Zipline fix:** evaluate, cherry-pick the
specific commit, version-bump as a `caliclan.N+1`. Treat upstream as a
reference, not a pipeline.

### 3.7 Caliclan-side compatibility for production guests

**Guest version pinning:** the manifest URL in DevConfig today is
unversioned (`/manifest.zipline.json`). For production:
- CDN serves manifests at `/manifests/<version>/manifest.zipline.json`.
- Caliclan's `composeApp` BuildConfig records the host's Konduit version
  at build time.
- App constructs the URL: `https://cdn.caliclan.dev/manifests/${BuildConfig.KONDUIT_VERSION}/manifest.zipline.json`.
- Old app binaries always pull a guest manifest pinned to their host's
  Konduit version → impossible for new guests to break old apps.

**Cached fallback:** Zipline's loader already caches the last successful
manifest. If the latest fetch fails, fall back to cached. Document this
as a host responsibility.

**Wire-compat broken outright:** if Caliclan must ship a major bump
(`2.0.0-caliclan.1`), it ships a new app binary AND switches to
`/manifests/v2/...` URL. Old binaries continue serving from
`/manifests/v1/...` indefinitely.

### 3.8 Security posture (signing optional)

Per locked-in decision, manifest signing is **not mandatory.** Risk
acknowledged: a compromised CDN or DNS can serve arbitrary code into
the app binary's QuickJS.

**Mitigations baked into the plan even without mandatory signing:**
- HTTPS-only manifest URLs in release builds. Enforced via
  `BuildConfig.DEBUG ? allow http : require https` check in
  `KonduitProvider`.
- Cert pinning **not required** but recommended once a stable CDN is
  chosen. Out of scope for this plan.
- `freshAtEpochMs` field on the manifest is populated by Zipline. Anti-
  rollback (rejecting stale manifests) is wireable later as a host hook.

If Caliclan's threat model changes, signing can be flipped on without
schema changes — Ed25519 keys + the `signatures` field in the manifest are
already supported by upstream Zipline.

### 3.9 Operations (minimal, not a phase)

These are ongoing rituals once Phase 2 lands:

- **Telemetry on guest load.** Wire `EventListener` (already in
  `MainActivity.kt:25` host-side) to whatever observability stack
  Caliclan adopts. Out of scope for this plan but a known TODO.
- **Manifest URL strategy** as in §3.7 — no daily ops, just a CDN
  convention.
- **Build cache.** Phase 2 configures Gradle remote build cache to keep
  CI reasonable (currently `publishToMavenLocal` is 6 minutes from
  scratch).

---

## 4. Phase plan

Phases are gated by deliverables. Each phase ends with a verification
checklist that must pass before the next starts.

### ✅ Phase 0 — Inventory (DONE)
**Deliverable:** `docs/PHASE_0_INVENTORY.md` — versions, modules, schema
widgets, Cmp* implementations, screens, concerns.

### ✅ Phase 1 — Fork + repoint (DONE)
**Deliverable:** Caliclan running on `dev.konduit:1.0.0-caliclan.1` from
mavenLocal, end-to-end verified on Android + iOS (Welcome screen renders
on both).

What landed: 4 commits in `~/AndroidStudioProjects/konduit` (+ pushed to
`https://github.com/waliasanchit007/konduit`); 1 commit in Caliclan
(`cbbc349` on `claude/vigilant-euclid-681447`).

### Phase 1.5 — Cleanup
**Why now:** before CI infrastructure builds on top of modules we'll
delete.

**Tasks:**
1. **Strip non-CMP modules from konduit fork:**
   - `konduit-widget-view`, `konduit-widget-uiview`, `konduit-widget-dom`
   - `konduit-layout-view`, `konduit-layout-uiview`, `konduit-layout-dom`
   - `konduit-lazylayout-view`, `konduit-lazylayout-uiview`,
     `konduit-lazylayout-dom`
   - `konduit-ui-basic-view`, `konduit-ui-basic-uiview`,
     `konduit-ui-basic-dom`
   - Test fixtures and snapshot folders for the above
   - Remove from `settings.gradle.kts` and any `build.gradle` references
   - Drop unused LFS PNGs (snapshot tests for stripped modules) — clone
     size goes from 153MB → ~80MB
2. **Class-name rename (optional, can defer to never):**
   - `RedwoodPlugin → KonduitPlugin` and ~78 sibling identifiers
   - Pure cosmetic; no functional impact. Skip if it doesn't bother you.
3. **Re-publish to mavenLocal as `1.0.0-caliclan.2`** (bump from .1).
4. **Bump Caliclan's `redwood = "1.0.0-caliclan.2"`** in
   `libs.versions.toml`. Re-verify Android + iOS run.

**Deliverable:** Konduit fork has only CMP-relevant modules. Caliclan
runs on `caliclan.2`. Both platforms verified.

**Verification checklist:**
- [ ] `./gradlew :androidApp:assembleDebug` succeeds in Caliclan
- [ ] `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` succeeds
- [ ] App launches on Android device, renders welcome screen
- [ ] App launches on iOS sim, renders welcome screen
- [ ] `du -sh ~/AndroidStudioProjects/konduit/.git` shows reduced size

### Phase 2 — CI + publishing infrastructure
**Goal:** Publishing is one `git tag && git push --tags` away. Builds
auto-validate on PR.

**Tasks:**
1. **PR-check workflow** (`.github/workflows/ci.yml` in konduit repo):
   - Linux runner for non-iOS jobs (build, test, lint)
   - macOS runner ONLY for iOS-specific tasks, gated to PRs that touch
     iOS or to main pushes
   - Cache `~/.gradle` and `.gradle` between runs
2. **Publish-on-tag workflow** (`.github/workflows/publish.yml`):
   ```yaml
   on:
     push:
       tags: ['v*']
   ```
   Reads version from tag, publishes to GitHub Packages with the repo's
   built-in `GITHUB_TOKEN`.
3. **Kotlin compatibility matrix** (`.github/workflows/compat-matrix.yml`):
   - Weekly cron
   - Override mechanism: add `kotlin = providers.gradleProperty("kotlin.version").orElse("2.1.0").get()` reading in `libs.versions.toml`
     extension (Konduit's build-support plugin), so
     `-Pkotlin.version=2.2.0` actually overrides
   - Test against Kotlin `2.1.0`, `2.1.20`, `2.2.0`
   - Failure is informational, doesn't block
4. **Gradle remote build cache:** configure `~/.gradle/init.gradle` or
   `gradle.properties` to use GitHub Actions cache as the remote backend.
5. **Default empty signatory** for `signJsPublication`:
   - Add `signing { isRequired = providers.systemProperty("RELEASE_SIGNING_ENABLED").orElse("true").map { it.toBoolean() } }` so
     `publishToMavenLocal` works without flags. (Today we pass
     `-DRELEASE_SIGNING_ENABLED=false` manually — bake in the default.)
6. **CHANGELOG.md** at konduit root, with `1.0.0-caliclan.1` and
   `caliclan.2` entries backfilled.

**Deliverable:** A clean tag push triggers GH Packages publishing. PR
checks gate merges. Compat matrix runs weekly.

**Verification checklist:**
- [ ] Push a test tag `v1.0.0-caliclan.2-test`, observe artifacts at
      `https://github.com/waliasanchit007/konduit/packages`
- [ ] Caliclan can resolve `dev.konduit:konduit-compose:1.0.0-caliclan.2`
      from GH Packages with a PAT, not just mavenLocal
- [ ] PR check on a no-op commit takes <5 min on linux, doesn't fire
      macOS unless an iOS-relevant file changed
- [ ] Compat matrix run succeeds for Kotlin 2.1.0 (current pin)

### Phase 3a — Dev tooling vertical slices
**Why before Phase 3:** Phase 3 (schema redesign) involves dozens of
trial-and-error iterations. Hot-reload + error boundary make that
tolerable.

**Tasks (subset of v1's Phase 5):**
1. **5d Error boundary** (`KonduitContent` catches guest crashes,
   renders fallback UI with retry). Implementation in Caliclan's
   `composeApp/src/commonMain/.../App.kt`:
   ```kotlin
   @Composable
   fun KonduitContent(screenId: String, modifier: Modifier = Modifier) {
       val state by rememberKonduitState(screenId)
       Box(modifier) {
           when (state) {
               is Loading -> KonduitLoadingPlaceholder()
               is Success -> KonduitRenderer(state.nodes)
               is Error   -> KonduitErrorFallback(state.error, onRetry = state::retry)
           }
       }
   }
   ```
2. **5a Compilation status overlay** (debug builds only). Banner at top
   of app showing reload state. Driven by a `StateFlow<KonduitDevState>`
   exposed from `KonduitProvider`. Wrapped in `if (BuildConfig.DEBUG)`.

**Deliverable:** Caliclan dev experience: edit presenter screen → save →
yellow "Kompiling..." banner → green "✓ Loaded" → screen reloads. Guest
crash shows red banner with file/line, host stays alive.

### Phase 3 — Schema redesign
**The big one.** Replace ad-hoc Cmp* widgets with a tiered, M3-aligned
schema.

**Approach:** build Tier 1 end-to-end first (all 10 widgets: schema +
Cmp* + facade + at least one presenter screen exercising each). Use
that as the unit of estimation for Tier 2 + Tier 3 — DO NOT commit to a
Tier 2/3 scope until Tier 1 is done and reviewed.

**Tier 1 — Foundation (all 10 must land before Tier 2 starts):**
| ID | Widget | Notes |
|---|---|---|
| 1 | Box | Container with overlap |
| 2 | Column | Vertical stack |
| 3 | Row | Horizontal stack |
| 4 | Spacer | Fixed gap |
| 5 | LazyColumn | Uses `konduit-lazylayout-*` protocol — different from regular widgets |
| 6 | LazyRow | Same |
| 7 | LazyItem | Slot inside lazy lists |
| 8 | Text | M3 typography via `SchemaTextStyle` |
| 9 | AsyncImage | Coil host-side; URL property |
| 10 | Icon | Material icons; tinted via `SchemaColor` |

Each Tier 1 widget needs:
- `@Widget(N)` declaration in Caliclan's `schema/Schema.kt`
- `Cmp*` impl in `composeApp/Protocol.kt`
- Entry in `CmpWidgetFactory`
- Facade function in `konduit-compose-facade` (Phase 4 module — start
  it during Tier 1)

**Tier 1 verification gate (before Tier 2 begins):**
- [x] All 10 widgets render correctly on Android (verified end-to-end on
      Galaxy S22 Ultra: Box backgrounds, Column/Row arrangements, Spacer
      gaps, Text typography, Icon tints, LazyRow horizontal scroll of
      12 chips, LazyColumn of 5 list rows, AsyncImage from picsum.photos)
- [x] iOS framework links + ATS exception added; runtime test deferred
      until phone+Mac share a Wi-Fi without AP isolation
- [x] Tier 1 retro completed (see "Phase 3 Tier 1 retro" below)
- [x] No regressions in existing Caliclan screens — N/A. Legacy
      MyText/MyButton/FlexRow/SduiCard etc. were deleted entirely
      (option ii from review). Welcome screen replaced by
      Tier1ShowcaseScreen.

**Phase 3 Tier 1 retro (input for Tier 2 scope):**

What landed differently from the original plan:

1. **`schema-types/` module is a hard requirement, not an optional split.**
   `konduit-schema` is published JVM-only (it processes annotations at
   build time), but property enum types referenced from `@Property val
   x: SchemaColor` need to be on every consumer's classpath — including
   Kotlin/JS for the guest. Solution: a separate KMP module for the
   plain enum types. Schema/ stays JVM-only with the `@Schema/@Widget`
   defs; schema-types/ is multiplatform (jvm/js/iosArm64/iosSimArm64).
   shared-widget gets `api(project(":schema-types"))` so generated
   widget code on every target sees the enum types. This pattern
   repeats for any consumer that adds @Property enums.

2. **Every schema enum needs `@Serializable` + the kotlinx-serialization
   plugin.** Without it the Konduit protocol can't ship enum values
   across host↔guest. Kotlin/JS specifically requires the annotation;
   the JVM has fallbacks but JS doesn't.

3. **Material icons aren't on the common Compose classpath** —
   `Icons.Filled.X` needs `compose.materialIconsExtended` explicitly.
   `compose.material3` doesn't include them.

4. **Coil 3 multiplatform image fetching needs explicit setup.** Adding
   `coil-compose` alone doesn't give you HTTP — Coil 3.0 split out the
   network fetcher. Use `coil-network-ktor2` (matches our Ktor 2.3.12)
   or `coil-network-ktor3` (requires Ktor 3.x). Per-platform Ktor
   engines: `ktor-client-okhttp` for android, `ktor-client-darwin` for
   iOS. **Coil 3 does NOT auto-register the ktor fetcher** — must call
   `setSingletonImageLoaderFactory` and pass `KtorNetworkFetcherFactory()`
   to `ImageLoader.components { add(...) }`.

5. **iOS App Transport Security needs an explicit exception for IP
   literals.** `NSAllowsLocalNetworking` covers `.local` hostnames +
   RFC 6761 names like `localhost`, but NOT `127.0.0.1`. For
   adb-reverse-style dev URLs, add `NSExceptionDomains` entries with
   `NSExceptionAllowsInsecureHTTPLoads=true` for both `127.0.0.1` and
   `localhost`. ngrok HTTPS URLs don't need this.

6. **Showcase needs to be inside a scrollable container.** The plan's
   "demo screen" implicitly assumed a single-screen layout; reality
   is the demo overflows and the bottom widgets get clipped. Use
   `LazyColumn` as the outer container; each section becomes a
   `LazyItem`.

7. **Dev URL is a fragile config knob.** Documented all options in
   `composeApp/src/commonMain/.../shared/DevConfig.kt`:
   - LAN IP (works on Wi-Fi WITHOUT AP isolation)
   - `10.0.2.2` (Android emulator only)
   - `127.0.0.1` (adb reverse over USB; fragile, USB drops break it)
   - `https://*.ngrok-free.dev` (default; works through most networks)

   Be aware: some ISP/router combinations break ngrok edge → agent
   transit; some Wi-Fi setups have AP isolation that breaks LAN-direct.
   Plan for at least one of these to be available.

**Tier 2 sizing (re-estimated post-Tier-1):**

Tier 1 took 7 commits to land (schema rewrite → host Cmp* → showcase →
schema-types extraction → @Serializable fix → materialIconsExtended →
Coil ktor2 → scrollable showcase). Each Tier 2 widget should be cheaper
since the foundation is laid (schema-types, theme bindings, factory
pattern, modifier wiring). Estimate: **Tier 2's 30 widgets in 2-3
batches of 10** (buttons → input → containers/selection/feedback/nav).

**Tier 2 — Core M3** (~30 widgets, IDs 21–80). Re-spec'd post-Tier-1
into 8 ordered batches. Each batch ends with a build + device verify
on Android (mandatory) and iOS sim (rapid-verify ritual, see §8.3).

**Batch 2.0 — LayoutModifier system (foundation)**
Land before any Tier 2 widget. See §8.2 for full design. One commit
that:
- Adds `dev.konduit:konduit-layout-modifiers` as a `redwoodSchema`
  source in Caliclan's schema build.
- Defines the modifier set in Caliclan's schema:
  `Padding`, `Size`, `Width`, `Height`, `Background`, `Weight`,
  `Clickable`, `FillMaxWidth`, `FillMaxHeight`, `FillMaxSize`, `Alpha`.
- Migrates Tier 1 widgets off their direct properties (`padding`,
  `background`, `fillMaxSize`, etc.) onto the modifier chain.
- Updates `Tier1ShowcaseScreen` to use the new modifier syntax.
- Verification gate: existing Tier 1 showcase still renders
  identically on Android + iOS.

**Batch 2.1 — Buttons + click feedback** (IDs 21–28)
Button, OutlinedButton, TextButton, FilledTonalButton, ElevatedButton,
IconButton, FloatingActionButton, ExtendedFloatingActionButton.
All share a common Cmp* pattern (lambda + label slot + variants).

**Batch 2.2 — Inputs** (IDs 31–33)
TextField, OutlinedTextField, SearchBar.
Shared state pattern (value + onValueChange).

**Batch 2.3 — Selection** (IDs 41–46)
Checkbox, RadioButton, Switch, Slider, RangeSlider, SegmentedButton.
Shared `value` + `onChange` event pattern.

**Batch 2.4 — Containers** (IDs 51–54)
Card, ElevatedCard, OutlinedCard, Surface.
Trivial wrappers around their Compose counterparts; can land in one
commit.

**Batch 2.5 — Feedback** (IDs 61–64)
LinearProgressIndicator, CircularProgressIndicator, Badge, Snackbar.
Mostly stateless except Snackbar (which needs a host-side queue).

**Batch 2.6 — Navigation structure** (IDs 71–78)
Scaffold, TopAppBar, LargeTopAppBar, MediumTopAppBar, NavigationBar,
NavigationBarItem, TabRow, Tab. Only batch that needs new schema
capability: **named slots** (TopAppBar's `title`/`navigationIcon`/
`actions`, Scaffold's `topBar`/`bottomBar`/`floatingActionButton`/
`content`). Plan: extend `@Children(N)` with a slot name; the Konduit
schema generator already supports this — verify upstream pattern.

**Batch 2.7 — Misc closeout** (IDs 79–80)
HorizontalDivider, VerticalDivider. Trivial.

**Tier 3 — Extended** (~16 widgets, IDs 100–199 per the §3.3 range
table): chips, list items, flow layouts, animations, sheets, dialogs,
pagers, pull-to-refresh, shimmer, navigation rail. Same: re-spec'd
after Tier 2. (An earlier draft of this section said "81–150"; that
overlapped with Tier 2's 21–80 + 19-id buffer for additive properties
and contradicted the wire-format range table — corrected to 100–199.)

**Batch 3.0 — Chips** (IDs 100–103) — first Tier 3 batch. FilterChip,
AssistChip, InputChip, SuggestionChip. Uniform shape: each chip has a
`label: String` property + a single `leadingIcon: () -> Unit` slot
(@Children(1)); the guest passes `{}` when no icon is wanted and the
host renders the chip in label-only mode. FilterChip + InputChip add a
`selected: Boolean` property; InputChip adds an `onClose: (() -> Unit)?`
property — null hides the trailing close affordance entirely.

**Batch 3.1 — List + Menus** (IDs 110–112). ListItem, DropdownMenu,
DropdownMenuItem.

ListItem: first 5-effective-slot widget. Three text properties
(headline / supporting / overline) + two @Children slots (leadingContent,
trailingContent). headline is required; supporting + overline collapse
to null when empty so the row uses M3's natural compact layout instead
of rendering blank text lines that push the row taller. Headline-as-slot
(rich-text headline) is deferred — additive @Children(3) can land later.
M3 ListItem has no native `enabled` flag; the host gates `clickable {}`
on `enabled && onClick != null` so a disabled row visually still renders
but won't fire onClick.

DropdownMenu: first popup-anchored widget. Wraps `material3.DropdownMenu`,
which uses Compose's Popup primitive — the popup positions itself
relative to the widget's coordinates in its parent. Typical Caliclan
usage is to wrap the trigger (e.g. an IconButton) and the menu in a Box,
so the menu anchors to the trigger naturally. The host deliberately does
NOT propagate the parent's modifier chain to the menu surface — applying
fillMaxWidth / padding etc. to the menu would shift the popup, not the
trigger. (If a future caller needs to size the menu surface itself, that
should land as an explicit `surfaceModifier` property, not silently via
the modifier chain.)

DropdownMenuItem: leaf row, mirrors M3 1:1. text + enabled + onClick
properties + leadingIcon + trailingIcon @Children slots.

**SchemaColor / SchemaTextStyle (defined as part of Tier 1):**
```kotlin
enum class SchemaColor {
    Primary, OnPrimary, PrimaryContainer, OnPrimaryContainer,
    Secondary, OnSecondary, SecondaryContainer, OnSecondaryContainer,
    Tertiary, OnTertiary,
    Surface, OnSurface, SurfaceVariant, OnSurfaceVariant,
    Background, OnBackground,
    Error, OnError, Outline, OutlineVariant,
    // Caliclan brand slots — extensible without breaking wire format
    Accent1, Accent2, Accent3, Accent4
}

enum class SchemaTextStyle {
    DisplayLarge, DisplayMedium, DisplaySmall,
    HeadlineLarge, HeadlineMedium, HeadlineSmall,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelMedium, LabelSmall
}
```

`Accent1..Accent4` slots: host's MaterialTheme maps these to Caliclan's
brand colors. Adding a brand color = re-skinning the host, not a schema
bump.

**GuestModifier (defined as part of Tier 1):**
Supported chains:
```
fillMaxWidth/Height/Size, padding(...), background(SchemaColor),
size/width/height(Dp), weight(Float, scope-typed),
clickable(onClick), alpha(Float), clip(SchemaShape),
border(width: Dp, color: SchemaColor), wrapContentWidth/Height
```
Explicitly NOT supported (hard-fail at codegen if guest tries):
`graphicsLayer{}`, `pointerInput{}`, `drawBehind{}`, `nestedScroll(...)`.

`weight()` is scope-typed (only valid in `ColumnScope` / `RowScope`),
matching real Compose. Reuse Redwood's `LayoutModifier` scope mechanism
— do not reinvent.

**Slot-typed widgets** (Scaffold, TopAppBar, NavigationBar with action
slots) are deferred to Tier 2 and may need schema-level work to express
multiple named child slots. Note as a known unknown.

**Deliverable:** Caliclan presenter screens rebuilt against the new
schema. Both platforms run. Old `Cmp*` widgets removed.

### Phase 4 — Compose facade
**Goal:** guest code in `presenter/` looks identical to normal CMP code.

**New module:** `konduit-compose-facade` (lives in the konduit repo).

Package layout mirrors `androidx.compose.*` exactly:
```
src/commonMain/kotlin/androidx/compose/
├── material3/   (Button, Text, Card, ... — every Tier 1+2+3 widget)
├── foundation/layout/   (Column, Row, Box, Spacer)
└── ui/Modifier.kt   (GuestModifier exposed as `Modifier`)
```

**This works ONLY because guest's classpath has no real AndroidX.**
Architecture invariant: `konduit-compose-facade` is depended on by
`presenter/` (guest), never by `composeApp/` (host). Adding it to host
classpath = name collision = crash. **Document this as a top-of-file
warning in the facade module's README.**

**API drift sync ritual:** `docs/FACADE_SYNC.md` will record:
- Pinned M3 version (e.g., `1.3.x`)
- Procedure for adding a new widget when M3 ships one
- Why we're not at the latest M3 (if applicable)

**Completion test (the v1 plan's standard):** show a presenter screen
to a Compose dev who's never heard of Konduit. They should be ~95%
unable to tell. Acknowledge known leaks: `SchemaColor` ≠ `Color`,
`GuestModifier` is a subset, `remember{}` semantics around QuickJS
heap.

**Deliverable:** All presenter screens migrated to facade imports. Diff
shows only import-line changes, zero logic changes.

### Phase 5 — Dev tooling rest
**Tasks (5b + 5c from v1):**
1. **5b Single dev command** — `./gradlew konduit:dev` chains:
   - `:presenter:jsBrowserDevelopmentExecutable --continuous`
   - `:dev-server:run`
   - Tails Logcat with Konduit filter
2. **5c Clean Logcat output** — custom `EventListener` formats events:
   ```
   D/Konduit: ⬇ Downloading manifest...
   D/Konduit: ✓ Loaded HomeScreen (1.2s, fresh)
   D/Konduit: 🔄 Reloading HomeScreen (file changed)
   E/Konduit: ✕ HomeScreen crashed at HomeScreen.kt:47
   ```
   Source maps enabled in presenter:
   ```kotlin
   zipline { sourceMapEnabled = true }
   ```

**Deliverable:** Dev workflow is one command. Logcat is readable. File:line
in stack traces.

### Phase 6 — Standalone library principles (NOT a phase, just guardrails)

This is the long-term north star: "any CMP app can adopt Konduit in a few
steps." Not a deliverable. Maintain these architectural constraints
across Phases 3+4+5 so we don't paint into a corner:

- `konduit-host` (whatever module name we pick) must have zero knowledge
  of any specific schema. Schema is consumed via the gradle plugin's
  generated code.
- Konduit ships **primitives + facade**, not domain widgets. Domain
  widgets live in consumer apps (Caliclan today, others later).
- `KonduitContent` is pure composable + `KonduitProvider` is the only
  setup. No global config, no service locators.

When/if Konduit goes public, this becomes Phase 6 with concrete tasks.
Until then, treat as a review checklist for Phase 3 and 4 decisions.

---

## 5. Documentation deliverables

Each phase produces or updates docs in the konduit repo (not Caliclan's):

| Doc | Owner phase | Status |
|---|---|---|
| `README.md` | Phase 1.5 | Generated from upstream; needs Konduit-flavored rewrite |
| `CHANGELOG.md` | Phase 2 | Backfill `caliclan.1` + `.2`, then living doc |
| `docs/QUICK_START.md` | Phase 4 | "How to consume konduit in a fresh CMP app" |
| `docs/MENTAL_MODEL.md` | Phase 4 | Host/Guest/Schema explained |
| `docs/ADDING_A_WIDGET.md` | Phase 3 | Step-by-step; refers to WIDGET_REGISTRY.md |
| `docs/WIDGET_REGISTRY.md` | Phase 3 | Numbered table; PR-reviewed |
| `docs/MODIFIER_GUIDE.md` | Phase 3 | What's supported, what isn't, scope rules |
| `docs/FACADE_SYNC.md` | Phase 4 | M3 version pin + drift procedure |
| `docs/UPGRADING_KOTLIN.md` | Phase 2 | Which IR/FIR APIs to check, runbook |
| `docs/CONTRIBUTING.md` | Phase 2 | Build, test, version, release |

Caliclan repo continues to own:
- `docs/PHASE_0_INVENTORY.md` ✅
- `docs/WIDGET_CONTRACT.md` ✅ (the host-implementation contract for future native hosts)
- `docs/KONDUIT_PLAN.md` (this file)

---

## 6. Glossary (canonical)

| Term | Meaning |
|---|---|
| Host | The CMP app binary that ships to app stores. Contains widget impls, Zipline runtime, and the facade is **never** here |
| Guest | Kotlin code compiled to `.zipline`, served from CDN. Contains screen logic. Updated without app store |
| Schema | The contract defining available widgets. Widget IDs are the wire format — never renumbered, never reused |
| Wire format | The on-disk/on-wire serialization of a UI tree. Stable within a major version |
| Protocol | Serialization layer between guest Compose and host widget tree. Generated by konduit-gradle-plugin |
| KonduitContent | Composable in the host that mounts a guest-driven screen region |
| KonduitProvider | Wraps the app, initializes Zipline loader, makes KonduitContent work |
| GuestModifier | Konduit's Modifier impl for guest code. Exposed as `Modifier` via the facade |
| Facade | `konduit-compose-facade` — package-mirrors `androidx.compose.*` so guest imports look identical to real CMP |
| `.zipline` file | Compiled QuickJS bytecode — the unit served from CDN |
| Manifest | `manifest.zipline.json` — lists `.zipline` modules, URLs, signatures, freshness timestamp |
| HostApi | Interface the guest calls into for native capabilities (data fetching, navigation, analytics) |
| Caliclan | The consumer app driving this fork's existence. Internal name |

---

## 7. Course corrections (May 2026 — post-Tier 1)

After Phase 3 Tier 1 landed and was verified end-to-end on both Android
and iOS, we agreed on five corrections to apply before Tier 2 starts.

### 8.1 Open the Caliclan PR before Tier 2

The branch `claude/vigilant-euclid-681447` has 12+ commits stacked since
the last main merge. Stacking another 30+ Tier 2 commits on top makes
review impossibly large. Action: open a PR for the Tier 1 work now,
review/squash → land on main, cut Tier 2 from a clean base.

The PR is the formal "Phase 3 Tier 1 closure" event.

### 8.2 Introduce the LayoutModifier system at Tier 2 start

Tier 1 widgets used direct `@Property` fields (`padding: Int`,
`background: SchemaColor`, `fillMaxSize: Boolean`, etc.) for layout
concerns. That choice was acceptable for 10 widgets but does not scale
— Tier 2's 30 widgets each multiply the property count and the schema
becomes unmaintainable. Worse, it can't express composable orthogonal
concerns (e.g., a `Button` that's `clickable` AND `padded` AND
`weight=1f` inside a Row).

**Solution: adopt Konduit's `konduit-layout-modifiers` module** — same
pattern Redwood/Konduit uses for upstream's own widgets. Caliclan's
schema declares which modifiers it wants; the Konduit gradle plugin
generates the wrappers; host Cmp* widgets receive a typed `Modifier`
chain and apply it.

**Modifier set for Tier 2 baseline** (declared in Caliclan's schema):

| Modifier | Args | Maps to Compose |
|---|---|---|
| `Padding` | start, top, end, bottom: Int (dp); 0 = none | `Modifier.padding(...)` |
| `Size` | width, height: Int (dp) | `Modifier.size(width.dp, height.dp)` |
| `Width` | value: Int (dp) | `Modifier.width(value.dp)` |
| `Height` | value: Int (dp) | `Modifier.height(value.dp)` |
| `Background` | color: SchemaColor | `Modifier.background(color)` |
| `Weight` | value: Double | `Modifier.weight(value.toFloat())` (Row/Column scope only) |
| `FillMaxWidth` | — | `Modifier.fillMaxWidth()` |
| `FillMaxHeight` | — | `Modifier.fillMaxHeight()` |
| `FillMaxSize` | — | `Modifier.fillMaxSize()` |
| `Alpha` | value: Double | `Modifier.alpha(value.toFloat())` |

**Dropped from the original §8.2 set:** `Clickable(onClick: () -> Unit)`. Konduit's protocol-guest codegen emits `ContextualSerializer(Function0<Unit>::class)` for lambda-typed modifier properties, which is invalid Kotlin (class literal not allowed on a generic type) and fails the JS compile. Click handlers stay as direct widget `@Property` fields — `Box.onClick` for now, `Button.onClick` etc. in Batch 2.1. If this codegen bug ever gets fixed in the konduit fork, we can revisit.

These cover ≥90% of real Compose modifier usage. Add more if needed
but resist the urge to add `graphicsLayer{}`, `pointerInput{}`,
`drawBehind{}` — those don't fit the schema model.

**Migration of Tier 1 widgets:** existing direct properties
(`padding`, `background`, `fillMaxSize`, `fillMaxWidth`, `verticalArrangement`,
`horizontalAlignment`, `verticalAlignment`, `horizontalArrangement`)
collapse onto the modifier chain. The widgets themselves get simpler:

  ```kotlin
  // Before (Tier 1)
  @Widget(2)
  data class Column(
      @Property(1) val padding: Int,
      @Property(2) val background: SchemaColor,
      @Property(3) val verticalArrangement: SchemaArrangement,
      @Property(4) val horizontalAlignment: SchemaHorizontalAlignment,
      @Property(5) val fillMaxSize: Boolean,
      @Children(1) val children: () -> Unit,
  )

  // After (Batch 2.0)
  @Widget(2)
  data class Column(
      @Property(1) val verticalArrangement: SchemaArrangement,
      @Property(2) val horizontalAlignment: SchemaHorizontalAlignment,
      @Children(1) val children: () -> Unit,
  )
  // padding, background, fillMaxSize move to the modifier chain
  ```

**Wire format implication:** this is a Tier 1 schema rewrite that
removes properties (3 from Box, 5 from Column, 5 from Row, 2 from
LazyColumn, 2 from LazyRow). Per §3.1 that's a major-bumpable change.
But — Tier 1 has not been deployed to any production guest, so we can
treat Batch 2.0 as a "schema reset within the same major version" and
keep `1.0.0-caliclan.N`. Document this caveat in the Batch 2.0 commit
message.

**Verification gate for Batch 2.0:** existing `Tier1ShowcaseScreen`
renders identically on Android and iOS after migration.

### 8.3 iOS rapid-verify ritual

Tier 1 verification on iOS was painful: build → manually open Xcode →
run on sim → check log. Tier 2's 8 batches × 2 platforms = 16 verify
cycles. We need a faster loop.

**Action:** add a script `scripts/verify-ios.sh` (Caliclan repo):

  ```bash
  #!/usr/bin/env bash
  set -e
  ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
  ./gradlew :presenter:compileDevelopmentExecutableKotlinJsZipline
  open iosApp/iosApp.xcodeproj
  echo "Now: hit ⌘R in Xcode. Once running, glance at Xcode's console."
  ```

Counterpart `scripts/verify-android.sh`:

  ```bash
  #!/usr/bin/env bash
  set -e
  ./gradlew :androidApp:installDebug
  adb shell pm clear com.example.serverdrivenui
  adb shell am start -n com.example.serverdrivenui/.MainActivity
  ```

Run after every Batch 2.x commit. Ensures iOS doesn't bit-rot the way
it would if we waited until the end of Tier 2 to test.

### 8.4 Phase 4 Compose Facade — defer (reclassify nice-to-have)

Plan v2 §4 had a full Phase 4 spec for `konduit-compose-facade`,
making guest imports look like:

  ```kotlin
  import androidx.compose.foundation.layout.Box  // facade
  ```

instead of

  ```kotlin
  import com.example.serverdrivenui.schema.compose.Box  // current
  ```

After Tier 1, the current schema-direct path is clean enough that the
facade's "make it look like real CMP" goal is mostly cosmetic. The
real benefit (familiar imports for Compose-experienced devs) doesn't
justify the cost (mirror M3 surface, sync ritual, classpath fragility).

**Action:** reclassify Phase 4 from "must do" to "do iff Konduit goes
public OR a developer onboarding survey says imports are confusing".
Cross out from the Tier 1→2→3→4→5 default path. If we revisit, it'll
be after Tier 3 is done and we have data on developer ergonomics.

Phase 4's deferral does NOT affect Phase 5 (rest of dev tooling) —
that runs after Tier 2/3.

### 8.5 Add Caliclan CI build gate

Currently the Caliclan repo (`waliasanchit007/ServerDrivenUI`) has no
CI. Konduit has CI but only validates Konduit changes; Caliclan's
schema changes (where most Tier 2 work happens) get no automated
build check before PR merge.

**Action:** add `.github/workflows/ci.yml` to Caliclan that runs:

  ```yaml
  - ./gradlew :androidApp:assembleDebug
  - ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
  - ./gradlew :presenter:compileDevelopmentExecutableKotlinJsZipline
  ```

on every PR + push to main. macOS-only (same reasoning as Konduit's CI).
30-min timeout. Caches `~/.gradle`. Catches:
- Schema codegen failures (missing imports, malformed @Property)
- Missing `@Serializable` on enums
- iOS framework link issues
- Presenter zipline compile errors

Land this AFTER Batch 2.0 (the LayoutModifier migration) so the CI
exercises the new modifier system from day one.

---

## 8. Decisions log

Track every decision that resolves an ambiguity. Append-only.

| Date | Decision | By |
|---|---|---|
| 2026-04 | Soft fork in name only — no upstream sync ritual | walsan679 |
| 2026-04 | GitHub Packages for all artifacts (private), mavenLocal for dev | walsan679 |
| 2026-04 | Wire-format compat: forward-compat within a major | claude (delegated) |
| 2026-04 | Manifest signing not mandatory in production | walsan679 |
| 2026-04 | Guest version pinning per app version: yes (versioned manifest URLs) | walsan679 |
| 2026-04 | No new tests written; upstream snapshots preserved | walsan679 |
| 2026-04 | Phase order: 0→1→1.5→2→3a→3→4→5→6-as-principles | walsan679 |
| 2026-04 | App-schema range moved 200+ → 1000+ to avoid upstream collision | claude |
| 2026-04 | `Accent1..Accent4` slots in SchemaColor for brand extensibility without bumps | claude |
| 2026-04 | Class-name rename (`RedwoodPlugin → KonduitPlugin`) optional, may never happen | walsan679 |
| 2026-05 | Phase 3 Tier 1 scope: option A (full Tier 1 in one shot) + option ii (delete legacy widgets, rewrite welcome screen) | walsan679 |
| 2026-05 | Schema split into `schema/` (JVM-only, @Schema/@Widget defs) + `schema-types/` (KMP, plain @Serializable enums) | claude |
| 2026-05 | Coil network: `coil-network-ktor2` (matches our Ktor 2.3.12), set up via `setSingletonImageLoaderFactory` in App.kt | claude |
| 2026-05 | Drop Linux CI, single macOS-only validator (Linux runner hung at 58m on cache restore) | walsan679 |
| 2026-05 | Course correction post-Tier-1: open Caliclan PR before Tier 2 starts | walsan679 |
| 2026-05 | Course correction: introduce LayoutModifier system as Batch 2.0 (foundation before Tier 2 widgets) | walsan679 |
| 2026-05 | Course correction: add iOS rapid-verify ritual + per-batch verification cycle | walsan679 |
| 2026-05 | Course correction: defer Phase 4 Compose Facade to "nice-to-have", revisit only if Konduit goes public | walsan679 |
| 2026-05 | Course correction: add Caliclan CI build gate (after Batch 2.0) | walsan679 |
| 2026-05 | Tier 2 batching: 8 ordered batches (2.0 modifiers → 2.1 buttons → 2.2 inputs → 2.3 selection → 2.4 containers → 2.5 feedback → 2.6 nav structure → 2.7 misc closeout) | walsan679 |
| 2026-05 | Batch 2.0 architecture: new `:shared-modifier` gradle module applies `dev.konduit.generator.modifiers` plugin; modifiers live as `@Modifier`-annotated classes alongside `@Widget`s in `:schema`; widget consumers depend transitively on the generated `*.schema.modifier.X` interfaces | claude |
| 2026-05 | Batch 2.0 scope reduction: drop `Clickable(onClick: () -> Unit)` modifier — Konduit codegen for lambda-typed modifier properties emits invalid Kotlin/JS (`Function0<Unit>::class`). Click handlers live as widget `@Property` instead. | claude |
| 2026-05 | Batch 2.0 Weight scoping: keep Weight as an `UnscopedElement` for now and have CmpRow/CmpColumn extract it from each child's modifier chain at render time. Promoting to a `ScopedElement` is a follow-up that needs RowScope/ColumnScope schema additions and Children-receiver changes. | claude |
| 2026-05 | Batch 2.0 wire-format reset: removed properties from Box (3), Column (3), Row (3), LazyColumn (2), LazyRow (2). Tier 1 was never deployed → schema reset within `1.0.0-caliclan.N`, no major bump. | walsan679 |
| 2026-05 | Batch 2.0 white-screen postmortem (gotcha #10): Konduit codegen emits `ContextualSerializer(EnumType::class)` for enum fields inside @Modifier classes. Without a registered SerializersModule the encode call throws `SerializationException`, the protocol path swallows it, and the host renders nothing. Mitigation: shared `SduiSerializersModule` in `:schema-types` plumbed into every `TreehouseApp.Spec.serializersModule` and the guest's `StandardAppLifecycle.json`. Add new enums to the module when introducing new modifier types. | claude |
| 2026-05 | Batch 2.3 SegmentedButtonRow: `material3.SegmentedButton` not in CMP iOS 1.8.0. Implement host-side as a Row of FilledTonalButton (selected) / OutlinedButton (unselected) — wire API stays the same, swap to native widget when CMP catches up. | claude |
| 2026-05 | Batch 2.5 Snackbar: render inline as a `material3.Snackbar` widget; auto-dismiss + queueing are the guest's responsibility. Host-side queue with `SnackbarHostState` and an event-style `Snackbar.show(message)` API deferred to Tier 3. | walsan679 |
| 2026-05 | Batch 2.6 multi-slot widgets: Konduit's `@Children(N)` already supports multiple named children groups (one per tag). Scaffold uses 4 slots (topBar/bottomBar/floatingActionButton/content); TopAppBars use 2 (navigationIcon/actions). No schema-generator changes needed. | claude |
| 2026-05 | Batch 2.6 NavigationBarItem: `material3.NavigationBarItem` requires `RowScope` from its parent. Render hand-rolled column (icon + label) for now; promote to native widget when scope-typed @Children land in Konduit. | claude |
| 2026-05 | **Tier 2 complete**. 30 widgets at IDs 21–80 + 10 layout modifiers shipped across 8 batches (2.0 through 2.7) plus the SerializersModule fix. Verified end-to-end on Android (Galaxy S22 Ultra) + iOS sim (iPhone 16 Pro). | walsan679 |
| 2026-05 | **CI auth gotcha** (§7.5): GitHub Packages' Maven registry rejects fine-grained PATs with HTTP 404 (not 401). Earlier `docs/CI_SETUP.md` told the user to make a fine-grained PAT — wrong. Fixed: docs + workflow header now mandate a classic PAT with `read:packages`; the workflow's Verify step also probes `konduit-gradle-plugin-1.0.0-caliclan.2.pom` directly so the failure mode is "PAT lacks read:packages" instead of a confusing plugin-not-found stack trace deep in Gradle. | claude |
| 2026-05 | Batch 3.0 chips ID range: use 100–103 per the §3.3 wire-format range table (Tier 3 = 100–199). Earlier draft `Tier 3 (~16 widgets, IDs 81–150)` text in HANDOVER + plan was stale and overlapped with the Tier 2 buffer; corrected to 100–199. IDs 81–99 stay reserved as a Tier 2 additive-property buffer. | claude |
| 2026-05 | Batch 3.0 chip slot shape: every chip has a single `leadingIcon: () -> Unit` @Children(1) slot. Trailing-icon support is property-only on InputChip (`onClose: (() -> Unit)?`) — that covers the conventional close-X behavior; FilterChip's selected-state check glyph is M3-rendered, so we suppress leadingIcon when `selected && hasIcon` to avoid icon double-up. AssistChip + FilterChip custom trailing slots are deferred (additive @Children can land later without breaking wire). | claude |
| 2026-05 | Batch 3.1 ListItem text-as-Strings: headline / supporting / overline are `String` properties, not @Composable slots. Empty string = hide that line. Rationale: ~95% of real-world list rows are text-only labels; forcing the guest to wrap each label in a Text widget would 4x the wire payload for the common case. Headline-as-slot (rich-text headline) deferred to a future @Children(3) — additive, won't break wire. | claude |
| 2026-05 | Batch 3.1 ListItem disabled-clickable behavior: M3 ListItem has no native `enabled` parameter. Host gates `Modifier.clickable {}` on `enabled && onClick != null` so a disabled row visually still renders but won't fire onClick. Visual disabled-state styling (greyed-out text) is NOT applied automatically — the guest can pass dimmer SchemaColors via Text styles if it wants the visual cue. Open question: bake a host-side disabled style into ListItem, or leave it to the guest? Current call: leave it. | claude |
| 2026-05 | Batch 3.1 DropdownMenu modifier propagation: the host does NOT apply the parent modifier chain to the menu surface. M3 DropdownMenu uses a Popup that positions itself relative to its parent's coordinates; applying fillMaxWidth / padding / etc. to the menu would shift the popup, not the trigger. If a future caller needs to size or style the menu surface itself, that should land as an explicit `surfaceModifier` property (not silently via the modifier chain). | claude |
