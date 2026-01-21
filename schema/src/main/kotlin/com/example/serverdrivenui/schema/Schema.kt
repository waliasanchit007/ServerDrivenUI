package com.example.serverdrivenui.schema

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import kotlin.Unit

@Schema(
    members = [
        // Core primitives
        MyText::class,
        MyButton::class,
        MyColumn::class,
        FlexRow::class,
        FlexColumn::class,
        Box::class,
        Spacer::class,
        // Input widgets
        SduiTextField::class,
        SduiSwitch::class,
        // Display widgets
        SduiImage::class,
        SduiCard::class,
        AsyncImage::class,
        // Navigation widgets
        ScreenStack::class,
        BackHandler::class,
        // Layout containers
        LazyList::class,
        BottomSheet::class,
        ScrollableColumn::class,
        AppScaffold::class,
        CoachGrid::class,
        // Text variants
        HeaderText::class,
        SecondaryText::class,
        // Interactive elements
        IconButton::class,
        Chip::class,
        ActionButton::class,
        // Enhanced styling primitives
        StyledText::class,
        StyledBox::class,
        Divider::class,
    ],
)
interface SduiSchema

// ============= Core Primitives =============

@Widget(1)
data class MyText(
    @Property(1) val text: String,
)

@Widget(2)
data class MyButton(
    @Property(1) val text: String,
    @Property(2) val onClick: () -> Unit,
)

@Widget(3)
data class MyColumn(
    @Children(1) val children: () -> Unit,
)

@Widget(4)
data class FlexRow(
    @Property(1) val horizontalArrangement: String, // "Start", "Center", "End", "SpaceBetween", "SpaceAround", "SpaceEvenly"
    @Property(2) val verticalAlignment: String, // "Top", "CenterVertically", "Bottom"
    @Property(3) val spacing: Int,  // dp between children, 0 = no spacing
    @Property(4) val padding: Int,  // dp padding around content, 0 = no padding
    @Children(1) val children: () -> Unit,
)

@Widget(5)
data class FlexColumn(
    @Property(1) val verticalArrangement: String, // "Top", "Center", "Bottom", "SpaceBetween", "SpaceAround", "SpaceEvenly"
    @Property(2) val horizontalAlignment: String, // "Start", "CenterHorizontally", "End"
    @Property(3) val spacing: Int,  // dp between children, 0 = no spacing
    @Property(4) val padding: Int,  // dp padding around content, 0 = no padding
    @Children(1) val children: () -> Unit,
)

@Widget(6)
data class Box(
    @Children(1) val children: () -> Unit,
)

@Widget(7)
data class Spacer(
    @Property(1) val width: Int,
    @Property(2) val height: Int,
)

// ============= Input Widgets =============

@Widget(8)
data class SduiTextField(
    @Property(1) val value: String,
    @Property(2) val label: String,
    @Property(3) val placeholder: String,
    @Property(4) val onValueChange: (String) -> Unit,
)

@Widget(9)
data class SduiSwitch(
    @Property(1) val checked: Boolean,
    @Property(2) val onCheckedChange: (Boolean) -> Unit,
)

// ============= Display Widgets =============

@Widget(10)
data class SduiImage(
    @Property(1) val url: String,
    @Property(2) val contentDescription: String,
)

@Widget(11)
data class SduiCard(
    @Property(1) val onClick: (() -> Unit)?,
    @Property(2) val backgroundColor: String,  // hex "#171717" or semantic "surface", "" = default
    @Property(3) val borderColor: String,      // hex or semantic, "" = no border
    @Property(4) val borderWidth: Int,         // dp, 0 = no border
    @Property(5) val borderRadius: Int,        // dp, 0 = default (12dp)
    @Property(6) val padding: Int,             // dp, 0 = default (16dp)
    @Children(1) val children: () -> Unit,
)

@Widget(15)
data class AsyncImage(
    @Property(1) val url: String,
    @Property(2) val contentDescription: String,
    @Property(3) val size: Int,
    @Property(4) val circular: Boolean,
)

// ============= Navigation Widgets =============

@Widget(12)
data class ScreenStack(
    @Children(1) val children: () -> Unit,
)

@Widget(13)
data class BackHandler(
    @Property(1) val enabled: Boolean,
    @Property(2) val onBack: () -> Unit,
)

// ============= Layout Containers =============

@Widget(14)
data class LazyList(
    @Children(1) val children: () -> Unit,
)

@Widget(21)
data class BottomSheet(
    @Property(1) val isVisible: Boolean,
    @Property(2) val onDismiss: () -> Unit,
    @Children(1) val content: () -> Unit,
)

@Widget(22)
data class ScrollableColumn(
    @Property(1) val padding: Int,
    @Children(1) val children: () -> Unit,
)

@Widget(27)
data class AppScaffold(
    @Property(1) val showBottomBar: Boolean,
    @Property(2) val selectedTab: String,
    @Property(3) val onTabSelected: (String) -> Unit,
    @Children(1) val content: () -> Unit,
)

@Widget(32)
data class CoachGrid(
    @Children(1) val children: () -> Unit,
)

// ============= Text Variants =============

@Widget(23)
data class HeaderText(
    @Property(1) val text: String,
    @Property(2) val size: String, // "large", "medium", "small"
)

@Widget(24)
data class SecondaryText(
    @Property(1) val text: String,
)

// ============= Interactive Elements =============

@Widget(25)
data class IconButton(
    @Property(1) val icon: String,
    @Property(2) val onClick: () -> Unit,
    @Property(3) val isSelected: Boolean,
)

@Widget(26)
data class Chip(
    @Property(1) val label: String,
)

@Widget(31)
data class ActionButton(
    @Property(1) val icon: String,
    @Property(2) val text: String,
    @Property(3) val variant: String, // "primary", "secondary", "ghost"
    @Property(4) val onClick: () -> Unit,
)

// ============= Enhanced Styling Primitives =============

/**
 * Text with full styling control.
 * style: "headline", "title", "titleMedium", "body", "bodySmall", "label", "labelSmall"
 * color: hex "#FAFAFA" OR semantic "primary", "secondary", "muted", "accent", "success", "error"
 * fontWeight: "normal", "medium", "semibold", "bold"
 */
@Widget(38)
data class StyledText(
    @Property(1) val text: String,
    @Property(2) val style: String,      // typography style
    @Property(3) val color: String,      // hex or semantic
    @Property(4) val fontWeight: String, // weight override
    @Property(5) val letterSpacing: Int, // sp, 0 = default
)

/**
 * Container with full styling control.
 * backgroundColor: hex OR semantic color token
 * width/height: dp value, -1 = wrap_content, -2 = match_parent
 */
@Widget(39)
data class StyledBox(
    @Property(1) val backgroundColor: String,  // hex or semantic
    @Property(2) val borderRadius: Int,        // dp
    @Property(3) val padding: Int,             // dp
    @Property(4) val paddingHorizontal: Int,   // dp, overrides padding if > 0
    @Property(5) val paddingVertical: Int,     // dp, overrides padding if > 0
    @Property(6) val width: Int,               // dp, -1 = wrap, -2 = fill
    @Property(7) val height: Int,              // dp, -1 = wrap, -2 = fill
    @Property(8) val contentAlignment: String, // "TopStart", "Center", "BottomEnd", etc
    @Children(1) val children: () -> Unit,
)

/**
 * Horizontal divider line.
 * color: hex OR semantic color token
 */
@Widget(40)
data class Divider(
    @Property(1) val color: String,  // hex or semantic
)
