package com.example.serverdrivenui.schema

import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import kotlin.Unit

@Schema(
    members = [
        MyText::class,
        MyButton::class,
    ],
)
interface SduiSchema

@Widget(1)
data class MyText(
    @Property(1) val text: String,
)

@Widget(2)
data class MyButton(
    @Property(1) val text: String,
    @Property(2) val onClick: () -> Unit,
)
