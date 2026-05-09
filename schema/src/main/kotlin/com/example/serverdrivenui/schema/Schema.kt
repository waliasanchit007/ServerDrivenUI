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
    @Children(1) val content: () -> Unit,
)

/** Elevated card (filled + shadow). */
@Widget(52)
data class ElevatedCard(
    @Property(1) val onClick: (() -> Unit)?,
    @Children(1) val content: () -> Unit,
)

/** Outlined card. */
@Widget(53)
data class OutlinedCard(
    @Property(1) val onClick: (() -> Unit)?,
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
