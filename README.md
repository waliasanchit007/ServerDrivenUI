# Caliclan / Konduit playground

A Compose Multiplatform server-driven UI app on top of a private fork of
CashApp's [Redwood](https://github.com/cashapp/redwood), renamed to
**Konduit**. Targets Android (Galaxy S22 Ultra) and iOS sim today; the
host is pure Compose Multiplatform so adding desktop / wasm later is
mostly a wiring exercise.

## Branches

This repo intentionally keeps **two parallel branches** so you can read
either era of the project without git-archeology.

| Branch | What it is | When to read |
|---|---|---|
| **`redwood-baseline`** | Frozen snapshot of the original Caliclan running on upstream `app.cash.redwood`. Shipped guest-driven nav and the welcome screen. | If you want to see how a stock Redwood SDUI app is wired before any of the renames. |
| **`konduit-main`** | Active development. Everything since the Konduit rename: the soft fork, GitHub-Packages publishing, layout-modifier system, and the full Tier 1 + Tier 2 widget catalog with showcase. | If you want to use or extend the SDUI app today. **All ongoing work targets this branch.** |
| `main` | Currently aliases the Redwood baseline tip; will be moved when we cut a release on `konduit-main`. |

The `redwood-baseline` branch is **read-only by convention** — bug fixes
and new features land on `konduit-main` only.

## What's on `konduit-main` (today)

- **56 server-rendered widgets**: 10 Tier 1 (foundation) + 30 Tier 2
  (M3 essentials) + 16 Tier 3 (chips, list+menus, overlays, pagers,
  pull-to-refresh, large-screen nav, pickers). IDs 1–171.
- 16 layout modifiers (padding, size, fillMax*, background, weight, alpha, **border, clip, clipCircle, wrapContent*, aspectRatio**).
- Hot-reload dev loop via Zipline + a 1-shot dev server.
- Verified end-to-end on real Android + iOS sim — see the showcase in
  `presenter/src/jsMain/.../screens/Tier1ShowcaseScreen.kt` and the
  drawer demo in `screens/NavDrawerDemoScreen.kt`.

Big-picture writeup in `docs/HANDOVER.md`. Full plan + decisions log in
`docs/KONDUIT_PLAN.md`.

## Repo layout

```
schema/                 @Schema / @Widget definitions (JVM-only)
schema-types/           @Serializable enums + SduiSerializersModule (KMP)
shared-modifier/        layout-modifier interfaces (generated)
shared-widget/          widget interfaces (generated)
shared-protocol-host/   host protocol adapter (generated)
shared-protocol-guest/  guest protocol adapter (generated)
shared/                 shared services / DTOs
composeApp/             KMP host: Cmp* widgets, Treehouse wiring, Coil
presenter/              Kotlin/JS guest: RootUi, Navigator, screens
androidApp/             Android entry point
iosApp/                 iOS entry point
dev-server/             Ktor file server + WebSocket hot reload
docs/                   HANDOVER, plan, CI setup, gotchas
```

## Building

### Local development (recommended)

You need a checkout of the [Konduit fork](https://github.com/waliasanchit007/konduit)
publishing to your local Maven cache:

```bash
cd ~/AndroidStudioProjects/konduit
./gradlew publishToMavenLocal -DRELEASE_SIGNING_ENABLED=false
```

Then for Caliclan:

```bash
# Android — install on the connected device.
./gradlew :androidApp:installDebug

# iOS sim — open in Xcode and ⌘R.
open iosApp/iosApp.xcodeproj

# Dev server (in a separate terminal).
./gradlew :dev-server:run
```

Manifest URL configured in `composeApp/src/commonMain/.../shared/DevConfig.kt`.

### CI

GitHub Actions runs `:androidApp:assembleDebug` +
`:composeApp:linkDebugFrameworkIosSimulatorArm64` +
`:presenter:compileDevelopmentExecutableKotlinJsZipline` on macOS for
every push and PR. **Setup requires a `KONDUIT_READ_TOKEN` repo secret**
— see `docs/CI_SETUP.md`.

## License / status

Internal playground. Not yet stable. Wire format is locked within
`1.0.0-caliclan.N`; major bumps may break wire format.
