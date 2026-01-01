package com.example.serverdrivenui.schema

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import kotlin.Unit

@Schema(
    members = [
        MyText::class,
        MyButton::class,
        MyColumn::class,
        FlexRow::class,
        FlexColumn::class,
        Box::class,
        Spacer::class,
        SduiTextField::class,
        SduiSwitch::class,
        SduiImage::class,
        SduiCard::class,
        // Navigation widgets
        ScreenStack::class,
        BackHandler::class,
        // Caliclan widgets
        LazyList::class,
        AsyncImage::class,
        StatusCard::class,
        ConsistencyStrip::class,
        CoachCard::class,
        ScheduleItem::class,
        // Premium UI widgets
        BottomNavigationBar::class,
        BottomSheet::class,
        ScrollableColumn::class,
        HeaderText::class,
        SecondaryText::class,
        IconButton::class,
        Chip::class,
    ],
)
interface SduiSchema

// Existing widgets
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

// Layout widgets
@Widget(4)
data class FlexRow(
    @Property(1) val horizontalArrangement: String, // "Start", "Center", "End", "SpaceBetween", "SpaceAround", "SpaceEvenly"
    @Property(2) val verticalAlignment: String, // "Top", "CenterVertically", "Bottom"
    @Children(1) val children: () -> Unit,
)

@Widget(5)
data class FlexColumn(
    @Property(1) val verticalArrangement: String, // "Top", "Center", "Bottom", "SpaceBetween", "SpaceAround", "SpaceEvenly"
    @Property(2) val horizontalAlignment: String, // "Start", "CenterHorizontally", "End"
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

// Input widgets
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

// Display widgets
@Widget(10)
data class SduiImage(
    @Property(1) val url: String,
    @Property(2) val contentDescription: String,
)

@Widget(11)
data class SduiCard(
    @Property(1) val onClick: (() -> Unit)?,
    @Children(1) val children: () -> Unit,
)

// ============= Navigation Widgets =============

/**
 * Container for screen content with transition support.
 */
@Widget(12)
data class ScreenStack(
    @Children(1) val children: () -> Unit,
)

/**
 * Back press interceptor widget.
 */
@Widget(13)
data class BackHandler(
    @Property(1) val enabled: Boolean,
    @Property(2) val onBack: () -> Unit,
)

// ============= Caliclan Widgets =============

/**
 * Scrollable vertical list for performance with many items.
 */
@Widget(14)
data class LazyList(
    @Children(1) val children: () -> Unit,
)

/**
 * Image loaded from URL with placeholder support.
 */
@Widget(15)
data class AsyncImage(
    @Property(1) val url: String,
    @Property(2) val contentDescription: String,
    @Property(3) val size: Int, // Size in dp (square)
    @Property(4) val circular: Boolean, // Clip to circle
)

/**
 * Membership status card with colored border.
 * status: "active", "expiring", "expired"
 */
@Widget(16)
data class StatusCard(
    @Property(1) val status: String,
    @Property(2) val title: String,
    @Property(3) val subtitle: String,
    @Property(4) val daysLeft: Int,
    @Property(5) val onClick: (() -> Unit)?,
)

/**
 * Weekly attendance strip (Mon-Sun indicators).
 * Each day status: "future", "today", "attended", "missed", "rest"
 */
@Widget(17)
data class ConsistencyStrip(
    @Property(1) val monday: String,
    @Property(2) val tuesday: String,
    @Property(3) val wednesday: String,
    @Property(4) val thursday: String,
    @Property(5) val friday: String,
    @Property(6) val saturday: String,
    @Property(7) val sunday: String,
)

/**
 * Coach preview card with photo, name, role.
 */
@Widget(18)
data class CoachCard(
    @Property(1) val name: String,
    @Property(2) val role: String,
    @Property(3) val photoUrl: String,
    @Property(4) val onClick: () -> Unit,
)

/**
 * Training schedule day row.
 */
@Widget(19)
data class ScheduleItem(
    @Property(1) val dayName: String,
    @Property(2) val date: String,
    @Property(3) val focus: String,
    @Property(4) val isToday: Boolean,
    @Property(5) val isAttended: Boolean,
    @Property(6) val isRestDay: Boolean,
    @Property(7) val onClick: () -> Unit,
)

// ============= Premium UI Widgets =============

/**
 * Bottom navigation bar with 3 tabs.
 * selectedTab: "home", "training", "membership"
 */
@Widget(20)
data class BottomNavigationBar(
    @Property(1) val selectedTab: String,
    @Property(2) val onTabSelected: (String) -> Unit,
)

/**
 * Modal bottom sheet overlay.
 */
@Widget(21)
data class BottomSheet(
    @Property(1) val isVisible: Boolean,
    @Property(2) val onDismiss: () -> Unit,
    @Children(1) val content: () -> Unit,
)

/**
 * Scrollable vertical column with padding.
 */
@Widget(22)
data class ScrollableColumn(
    @Property(1) val padding: Int,
    @Children(1) val children: () -> Unit,
)

/**
 * Large header text with size variants.
 * size: "large" (28sp), "medium" (20sp), "small" (16sp)
 */
@Widget(23)
data class HeaderText(
    @Property(1) val text: String,
    @Property(2) val size: String,
)

/**
 * Secondary/caption text in grey.
 */
@Widget(24)
data class SecondaryText(
    @Property(1) val text: String,
)

/**
 * Icon button with optional badge.
 * icon: "home", "calendar", "card", "arrow_back", "close"
 */
@Widget(25)
data class IconButton(
    @Property(1) val icon: String,
    @Property(2) val onClick: () -> Unit,
    @Property(3) val isSelected: Boolean,
)

/**
 * Compact chip/tag for categories.
 */
@Widget(26)
data class Chip(
    @Property(1) val label: String,
)

