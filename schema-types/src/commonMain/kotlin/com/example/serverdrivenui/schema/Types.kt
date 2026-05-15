package com.example.serverdrivenui.schema

import kotlinx.serialization.Serializable

/**
 * Enum types used as @Property values in the schema. Live in a Kotlin
 * Multiplatform module so they're visible to every target that consumes
 * generated widget code (jvm/js/iosArm64/iosSimulatorArm64), since the
 * @Schema/@Widget definitions in `schema/` are JVM-only (konduit-schema
 * is published JVM-only).
 *
 * Each enum is @Serializable so the Konduit protocol can ship its values
 * across the host/guest boundary via kotlinx.serialization. Kotlin/JS
 * specifically requires this annotation on enums (no implicit fallback).
 *
 * Wire format: enum entry order is the wire format. Add new entries at
 * the end only. Never reorder, rename, or remove entries within a major
 * version.
 */

/**
 * Maps to MaterialTheme.colorScheme on the host. Accent1..4 are slots Caliclan
 * can reskin without bumping the wire format. Transparent = no background.
 */
@Serializable
enum class SchemaColor {
    Primary, OnPrimary, PrimaryContainer, OnPrimaryContainer,
    Secondary, OnSecondary, SecondaryContainer, OnSecondaryContainer,
    Tertiary, OnTertiary,
    Surface, OnSurface, SurfaceVariant, OnSurfaceVariant,
    Background, OnBackground,
    Error, OnError, Outline, OutlineVariant,
    Accent1, Accent2, Accent3, Accent4,
    Transparent,
}

/** Maps to MaterialTheme.typography on the host. M3 type scale. */
@Serializable
enum class SchemaTextStyle {
    DisplayLarge, DisplayMedium, DisplaySmall,
    HeadlineLarge, HeadlineMedium, HeadlineSmall,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelMedium, LabelSmall,
}

@Serializable
enum class SchemaArrangement { Start, Center, End, SpaceBetween, SpaceAround, SpaceEvenly }

@Serializable
enum class SchemaHorizontalAlignment { Start, CenterHorizontally, End }

@Serializable
enum class SchemaVerticalAlignment { Top, CenterVertically, Bottom }

/** Curated set of Material icons available to the guest. Add as needed. */
@Serializable
enum class SchemaIconName {
    Home, Settings, Star, Favorite, Search, Menu, Close, Add, ArrowBack, ArrowForward,
    Person, Notifications, Email, Phone, Lock, Edit, Delete, Check, Info, Warning,
    // Content / typography icons
    FormatQuote, Brush, AutoStories,
}

/**
 * Horizontal alignment of [com.example.serverdrivenui.schema.Text]
 * content within its laid-out box. Maps to `androidx.compose.ui.text.style.TextAlign`
 * on the host (Start/Center/End/Justify; `Unspecified` is intentionally
 * not exposed — guests express "use parent default" by passing
 * [SchemaTextAlign.Start]).
 *
 * Wire-additive on the [com.example.serverdrivenui.schema.Text] widget
 * (Property 4) with [Start] as the default — older payloads decode
 * cleanly and render as today.
 */
@Serializable
enum class SchemaTextAlign { Start, Center, End, Justify }

/**
 * Font weight slot. Subset of Compose's `FontWeight` constants —
 * covers the slots a designer is likely to ask for without exposing
 * 100..900 granular weights (most fonts only ship a few weights anyway).
 *
 * Maps to `androidx.compose.ui.text.font.FontWeight.{Light/Normal/Medium/SemiBold/Bold/ExtraBold}`
 * on the host. Wire-additive on the [com.example.serverdrivenui.schema.Text]
 * widget (Property 5) with [Normal] as the default.
 */
@Serializable
enum class SchemaFontWeight {
    Light, Normal, Medium, SemiBold, Bold, ExtraBold,
}

/**
 * Generic typeface family. The host resolves each enum to a platform
 * default — Compose Multiplatform's `FontFamily.{Default,Serif,SansSerif,
 * Monospace,Cursive}`. To plug in a CUSTOM typeface (a brand font shipped
 * by the host), the host's `toComposeFontFamily()` mapping for
 * [SchemaFontFamily.Default] can swap in the brand `FontFamily` — same
 * "theme-the-default-slot" pattern used by [SchemaColor.Primary].
 *
 * Wire-additive on [com.example.serverdrivenui.schema.Text] (Property 6)
 * with [Default] as the default.
 */
@Serializable
enum class SchemaFontFamily {
    Default, Serif, SansSerif, Monospace, Cursive,
}

/**
 * Stacking alignment for a [com.example.serverdrivenui.schema.Box]'s
 * children. Maps to `androidx.compose.ui.Alignment.{TopStart, …,
 * BottomEnd}` on the host. The Box itself can still be sized via the
 * Size/Width/Height modifiers; this only changes where unconstrained
 * children land within it.
 *
 * Wire-additive on [com.example.serverdrivenui.schema.Box] (Property 2)
 * with [TopStart] as the default (which matches Compose's default and
 * the previous Box behavior).
 */
@Serializable
enum class SchemaBoxAlignment {
    TopStart, TopCenter, TopEnd,
    CenterStart, Center, CenterEnd,
    BottomStart, BottomCenter, BottomEnd,
}

/**
 * Enter/exit transition family for AnimatedVisibility.
 *
 * Each entry maps to one or a combination of Compose's
 * EnterTransition / ExitTransition factories:
 *
 *   None              → EnterTransition.None / ExitTransition.None  (instant swap)
 *   Fade              → fadeIn() / fadeOut()
 *   SlideVertical     → slideInVertically() / slideOutVertically()  (from above)
 *   SlideHorizontal   → slideInHorizontally() / slideOutHorizontally() (from start)
 *   Expand            → expandIn() / shrinkOut()
 *   Scale             → scaleIn() / scaleOut()
 *   FadeAndSlide      → fadeIn() + slideInVertically() / fadeOut() + slideOutVertically()
 *   FadeAndScale      → fadeIn() + scaleIn() / fadeOut() + scaleOut()
 *
 * Slide direction is fixed at the widget level (vertical = from above
 * for enter / to below for exit; horizontal = from start for enter / to
 * end for exit) because parameterizing direction would require either a
 * companion enum or a packed Int — not worth the wire complexity for
 * the 80% case. Use [None] for "render immediately, no animation".
 */
@Serializable
enum class SchemaTransition {
    None,
    Fade,
    SlideVertical,
    SlideHorizontal,
    Expand,
    Scale,
    FadeAndSlide,
    FadeAndScale,
}
