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
