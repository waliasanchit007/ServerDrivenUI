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
        LazyVerticalGrid::class,
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
        // Tier 2 — Selection (IDs 41–46)
        Checkbox::class,
        RadioButton::class,
        Switch::class,
        Slider::class,
        RangeSlider::class,
        SegmentedButtonRow::class,
        // Tier 2 — Containers (IDs 51–54)
        Card::class,
        ElevatedCard::class,
        OutlinedCard::class,
        Surface::class,
        // Tier 2 — Feedback (IDs 61–64)
        LinearProgressIndicator::class,
        CircularProgressIndicator::class,
        Badge::class,
        Snackbar::class,
        // Tier 2 — Navigation structure (IDs 71–78)
        Scaffold::class,
        TopAppBar::class,
        LargeTopAppBar::class,
        MediumTopAppBar::class,
        NavigationBar::class,
        NavigationBarItem::class,
        TabRow::class,
        Tab::class,
        // Tier 2 — Misc (IDs 79–80)
        HorizontalDivider::class,
        VerticalDivider::class,
        // Tier 3 — Chips (IDs 100–103)
        FilterChip::class,
        AssistChip::class,
        InputChip::class,
        SuggestionChip::class,
        // Tier 3 — List + Menus (IDs 110–112)
        ListItem::class,
        DropdownMenu::class,
        DropdownMenuItem::class,
        // Tier 3 — Overlays (IDs 120–121)
        ModalBottomSheet::class,
        AlertDialog::class,
        // Tier 3 — Pagers (IDs 140–142)
        HorizontalPager::class,
        VerticalPager::class,
        PagerIndicator::class,
        // Tier 3 — Pull-to-refresh (ID 150)
        PullToRefreshBox::class,
        // Tier 3 — Large-screen navigation (IDs 160–163)
        NavigationRail::class,
        NavigationRailItem::class,
        ModalNavigationDrawer::class,
        NavigationDrawerItem::class,
        // Tier 3 — Pickers (IDs 170–171)
        DatePickerDialog::class,
        TimePickerDialog::class,
        // Tier 3 — Animations (IDs 180+)
        AnimatedVisibility::class,
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
        // Tier 3 modifier additions (tags 12–17)
        Border::class,
        Clip::class,
        ClipCircle::class,
        WrapContentWidth::class,
        WrapContentHeight::class,
        AspectRatio::class,
        // Tier 3 modifier addition (tag 18) — Offset for decorative
        // overlays (watermarks, badge nudges). Wire-additive: the
        // generated runtime accepts unknown modifiers gracefully, but
        // including the class in `members` is required for codegen to
        // emit the interface + .offset() extension function.
        Offset::class,
        // Tier 3 window-inset modifiers (tags 19–24) — full safe-area
        // story for root containers and IME-aware sheets.
        StatusBarsPadding::class,
        NavigationBarsPadding::class,
        ImePadding::class,
        SystemBarsPadding::class,
        DisplayCutoutPadding::class,
        SafeContentPadding::class,
        // Tag 25 — brand-color escape hatch (ARGB literal).
        CustomBackground::class,
        LinearGradient::class,
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
    /**
     * Default alignment for unconstrained children. Wire-additive
     * (Property 2, added after initial release) — older guests serialize
     * the default [SchemaBoxAlignment.TopStart] and round-trip cleanly.
     *
     * Use `TopEnd` for decorative overlays (a watermark glyph anchored
     * at the top-right of a card), `Center` for centered loading
     * spinners, and the defaults for the legacy "stack children at
     * top-start" behavior.
     */
    @Property(2) val contentAlignment: SchemaBoxAlignment = SchemaBoxAlignment.TopStart,
    /**
     * Long-press handler. `null` = no long-press response. When non-null,
     * the host wires `Modifier.combinedClickable(onLongClick = …)` so the
     * regular [onClick] still fires on tap. Wire-additive (Property 3).
     */
    @Property(3) val onLongClick: (() -> Unit)? = null,
    /**
     * Double-tap handler. `null` = no double-tap response. Wire-additive
     * (Property 4). Same combinedClickable wiring as [onLongClick].
     */
    @Property(4) val onDoubleClick: (() -> Unit)? = null,
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

/**
 * Lazily-rendered grid with a fixed column count, laid out vertically
 * (rows grow downward, items flow left → right then wrap). Children
 * must be [LazyItem] widgets — each item occupies one cell.
 *
 * Maps to Compose's `LazyVerticalGrid(columns = GridCells.Fixed(N))`.
 * The `columns` count is fixed at the protocol layer for wire economy;
 * adaptive grids (`GridCells.Adaptive(minSize)`) can be added later as
 * a sibling widget without breaking this one.
 *
 * Spacing between cells (both axes) can be tuned via [contentPaddingDp]
 * and [itemSpacingDp]. Default 0 reproduces the M3 default; pass
 * positive values to add gutters around / between cells.
 */
@Widget(11)
data class LazyVerticalGrid(
    /** Number of equal-width columns. Must be >= 1. */
    @Property(1) val columns: Int,
    /** Padding (dp) around the entire grid's content area. */
    @Property(2) val contentPaddingDp: Int = 0,
    /** Vertical + horizontal gap (dp) between cells. */
    @Property(3) val itemSpacingDp: Int = 0,
    @Children(1) val items: () -> Unit,
)

