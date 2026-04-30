# Konduit Widget Contract

> Defines the abstraction boundary that lets a guest .zipline bundle drive UI
> on **any** native host — not just Compose Multiplatform.
>
> This document is the contract a future native iOS / native Android / web /
> server host must implement to render Caliclan screens without depending on
> Compose.

## Architecture in three layers

```
┌─────────────────────────────────────────────────────────────────┐
│ GUEST  (runs in QuickJS — pure logic, no UI toolkit dependency) │
│   • presenter/                          (Compose @Composable)   │
│   • shared-protocol-guest/              (generated from schema) │
│   • shared-widget/        (interface declarations only)         │
│   • konduit-compose, konduit-treehouse-guest                    │
└────────────────────────────┬────────────────────────────────────┘
                             │  protocol (mutations + events)
                             │  Zipline RPC over QuickJS
┌────────────────────────────▼────────────────────────────────────┐
│ HOST  (per platform — implements widgets in the native toolkit) │
│   • shared-protocol-host/               (generated from schema) │
│   • host's CmpButton / CmpText / CmpRow ...                     │
│   • konduit-protocol-host                                       │
└─────────────────────────────────────────────────────────────────┘
```

The **schema** in `schema/src/main/kotlin/com/example/serverdrivenui/schema/Schema.kt`
is platform-agnostic Kotlin/JVM. It generates two sides:

1. **Guest interfaces** (`shared-widget/`) — what the presenter calls.
2. **Host interfaces** (`shared-protocol-host/`) — what a host must implement.

A new host platform implements the host side only. The guest is reused as-is.

## What a host must provide

For each widget defined in `Schema.kt`, the host must implement an instance
of the generated `*Widget<W>` interface, where `W` is the host's native view
type (Composable function, `UIView`, `View`, virtual DOM node, JSON, etc.).

### The 13 Caliclan widgets

| Schema widget | Host responsibility |
|---|---|
| `CmpRow`            | Horizontal container with children |
| `CmpColumn`         | Vertical container with children |
| `CmpText`           | Display a string |
| `CmpButton`         | Tappable label that emits `onClick` events |
| `CmpTextField`      | Editable text input that emits `onValueChange` |
| `CmpSpacer`         | Fixed-size gap |
| `CmpImage`          | Display an image from a URL |
| `CmpDivider`        | Thin horizontal line |
| `CmpScrollView`     | Vertically scrolling container |
| `CmpCard`           | Elevated container with rounded corners |
| `CmpSwitch`         | Boolean toggle that emits `onCheckedChange` |
| `CmpCheckbox`       | Boolean toggle that emits `onCheckedChange` |
| `CmpRadioButton`    | Selection that emits `onSelected` |

The exact property + event signatures are codified in the generated host
protocol code under `shared-protocol-host/build/generated/source/redwood/`.

### Modifier system

Schema also defines layout modifiers (padding, size, weight, alignment, etc.)
in `konduit-layout-modifiers`. The host receives modifiers as a typed
`Modifier` chain on every widget and is responsible for applying them in the
order received. Order matters — `padding(8).background(red)` ≠
`background(red).padding(8)`.

## What a host must NOT do

- **Don't read the guest .zipline bundle directly.** Always go through
  `konduit-treehouse-host` which loads, sandboxes, and pumps events for you.
- **Don't reach into protocol details.** Use the generated host protocol
  classes; they're the public surface.
- **Don't introduce platform-specific extensions to the schema.** New widgets
  must be added to `schema/` and regenerated for both sides — otherwise the
  guest can't address them.

## Reference implementation

The current Caliclan host is in [composeApp/](../composeApp/), specifically
`composeApp/src/commonMain/kotlin/com/example/serverdrivenui/shared/`. It uses
Compose Multiplatform — every `Cmp*` widget is a `@Composable`. A native iOS
host would mirror this 1:1 with `UIView`s implementing the same generated
interfaces.

## Adding a new widget

1. Add the `@Widget(N)` declaration to `schema/src/main/kotlin/.../Schema.kt`.
2. Run `./gradlew :schema:redwoodKotlinGenerate :shared-widget:redwoodKotlinGenerate :shared-protocol-host:redwoodKotlinGenerate :shared-protocol-guest:redwoodKotlinGenerate`.
3. Implement the generated host interface in **every host** (composeApp today,
   future native hosts later).
4. Use the new widget in `presenter/`.
5. Bump the schema's `RedwoodVersion` if the change is breaking — the
   protocol negotiates compatibility at startup.

## Why this boundary exists

Konduit (forked from CashApp Redwood 0.18.0) intentionally separates
`konduit-protocol-host` (no UI dependency) from `konduit-treehouse-host-composeui`
(Compose-only). The protocol-host module remains a clean abstraction layer
so that a future native iOS host can sit on `konduit-protocol-host` without
pulling Compose Multiplatform into the iOS app.

This separation was preserved during the Redwood → Konduit rename and is the
single most important architectural property of this fork.
