package com.example.serverdrivenui.schema

import dev.konduit.schema.Children
import dev.konduit.schema.Modifier
import dev.konduit.schema.Property
import dev.konduit.schema.Schema
import dev.konduit.schema.Widget

/**
 * Phase 3 Tier 1 schema (post Batch 2.0) — Konduit primitives + Caliclan
 * navigation widgets, plus the LayoutModifier set.
 *
 * Widget ID ranges per docs/KONDUIT_PLAN.md §3.3:
 *   1–10    Konduit Tier 1 (foundation widgets)
 *   11–99   Konduit Tier 2 reserved
 *   100–199 Konduit Tier 3 reserved
 *   200–999 Future Konduit growth
 *   1000+   Caliclan / consumer app domain widgets
 *
 * Modifier tag space is independent of widget tag space; Caliclan modifiers
 * start at 1.
 *
 * Wire format: widget IDs are immutable. Once assigned, never reused, never
 * renumbered. Property additions only with defaults; major bump for any
 * breaking change. Batch 2.0 removes the Tier 1 layout properties (padding,
 * background, fillMaxSize, etc.) — per HANDOVER.md it's treated as a
 * "schema reset within the same major" since Tier 1 was never deployed.
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
        // Tier 2 — Buttons (IDs 21–28)
        Button::class,
        OutlinedButton::class,
        TextButton::class,
        FilledTonalButton::class,
        ElevatedButton::class,
        IconButton::class,
        FloatingActionButton::class,
        ExtendedFloatingActionButton::class,
        // Tier 2 — Inputs (IDs 31–33)
        TextField::class,
        OutlinedTextField::class,
        SearchBar::class,
        // Caliclan navigation primitives (IDs 1000+)
        ScreenStack::class,
        BackHandler::class,
        // Layout modifiers (tags 1–11)
        Padding::class,
        Size::class,
        Width::class,
        Height::class,
        Background::class,
        Weight::class,
        FillMaxWidth::class,
        FillMaxHeight::class,
        FillMaxSize::class,
        Alpha::class,
    ],
)
interface SduiSchema

// ============================================================================
// Tier 1 — Konduit primitives (post LayoutModifier migration)
// ============================================================================

/**
 * Container with z-stacked children. Layout is modifier-driven; the lone
 * direct property is [onClick] because the codegen path for lambda-typed
 * modifier properties is broken on Kotlin/JS (`Function0<Unit>::class` is
 * not a valid class literal). Tier 2 widgets that take callbacks (Button,
 * IconButton, etc.) follow the same convention — keep the lambda as a
 * widget @Property, not a Modifier.
 */
@Widget(1)
data class Box(
    @Property(1) val onClick: (() -> Unit)?,
    @Children(1) val children: () -> Unit,
)

/**
 * Vertical stack. Container axis properties (arrangement / cross-axis
 * alignment) stay as direct @Property; per-child layout (padding,
 * background, weight, fillMax*) lives on the modifier chain.
 */
@Widget(2)
data class Column(
    @Property(1) val verticalArrangement: SchemaArrangement,
    @Property(2) val horizontalAlignment: SchemaHorizontalAlignment,
    @Children(1) val children: () -> Unit,
)

/** Horizontal stack. Same modifier model as [Column]. */
@Widget(3)
data class Row(
    @Property(1) val horizontalArrangement: SchemaArrangement,
    @Property(2) val verticalAlignment: SchemaVerticalAlignment,
    @Children(1) val children: () -> Unit,
)

/**
 * Fixed-size gap. width=0 or height=0 mean unset.
 *
 * Spacer keeps direct width/height properties because they ARE the widget —
 * not redundant decoration like Box's padding. Modifier-based sizing also
 * works on top of these.
 */
@Widget(4)
data class Spacer(
    @Property(1) val width: Int,   // dp
    @Property(2) val height: Int,  // dp
)

/** Lazily-rendered vertical list. Children must be LazyItem widgets. */
@Widget(5)
data class LazyColumn(
    @Children(1) val items: () -> Unit,
)

/** Lazily-rendered horizontal list. Children must be LazyItem widgets. */
@Widget(6)
data class LazyRow(
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

/** Display an image fetched from a URL. */
@Widget(9)
data class AsyncImage(
    @Property(1) val url: String,
    @Property(2) val contentDescription: String,
)

/** Material icon. Sized via modifier (Size/Width/Height); default is 24dp. */
@Widget(10)
data class Icon(
    @Property(1) val name: SchemaIconName,
    @Property(2) val tint: SchemaColor,
)

// ============================================================================
// Tier 2 — Buttons (IDs 21–28) — see KONDUIT_PLAN.md §4 Batch 2.1
//
// Text-based buttons (21–25) take `text: String` directly. Apps that need
// custom label styling can lean on the modifier chain or, in a future
// schema bump, switch to a children slot.
//
// Click handlers stay as widget @Property — see HANDOVER.md gotcha #8
// (Konduit codegen for lambda-typed modifier properties is broken on JS).
// ============================================================================

/** Filled (high-emphasis) button. */
@Widget(21)
data class Button(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
)

/** Outlined (medium-emphasis) button. */
@Widget(22)
data class OutlinedButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
)

