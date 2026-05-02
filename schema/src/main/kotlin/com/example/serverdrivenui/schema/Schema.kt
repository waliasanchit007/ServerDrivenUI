package com.example.serverdrivenui.schema

import dev.konduit.schema.Children
import dev.konduit.schema.Property
import dev.konduit.schema.Schema
import dev.konduit.schema.Widget

/**
 * Phase 3 Tier 1 schema — Konduit primitives + Caliclan navigation widgets.
 *
 * ID ranges per docs/KONDUIT_PLAN.md §3.3:
 *   1–10    Konduit Tier 1 (foundation widgets)
 *   11–99   Konduit Tier 1+2 reserved
 *   100–199 Konduit Tier 3 reserved
 *   200–999 Future Konduit growth
 *   1000+   Caliclan / consumer app domain widgets
 *
 * Wire format: widget IDs are immutable. Once assigned, never reused, never
 * renumbered. Property additions only with defaults; major bump for any
 * breaking change.
 */
@Schema(
    members = [
        // Tier 1 — Konduit primitives (IDs 1–10)
        Box::class,
        Column::class,
        Row::class,
        Spacer::class,
        LazyColumn::class,
        LazyRow::class,
        LazyItem::class,
        Text::class,
        AsyncImage::class,
        Icon::class,
        // Caliclan navigation primitives (IDs 1000+)
        ScreenStack::class,
        BackHandler::class,
    ],
)
interface SduiSchema

// ============================================================================
// Tier 1 — Konduit primitives
// ============================================================================

/** Container with z-stacked children. */
@Widget(1)
data class Box(
    @Property(1) val padding: Int,             // dp; 0 = no padding
    @Property(2) val background: SchemaColor,  // SchemaColor.Transparent = none
    @Property(3) val onClick: (() -> Unit)?,
    @Children(1) val children: () -> Unit,
)

/** Vertical stack. */
@Widget(2)
data class Column(
    @Property(1) val padding: Int,
    @Property(2) val background: SchemaColor,
    @Property(3) val verticalArrangement: SchemaArrangement,
    @Property(4) val horizontalAlignment: SchemaHorizontalAlignment,
    @Property(5) val fillMaxSize: Boolean,
    @Children(1) val children: () -> Unit,
)

/** Horizontal stack. */
@Widget(3)
data class Row(
    @Property(1) val padding: Int,
    @Property(2) val background: SchemaColor,
    @Property(3) val horizontalArrangement: SchemaArrangement,
    @Property(4) val verticalAlignment: SchemaVerticalAlignment,
    @Property(5) val fillMaxWidth: Boolean,
    @Children(1) val children: () -> Unit,
)

/** Fixed-size gap. width=0 or height=0 mean unset. */
@Widget(4)
data class Spacer(
    @Property(1) val width: Int,   // dp
    @Property(2) val height: Int,  // dp
)

/** Lazily-rendered vertical list. Children must be LazyItem widgets. */
@Widget(5)
data class LazyColumn(
    @Property(1) val padding: Int,
    @Property(2) val fillMaxSize: Boolean,
    @Children(1) val items: () -> Unit,
)

/** Lazily-rendered horizontal list. Children must be LazyItem widgets. */
@Widget(6)
data class LazyRow(
    @Property(1) val padding: Int,
    @Property(2) val fillMaxWidth: Boolean,
    @Children(1) val items: () -> Unit,
)

/** Single slot inside a LazyColumn/LazyRow. */
@Widget(7)
data class LazyItem(
    @Children(1) val children: () -> Unit,
)

/** Display a string. */
@Widget(8)
data class Text(
    @Property(1) val text: String,
    @Property(2) val color: SchemaColor,
    @Property(3) val style: SchemaTextStyle,
)

/** Display an image fetched from a URL. width=0/height=0 = intrinsic size. */
@Widget(9)
data class AsyncImage(
    @Property(1) val url: String,
    @Property(2) val contentDescription: String,
    @Property(3) val width: Int,    // dp
    @Property(4) val height: Int,   // dp
)

/** Material icon. size=0 = default 24dp. */
@Widget(10)
data class Icon(
    @Property(1) val name: SchemaIconName,
    @Property(2) val tint: SchemaColor,
    @Property(3) val size: Int,
)

// ============================================================================
// Caliclan navigation primitives (IDs 1000+)
// ============================================================================

/**
 * Container for the currently visible screen. The guest renders a single
 * screen inside this widget; future versions may add transitions.
 */
@Widget(1000)
data class ScreenStack(
    @Children(1) val children: () -> Unit,
)

/**
 * Back-press / swipe-back interceptor. When enabled=true the host routes the
 * gesture to the guest's onBack callback instead of the system default.
 */
@Widget(1001)
data class BackHandler(
    @Property(1) val enabled: Boolean,
    @Property(2) val onBack: () -> Unit,
)

// Enum types (SchemaColor, SchemaTextStyle, SchemaArrangement, etc.) live in
// the schema-types module so they're available to every target — the schema/
// module itself is JVM-only because konduit-schema is published JVM-only.
