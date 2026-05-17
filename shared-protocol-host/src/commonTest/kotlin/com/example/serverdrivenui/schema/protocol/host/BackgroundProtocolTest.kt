package com.example.serverdrivenui.schema.protocol.host

import com.example.serverdrivenui.schema.SduiSerializersModule
import com.example.serverdrivenui.schema.modifier.Background
import dev.konduit.protocol.ModifierElement
import dev.konduit.protocol.ModifierTag
import dev.konduit.protocol.host.ProtocolMismatchHandler
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression test for the [Background] modifier's additive-default contract.
 *
 * The schema declares `cornerRadiusDp: Int = 0` so that older payloads (which
 * pre-date the rounded-background feature) continue to decode with a sensible
 * default. We test the actual generated [SduiSchemaHostProtocol.createModifier]
 * code path — not a stub — so a future Konduit codegen change that drops the
 * default would surface here.
 *
 * Background on why this matters: protocol mismatches due to a missing optional
 * field would silently abort the whole batch via SerializationException, and
 * the user-visible symptom is "blank guest UI on host". See SduiSerializers.kt
 * for the related contextual-serializer story.
 */
class BackgroundProtocolTest {
    private val json = Json {
        serializersModule = SduiSerializersModule
        ignoreUnknownKeys = true
    }
    private val protocol = SduiSchemaHostProtocol.create(json, ProtocolMismatchHandler.Throwing)

    @Test
    fun oldPayloadWithoutCornerRadiusDecodesToZero() {
        // Pre-rounded-background JSON: only the `color` field is present.
        val element = ModifierElement(
            tag = ModifierTag(5),
            value = buildJsonObject {
                put("color", JsonPrimitive("Primary"))
            },
        )

        val modifier = protocol.createModifier(element)
        assertTrue(modifier is Background, "decoded modifier should be Background, was ${modifier::class}")
        assertEquals(0, modifier.cornerRadiusDp, "missing cornerRadiusDp must default to 0")
    }

    @Test
    fun newPayloadWithCornerRadiusDecodesAccurately() {
        val element = ModifierElement(
            tag = ModifierTag(5),
            value = buildJsonObject {
                put("color", JsonPrimitive("SecondaryContainer"))
                put("cornerRadiusDp", JsonPrimitive(16))
            },
        )

        val modifier = protocol.createModifier(element)
        assertTrue(modifier is Background)
        assertEquals(16, modifier.cornerRadiusDp)
    }
}
