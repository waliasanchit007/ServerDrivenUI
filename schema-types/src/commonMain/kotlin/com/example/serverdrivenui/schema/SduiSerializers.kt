package com.example.serverdrivenui.schema

import kotlinx.serialization.modules.SerializersModule

/**
 * Schema-wide [SerializersModule] used by both guest and host so the
 * Konduit protocol can encode/decode our enums.
 *
 * Why this exists: Konduit's codegen emits `ContextualSerializer(SchemaColor::class)`
 * inside generated modifier serializers (e.g. for `Background.color`). When
 * no contextual serializer is registered the encode call throws
 * `SerializationException`, which silently aborts the protocol batch and
 * results in a fully blank guest-driven UI on the host side. Registering
 * the enum's auto-generated serializer fixes both encode (guest) and decode
 * (host) paths.
 *
 * Apply on the host via `TreehouseApp.Spec.serializersModule`. Apply on the
 * guest by passing a [kotlinx.serialization.json.Json] with this module to
 * `StandardAppLifecycle`.
 *
 * Add new enums here when they appear as fields on a `@Modifier` class.
 * Enums used only as `@Property` on a `@Widget` do NOT need to be registered
 * (codegen uses their direct `.serializer()` for those).
 */
public val SduiSerializersModule: SerializersModule = SerializersModule {
    contextual(SchemaColor::class, SchemaColor.serializer())
    // Add other enums here when they're used as a field on an @Modifier:
    // contextual(SchemaTextStyle::class, SchemaTextStyle.serializer())
    // contextual(SchemaArrangement::class, SchemaArrangement.serializer())
    // ...
}
