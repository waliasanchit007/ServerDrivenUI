# Changelog

Resolved entries from `docs/KNOWN_BUGS.md`. Sorted newest-first; each
entry links to the fix commit so external integrators can find the
shape of the fix via `git log` search.

Format is loosely [Keep a Changelog](https://keepachangelog.com/) —
each entry has the originating bug number from KNOWN_BUGS, a one-line
summary, and the fix commit.

---

## Unreleased

### Konduit fork (`1.0.0-caliclan.3-SNAPSHOT`)

- **KNOWN_BUGS U7** — `TreehouseApp.Spec.retain(service)` helper shipped.
  Strong-ref pass-through that keeps anonymous inline service
  implementations alive for the lifetime of the Spec, so the first
  guest call no longer fails with "no such service (service closed?)".
  Use as `zipline.bind<HostX>("x", retain(object : HostX { … }))`.
  Lives in `konduit-treehouse-host`'s `TreehouseApp.kt`. Validated end-
  to-end in DevoStatus (`KonduitDemoScreen.kt`).
- **KNOWN_BUGS U10** — Modifier-serializer codegen no longer
  white-screens on enum properties. The protocol-guest generator
  (`konduit-tooling-codegen` commit `79a314004`) now emits
  `ContextualSerializer(MyEnum::class, MyEnum.serializer(), emptyArray())`
  for every non-parameterized `ClassName` typed property, so the
  fallback path resolves the auto-generated `.serializer()` companion
  whether or not the integrator registered a contextual serializer.
  Worst documented failure mode in the integration — silent
  SerializationException at encode → swallowed by protocol path →
  blank `TreehouseContent` with zero logs — is now closed. The
  `SduiSerializersModule` workaround is redundant but kept (harmless).
- **KNOWN_BUGS U1 + U2** (mitigations) — `Spec.bindWithTimeout { block }`
  helper turns both silent-hang failure shapes into an actionable
  `ZiplineBindTimeoutException` after 30s. Exception message lists
  both candidates (U1 suspect-signature shape; U2 missing Zipline
  Gradle plugin) plus the workaround for each. Used in DevoStatus's
  `KonduitDemoScreen.kt` as the reference pattern.
- **KNOWN_BUGS U3** (mitigation) — `Spec.requireSerializerOf<T>()`
  bind-time pre-flight check throws `MissingSerializerException` if a
  `@Serializable` wire type's serializer can't be resolved. Catches
  the missing-kotlinx-serialization-plugin shape at bind time instead
  of waiting for the first guest call to send the type across the
  wire. DevoStatus's `KonduitQuotesScreen.kt` validates `Quote` this
  way.

### Fixed

- **KNOWN_BUGS #6** — `coil-network-ktor2` conflict with consumer apps
  using Ktor 3. composeApp's Coil network fetcher is now platform-split:
  `coil-network-okhttp` on Android (zero Ktor footprint — OkHttp is
  already a Zipline transitive dep) + `coil-network-ktor2` on iOS (still
  matches `ktor-client-darwin`). Common `App.kt` now uses an
  `installCoilNetworkFetcher()` expect/actual hook. Bumped in commit
  TBD.

- **KNOWN_BUGS #4** — Schema modifier gap (alpha, offset, border,
  custom fonts). All four shipped:
  - `@Modifier(11) Alpha(value: Double)` — direct `Modifier.alpha`
    mapping. Commit `58815b1`.
  - `@Modifier(12) Border(...)` — `SchemaBorderStroke`-style fields,
    maps to `Modifier.border(width, color, shape)`. Shipped as part of
    Tier 3 modifier additions.
  - `@Modifier(18) Offset(x: Int, y: Int)` — dp units, negative
    allowed. Maps to `Modifier.offset(x.dp, y.dp)`. Commit `b62d366`.
  - `SchemaFontFamily` enum (Default/Serif/SansSerif/Monospace/Cursive)
    plumbed through Text widget. Maps to Compose `FontFamily.*` slots.
    Commit `959d5bb` (text font props) + later extensions.

- **KNOWN_BUGS #9** — "lateinit var services inside bindServices
  silently skip binding on 2nd TreehouseApp mount." Root cause pinned
  in commit `a2ed1a0`: integrator's `remember(...)` was keying on
  unstable lambdas (anonymous lambdas get a new identity per
  recomposition), creating a fresh TreehouseApp on every recomposition
  and racing the previous one's Zipline runtime. Resolution is
  documented + canonical helper `rememberKonduitApp` ships in commit
  `97be91e`.

---

## Older — pre-1.0

Schema additions that closed visual-fidelity gaps surfaced by the
DevoStatus integration:

| Commit | Feature |
|---|---|
| `bf2d1fd` | `AnimatedVisibility` widget with curated `SchemaTransition` set |
| `58417a4` | Long-press + double-tap gestures on Box / Card |
| `205223b` | Brand-color escape hatch: `CustomBackground` modifier + `customColorArgb` on Text/Icon/Card |
| `08c087e` | WindowInsets-aware modifiers (StatusBars / NavigationBars / ImePadding / SystemBars / SafeContent / DisplayCutout) |
| `c1f3876` | `cornerRadiusDp` on buttons / cards / surface / chips |
| `959d5bb` | Text `fontSizeSp` / `lineHeightSp` / `letterSpacingHundredthsSp` overrides |
| `89cdcb7` | `Card.containerColor` + `Card.contentColor` |
| `b62d366` | `Offset` modifier (closes #4 sub-item) + extra Text/Box style props |

---

## Process

When you fix a bug from KNOWN_BUGS.md:

1. Land the fix in a feature commit.
2. Add a `### Fixed` line here under Unreleased, citing the
   KNOWN_BUGS entry number + the fix commit hash.
3. Either delete the bug's section from KNOWN_BUGS.md, or annotate it
   "**Status:** Fixed in commit `<hash>` — see CHANGELOG."
4. (Optional) write a regression test that fails on the parent commit
   and passes on the fix commit.