/** Display a string. */
@Widget(8)
data class Text(
    @Property(1) val text: String,
    @Property(2) val color: SchemaColor,
    @Property(3) val style: SchemaTextStyle,
    /**
     * Horizontal alignment of the text within its laid-out box. Combined
     * with a width-constraining modifier (e.g. `Modifier.fillMaxWidth()`),
     * this determines whether the text glyphs sit at the start/center/end.
     * Wire-additive (Property 4) with default [SchemaTextAlign.Start] —
     * the default preserves the previous behavior and lets older guests
     * compile without changes (Redwood codegen propagates the Kotlin
     * default to the generated composable signature).
     */
    @Property(4) val textAlign: SchemaTextAlign = SchemaTextAlign.Start,
    /**
     * Font weight override. The selected [SchemaTextStyle] already
     * carries a default weight from M3's typography scale; this lets the
     * guest pick a heavier or lighter cut without redefining the whole
     * style. Wire-additive (Property 5) with default
     * [SchemaFontWeight.Normal]; passing [Normal] preserves the previous
     * "use the style's default weight" behavior because the host
     * interprets Normal as "don't override".
     */
    @Property(5) val fontWeight: SchemaFontWeight = SchemaFontWeight.Normal,
    /**
     * Typeface family override. Same opt-in semantics as [fontWeight]:
     * the host interprets [SchemaFontFamily.Default] as "use the style's
     * default family", and any other value overrides it. Wire-additive
     * (Property 6).
     */
    @Property(6) val fontFamily: SchemaFontFamily = SchemaFontFamily.Default,
    /**
     * Maximum number of lines. `0` means unbounded (the previous default).
     * When the text would exceed this many lines, the host applies
     * `TextOverflow.Ellipsis`. Wire-additive (Property 7).
     */
    @Property(7) val maxLines: Int = 0,
    /**
     * Font size override, in sp. `0` (the default) means "use the size
     * baked into the chosen [SchemaTextStyle]" (M3's typography scale).
     * Positive values override — useful for one-off display sizes
     * ("Hero" headlines bigger than `DisplayLarge`, or compact
     * timestamps below `LabelSmall`).
     *
     * Wire-additive (Property 8). The host treats `0` as a sentinel for
     * "don't override" so existing payloads stay rendered identically.
     */
    @Property(8) val fontSizeSp: Int = 0,
    /**
     * Line-height override, in sp. `0` (the default) means "use the
     * line height baked into the chosen [SchemaTextStyle]" (M3 derives
     * line height from typography). Positive values override.
     *
     * Wire-additive (Property 9). Same `0`-as-sentinel pattern as
     * [fontSizeSp].
     */
    @Property(9) val lineHeightSp: Int = 0,
    /**
     * Letter-spacing override, in 0.01-sp units (so `5` = `0.05.sp`).
     * `0` means "use style default". Wire-additive (Property 10).
     *
     * Why hundredths-of-sp instead of a real Double: most CMP M3 type
     * scales use letter-spacing values like 0.5, 0.25, -0.4 — three
     * digits of precision is overkill, and an Int wire is cheaper to
     * serialize. Mapping back: `value / 100.0`.
     */
    @Property(10) val letterSpacingHundredthsSp: Int = 0,
    /**
     * Custom text color as a packed ARGB Long. `null` (the default,
     * wire-additive) means "use the [color] theme slot". When non-null,
     * the raw color wins — bypasses [SchemaColor] entirely.
     *
     * Format: `0xAARRGGBB` (alpha is the high byte). The host hands
     * this to `Color(argb.toInt())`. Wire-additive (Property 11).
     *
     * Use case: brand text colors that aren't in the M3 theme palette
     * (e.g. DevoStatus's PrimaryMaroon `0xFF7A1F1F`).
     */
    @Property(11) val customColorArgb: Long? = null,
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
    /**
     * Custom tint color as a packed ARGB Long. `null` (the default,
     * wire-additive) → use the [tint] theme slot. Non-null wins.
     * See [Text.customColorArgb] for the format.
     */
    @Property(3) val customTintArgb: Long? = null,
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
    /**
     * Corner radius in dp. `-1` (the default, wire-additive sentinel)
     * means "use the M3 default for this widget" — the host falls
     * through to `ButtonDefaults.shape`. Positive values override:
     * `0` for sharp corners, large values (>= height/2) for a pill.
     *
     * Wire-additive (Property 4). Across the button family this same
     * pattern with the same Property tag is used everywhere, so a
     * single guest helper can apply it uniformly.
     */
    @Property(4) val cornerRadiusDp: Int = -1,
)

/** Outlined (medium-emphasis) button. */
@Widget(22)
data class OutlinedButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(4) val cornerRadiusDp: Int = -1,
)

/** Text-only (low-emphasis) button. */
@Widget(23)
data class TextButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(4) val cornerRadiusDp: Int = -1,
)

/** Filled tonal (medium-emphasis) button — softer than [Button]. */
@Widget(24)
data class FilledTonalButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(4) val cornerRadiusDp: Int = -1,
)

