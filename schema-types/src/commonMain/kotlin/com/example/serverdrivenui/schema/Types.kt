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
}
