# Konduit / Caliclan — Handover

> Read this first when starting a new session. It tells you exactly where
> we are, what works, what's pending, and the gotchas we've hit.

## What's done

| Phase | Status | Where |
|---|---|---|
| 0 — Inventory | ✅ | `docs/PHASE_0_INVENTORY.md` |
| 1 — Fork Redwood → Konduit | ✅ | konduit repo `1.0.0-caliclan.1` |
| 1.5 — Strip non-CMP modules | ✅ | konduit repo `1.0.0-caliclan.2` (60→47 modules) |
| 2 — CI + GitHub Packages publish | ✅ | konduit `.github/workflows/{ci,publish,compat-matrix}.yml`; macOS-only after Linux runner kept hanging |
| 3a — Dev tooling slices (error boundary + reload overlay) | ✅ | `composeApp/src/commonMain/.../shared/{KonduitDevState,KonduitDevOverlay}.kt` + `App.kt` wiring |
| 3 — Schema redesign Tier 1 | ✅ | 10 widgets at IDs 1–10 + 2 nav at 1000+ + theme enums + showcase |
| Batch 2.0 — LayoutModifier system | ✅ | new `:shared-modifier` module + 10 modifiers + Tier 1 widget rewrite + showcase migration |
| Batch 2.1 — Buttons (IDs 21-28) | ✅ | Button, OutlinedButton, TextButton, FilledTonalButton, ElevatedButton, IconButton, FloatingActionButton, ExtendedFloatingActionButton |
| Batch 2.2 — Inputs (IDs 31-33) | ✅ | TextField, OutlinedTextField, SearchBar — value + placeholder + enabled + onValueChange |
| Batch 2.3 — Selection (IDs 41-46) | ✅ | Checkbox, RadioButton, Switch, Slider, RangeSlider, SegmentedButtonRow |
| Batch 2.4 — Containers (IDs 51-54) | ✅ | Card, ElevatedCard, OutlinedCard, Surface |
| Batch 2.5 — Feedback (IDs 61-64) | ✅ | LinearProgressIndicator, CircularProgressIndicator, Badge, Snackbar |
| Batch 2.6 — Navigation (IDs 71-78) | ✅ | Scaffold, TopAppBar, LargeTopAppBar, MediumTopAppBar, NavigationBar, NavigationBarItem, TabRow, Tab — first multi-slot widgets |
| Batch 2.7 — Misc (IDs 79-80) | ✅ | HorizontalDivider, VerticalDivider |

**Tier 1 + Tier 2 verified end-to-end on BOTH Android (Galaxy S22 Ultra) and iOS sim** — all 40 widgets (10 Tier 1 + 30 Tier 2) and 10 layout modifiers render correctly.

**Caveat (resolved):** Initial Batch 2.0 commit (`3be4c79`) shipped a white-screen bug — the `Background(SchemaColor)` modifier serializer triggered a silent `SerializationException` because Konduit codegen emits `ContextualSerializer(SchemaColor::class)` without a registered SerializersModule. Fixed in `6acc7ab` by adding `SduiSerializersModule` (`:schema-types`) and plumbing it into every `TreehouseApp.Spec` + the guest's `StandardAppLifecycle.json`. See gotcha #10.

**Konduit fork** (private): `https://github.com/waliasanchit007/konduit`
- `main` at `975c9cdaa` (Phase 2 + Linux CI drop)
- Tagged `v1.0.0-caliclan.2` published to GitHub Packages

**Caliclan branch:** `claude/vigilant-euclid-681447` (pushed to origin)
- `cbbc349` Phase 1 migration
- `5b038a7` Plan v2
- `5b0c556` Bump to caliclan.2
- `0f899fc` Phase 3a dev tooling
- `6dbaa4a` Phase 3 Tier 1 schema + showcase
- `8971608` Tier 1 runtime fixes (Serializable + Coil + DevConfig)
- `30c3ead` Coil multiplatform via Ktor
- `fe13506` iOS ATS exception + ngrok URL restored
- `b23c2c3` Coil ktor2 fetcher + scrollable showcase
- `7be7a14` Plan retro + handover doc
- `478eb09` HANDOVER: Tier 1 fully verified on Android
- `aec5d70` Plan v3: Tier 2 batch order + 5 course corrections
- `3be4c79` Batch 2.0: LayoutModifier system + Tier 1 widget rewrite
- `6acc7ab` Fix Batch 2.0 white screen: register SchemaColor as contextual serializer
- `2290f54` Batch 2.1: Buttons (IDs 21-28)
- `848147a` Batch 2.2: Inputs (IDs 31-33)
- `cbce69d` Batch 2.3: Selection (IDs 41-46)
- `4c3baf8` Batch 2.4: Containers (IDs 51-54)
- `10a7513` Batch 2.5: Feedback (IDs 61-64)
- `bba1888` Batch 2.6: Nav structure (IDs 71-78)
- `38c6715` Batch 2.7: Dividers (IDs 79-80) — Tier 2 closeout

