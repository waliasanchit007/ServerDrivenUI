# Phase 0 Inventory — Redwood → Konduit Migration

> Snapshot of the ServerDrivenUI (Caliclan) codebase before the Konduit fork.
> Audit date: 2026-04-27. Reference: KONDUIT_PLAN.md, Phase 0.

---

## 1. Dependency Versions

Source: [gradle/libs.versions.toml](../gradle/libs.versions.toml)

| Dependency | Version |
|---|---|
| Redwood (`app.cash.redwood`) | 0.18.0 |
| Zipline | 1.24.0 |
| Kotlin | 2.1.0 |
| Compose Multiplatform | 1.8.0 |
| AGP | 8.13.2 |
| OkHttp | 4.12.0 |
| Ktor | 2.3.12 |
| Coil | 3.0.4 |
| AndroidX Activity | 1.12.2 |
| AndroidX AppCompat | 1.7.1 |
| AndroidX Core | 1.17.0 |
| AndroidX Lifecycle | 2.9.6 |

---

## 2. Module Structure

Source: [settings.gradle.kts](../settings.gradle.kts)

| Module | Targets | Role |
|---|---|---|
| `:composeApp` | Android, iOS | Treehouse host + Cmp* widget impls + App.kt |
| `:androidApp` | Android | Android entry point (MainActivity, TreehouseHelper) |
| `:schema` | JVM | Redwood `@Schema` — defines 13 widgets |
| `:presenter` | JS | Guest screens, Navigator, RootUi |
| `:shared` | JVM, JS, iOS | `SduiAppService`, `HostConsole` interfaces |
| `:shared-widget` | JVM, JS, iOS | Generated widget interfaces |
| `:shared-protocol-host` | JVM, iOS | Generated host protocol |
| `:shared-protocol-guest` | JS | Generated guest protocol |
| `:dev-server` | JVM | Ktor dev server for hot-reload |

---

## 3. Redwood Artifacts In Use

| Artifact | Consumed by |
|---|---|
| `redwood-schema` | `:schema` |
| `redwood-compose` | `:composeApp`, `:presenter` |
| `redwood-widget` | `:composeApp`, `:presenter`, `:shared-widget` |
| `redwood-treehouse` | `:composeApp`, `:presenter`, `:shared` |
| `redwood-treehouse-host` | `:composeApp`, `:androidApp` |
| `redwood-treehouse-host-composeui` | `:composeApp` |
| `redwood-treehouse-guest` | `:presenter` |
| `redwood-protocol` | `:shared` |
| `redwood-protocol-host` | `:shared`, `:shared-protocol-host` |
| `redwood-protocol-guest` | `:shared-protocol-guest` |

### Codegen plugins
`redwood` (schema), `redwood-generator-compose`, `redwood-generator-widget`, `redwood-generator-protocol-host`, `redwood-generator-protocol-guest`.

### Not used (safe to delete in Phase 1 strip)
`redwood-widget-view`, `redwood-widget-uiview`, `redwood-widget-dom`.

---

## 4. Schema Inventory

Source: [schema/src/main/kotlin/com/example/serverdrivenui/schema/Schema.kt](../schema/src/main/kotlin/com/example/serverdrivenui/schema/Schema.kt)

| ID | Widget | Properties | Notes |
|---|---|---|---|
| 1 | MyText | `text: String` | OK |
| 2 | MyButton | `text`, `onClick` | OK |
| 3 | MyColumn | `children` | OK — opinionated layout (fillMaxSize, centered) |
| 4 | FlexRow | `horizontalArrangement: String`, `verticalAlignment: String`, `children` | ⚠ String-typed enums |
| 5 | FlexColumn | `verticalArrangement: String`, `horizontalAlignment: String`, `children` | ⚠ String-typed enums |
| 6 | Box | `children` | Defined, **unused** in any screen |
| 7 | Spacer | `width: Int`, `height: Int` | Implicit `dp` |
| 8 | SduiTextField | `value`, `label`, `placeholder`, `onValueChange` | OK |
| 9 | SduiSwitch | `checked`, `onCheckedChange` | OK |
| 10 | SduiImage | `url`, `contentDescription` | Defined, **unused** |
| 11 | SduiCard | `onClick: (() -> Unit)?`, `children` | OK |
| 12 | ScreenStack | `children` | Navigation root container |
| 13 | BackHandler | `enabled`, `onBack` | Non-visual; CMP BackHandler |