/** Elevated (medium-emphasis) button with shadow. */
@Widget(25)
data class ElevatedButton(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(4) val cornerRadiusDp: Int = -1,
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
// Tier 2 — Selection (IDs 41–46) — see KONDUIT_PLAN.md §4 Batch 2.3
// ============================================================================

/** Two-state checkbox. */
@Widget(41)
data class Checkbox(
    @Property(1) val checked: Boolean,
    @Property(2) val enabled: Boolean,
    @Property(3) val onCheckedChange: ((Boolean) -> Unit)?,
)

/**
 * Single radio button. Group selection is the guest's job — track which
 * value is selected and toggle [selected] accordingly per button.
 */
@Widget(42)
data class RadioButton(
    @Property(1) val selected: Boolean,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
)

/** On / off switch. */
@Widget(43)
data class Switch(
    @Property(1) val checked: Boolean,
    @Property(2) val enabled: Boolean,
    @Property(3) val onCheckedChange: ((Boolean) -> Unit)?,
)

/**
 * Continuous-value slider. [valueFrom]..[valueTo] (defaults 0f..1f).
 * [steps] = 0 means continuous; >0 inserts that many discrete stops.
 */
@Widget(44)
data class Slider(
    @Property(1) val value: Float,
    @Property(2) val valueFrom: Float,
    @Property(3) val valueTo: Float,
    @Property(4) val steps: Int,
    @Property(5) val enabled: Boolean,
    @Property(6) val onValueChange: ((Float) -> Unit)?,
)

/** Two-thumb range slider. */
@Widget(45)
data class RangeSlider(
    @Property(1) val rangeStart: Float,
    @Property(2) val rangeEnd: Float,
    @Property(3) val valueFrom: Float,
    @Property(4) val valueTo: Float,
    @Property(5) val steps: Int,
    @Property(6) val enabled: Boolean,
    @Property(7) val onRangeChange: ((Float, Float) -> Unit)?,
)

/**
 * Segmented button row. The guest passes a comma-separated list of labels
 * and tracks which index is selected. We model labels as a String so we
 * don't need to invent a List<String> @Property type for this batch — that
 * can come later if we need richer per-segment data.
 */
@Widget(46)
data class SegmentedButtonRow(
    @Property(1) val labelsCsv: String,
    @Property(2) val selectedIndex: Int,
    @Property(3) val onSelectionChange: ((Int) -> Unit)?,
)

// ============================================================================
// Tier 2 — Containers (IDs 51–54) — see KONDUIT_PLAN.md §4 Batch 2.4
//
// All four wrap a children slot. `onClick` is optional — pass null for a
// non-clickable static container.
// ============================================================================

/** Filled card. */
@Widget(51)
data class Card(
    @Property(1) val onClick: (() -> Unit)?,
    /**
     * Container fill color. Wire-additive (Property 2, default Surface).
     * Material3's `Card` defaults to `surfaceContainerHighest` which in
     * many themes is a tinted variant of Surface — too dark when the
     * integrator wanted a clean white card. Use [SchemaColor.Background]
     * or [SchemaColor.Surface] for a white card matching native
     * `containerColor = Color.White` semantics.
     *
     * Note: M3 `Card` already rounds and clips its content; sibling
     * [Border] / [Clip] modifiers still work for additional outline /
     * corner control beyond the M3 defaults.
     */
    @Property(2) val containerColor: SchemaColor = SchemaColor.Surface,
    /**
     * Text/icon color inside the card. Wire-additive (Property 3,
     * default OnSurface). Material3's `Card` derives this from
     * `containerColor` but doesn't always give the right contrast on
     * custom containerColors — this lets the guest pick explicitly.
     */
    @Property(3) val contentColor: SchemaColor = SchemaColor.OnSurface,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default (~12dp for Card). */
    @Property(4) val cornerRadiusDp: Int = -1,
    /**
     * Custom container color as packed ARGB. `null` → use [containerColor].
     * See [Text.customColorArgb] for format. Wire-additive (Property 5).
     */
    @Property(5) val customContainerColorArgb: Long? = null,
    /**
     * Custom content color as packed ARGB. `null` → use [contentColor].
     * Wire-additive (Property 6).
     */
    @Property(6) val customContentColorArgb: Long? = null,
    /** Long-press handler. See [Box.onLongClick]. Wire-additive (Property 7). */
    @Property(7) val onLongClick: (() -> Unit)? = null,
    /** Double-tap handler. See [Box.onDoubleClick]. Wire-additive (Property 8). */
    @Property(8) val onDoubleClick: (() -> Unit)? = null,
    @Children(1) val content: () -> Unit,
)

/** Elevated card (filled + shadow). */
@Widget(52)
data class ElevatedCard(
    @Property(1) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(2) val cornerRadiusDp: Int = -1,
    @Children(1) val content: () -> Unit,
)

/** Outlined card. */
@Widget(53)
data class OutlinedCard(
    @Property(1) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(2) val cornerRadiusDp: Int = -1,
    @Children(1) val content: () -> Unit,
)

/**
 * Generic Material surface with a tonal elevation level (0..5).
 * Useful as a styled background. Click handler is optional.
 */
@Widget(54)
data class Surface(
    @Property(1) val tonalElevationDp: Int,
    @Property(2) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default (0dp / rectangle for Surface). */
    @Property(3) val cornerRadiusDp: Int = -1,
    @Children(1) val content: () -> Unit,
)

// ============================================================================
// Tier 2 — Feedback (IDs 61–64) — see KONDUIT_PLAN.md §4 Batch 2.5
// ============================================================================

/**
 * Horizontal progress bar. When [indeterminate] is true the bar animates
 * with no specific progress; otherwise [progress] (0..1) is rendered.
 */
@Widget(61)
data class LinearProgressIndicator(
    @Property(1) val progress: Float,
    @Property(2) val indeterminate: Boolean,
)

/**
 * Circular progress spinner. When [indeterminate] is true the ring rotates;
 * otherwise [progress] (0..1) is rendered as an arc.
 */
@Widget(62)
data class CircularProgressIndicator(
    @Property(1) val progress: Float,
    @Property(2) val indeterminate: Boolean,
)

/**
 * Small numeric / string badge. Renders standalone (not anchored to an
 * Icon yet — anchoring is a Tier 3 enhancement). Empty [text] means a dot.
 */
@Widget(63)
data class Badge(
    @Property(1) val text: String,
)

/**
 * Inline snackbar. The guest decides when to add and remove the widget;
 * auto-dismiss / queueing is up to the guest (use LaunchedEffect + delay
 * to remove after a duration). Tier 3 may add a host-side queue.
 */
@Widget(64)
data class Snackbar(
    @Property(1) val message: String,
    @Property(2) val actionLabel: String,
    @Property(3) val onActionClick: (() -> Unit)?,
)

// ============================================================================
// Tier 2 — Navigation structure (IDs 71–78) — see KONDUIT_PLAN.md §4 Batch 2.6
//
// First batch with multi-slot widgets. @Children(N) tags map to named
// slots in the host (topBar, content, etc.); the guest emits each slot's
// children inside its corresponding lambda.
// ============================================================================

/**
 * Material 3 Scaffold with named slots. Slot order:
 *   topBar (1), bottomBar (2), floatingActionButton (3), content (4).
 */
@Widget(71)
data class Scaffold(
    @Children(1) val topBar: () -> Unit,
    @Children(2) val bottomBar: () -> Unit,
    @Children(3) val floatingActionButton: () -> Unit,
    @Children(4) val content: () -> Unit,
)

/** Standard top app bar. */
@Widget(72)
data class TopAppBar(
    @Property(1) val title: String,
    @Children(1) val navigationIcon: () -> Unit,
    @Children(2) val actions: () -> Unit,
)

/** Large (two-line) top app bar. */
@Widget(73)
data class LargeTopAppBar(
    @Property(1) val title: String,
    @Children(1) val navigationIcon: () -> Unit,
    @Children(2) val actions: () -> Unit,
)

/** Medium-height top app bar. */
@Widget(74)
data class MediumTopAppBar(
    @Property(1) val title: String,
    @Children(1) val navigationIcon: () -> Unit,
    @Children(2) val actions: () -> Unit,
)

/** Bottom navigation bar. Children should be [NavigationBarItem]s. */
@Widget(75)
data class NavigationBar(
    @Children(1) val items: () -> Unit,
)

/**
 * One slot in a [NavigationBar]. The single child is the icon
 * (typically an [Icon] widget).
 */
@Widget(76)
data class NavigationBarItem(
    @Property(1) val selected: Boolean,
    @Property(2) val label: String,
    @Property(3) val onClick: (() -> Unit)?,
    @Children(1) val icon: () -> Unit,
)

/** Tab row. Children should be [Tab]s. */
@Widget(77)
data class TabRow(
    @Property(1) val selectedTabIndex: Int,
    @Children(1) val tabs: () -> Unit,
)

/**
 * One tab in a [TabRow]. The optional single child is a leading icon.
 * Pass an empty children block for text-only tabs.
 */
@Widget(78)
data class Tab(
    @Property(1) val selected: Boolean,
    @Property(2) val text: String,
    @Property(3) val onClick: (() -> Unit)?,
    @Children(1) val icon: () -> Unit,
)

// ============================================================================
// Tier 2 — Misc (IDs 79–80) — see KONDUIT_PLAN.md §4 Batch 2.7
// ============================================================================

/**
 * Thin horizontal rule. Use the modifier chain to constrain width
 * (FillMaxWidth is typical) and Padding to add insets.
 */
@Widget(79)
data class HorizontalDivider(
    @Property(1) val thicknessDp: Int,
    @Property(2) val color: SchemaColor,
)

/**
 * Thin vertical rule. Constrain height via the modifier chain (e.g.
 * Height(24)). Pairs naturally with widgets in a Row.
 */
@Widget(80)
data class VerticalDivider(
    @Property(1) val thicknessDp: Int,
    @Property(2) val color: SchemaColor,
)

// ============================================================================
// Tier 3 — Chips (IDs 100–103) — see KONDUIT_PLAN.md §4 Batch 3.0
//
// Material 3 chip family. All four share a single leadingIcon slot
// (@Children(1)) so the wire shape is uniform; the guest passes an empty
// `{}` lambda when no icon is desired. Trailing-icon support is
// FilterChip/InputChip-only on the host side: InputChip exposes a
// dedicated `onClose` callback (its conventional close-X behavior), and
// FilterChip auto-renders a check mark when selected. Custom trailing
// content for AssistChip and FilterChip is deferred — additive @Children
// can land later without breaking wire format.
// ============================================================================

/**
 * Toggleable chip used to filter content. The host renders a check mark
 * automatically when [selected] is true; the leadingIcon slot is shown
 * to the left of the label when [selected] is false (Material 3 default
 * behavior).
 */
@Widget(100)
data class FilterChip(
    @Property(1) val selected: Boolean,
    @Property(2) val label: String,
    @Property(3) val enabled: Boolean,
    @Property(4) val onClick: (() -> Unit)?,
    /**
     * Background color when [selected] = true. Wire-additive (Property 5).
     * Default [SchemaColor.SecondaryContainer] matches M3's
     * `FilterChipDefaults.filterChipColors().selectedContainerColor`.
     * Use [SchemaColor.Tertiary] for the "selected = brand accent"
     * pattern (e.g. DevoStatus's saffron-filled chip).
     */
    @Property(5) val selectedContainerColor: SchemaColor = SchemaColor.SecondaryContainer,
    /**
     * Label text color when [selected] = true. Wire-additive (Property 6).
     * Default [SchemaColor.OnSecondaryContainer]; pair with whatever
     * gives the right contrast against [selectedContainerColor].
     */
    @Property(6) val selectedLabelColor: SchemaColor = SchemaColor.OnSecondaryContainer,
    /**
     * Border color in the UNSELECTED state. Wire-additive (Property 7).
     * Default [SchemaColor.OutlineVariant] matches M3's default
     * filter-chip border. Use [SchemaColor.Tertiary] (etc.) for a
     * branded outline.
     */
    @Property(7) val borderColor: SchemaColor = SchemaColor.OutlineVariant,
    /**
     * Border color in the SELECTED state. Wire-additive (Property 8).
     * Default [SchemaColor.Transparent] matches M3's filled-when-
     * selected look (no visible border on top of the container fill).
     * Set the same color as [selectedContainerColor] to get a "filled
     * chip with matching outline" look.
     */
    @Property(8) val selectedBorderColor: SchemaColor = SchemaColor.Transparent,
    /**
     * Corner radius in dp. Wire-additive (Property 9, default 8). M3's
     * filter chip default is `MaterialTheme.shapes.small` which
     * approximates 8dp; pass a large value (>= chip height / 2 — 50
     * comfortably overshoots the M3 32dp height) for a fully pill /
     * circular shape matching native chip styling.
     */
    @Property(9) val cornerRadiusDp: Int = 8,
    @Children(1) val leadingIcon: () -> Unit,
)

/**
 * Action chip — non-toggleable, mirrors a button's contract. Use for
 * "Take action" affordances (e.g. "Save", "Open").
 */
@Widget(101)
data class AssistChip(
    @Property(1) val label: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default (8dp for chips). */
    @Property(4) val cornerRadiusDp: Int = -1,
    @Children(1) val leadingIcon: () -> Unit,
)

/**
 * Chip representing a discrete entered value (think: a token in a search
 * field or a recipient pill). [onClose] fires when the user taps the
 * trailing close affordance the host renders automatically.
 *
 * Note: leaving [onClose] null hides the close icon entirely.
 */
@Widget(102)
data class InputChip(
    @Property(1) val selected: Boolean,
    @Property(2) val label: String,
    @Property(3) val enabled: Boolean,
    @Property(4) val onClick: (() -> Unit)?,
    @Property(5) val onClose: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(6) val cornerRadiusDp: Int = -1,
    @Children(1) val leadingIcon: () -> Unit,
)

/**
 * Suggestion chip — non-toggleable, hint-style affordance the user can
 * tap to populate a query / shortcut a flow.
 */
@Widget(103)
data class SuggestionChip(
    @Property(1) val label: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    /** See [Button.cornerRadiusDp]. `-1` = M3 default. */
    @Property(4) val cornerRadiusDp: Int = -1,
    @Children(1) val leadingIcon: () -> Unit,
)

// ============================================================================
// Tier 3 — List + Menus (IDs 110–112) — see KONDUIT_PLAN.md §4 Batch 3.1
//
// First widget batch with a popup-anchored child (DropdownMenu) and the
// first 5-effective-slot widget (ListItem: 3 text lines + leading +
// trailing). DropdownMenu is positioned by Compose's Popup primitive
// relative to wherever the widget appears in its parent — typical
// Caliclan usage is to wrap the trigger (e.g. an IconButton) and the
// DropdownMenu in a Box, so the menu anchors to the trigger naturally.
// ============================================================================

/**
 * Material 3 list row. Headline is required (mainline label); supporting
 * + overline render only when non-empty (empty string = hide that line).
 * leading + trailing slots accept any composable but are typically
 * Icon / AsyncImage for avatars + chevrons / Switches for trailing
 * controls. Pass `null` to onClick for a non-clickable / read-only row.
 *
 * Headline-as-slot (e.g. for rich-text headlines) can land later as an
 * additive @Children(3); deferred for now since strings cover the
 * overwhelming majority of real list rows.
 */
@Widget(110)
data class ListItem(
    @Property(1) val headline: String,
    @Property(2) val supporting: String,
    @Property(3) val overline: String,
    @Property(4) val enabled: Boolean,
    @Property(5) val onClick: (() -> Unit)?,
    @Children(1) val leadingContent: () -> Unit,
    @Children(2) val trailingContent: () -> Unit,
)

/**
 * Anchored popup that overlays the screen when [expanded] is true.
 * Position is decided by Compose's Popup machinery relative to the
 * widget's coordinates in its parent — wrap the trigger + the menu in
 * a Box so the menu anchors to the trigger.
 *
 * [onDismissRequest] fires when the user taps outside, presses back, or
 * hits escape. The guest is responsible for flipping its `expanded`
 * state to false in response. Children are rendered in M3's internal
 * ColumnScope; intended children are [DropdownMenuItem] instances but
 * any widget will render.
 */
@Widget(111)
data class DropdownMenu(
    @Property(1) val expanded: Boolean,
    @Property(2) val onDismissRequest: () -> Unit,
    @Children(1) val content: () -> Unit,
)

/**
 * Single row inside a [DropdownMenu]. [text] is the row label;
 * leadingIcon + trailingIcon are optional (empty `{}` = hide). [onClick]
 * typically also dismisses the parent menu — the guest decides.
 */
@Widget(112)
data class DropdownMenuItem(
    @Property(1) val text: String,
    @Property(2) val enabled: Boolean,
    @Property(3) val onClick: (() -> Unit)?,
    @Children(1) val leadingIcon: () -> Unit,
    @Children(2) val trailingIcon: () -> Unit,
)

// ============================================================================
// Tier 3 — Overlays (IDs 120–121) — see KONDUIT_PLAN.md §4 Batch 3.2
//
// Visibility model for both widgets: the GUEST owns the visible-Boolean
// state and conditionally composes the widget. When the guest emits the
// widget the host renders it; when the guest omits it the widget leaves
// the tree. Each widget's `onDismissRequest` fires when the user taps
// the scrim / presses back / swipes (ModalBottomSheet only). The guest
// reacts by flipping its Boolean to false on the next tick — M3 runs
// the hide animation internally BEFORE calling onDismissRequest, so
// cutting the widget from the tree afterwards doesn't truncate it.
// ============================================================================

/**
 * Bottom sheet overlay anchored to the bottom edge of the window. Only
 * present in the widget tree when visible — the guest controls
 * visibility by conditionally composing this widget.
 *
 * [skipPartiallyExpanded]: when true, the sheet expands fully on first
 * show and skips the half-expanded intermediate state. Set true for
 * dialog-style sheets with fixed content; leave false for media or
 * map-style sheets that should partially peek.
 *
 * Tonal elevation, drag handle visibility, scrim color etc. are not yet
 * exposed — they default to M3's standard look. Add as @Property additions
 * later without breaking wire if/when callers need them.
 */
@Widget(120)
data class ModalBottomSheet(
    @Property(1) val onDismissRequest: () -> Unit,
    @Property(2) val skipPartiallyExpanded: Boolean,
    @Children(1) val content: () -> Unit,
)

/**
 * Modal alert dialog with title + body text + up to three action slots.
 * Same visibility model as ModalBottomSheet — the guest conditionally
 * composes this widget.
 *
 * [title] and [text] are String properties (empty = hide that line) for
 * the same reason as ListItem: ~95% of dialogs have plain-string content
 * and forcing every dialog to wrap its text in Text widgets would bloat
 * the wire payload. Rich-text title / text can land as additive
 * @Children(4) / @Children(5) later without breaking wire.
 *
 * [confirmButton] is required by M3; the guest is responsible for
 * placing a Button there. [dismissButton] is optional (empty `{}` =
 * hide). [icon] sits above the title when populated.
 */
@Widget(121)
data class AlertDialog(
    @Property(1) val title: String,
    @Property(2) val text: String,
    @Property(3) val onDismissRequest: () -> Unit,
    @Children(1) val icon: () -> Unit,
    @Children(2) val confirmButton: () -> Unit,
    @Children(3) val dismissButton: () -> Unit,
)

// ============================================================================
// Tier 3 — Pagers (IDs 140–142) — see KONDUIT_PLAN.md §4 Batch 3.3
//
// Page model: each child of [HorizontalPager.pages] / [VerticalPager.pages]
// is ONE page. The host iterates the children list to derive pageCount and
// renders only the i-th child as the i-th page's content (CmpChildren
// already exposes `widgets: List<Widget>` for indexed access).
//
// V1 limitation: programmatic page jumps from the guest are NOT
// supported. The host wires `rememberPagerState(initialPage)` once;
// subsequent changes to initialPage are ignored. Adding a `currentPage`
// @Property + LaunchedEffect(currentPage) { state.animateScrollToPage }
// is an additive change that can land later without breaking wire.
// ============================================================================

/**
 * Swipe-paged horizontal container. Children are individual pages —
 * the host renders only the currently visible page (no deep
 * pre-composition). `onPageChanged` fires AFTER a fling settles on a
 * new page (uses M3's `PagerState.settledPage`).
 *
 * [pageSpacingDp]: gap between adjacent pages while swiping. 0 = pages
 * touch edges. [userScrollEnabled] = false locks paging to programmatic
 * control (currently always external — see V1 limitation above).
 */
@Widget(140)
data class HorizontalPager(
    @Property(1) val initialPage: Int,
    @Property(2) val onPageChanged: ((Int) -> Unit)?,
    @Property(3) val userScrollEnabled: Boolean,
    @Property(4) val pageSpacingDp: Int,
    @Children(1) val pages: () -> Unit,
)

/** Vertical analog of [HorizontalPager]. Pages stack top-to-bottom. */
@Widget(141)
data class VerticalPager(
    @Property(1) val initialPage: Int,
    @Property(2) val onPageChanged: ((Int) -> Unit)?,
    @Property(3) val userScrollEnabled: Boolean,
    @Property(4) val pageSpacingDp: Int,
    @Children(1) val pages: () -> Unit,
)

/**
 * Host-rolled dot-style page indicator. Renders [pageCount] small
 * dots in a Row; the dot at index [currentPage] uses [activeColor],
 * the rest use [inactiveColor]. The guest is expected to track
 * `currentPage` itself (typically from a Pager's `onPageChanged`).
 *
 * No 1:1 M3 widget exists for this — it's a Caliclan-side helper. If
 * future needs require custom indicators (numbers, custom shapes), an
 * additive @Children(1) `customIndicator` slot can land later.
 */
@Widget(142)
data class PagerIndicator(
    @Property(1) val pageCount: Int,
    @Property(2) val currentPage: Int,
    @Property(3) val activeColor: SchemaColor,
    @Property(4) val inactiveColor: SchemaColor,
)

// ============================================================================
// Tier 3 — Pull-to-refresh (ID 150) — see KONDUIT_PLAN.md §4 Batch 3.4
// ============================================================================

/**
 * Wraps content in M3's `PullToRefreshBox`. The user pulls down past
 * the threshold to fire [onRefresh]; the host shows the spinner while
 * [isRefreshing] is true. Typical guest flow:
 *
 *   var refreshing by remember { mutableStateOf(false) }
 *   PullToRefreshBox(
 *       isRefreshing = refreshing,
 *       onRefresh = {
 *           refreshing = true
 *           // kick off async work; flip refreshing back to false when done
 *       },
 *   ) { LazyColumn { ... } }
 *
 * Custom indicator slot is not exposed in v1 — M3's default circular
 * spinner is fine for almost all uses. Add as additive @Children(2)
 * later if needed.
 */
@Widget(150)
data class PullToRefreshBox(
    @Property(1) val isRefreshing: Boolean,
    @Property(2) val onRefresh: () -> Unit,
    @Children(1) val content: () -> Unit,
)

// ============================================================================
// Tier 3 — Large-screen navigation (IDs 160–163) — see KONDUIT_PLAN.md §4 Batch 3.5
//
// First batch with controlled-component drawer state: ModalNavigationDrawer
// uses a guest-held `drawerOpen` Boolean + `onDrawerStateChange` callback
// — same pattern as Switch/Checkbox extended to a more complex state
// transition. The host syncs M3's DrawerState to the guest's Boolean
// via LaunchedEffect, and reports user gestures back via snapshotFlow.
//
// NavigationRail is a sibling to NavigationBar (ID 75) — same widget
// shape, different M3 placement (sidebar vs. bottom bar).
// ============================================================================

/**
 * Vertical sidebar nav, intended for tablet / large-screen layouts
 * but renders on phones too. Mirror of NavigationBar (ID 75) for the
 * vertical axis. [header] is an optional top slot (e.g. a logo or
 * menu button); empty `{}` hides it.
 */
@Widget(160)
data class NavigationRail(
    @Children(1) val header: () -> Unit,
    @Children(2) val items: () -> Unit,
)

/**
 * Single rail entry. Same property shape as NavigationBarItem (ID 76):
 * selected / label / enabled / onClick + an icon @Children slot. M3
 * renders rail items vertically inside the rail's Column scope.
 */
@Widget(161)
data class NavigationRailItem(
    @Property(1) val selected: Boolean,
    @Property(2) val label: String,
    @Property(3) val enabled: Boolean,
    @Property(4) val onClick: (() -> Unit)?,
    @Children(1) val icon: () -> Unit,
)

/**
 * Side-drawer overlay. The drawer slides in from the start edge; the
 * main app content sits in [content] (always visible behind/beside the
 * drawer depending on state).
 *
 * Controlled-component pattern: the guest holds [drawerOpen] and
 * mirrors changes via [onDrawerStateChange]. The host syncs M3's
 * DrawerState to drawerOpen via LaunchedEffect; user gestures (swipe
 * to open / close, tap scrim) feed back through onDrawerStateChange.
 *
 * The host wraps [drawerContent] in M3's `ModalDrawerSheet` for surface
 * styling, so the guest just emits the items (typically
 * NavigationDrawerItems) without worrying about the sheet container.
 */
@Widget(162)
data class ModalNavigationDrawer(
    @Property(1) val drawerOpen: Boolean,
    @Property(2) val onDrawerStateChange: ((Boolean) -> Unit)?,
    @Property(3) val gesturesEnabled: Boolean,
    @Children(1) val drawerContent: () -> Unit,
    @Children(2) val content: () -> Unit,
)

/**
 * Single row inside a [ModalNavigationDrawer]'s drawerContent. M3
 * styles the row as a pill with selected-state background. [icon] is
 * optional (empty `{}` = hide); [badge] sits on the trailing edge
 * (typically a Badge widget showing an unread count).
 */
@Widget(163)
data class NavigationDrawerItem(
    @Property(1) val selected: Boolean,
    @Property(2) val label: String,
    @Property(3) val onClick: (() -> Unit)?,
    @Children(1) val icon: () -> Unit,
    @Children(2) val badge: () -> Unit,
)

// ============================================================================
// Tier 3 — Pickers (IDs 170–171) — see KONDUIT_PLAN.md §4 Batch 3.6
//
// Same conditional-render visibility model as the Tier 3 overlays
// (Batch 3.2): the guest holds a Boolean and only emits the picker
// dialog widget when visible. The host packages M3's DatePicker /
// TimePicker inside a dialog scaffold (DatePickerDialog for date,
// hand-rolled AlertDialog wrapper for time since M3 has no first-class
// TimePickerDialog) with built-in OK / Cancel buttons.
//
// Wire-format choices documented in KONDUIT_PLAN §4 Batch 3.6:
//   - Date encoded as Long millis (UTC midnight). 0L = "no preset".
//   - Time encoded with two scalar properties (initialHour, initialMinute)
//     and onConfirm gets a packed Int (hour * 60 + minute, minutes-of-day,
//     0..1439). Single-arg callback avoids any uncertainty about Konduit
//     codegen for multi-arg @Property lambdas — kept minimal.
//
// OK / Cancel labels are hardcoded in v1; add `confirmLabel: String` +
// `dismissLabel: String` later for i18n without breaking wire.
// ============================================================================

/**
 * Modal date picker. Visibility lives in the guest (conditionally
 * compose this widget). Built-in OK / Cancel buttons; OK fires
 * [onConfirm] with the selected date in UTC midnight millis, then
 * the guest is responsible for hiding the dialog.
 *
 * [initialSelectedDateMillis]: starting selection in UTC midnight
 * millis. Pass 0L for "no preset" (the dialog opens with no date
 * selected and the OK button is disabled until the user picks one).
 */
@Widget(170)
data class DatePickerDialog(
    @Property(1) val initialSelectedDateMillis: Long,
    @Property(2) val onConfirm: ((Long) -> Unit)?,
    @Property(3) val onDismissRequest: () -> Unit,
)

/**
 * Modal time picker. Same visibility model as [DatePickerDialog].
 * Built-in OK / Cancel buttons; OK fires [onConfirm] with the selected
 * time encoded as MINUTES SINCE MIDNIGHT (0..1439). Guest decodes:
 *
 *   val hour = packedMinutes / 60
 *   val minute = packedMinutes % 60
 *
 * The single-Int encoding (rather than two-arg `(Int, Int) -> Unit`)
 * sidesteps any uncertainty about Konduit codegen for multi-arg lambda
 * @Properties — every existing widget in the schema uses single-arg
 * lambdas. Two-arg can land later additively if proven safe.
 *
 * [is24Hour] toggles between 24-hour and AM/PM display modes.
 */
@Widget(171)
data class TimePickerDialog(
    @Property(1) val initialHour: Int,
    @Property(2) val initialMinute: Int,
    @Property(3) val is24Hour: Boolean,
    @Property(4) val onConfirm: ((Int) -> Unit)?,
    @Property(5) val onDismissRequest: () -> Unit,
)

// ============================================================================
// Tier 3 — Animations (IDs 180–189)
//
// AnimatedVisibility is the foundational primitive — show/hide a subtree
// with a tween. The schema's enter/exit transition is a curated enum
// (SchemaTransition) rather than a free-form spec because:
//   - Compose's EnterTransition / ExitTransition factories are not
//     serializable (lambdas, animation specs, internal state)
//   - 80% of usage is one of the named families (fade, slide, scale,
//     expand, fade-and-slide combos)
//
// Duration is exposed as a single Int millis. Future versions may add
// per-property duration or easing curves; today the host applies a
// shared `tween(durationMillis)` to all involved transitions.
// ============================================================================

/**
 * Animate the presence of [content]. When [visible] flips false→true
 * the host runs [enterTransition] over [durationMillis]; flipping
 * true→false runs [exitTransition].
 *
 * `SchemaTransition.None` for either side disables that direction's
 * animation (the content snaps in/out instantly). Use for content
 * that should appear without fanfare (e.g. error banners) but exit
 * with a slide.
 */
@Widget(180)
data class AnimatedVisibility(
    @Property(1) val visible: Boolean,
    @Property(2) val enterTransition: SchemaTransition = SchemaTransition.Fade,
    @Property(3) val exitTransition: SchemaTransition = SchemaTransition.Fade,
    @Property(4) val durationMillis: Int = 300,
    @Children(1) val content: () -> Unit,
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

/**
 * Solid background fill. SchemaColor.Transparent renders as no background.
 *
 * [cornerRadiusDp] = 0 (the default for backward compatibility) paints a
 * rectangular fill; >0 paints a rounded fill of that radius. The fill shape
 * is independent of any sibling Clip — when you want both rounded fill AND
 * clipped content, set Clip(cornerRadiusDp) too. Adding the param is wire-
 * additive: existing manifests serialize the default and round-trip cleanly.
 */
@Modifier(5)
data class Background(
    val color: SchemaColor,
    val cornerRadiusDp: Int = 0,
    /**
     * Alpha multiplier for the [color], in `[0.0, 1.0]`. Default 1.0 =
     * fully opaque (the previous behavior). Common values: 0.1 for a
     * faint tinted action bar (`Saffron @ 10%`), 0.2 for a subtle card
     * border tint, 0.5 for a translucent overlay.
     *
     * Wire-additive (Background param 3). Older payloads decode as
     * 1.0 (fully opaque). Stay within [0.0, 1.0]; values outside that
     * range pass through to Compose's `Color.copy(alpha)`, which
     * clamps internally.
     *
     * Why on Background rather than as a separate Alpha-on-color
     * modifier: a "tinted background" is the overwhelmingly common
     * case (M3's surface variants, action bars, etc.), and mixing
     * `Modifier.alpha()` with `Modifier.background()` dims the
     * children too — not what the integrator wants here.
     */
    val alpha: Double = 1.0,
)

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

// ============================================================================
// Tier 3 modifier additions (tags 12–17) — see KONDUIT_PLAN.md §8.3 follow-ups
//
// All Tier 3 modifiers stay UNSCOPED (any widget can apply any of them).
// Border and Background both support `cornerRadiusDp` (default 0 = the
// original rectangular behavior) — the additive-with-default pattern keeps
// older payloads decoding cleanly. Card / OutlinedCard widgets remain a
// good choice when you want a Material-styled container instead of
// hand-composing clip+bg+border.
//
// Ordering caveat: Compose modifier chains are order-sensitive. The host
// applies these in the order the guest appended them; standard Compose
// rules apply (e.g. clip BEFORE background to clip the fill). The existing
// Background and Border special-cases (always applied last after the loop,
// so they layer correctly with fillMax / size and resolve color in a
// composable scope) are preserved.
// ============================================================================

/**
 * Stroke around the widget's outer bounds. [cornerRadiusDp] = 0 (the
 * default for backward compatibility) renders a rectangular stroke;
 * positive values render a rounded-rectangle stroke that matches a
 * sibling Clip(cornerRadiusDp) — the typical pattern for rounded cards
 * with an outline.
 *
 * Note: Konduit's modifier wire format embeds field defaults via
 * kotlinx.serialization, so adding [cornerRadiusDp] with a default is
 * additive. Older payloads (without the field) decode to 0 = the
 * previous rectangular behavior.
 */
@Modifier(12)
data class Border(
    val thicknessDp: Int,
    val color: SchemaColor,
    val cornerRadiusDp: Int = 0,
)

/**
 * Clip the widget to a rounded-rectangle shape with [cornerRadiusDp]
 * radius. 0 = no-op (sharp corners). Use for rounded cards, pill-shaped
 * buttons (radius == height/2), and other rounded containers.
 */
@Modifier(13)
data class Clip(val cornerRadiusDp: Int)

/**
 * Clip the widget to a perfect circle inscribed in its bounding box.
 * Use for avatar-style images and circular buttons. The widget should
 * be sized to a square (Size with equal width/height, or AspectRatio(1.0)
 * + a width constraint) so the circle isn't elliptical.
 */
@Modifier(14)
object ClipCircle

/**
 * Allow the widget to size itself to its content along the width axis,
 * even if its parent imposed a wider min-width constraint. Inverse of
 * [FillMaxWidth] for cases where you want the widget to be only as wide
 * as it needs to be inside a wider parent.
 */
@Modifier(15)
object WrapContentWidth

/** Vertical analog of [WrapContentWidth]. */
@Modifier(16)
object WrapContentHeight

/**
 * Constrain the widget so width/height = [ratio]. 1.0 → square; > 1 →
 * wider than tall; < 1 → taller than wide. Pair with [FillMaxWidth] (or
 * [Width]) so Compose has one explicit dimension to compute the other
 * against — `Modifier.fillMaxWidth().aspectRatio(16.0/9)` gives a
 * widescreen frame.
 */
@Modifier(17)
data class AspectRatio(val ratio: Double)

/**
 * Translate the widget by [x] / [y] dp from its layout-determined
 * position WITHOUT participating in the parent's measure pass — equivalent
 * to `Modifier.offset(x.dp, y.dp)`. The widget still takes up its original
 * space in the parent; only the paint position shifts.
 *
 * Use for decorative overlays: a watermark glyph anchored at a card's
 * top-right corner, a notification badge nudged off a bell icon, etc.
 * Pair with [ClipCircle] / [Clip] when the offset would otherwise push
 * the widget outside a clipped parent.
 *
 * Sign convention follows Compose: positive [x] shifts right, positive
 * [y] shifts down. Use negatives to nudge up / left (e.g. `Offset(12, -12)`
 * lifts the glyph slightly above the card top edge).
 *
 * Ordering caveat: order matters relative to size/clip. Apply Offset
 * AFTER any size-defining modifiers but BEFORE clip-to-parent for the
 * common "overhang glyph" pattern.
 */
@Modifier(18)
data class Offset(val x: Int, val y: Int)  // dp; negative allowed

// ============================================================================
// Window-inset modifiers (tags 19–24) — see the parity-with-Compose plan
//
// All six match a Compose Foundation extension 1:1:
//   StatusBarsPadding       ↔ Modifier.statusBarsPadding()
//   NavigationBarsPadding   ↔ Modifier.navigationBarsPadding()
//   ImePadding              ↔ Modifier.imePadding()
//   SystemBarsPadding       ↔ Modifier.systemBarsPadding()        (status + nav)
//   DisplayCutoutPadding    ↔ Modifier.displayCutoutPadding()     (notch / curve)
//   SafeContentPadding      ↔ Modifier.safeContentPadding()       (every inset)
//
// Modeling these as modifiers rather than a service is intentional: the
// integration boilerplate is zero (no zipline.bind on the host), the
// behavior is correct under config changes (the M3 LocalDensity +
// WindowInsets pipeline already recomposes when the IME shows / status
// bar resizes), and the guest API matches native Compose verbatim.
// ============================================================================

/** Pad by the status-bar inset. Maps to `Modifier.statusBarsPadding()`. */
@Modifier(19)
object StatusBarsPadding

/** Pad by the navigation-bar inset. Maps to `Modifier.navigationBarsPadding()`. */
@Modifier(20)
object NavigationBarsPadding

/** Pad by the IME (soft-keyboard) inset. Animates with the IME show/hide. */
@Modifier(21)
object ImePadding

/** Pad by both status + navigation bars. Maps to `Modifier.systemBarsPadding()`. */
@Modifier(22)
object SystemBarsPadding

/** Pad around the display cutout / curved edges. */
@Modifier(23)
object DisplayCutoutPadding

/**
 * Pad by ALL system insets — status bar, navigation bar, IME, display
 * cutout, and any other unsafe regions. Use on root screen containers
 * to ensure no content lands under any system overlay.
 */
@Modifier(24)
object SafeContentPadding

/**
 * Solid background fill using a custom ARGB color — escape hatch for
 * brand colors that don't exist on [SchemaColor]'s M3 theme slots.
 *
 * [argb] is a packed Android color int (`0xAARRGGBB`). Example:
 *   saffron `Color(0xFFFF6F00)` → `argb = 0xFFFF6F00L`.
 *
 * [alpha] is an independent multiplier in `[0.0, 1.0]` applied on top
 * of the alpha bits in [argb]. Pass `1.0` to keep [argb]'s native
 * alpha intact; pass `0.1` (etc.) to make a translucent tint without
 * rewriting [argb].
 *
 * [cornerRadiusDp] mirrors [Background.cornerRadiusDp].
 *
 * Companion to [Background] (which uses theme slots) — same use cases
 * (action-bar tints, brand-colored containers) when the exact color
 * isn't on the M3 theme.
 */
@Modifier(25)
data class CustomBackground(
    val argb: Long,
    val cornerRadiusDp: Int = 0,
    val alpha: Double = 1.0,
)

/**
 * Linear gradient background. Maps to
 * `Brush.linearGradient(colors = [startArgb, endArgb])` painted as the
 * widget's background. The gradient direction is controlled by
 * [angleDegrees]: 0° = top → bottom, 90° = start → end (LTR locales),
 * 45° = top-left → bottom-right, etc. Matches Compose's
 * `Brush.linearGradient(start = ..., end = ...)` direction semantics
 * but with a single-number wire payload.
 *
 * Each endpoint takes a packed ARGB Long (`0xAARRGGBB`) so any brand
 * color can be expressed without needing a SchemaColor slot. Use
 * [startAlpha] / [endAlpha] as independent multipliers on top of the
 * ARGB's native alpha bits — the same convention as [CustomBackground].
 *
 * [cornerRadiusDp] applies the same rounded clip as [Background] /
 * [CustomBackground] so card-shaped gradients work without an extra
 * [Clip] modifier.
 *
 * Common patterns:
 *   - Brand vertical fade: `LinearGradient(brandLightArgb, brandDarkArgb,
 *     angleDegrees = 0)`
 *   - Diagonal accent: `LinearGradient(saffronArgb, maroonArgb,
 *     angleDegrees = 45)`
 *   - Subtle scrim under hero image: `LinearGradient(transparentArgb,
 *     blackArgb, startAlpha = 0.0, endAlpha = 0.6)`
 *
 * For more than two color stops, layer multiple gradients via stacked
 * Box children — keeps the wire format compact for the 90% case.
 */
@Modifier(26)
data class LinearGradient(
    val startArgb: Long,
    val endArgb: Long,
    /**
     * Direction, in degrees. 0° = top → bottom. 90° = start → end. Any
     * value works (45° = TL→BR, 180° = bottom → top, etc.).
     */
    val angleDegrees: Int = 0,
    val startAlpha: Double = 1.0,
    val endAlpha: Double = 1.0,
    val cornerRadiusDp: Int = 0,
)

// Enum types (SchemaColor, SchemaTextStyle, SchemaArrangement, etc.) live in
// the schema-types module so they're available to every target — the schema/
// module itself is JVM-only because konduit-schema is published JVM-only.