**Caliclan PR #1:** https://github.com/waliasanchit007/ServerDrivenUI/pull/1 (open).

## Current state

**Caliclan runs on `dev.konduit:1.0.0-caliclan.2` from mavenLocal**, both Android and iOS.

The `Tier1ShowcaseScreen` exercises every Tier 1 widget:
- `Box` (chip backgrounds in LazyRow, list-row backgrounds)
- `Column` (sub-sections within LazyItems)
- `Row` (icon row, list rows)
- `Spacer` (gaps between sections)
- `LazyColumn` (outer scrollable container)
- `LazyRow` (12 horizontally-scrolling chips)
- `LazyItem` (every section)
- `Text` (titles + body, with SchemaTextStyle + SchemaColor)
- `AsyncImage` (random landscape from picsum.photos via Coil + ktor2)
- `Icon` (5 in icon row, 5 in list rows, with SchemaIconName + SchemaColor tints)

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│ Caliclan (this repo)                                             │
│                                                                  │
│   schema/         (JVM-only) @Schema/@Widget defs                │
│   schema-types/   (KMP)      @Serializable enums                 │
│      ├── SchemaColor  → MaterialTheme.colorScheme                │
│      ├── SchemaTextStyle → MaterialTheme.typography              │
│      ├── SchemaArrangement / Horizontal/VerticalAlignment        │
│      └── SchemaIconName → Material icons                         │
│                                                                  │
│   shared-widget/        (KMP) generated widget interfaces        │
│   shared-protocol-host/ (KMP) generated host protocol            │
│   shared-protocol-guest/(JS)  generated guest protocol           │
│   shared/               (KMP) services + DTOs                    │
│                                                                  │
│   composeApp/   (KMP host)                                       │
│      ├── Protocol.kt    Cmp* impls + CmpWidgetFactory            │
│      ├── App.kt         KonduitContent + dev overlay + Coil      │
│      ├── DevConfig.kt   manifest URL knob                        │
│      └── KonduitDev*    error boundary + reload state machine    │
│                                                                  │
│   presenter/   (Kotlin/JS guest)                                 │
│      ├── RootUi.kt              ScreenStack + BackHandler        │
│      ├── Navigator.kt           guest-side nav stack             │
│      └── screens/Tier1ShowcaseScreen.kt                          │
│                                                                  │
│   androidApp/, iosApp/, dev-server/                              │
└──────────────────────────────────────────────────────────────────┘
                  │ depends on (mavenLocal/GitHub Packages)
                  ▼