---

## 5. Host Protocol (Cmp* impls)

Source: [composeApp/src/commonMain/kotlin/com/example/serverdrivenui/shared/Protocol.kt](../composeApp/src/commonMain/kotlin/com/example/serverdrivenui/shared/Protocol.kt)

All 13 widgets have matching `Cmp*` classes wired into `CmpWidgetFactory` (Protocol.kt:428–481). String-arrangement parsers at Protocol.kt:302–334 silently default to `Start`/`Top` on unknown values.

---

## 6. Treehouse / App Setup

- **Android entry:** [androidApp/src/main/kotlin/com/example/serverdrivenui/MainActivity.kt](../androidApp/src/main/kotlin/com/example/serverdrivenui/MainActivity.kt) — uses [TreehouseHelper.java](../androidApp/src/main/java/com/example/serverdrivenui/TreehouseHelper.java) to build `TreehouseAppFactory`. Manifest URL from `DevConfig`. Hot-reload via `HotReloadManager` WebSocket. `ManifestVerifier.NO_SIGNATURE_CHECKS` (dev only).
- **Common host composable:** `App(treehouseApp)` wraps `TreehouseContent` with `SduiSchemaWidgetSystem(CmpWidgetFactory)`.
- **Guest entry:** `presenter/.../Main.kt` binds `SduiAppServiceImpl`, takes `HostConsole`, polyfills JS console.
- **Services bound:** `HostConsole` (host → guest log bridge), `SduiAppService` (guest → host launch). No navigation service — guest owns nav state via `Navigator`.

---

## 7. Guest Screens

Source: `presenter/src/jsMain/kotlin/com/example/serverdrivenui/presenter/screens/`

| Screen | Widgets used |
|---|---|
| HomeScreen | FlexColumn, MyText, Spacer, SduiCard, SduiSwitch, MyButton |
| ProfileScreen | FlexColumn, MyText, Spacer, SduiTextField, SduiCard, FlexRow, MyButton |
| SettingsScreen | FlexColumn, MyText, Spacer, MyButton |

`RootUi` wraps the current screen in `BackHandler` + `ScreenStack`. `Navigator` exposes `push/pop/replaceCurrent/replaceAll`.

---

## 8. Concerns to Address In Later Phases

| # | Concern | Phase |
|---|---|---|
| 1 | `FlexRow`/`FlexColumn` arrangement+alignment as `String` with silent fallback | Phase 3 (schema redesign) |
| 2 | `Box` (id 6) and `SduiImage` (id 10) unused in any screen | Phase 3 |
| 3 | `ManifestVerifier.NO_SIGNATURE_CHECKS` in MainActivity | Pre-prod |
| 4 | Verbose `println` logging across Protocol.kt and screens | Phase 5 (dev tooling) |
| 5 | No error boundary around guest UI — guest crashes can propagate | Phase 5 |

---

## 9. Phase 1 Readiness

- ✅ All Redwood artifacts in use are documented above.
- ✅ No platform-specific widget modules (`widget-view`/`uiview`/`dom`) — clean strip.
- ✅ `TreehouseHelper` exists ([TreehouseHelper.java](../androidApp/src/main/java/com/example/serverdrivenui/TreehouseHelper.java)) — earlier "missing" flag was incorrect.
- ✅ Code compiles and runs on both Android and iOS as of the last commit (`1d76527`).

**Ready to begin Phase 1.**