/** Text-only (low-emphasis) button. */
@Widget(23)
data class TextButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
)

/** Filled tonal (medium-emphasis) button — softer than [Button]. */
@Widget(24)
data class FilledTonalButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
)

/** Elevated (medium-emphasis) button with shadow. */
@Widget(25)
data class ElevatedButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
)

/** Icon-only button. The single child should be an [Icon]. */
@Widget(26)
data class IconButton(
    @Property(1) val enabled: Boolean,
    @Property(2) val onClick: (() -> Unit)?,
    @Children(1) val content: () -> Unit,
)

/** Floating action button (circular). The single child should be an [Icon]. */
@Widget(27)
data class FloatingActionButton(
    @Property(1) val onClick: (() -> Unit)?,
    @Children(1) val content: () -> Unit,
)

/**
 * Extended FAB — pill-shaped FAB with a label and optional leading icon
 * supplied as the single child.
 */
@Widget(28)
data class ExtendedFloatingActionButton(
    @Property(1) val text: String,
    @Property(2) val onClick: (() -> Unit)?,
    @Children(1) val icon: () -> Unit,
)

// ============================================================================
// Tier 2 — Inputs (IDs 31–33) — see KONDUIT_PLAN.md §4 Batch 2.2
//
// The text-field family uses a hard-fork-of-Compose value + onValueChange
// pattern. The host owns the editing state; on each keystroke it fires
// onValueChange so the guest can react and write back via the value
// property. Single-line, no formatter / mask / IME hints in this batch.
// ============================================================================

/** Filled text field (Material 3 default). */
@Widget(31)
data class TextField(
    @Property(1) val value: String,
    @Property(2) val placeholder: String,
    @Property(3) val enabled: Boolean,
    @Property(4) val onValueChange: ((String) -> Unit)?,
)

/** Outlined text field. */
@Widget(32)
data class OutlinedTextField(
    @Property(1) val value: String,
    @Property(2) val placeholder: String,
    @Property(3) val enabled: Boolean,
    @Property(4) val onValueChange: ((String) -> Unit)?,
)

/**
 * Search bar — outlined text field with a leading search icon and
 * placeholder. Same value + onValueChange contract as [TextField].
 */
@Widget(33)
data class SearchBar(
    @Property(1) val value: String,
    @Property(2) val placeholder: String,
    @Property(3) val enabled: Boolean,
    @Property(4) val onValueChange: ((String) -> Unit)?,
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

// ============================================================================
// Layout modifiers (Batch 2.0 — see KONDUIT_PLAN.md §8.2)
//
// All modifiers are unscoped: applicable to any widget. Weight only takes
// effect inside Row/Column on the host; in non-flex parents the host
// silently ignores it. Promoting Weight to a scope-typed modifier is a
// future refinement (would require RowScope/ColumnScope schema additions
// and a children-receiver change on Row + Column).
// ============================================================================

/** Padding around the widget. All values in dp; 0 means none. */
@Modifier(1)
data class Padding(
    val start: Int,
    val top: Int,
    val end: Int,
    val bottom: Int,
)

/** Required size for the widget. */
@Modifier(2)
data class Size(
    val width: Int,   // dp
    val height: Int,  // dp
)

/** Required width. */
@Modifier(3)
data class Width(val value: Int)  // dp

/** Required height. */
@Modifier(4)
data class Height(val value: Int)  // dp

/** Solid background fill. SchemaColor.Transparent renders as no background. */
@Modifier(5)
data class Background(val color: SchemaColor)

/** Flex weight along the parent's main axis. Only meaningful in Row/Column. */
@Modifier(6)
data class Weight(val value: Double)

/** Fill the parent's full width. */
@Modifier(8)
object FillMaxWidth

/** Fill the parent's full height. */
@Modifier(9)
object FillMaxHeight

/** Fill the parent's full width AND height. */
@Modifier(10)
object FillMaxSize

/** Render at the given alpha (0.0..1.0). */
@Modifier(11)
data class Alpha(val value: Double)

// Enum types (SchemaColor, SchemaTextStyle, SchemaArrangement, etc.) live in
// the schema-types module so they're available to every target — the schema/
// module itself is JVM-only because konduit-schema is published JVM-only.