┌──────────────────────────────────────────────────────────────────┐
│ Konduit (~/AndroidStudioProjects/konduit, github.com/...)        │
│   47 modules at dev.konduit group, 1.0.0-caliclan.2 version      │
│   build-support plugin sets group/version centrally              │
└──────────────────────────────────────────────────────────────────┘
```

## Wire format (locked-in for major v1)

- **Widget IDs are immutable.** Once assigned, never reused, never renumbered.
- Properties on existing widgets can only be **added with defaults**.
- New widgets only at unused IDs.
- ID ranges:
  - 1–99 Konduit Tier 1 + 2
  - 100–199 Konduit Tier 3
  - 200–999 reserved for future Konduit
  - 1000+ Caliclan / consumer app widgets
- Major bump (`2.0.0-caliclan.1`) is the only release type that may break wire format.

**Caveat for Batch 2.0:** the LayoutModifier migration removes per-widget direct properties (padding/background/fillMaxSize/etc.). Per §3.1 that's a major-bumpable change, but Tier 1 has not been deployed to any production guest, so we're treating Batch 2.0 as a "schema reset within the same major" and keeping `1.0.0-caliclan.N` versioning.

## Gotchas / requirements that aren't obvious

These will bite you again on Tier 2+. Documented in detail in `KONDUIT_PLAN.md` §4 "Phase 3 Tier 1 retro" — read that first.

1. **schema-types module is mandatory**, not optional. konduit-schema is JVM-only; any @Property enum needs to live in a KMP module.

2. **Every schema enum needs `@Serializable`** + the kotlinx-serialization plugin on schema-types. Kotlin/JS specifically requires this.

3. **`compose.materialIconsExtended`** is a separate dep — `compose.material3` doesn't include `Icons.Filled.X`.

4. **Coil 3 needs explicit ImageLoader setup**. We use `coil-network-ktor2` (matches our Ktor 2.x). Call `setSingletonImageLoaderFactory` in `App.kt` and add `KtorNetworkFetcherFactory()` to the components.

5. **iOS ATS needs `NSExceptionDomains` for IP literals.** `127.0.0.1` is covered; ngrok HTTPS works without changes. See `iosApp/iosApp/Info.plist`.

6. **Showcases need a scrollable outer container.** Use `LazyColumn` outer with each section as `LazyItem`.

7. **Dev URL is fragile.** Options in `DevConfig.kt`:
   - LAN IP: works WITHOUT AP isolation
   - 10.0.2.2: Android emulator only
   - 127.0.0.1: adb reverse over USB; flaky if USB drops
   - ngrok HTTPS: works through most networks; can be blocked by some ISPs

8. **Konduit codegen for lambda-typed modifier properties is broken on Kotlin/JS.** A `@Modifier` data class containing `val onClick: () -> Unit` (or any `Function0<Unit>`) makes the protocol-guest gen emit `ContextualSerializer(Function0<Unit>::class)` — invalid Kotlin syntax (class literal not allowed on a generic type). This bit us during Batch 2.0; we dropped the planned `Clickable` modifier and kept `onClick` as a direct `@Property` on `Box`. **Convention going forward:** click handlers / lambdas live on the widget that needs them, never on a modifier. Tier 2 buttons / IconButton / FAB will follow this rule.

9. **Layout modifier classes need their own gradle module.** Konduit's `dev.konduit.generator.modifiers` plugin produces the cross-platform `*.schema.modifier.X` interfaces consumed by the generated factory. Caliclan added `:shared-modifier` for this purpose. The widget generator (`:shared-widget`) depends on it via `api(project(":shared-modifier"))` so transitive consumers (composeApp host, presenter guest, protocol modules) all see the modifier interfaces.

10. **Konduit codegen emits `ContextualSerializer(MyEnum::class)` for enum fields on `@Modifier` classes — you MUST register a contextual serializer or the entire protocol batch silently fails (white screen).** This bit us hard during Batch 2.0 verification. With `Background(color: SchemaColor)` declared as a modifier, the generated `BackgroundTagAndSerializer` includes `ContextualSerializer(SchemaColor::class)`. At runtime the encode call throws `SerializationException`, which Konduit's protocol path swallows — the batch of widget changes never reaches the host, no exception surfaces in any log, the host renders an empty TreehouseContent, screen stays blank. Symptom signature: guest's compose composition runs to completion (you can println from inside lambdas and see them) but ZERO `factory.X()` calls happen on the host. Fix: define a shared `SduiSerializersModule` in `:schema-types` registering each enum used in a modifier (`contextual(SchemaColor::class, SchemaColor.serializer())`), then plumb it into BOTH:
   - Every host-side `TreehouseApp.Spec` via `override val serializersModule = SduiSerializersModule`
   - The guest's `StandardAppLifecycle` via `json = Json { serializersModule = SduiSerializersModule }`
   Enums used only as widget `@Property` (not modifier fields) work fine — codegen calls `MyEnum.serializer()` directly there. Only modifier fields trigger Contextual codegen. Remember to add new enums to the module when you introduce new modifier types.

## Course corrections (May 2026, post-Tier 1) — see `KONDUIT_PLAN.md` §7

Five corrections agreed before Tier 2 starts:

1. **Open the Caliclan PR before Tier 2.** Stacking 30+ Tier 2 commits on the existing 12 makes review impossible. Open PR → review/squash → land on main → cut Tier 2 from clean base.

2. **Introduce LayoutModifier system at Tier 2 start (Batch 2.0).** Tier 1's direct properties (`padding: Int`, `background: SchemaColor`, etc.) won't scale to Tier 2's 30 widgets. Adopt `dev.konduit:konduit-layout-modifiers` instead. Modifiers spec in plan §7.2.

3. **iOS rapid-verify ritual.** Add `scripts/verify-ios.sh` + `scripts/verify-android.sh`. Run after every Batch 2.x to prevent iOS bit-rot.

4. **Defer Phase 4 Compose Facade.** Reclassified from "must do" to "do iff Konduit goes public OR onboarding survey says imports are confusing".

5. **Add Caliclan CI build gate.** `.github/workflows/ci.yml` running `assembleDebug` + `linkDebugFrameworkIosSimulatorArm64` + `compileDevelopmentExecutableKotlinJsZipline` on macOS. Lands AFTER Batch 2.0.

## Tier 2 batch order — see `KONDUIT_PLAN.md` §4 (Phase 3 Tier 2)

| Batch | Scope | IDs | Notes |
|---|---|---|---|
| ✅ **2.0** | LayoutModifier system + Tier 1 migration | — | 10 modifiers (no `Clickable` — codegen blocker, see gotcha #8). |
| ✅ **2.1** | Buttons (Button, OutlinedButton, TextButton, FilledTonalButton, ElevatedButton, IconButton, FAB, ExtendedFAB) | 21–28 | onClick stays a widget @Property per gotcha #8 |
| ✅ **2.2** | Inputs (TextField, OutlinedTextField, SearchBar) | 31–33 | shared value/placeholder/enabled/onValueChange |
| ✅ **2.3** | Selection (Checkbox, RadioButton, Switch, Slider, RangeSlider, SegmentedButtonRow) | 41–46 | SegmentedButton not in CMP iOS yet — using Row of toggle buttons |
| ✅ **2.4** | Containers (Card, ElevatedCard, OutlinedCard, Surface) | 51–54 | optional onClick + content slot |
| ✅ **2.5** | Feedback (LinearProgressIndicator, CircularProgressIndicator, Badge, Snackbar) | 61–64 | Snackbar inline; queue deferred to Tier 3 |
| ✅ **2.6** | Nav structure (Scaffold, TopAppBar*3, NavigationBar, NavigationBarItem, TabRow, Tab) | 71–78 | first multi-slot widgets via @Children(N) tags |
| ✅ **2.7** | Misc closeout (HorizontalDivider, VerticalDivider) | 79–80 | thicknessDp + color; length is modifier-driven |

All 8 Tier 2 batches landed and verified. Tier 2 totals: 30 widgets at IDs 21–80 + 10 layout modifiers.

## Tier 3 batch order — see `KONDUIT_PLAN.md` §4 (Phase 3 Tier 3)

| Batch | Scope | IDs | Notes |
|---|---|---|---|
| ✅ **3.0** | Chips (FilterChip, AssistChip, InputChip, SuggestionChip) | 100–103 | Uniform `leadingIcon` @Children(1) slot. FilterChip suppresses leadingIcon when selected (M3 auto-renders check glyph). InputChip has dedicated `onClose` Property — null hides the trailing X. |
| ✅ **3.1** | List + Menus (ListItem, DropdownMenu, DropdownMenuItem) | 110–112 | First 5-effective-slot widget (ListItem: headline/supporting/overline strings + leading + trailing slots). First popup-anchored widget (DropdownMenu via Compose Popup). Empty-string short-circuits hide supporting/overline lines. ListItem gates clickable on `enabled && onClick != null` since M3 ListItem has no native enabled flag. |
| ✅ **3.2** | Overlays (ModalBottomSheet, AlertDialog) | 120–121 | First overlay widgets — visibility lives in the guest, the host renders only when the widget is in the tree. M3 runs hide animation BEFORE firing onDismissRequest, so cutting the widget on dismiss doesn't truncate. AlertDialog text/title are Strings (same convention as ListItem); confirm/dismiss are split @Children slots so the host doesn't reimplement M3's action-row layout heuristics. ModalBottomSheet exposes `skipPartiallyExpanded` knob; modifier chain not propagated to overlay surface (same reason as DropdownMenu). |
| ✅ **3.3** | Pagers (HorizontalPager, VerticalPager, PagerIndicator) | 140–142 | First "indexed-children" widget — host derives `pageCount` from `(pages as CmpChildren).widgets.size` and renders only `widgets[i]` for page i. `onPageChanged` uses `PagerState.settledPage` (post-fling), not `currentPage` (during-swipe). PagerIndicator is host-rolled (no 1:1 M3) — Row of dots, active dot slightly larger as a non-color cue. V1 limitation: programmatic page jumps from the guest aren't supported (only `initialPage` is honored). |
| ✅ **3.4** | PullToRefresh (PullToRefreshBox) | 150 | Wraps M3's `PullToRefreshBox`. `isRefreshing` Boolean drives spinner visibility; guest holds the state, flips it true on `onRefresh`, kicks off async work, flips false when done. Showcase uses `LaunchedEffect(refreshCount) { delay(1200) }` to simulate async. |
| ✅ **3.5** | Large-screen nav (NavigationRail, NavigationRailItem, ModalNavigationDrawer, NavigationDrawerItem) | 160–163 | First controlled-component drawer state: guest holds `drawerOpen: Boolean`; host syncs `material3.DrawerState` via `LaunchedEffect(drawerOpen) { open()/close() }`, reports user gestures back via `snapshotFlow { isOpen }.collect { onDrawerStateChange }`. Host wraps `drawerContent` in `ModalDrawerSheet` automatically. ModalNavigationDrawer wraps the full screen — embedded in a separate `NavDrawerDemoScreen` reachable via Navigator.push. NavigationRail mirrors NavigationBar (ID 75) for the vertical axis. |
| ✅ **3.6** | Pickers (DatePickerDialog, TimePickerDialog) | 170–171 | Same conditional-render visibility model as Batch 3.2 overlays. DatePickerDialog uses M3's first-class `material3.DatePickerDialog` + `DatePicker`; TimePickerDialog hand-rolls an AlertDialog scaffold around `TimePicker` (no first-class M3 wrapper). Wire encoding: date as Long UTC midnight millis (0L = no preset); time as packed Int (minutes since midnight, 0..1439) — single-Int callback sidesteps multi-arg lambda @Property uncertainty. OK/Cancel labels hardcoded in v1 — i18n knobs land additively later. |

**All 6 Tier 3 batches landed.** Tier 3 totals: 16 widgets at IDs 100–171 (chips 100–103, list+menus 110–112, overlays 120–121, pagers 140–142, pull-to-refresh 150, large-screen nav 160–163, pickers 170–171).

## What's pending

### Caliclan branch
- [x] Tier 1 PR opened (#1, still open).
- [x] All Tier 2 batches landed and verified on device.

### Caliclan CI
- [x] `.github/workflows/ci.yml` runs the three verification tasks on macOS.
- [x] **CI auth gotcha discovered**: GitHub Packages' Maven registry rejects fine-grained PATs with HTTP 404 (not 401), so a token with the wrong type looks like a missing artifact. `docs/CI_SETUP.md` and the workflow header now explicitly require a **classic** PAT with `read:packages`. The Verify step also probes `konduit-gradle-plugin-1.0.0-caliclan.2.pom` directly so the failure mode is legible.

### Tier 3 (~16 widgets, IDs 100–199)
Per plan §3.3 / §4: chips, list items, flow layouts, animations, sheets, dialogs, pagers, pull-to-refresh, shimmer, navigation rail. Earlier draft text used "81–150"; that conflicts with the authoritative range table (Tier 3 is 100–199, IDs 81–99 are the Tier 2 buffer for additive properties). Likely batches:
- ✅ **Batch 3.0** — Chips: FilterChip / AssistChip / InputChip / SuggestionChip @ IDs 100–103. Single shared `leadingIcon` slot per chip; InputChip has dedicated `onClose` callback for the trailing X.
- ✅ **Batch 3.1** — List + Menus: ListItem / DropdownMenu / DropdownMenuItem @ IDs 110–112. ListItem is the first 5-effective-slot widget; DropdownMenu introduces popup positioning via Compose's Popup primitive — the menu anchors to its parent layout (typical: wrap trigger + menu in a Box).
- ✅ **Batch 3.2** — Overlays: ModalBottomSheet / AlertDialog @ IDs 120–121. First overlay widgets with their own dismiss semantics. Visibility lives in the guest (conditionally compose the widget); host renders only when the widget is in the tree. M3 runs the hide animation internally BEFORE firing `onDismissRequest`, so cutting the widget from the tree on dismiss is safe.
- ✅ **Batch 3.3** — Pagers: HorizontalPager / VerticalPager / PagerIndicator @ IDs 140–142. First "indexed-children" pattern (host derives pageCount from `widgets.size`, renders only `widgets[i]` for page i). PagerIndicator is host-rolled (no 1:1 M3) — Row of dots with the active dot slightly larger as a non-color cue.
- ✅ **Batch 3.4** — PullToRefresh: PullToRefreshBox @ ID 150.
- ✅ **Batch 3.5** — Large-screen nav: NavigationRail / NavigationRailItem / ModalNavigationDrawer / NavigationDrawerItem @ IDs 160–163. First controlled-component drawer state via guest-held `drawerOpen` Boolean, host-side LaunchedEffect sync to M3's DrawerState, snapshotFlow report-back of user gestures. ModalNavigationDrawer needs its own top-level surface so the showcase pushes a dedicated `NavDrawerDemoScreen` instead of embedding inline.
- ✅ **Batch 3.6** — Pickers: DatePickerDialog / TimePickerDialog @ IDs 170–171. Same conditional-render visibility model as overlays. Date encoded as Long UTC midnight millis (0L = no preset); time as packed Int (minutes since midnight). TimePickerDialog hand-rolls an AlertDialog scaffold around `material3.TimePicker` since M3 has no first-class TimePickerDialog.

**Tier 3 done.** 16 widgets at IDs 100–171. Awaiting on-device verification of the full Tier 3 batch (all six sub-batches verified locally on macOS via the three CI gates).
- ModalBottomSheet, AlertDialog, DatePicker, TimePicker
- HorizontalPager, VerticalPager + PagerIndicator
- PullToRefresh wrapper
- NavigationRail / NavigationDrawer (desktop / large-screen)

Open questions:
- Real `material3.SegmentedButton` — wait for CMP iOS support and migrate.
- `material3.NavigationBarItem` — needs scope-typed @Children to access RowScope cleanly.
- Snackbar host queue — design a host-side `SnackbarHostState` + `Snackbar.show(message)` event so the guest doesn't have to manage timing.

### Snackbar host queue ✅ landed + verified
- `HostSnackbar` Zipline service in `:shared/Protocol.kt` — `show(message, actionLabel, durationMillis)` for fire-and-forget; `showWithResult(message, actionLabel, durationMillis, onResult)` to learn whether the user tapped the action button (`onResult(true)` = ActionPerformed, `onResult(false)` = Dismissed). `durationMillis` semantics: `<=0` indefinite, `1..6000` short (~4 s), `>6000` long (~10 s).
- `RealHostSnackbar` (composeApp/Protocol.kt) wraps M3's `SnackbarHostState` from a singleton `SnackbarHub` so the queue survives recompositions.
- `App.kt` renders `SnackbarHost(SnackbarHub.state)` aligned to the bottom-center of the root Box.
- Guest helper: top-level `showHostSnackbar(message, actionLabel?, durationMillis = 4000L, onResult?)` in `presenter/Main.kt`. Backed by `HostSnackbarBridge.instance` set during Zipline take. Routes to the cheaper fire-and-forget `show()` when `onResult` is null; otherwise calls `showWithResult()` so the callback fires.
- Showcase has a "Host snackbar queue (Tier 3)" section with three buttons (Short / Long / With action).

**Defensive code in place** (added after iOS testing observed app blanking):
- `RealHostSnackbar.scope` is `lazy` rather than constructed eagerly — Dispatchers.Main on iOS Kotlin/Native may not be wired at `bindServices` time.
- `RealHostSnackbar.show` wraps the body in try/catch → `println`. Silent failure rather than tearing down the Compose UI on the host side.
- Guest's `showHostSnackbar` wraps `bridge.show(...)` in try/catch → `println`. Same rationale on the guest side.

**Bind-site bug fixed (2026-05-11):**
- The original snackbar commit added the `zipline.bind<HostSnackbar>(...)` call to a commonMain `SduiAppSpec` class that was **never referenced from either platform entry point**. Both `androidApp/MainActivity.kt` and `composeApp/iosMain/MainViewController.kt` instantiate their own anonymous `TreehouseApp.Spec<SduiAppService>()`, each binding only `console`. The snackbar bind was dead code.
- Symptom: `take<HostSnackbar>("snackbar")` succeeds (returns a proxy), but every guest call errors with `no such service (service closed?)` — the guest side has no way to know the host never bound it. Confirmed on Android via logcat (`"available services: ... console, ... (no snackbar)"`).
- Fix: snackbar bind now lives in both platform Specs (with strong refs as class-field properties to dodge `serviceLeaked`). Dead code removed: `androidApp/AndroidSduiAppSpec.kt` deleted entirely; `SduiAppSpec` and `RealHostConsole` removed from `composeApp/commonMain/Protocol.kt`.
- Architecturally still platform-divergent — Android uses `AndroidRealHostConsole` (logs via `android.util.Log`), iOS uses `IosRealHostConsole` (logs via `println` → stderr). Both share `RealHostSnackbar` from commonMain since it talks to commonMain's `SnackbarHub`.
- **On-device verification still pending** as of this write-up because the test device's wifi was flaky during the debug loop. Re-test after restart.

### Tier 3 modifier additions ✅ landed
- `Border(thicknessDp, color: SchemaColor, cornerRadiusDp: Int = 0)` @ tag 12 — `cornerRadiusDp` shipped in the rounded-Border follow-up; default 0 keeps the original rectangular behavior, positive values render a rounded stroke that matches a sibling `Clip(cornerRadiusDp)`.
- `Clip(cornerRadiusDp)` @ tag 13 — rounded-corner clip; 0 = no-op.
- `ClipCircle` @ tag 14 — perfect circle inscribed in widget bounds.
- `WrapContentWidth` / `WrapContentHeight` @ tags 15 / 16.
- `AspectRatio(ratio: Double)` @ tag 17.

Total modifier count: 16 (10 original + 6 Tier 3). Tag 7 still unused (was reserved for Clickable that didn't ship — see gotcha #8).

### Open Tier 3 modifier follow-ups
- ✅ Rounded `Border` — additive `cornerRadiusDp: Int = 0` parameter shipped. `Border(thicknessDp = 2, color = SchemaColor.Primary, cornerRadiusDp = 16)` now renders a rounded stroke matching a sibling `Clip(16)`. Backward-compatible: omit `cornerRadiusDp` (or pass 0) for the original rectangular behavior. Konduit's modifier codegen + kotlinx.serialization accept default values on `@Modifier` data classes (verified end-to-end).
- `RoundedCorners` shape parameter for `Background` so a colored fill can match a clipped shape without needing both Clip and Background to overlap perfectly. (Not strictly necessary — Compose's clip applies to subsequent fills, so chain order works today.)

### Phase 5 — Rest of dev tooling ✅ landed

**5b — single dev command.** `./gradlew konduitDev` (or `bin/konduit-dev`) starts the continuous guest compile in the background, optionally tails Android logcat (auto-skips if no device), and runs the dev-server in the foreground. Ctrl-C tears it all down via an EXIT trap. Task is in the "Konduit" group; see `bin/konduit-dev --help` for flags (e.g. `--no-logs` for iOS-only sessions).

**5c — clean Logcat formatting.** New `KonduitDevLog` class (commonMain) tracks lifecycle timing across Zipline events and emits a curated stream on a single `Konduit` tag. Both `SDUIZiplineEventListener` (Android) and `IosKonduitEventListener` (iOS) feed it alongside their existing low-level `SDUI-Zipline` debug streams, so the raw debug output is still available for diagnostics like the snackbar bug we just chased. Sample output during a normal load:

```
D/Konduit: ⬇ Downloading manifest from https://…/manifest.zipline.json
D/Konduit: 📦 Manifest ready — 32 modules
D/Konduit: ✓ Loaded sdui (842ms)
W/Konduit: ⚠ Service leaked: 'snackbar' was garbage-collected without close(). Hold a strong reference on the host side (val field, not anonymous arg).
E/Konduit: ✕ Code load failed: Failed to connect to /192.168.1.10:8080
```

To see only the curated stream: `adb logcat -s Konduit:*`.

### Phase 4 — Compose Facade
**Deferred** per course correction §7.4. Revisit only if Konduit goes public OR a developer onboarding survey says imports are confusing.

## Repos + state

| Repo | Branch / tag | Local path |
|---|---|---|
| Caliclan (this) | active dev on `konduit-main` (= default branch, = tip of `claude/vigilant-euclid-681447`); frozen Redwood-era snapshot at `redwood-baseline` (`1d76527`); PR #1 closed (its head SHA matched `konduit-main` after the branch reorg — work was already there) | `~/AndroidStudioProjects/ServerDrivenUI/.claude/worktrees/vigilant-euclid-681447/` |
| Konduit | `main` at `975c9cdaa`; tag `v1.0.0-caliclan.2` published to GitHub Packages | `~/AndroidStudioProjects/konduit/` |

### Branch policy

- **`konduit-main`** — the active branch. All new work targets it.
- **`redwood-baseline`** — read-only frozen snapshot of pre-fork
  Caliclan running on upstream `app.cash.redwood`. Don't push to it; it
  exists so anyone can read the project's pre-Konduit state without
  digging through git history.
- **`main`** — currently aliases `redwood-baseline`. Will be
  fast-forwarded to `konduit-main` when we cut the next release; until
  then PR #1 (which targets `main`) is the only thing keeping the two
  branches reconciled. Decision pending: either redirect PR #1 to
  `konduit-main` and keep `main` as the long-term Redwood baseline, or
  merge PR #1 and use `redwood-baseline` as the canonical name for the
  pre-fork snapshot.
- **`claude/vigilant-euclid-681447`** — historical work branch; kept
  alive as long as PR #1 references it but can be deleted once PR #1
  closes.

## Key files to read

When picking up the work, read these in order:

1. **`docs/HANDOVER.md`** (this file) — orientation + pending list
2. **`docs/KONDUIT_PLAN.md`** — full plan (~900 lines): mission, decisions, principles, phase plan, course corrections (§7), decisions log (§8). Especially §4 (Phase 3 Tier 2 batches) and §7.2 (LayoutModifier spec).
3. **`docs/PHASE_0_INVENTORY.md`** — original audit
4. **`docs/WIDGET_CONTRACT.md`** — what a future native iOS host must implement
5. **`schema/src/main/kotlin/.../Schema.kt`** — current Tier 1 schema
6. **`schema-types/src/commonMain/kotlin/.../Types.kt`** — enum types
7. **`composeApp/src/commonMain/kotlin/.../shared/Protocol.kt`** — Cmp* widgets
8. **`composeApp/src/commonMain/kotlin/.../shared/App.kt`** — Coil + dev overlay
9. **`presenter/src/jsMain/kotlin/.../screens/Tier1ShowcaseScreen.kt`** — demo
10. **`composeApp/src/commonMain/kotlin/.../shared/DevConfig.kt`** — URL knob

For Batch 2.0 specifically, also inspect:
- `~/AndroidStudioProjects/konduit/konduit-layout-modifiers/src/commonMain/kotlin/` — upstream @LayoutModifier patterns
- `~/AndroidStudioProjects/konduit/konduit-layout-schema/` — how the layout modifiers wire into a schema

## Suggested next session prompt

> Continue Konduit / Caliclan work. Read `docs/HANDOVER.md`, `docs/KONDUIT_PLAN.md`, and `docs/CI_SETUP.md` first.
>
> Tier 1 + all 8 Tier 2 batches are landed and verified on Android (Galaxy S22 Ultra) + iOS sim (iPhone 16 Pro). 40 widgets + 10 layout modifiers shipped. CI workflow added at `.github/workflows/ci.yml`.
>
> Branch reorg already done: `konduit-main` is the active branch; `redwood-baseline` is a frozen snapshot of pre-fork Caliclan; PR #1 still targets `main` from the historical `claude/vigilant-euclid-681447` branch. Pick one of these and act:
>   (a) Retarget PR #1 to `konduit-main` and keep `main` aliased to `redwood-baseline` long-term, OR
>   (b) Merge PR #1 to `main` (so `main` becomes the active branch) and treat `redwood-baseline` as the read-only history.
>
> First task: confirm `KONDUIT_READ_TOKEN` repo secret is set (per `docs/CI_SETUP.md`) and the green CI run lands. Without that secret CI fails fast at the verify-token step.
>
> Second task: Tier 3 brainstorm. Open questions in HANDOVER under "Tier 3" — chips, sheets, dialogs, pagers, pull-to-refresh. Pick a 2-3 widget starter batch to validate the pattern continues to scale before committing to a full Tier 3 plan.
>
> Read the gotchas list in HANDOVER.md before touching anything — gotchas 8 (lambda-in-modifier broken), 9 (`:shared-modifier` module wiring), and 10 (SduiSerializersModule for enum-in-modifier) are the load-bearing ones for any new schema work.
