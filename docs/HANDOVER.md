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

**Konduit fork** (private): `https://github.com/waliasanchit007/konduit`
- `main` at `975c9cdaa` (Phase 2 + Linux CI drop)
- Tagged `v1.0.0-caliclan.2` published to GitHub Packages

**Caliclan branch:** `claude/vigilant-euclid-681447` (10 commits ahead of main)
- `cbbc349` Phase 1 migration
- `5b038a7` Plan v2
- `5b0c556` Bump to caliclan.2
- `0f899fc` Phase 3a dev tooling
- `6dbaa4a` Phase 3 Tier 1 schema + showcase
- `8971608` Tier 1 runtime fixes (Serializable + Coil + DevConfig)
- `30c3ead` Coil multiplatform via Ktor
- `fe13506` iOS ATS exception + ngrok URL restored
- `b23c2c3` Coil ktor2 fetcher + scrollable showcase

## Current state

**Caliclan runs on `dev.konduit:1.0.0-caliclan.2` from mavenLocal.**

The Tier 1 showcase screen demonstrates every Tier 1 widget:
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

Verified end-to-end on Android (Galaxy S22 Ultra).
iOS framework links cleanly; runtime test deferred until network cooperates.

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│ Caliclan (this repo)                                             │
│                                                                  │
│   schema/         (JVM-only) @Schema/@Widget defs               │
│   schema-types/   (KMP)      @Serializable enums                │
│      ├── SchemaColor  → MaterialTheme.colorScheme               │
│      ├── SchemaTextStyle → MaterialTheme.typography             │
│      ├── SchemaArrangement / Horizontal/VerticalAlignment       │
│      └── SchemaIconName → Material icons                        │
│                                                                  │
│   shared-widget/        (KMP) generated widget interfaces       │
│   shared-protocol-host/ (KMP) generated host protocol           │
│   shared-protocol-guest/(JS)  generated guest protocol          │
│   shared/               (KMP) services + DTOs                   │
│                                                                  │
│   composeApp/   (KMP host)                                      │
│      ├── Protocol.kt    Cmp* impls + CmpWidgetFactory           │
│      ├── App.kt         KonduitContent + dev overlay + Coil     │
│      ├── DevConfig.kt   manifest URL knob                       │
│      └── KonduitDev*    error boundary + reload state machine   │
│                                                                  │
│   presenter/   (Kotlin/JS guest)                                │
│      ├── RootUi.kt              ScreenStack + BackHandler       │
│      ├── Navigator.kt           guest-side nav stack            │
│      └── screens/Tier1ShowcaseScreen.kt                         │
│                                                                  │
│   androidApp/, iosApp/, dev-server/                             │
└──────────────────────────────────────────────────────────────────┘
                  │ depends on (mavenLocal/GitHub Packages)
                  ▼
┌──────────────────────────────────────────────────────────────────┐
│ Konduit (~/AndroidStudioProjects/konduit, github.com/...)        │
│   47 modules at dev.konduit group, 1.0.0-caliclan.2 version     │
│   build-support plugin sets group/version centrally             │
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
- Major bump (`2.0.0-caliclan.1`) is the only release type that may break
  wire format.

## Gotchas / requirements that aren't obvious

These will bite you again on Tier 2+. They're documented in `KONDUIT_PLAN.md`
§4 "Phase 3 Tier 1 retro" — read that first.

1. **schema-types module is mandatory**, not optional. Konduit-schema is JVM-only;
   any @Property enum needs to live in a KMP module.

2. **Every schema enum needs `@Serializable`** + the kotlinx-serialization plugin
   on schema-types. Kotlin/JS specifically requires this.

3. **`compose.materialIconsExtended`** is a separate dep — `compose.material3`
   doesn't include `Icons.Filled.X`.

4. **Coil 3 needs explicit ImageLoader setup**. We use `coil-network-ktor2`
   (Ktor 2.x compat). Call `setSingletonImageLoaderFactory` in `App.kt` and
   add `KtorNetworkFetcherFactory()` to the components. See `App.kt`.

5. **iOS ATS needs `NSExceptionDomains` for IP literals.** `127.0.0.1` is
   covered; ngrok HTTPS works without changes. See `iosApp/iosApp/Info.plist`.

6. **Showcases need a scrollable outer container.** Use `LazyColumn` outer
   with each section as `LazyItem`.

7. **Dev URL is fragile.** Options in `DevConfig.kt`:
   - LAN IP: works WITHOUT AP isolation
   - 10.0.2.2: Android emulator only
   - 127.0.0.1: adb reverse over USB; flaky if USB drops
   - ngrok HTTPS: works through most networks; can be blocked by some ISPs

## What's pending

### Tier 1 closure
- [x] All 10 widgets verified rendering on Android (Galaxy S22 Ultra) —
      including AsyncImage with the `coil-network-ktor2` + scrollable
      `LazyColumn` fix at `b23c2c3`.
- [ ] Verify Tier 1 showcase end-to-end on iOS sim (build green; runtime
      pending — user's Wi-Fi has been flaky for ngrok edge transit, so
      retry whenever the next session has a stable network)

### Caliclan branch
- [ ] Push `claude/vigilant-euclid-681447` to origin (10 commits, no PR yet)

### Tier 2 (next major work)
Not yet started. Per plan v2 §4: ~30 widgets across IDs 21–80
(buttons → text fields → selection → containers → feedback → navigation).
Tier 2 sizing re-estimated after Tier 1: 2–3 batches of 10 widgets each.

### Phase 4 — Compose Facade
After Tier 2/3 land. Module `konduit-compose-facade` mirroring
`androidx.compose.*` packages so guest code looks identical to real CMP.

### Phase 5 — Rest of dev tooling
Single `./gradlew konduit:dev` command + clean Logcat formatting.

## Repos + state

| Repo | Branch / tag | Local path |
|---|---|---|
| Caliclan (this) | `claude/vigilant-euclid-681447` (10 commits ahead of main, unpushed) | `~/AndroidStudioProjects/ServerDrivenUI/.claude/worktrees/vigilant-euclid-681447/` |
| Konduit | `main` at `975c9cdaa`; tag `v1.0.0-caliclan.2` published | `~/AndroidStudioProjects/konduit/` |

## Key files to read

When picking up the work, read these in order:

1. **`docs/KONDUIT_PLAN.md`** — full plan (~700 lines), v2 with Tier 1 retro
2. **`docs/PHASE_0_INVENTORY.md`** — original audit
3. **`docs/WIDGET_CONTRACT.md`** — what a future native iOS host must implement
4. **`schema/src/main/kotlin/.../Schema.kt`** — current schema (Tier 1 + nav)
5. **`schema-types/src/commonMain/kotlin/.../Types.kt`** — enum types
6. **`composeApp/src/commonMain/kotlin/.../shared/Protocol.kt`** — Cmp* widgets
7. **`composeApp/src/commonMain/kotlin/.../shared/App.kt`** — Coil + dev overlay
8. **`presenter/src/jsMain/kotlin/.../screens/Tier1ShowcaseScreen.kt`** — demo
9. **`composeApp/src/commonMain/kotlin/.../shared/DevConfig.kt`** — URL knob

## Suggested next session prompt

> Continue Konduit work. Read docs/HANDOVER.md and docs/KONDUIT_PLAN.md
> first. The Caliclan branch `claude/vigilant-euclid-681447` has 10 unpushed
> commits; Tier 1 is complete and runtime-verified on Android. Next work
> is your call: (a) close iOS runtime gap; (b) start Tier 2 widgets;
> (c) push the Caliclan branch and open a PR; (d) something else.
