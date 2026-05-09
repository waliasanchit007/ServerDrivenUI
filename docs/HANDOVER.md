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

## What's pending

### Caliclan branch
- [x] Tier 1 PR opened (#1, still open).
- [x] All Tier 2 batches landed and verified on device.

### Caliclan CI
- [x] `.github/workflows/ci.yml` runs the three verification tasks on macOS. **Setup required:** add `KONDUIT_READ_TOKEN` repo secret — see `docs/CI_SETUP.md`.

### Tier 3 (~16 widgets, IDs 81–150)
Per plan §4: chips, list items, flow layouts, animations, sheets, dialogs, pagers, pull-to-refresh, shimmer, navigation rail. Re-spec'd after Tier 2 settles. Likely batches:
- Chips (FilterChip, AssistChip, InputChip, SuggestionChip)
- ListItem + DropdownMenu/MenuItem
- ModalBottomSheet, AlertDialog, DatePicker, TimePicker
- HorizontalPager, VerticalPager + PagerIndicator
- PullToRefresh wrapper
- NavigationRail / NavigationDrawer (desktop / large-screen)

Open questions:
- Real `material3.SegmentedButton` — wait for CMP iOS support and migrate.
- `material3.NavigationBarItem` — needs scope-typed @Children to access RowScope cleanly.
- Snackbar host queue — design a host-side `SnackbarHostState` + `Snackbar.show(message)` event so the guest doesn't have to manage timing.

### Tier 3 modifier additions to consider
- `border(thicknessDp, color: SchemaColor)`
- `clip(shape: SchemaShape)` — needs SchemaShape enum
- `wrapContentWidth() / wrapContentHeight()`
- `aspectRatio(ratio: Float)`

### Phase 5 — Rest of dev tooling
Single `./gradlew konduit:dev` command + clean Logcat formatting.

### Phase 4 — Compose Facade
**Deferred** per course correction §7.4. Revisit only if Konduit goes public OR a developer onboarding survey says imports are confusing.

## Repos + state

| Repo | Branch / tag | Local path |
|---|---|---|
| Caliclan (this) | `claude/vigilant-euclid-681447` (Tier 1 + all 8 Tier 2 batches landed; PR #1 open) | `~/AndroidStudioProjects/ServerDrivenUI/.claude/worktrees/vigilant-euclid-681447/` |
| Konduit | `main` at `975c9cdaa`; tag `v1.0.0-caliclan.2` published | `~/AndroidStudioProjects/konduit/` |

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

> Continue Konduit / Caliclan work. Read `docs/HANDOVER.md` and `docs/KONDUIT_PLAN.md` first.
>
> Tier 1 + all 8 Tier 2 batches are landed and verified on Android (Galaxy S22 Ultra) + iOS sim (iPhone 16 Pro). 40 widgets + 10 layout modifiers shipped.
>
> First task: add the Caliclan CI build gate (§7.5) — `.github/workflows/ci.yml` running `assembleDebug` + `linkDebugFrameworkIosSimulatorArm64` + `compileDevelopmentExecutableKotlinJsZipline` on macOS. Lock in the verification surface we built up by hand.
>
> Second task: rebase / merge PR #1 onto main, push the now-final batch sequence, and tag a checkpoint (e.g. `caliclan-tier-2`).
>
> Third task: Tier 3 brainstorm. Open questions in HANDOVER under "Tier 3" — chips, sheets, dialogs, pagers, pull-to-refresh. Pick a 2-3 widget starter batch to validate the pattern continues to scale before committing to a full Tier 3 plan.
>
> Read the gotchas list in HANDOVER.md before touching anything — gotchas 8 (lambda-in-modifier broken), 9 (`:shared-modifier` module wiring), and 10 (SduiSerializersModule for enum-in-modifier) are the load-bearing ones for any new schema work.
