@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.serverdrivenui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn as ComposeLazyColumn
import androidx.compose.foundation.lazy.LazyRow as ComposeLazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon as ComposeIcon
import androidx.compose.material3.Text as ComposeText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier as ComposeModifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage as CoilAsyncImage
import dev.konduit.Modifier as KonduitModifier
import dev.konduit.treehouse.TreehouseApp
import dev.konduit.treehouse.AppService
import dev.konduit.widget.Widget
import app.cash.zipline.Zipline
import com.example.serverdrivenui.schema.*
import com.example.serverdrivenui.schema.widget.*
import com.example.serverdrivenui.schema.modifier.Alpha as MAlpha
import com.example.serverdrivenui.schema.modifier.AspectRatio as MAspectRatio
import com.example.serverdrivenui.schema.modifier.Background as MBackground
import com.example.serverdrivenui.schema.modifier.Border as MBorder
import com.example.serverdrivenui.schema.modifier.Clip as MClip
import com.example.serverdrivenui.schema.modifier.ClipCircle as MClipCircle
import com.example.serverdrivenui.schema.modifier.FillMaxHeight as MFillMaxHeight
import com.example.serverdrivenui.schema.modifier.FillMaxSize as MFillMaxSize
import com.example.serverdrivenui.schema.modifier.FillMaxWidth as MFillMaxWidth
import com.example.serverdrivenui.schema.modifier.Height as MHeight
import com.example.serverdrivenui.schema.modifier.Padding as MPadding
import com.example.serverdrivenui.schema.modifier.Size as MSize
import com.example.serverdrivenui.schema.modifier.Weight as MWeight
import com.example.serverdrivenui.schema.modifier.Width as MWidth
import com.example.serverdrivenui.schema.modifier.WrapContentHeight as MWrapContentHeight
import com.example.serverdrivenui.schema.modifier.WrapContentWidth as MWrapContentWidth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private typealias CmpRender = @Composable (ComposeModifier) -> Unit

// ============================================================================
// Theme bindings: SchemaColor -> MaterialTheme.colorScheme,
//                 SchemaTextStyle -> MaterialTheme.typography
// ============================================================================

@Composable
private fun SchemaColor.toComposeColor(): Color = when (this) {
    SchemaColor.Primary -> MaterialTheme.colorScheme.primary
    SchemaColor.OnPrimary -> MaterialTheme.colorScheme.onPrimary
    SchemaColor.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
    SchemaColor.OnPrimaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
    SchemaColor.Secondary -> MaterialTheme.colorScheme.secondary
    SchemaColor.OnSecondary -> MaterialTheme.colorScheme.onSecondary
    SchemaColor.SecondaryContainer -> MaterialTheme.colorScheme.secondaryContainer
    SchemaColor.OnSecondaryContainer -> MaterialTheme.colorScheme.onSecondaryContainer
    SchemaColor.Tertiary -> MaterialTheme.colorScheme.tertiary
    SchemaColor.OnTertiary -> MaterialTheme.colorScheme.onTertiary
    SchemaColor.Surface -> MaterialTheme.colorScheme.surface
    SchemaColor.OnSurface -> MaterialTheme.colorScheme.onSurface
    SchemaColor.SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
    SchemaColor.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
    SchemaColor.Background -> MaterialTheme.colorScheme.background
    SchemaColor.OnBackground -> MaterialTheme.colorScheme.onBackground
    SchemaColor.Error -> MaterialTheme.colorScheme.error
    SchemaColor.OnError -> MaterialTheme.colorScheme.onError
    SchemaColor.Outline -> MaterialTheme.colorScheme.outline
    SchemaColor.OutlineVariant -> MaterialTheme.colorScheme.outlineVariant
    // Brand accent slots — host-themed; today they map to surfaceVariant
    // tints. Replace with Caliclan brand colors when the design system lands.
    SchemaColor.Accent1 -> MaterialTheme.colorScheme.primary
    SchemaColor.Accent2 -> MaterialTheme.colorScheme.secondary
    SchemaColor.Accent3 -> MaterialTheme.colorScheme.tertiary
    SchemaColor.Accent4 -> MaterialTheme.colorScheme.surfaceVariant
    SchemaColor.Transparent -> Color.Transparent
}

@Composable
private fun SchemaTextStyle.toTextStyle(): TextStyle = when (this) {
    SchemaTextStyle.DisplayLarge -> MaterialTheme.typography.displayLarge
    SchemaTextStyle.DisplayMedium -> MaterialTheme.typography.displayMedium
    SchemaTextStyle.DisplaySmall -> MaterialTheme.typography.displaySmall
    SchemaTextStyle.HeadlineLarge -> MaterialTheme.typography.headlineLarge
    SchemaTextStyle.HeadlineMedium -> MaterialTheme.typography.headlineMedium
    SchemaTextStyle.HeadlineSmall -> MaterialTheme.typography.headlineSmall
    SchemaTextStyle.TitleLarge -> MaterialTheme.typography.titleLarge
    SchemaTextStyle.TitleMedium -> MaterialTheme.typography.titleMedium
    SchemaTextStyle.TitleSmall -> MaterialTheme.typography.titleSmall
    SchemaTextStyle.BodyLarge -> MaterialTheme.typography.bodyLarge
    SchemaTextStyle.BodyMedium -> MaterialTheme.typography.bodyMedium
    SchemaTextStyle.BodySmall -> MaterialTheme.typography.bodySmall
    SchemaTextStyle.LabelLarge -> MaterialTheme.typography.labelLarge
    SchemaTextStyle.LabelMedium -> MaterialTheme.typography.labelMedium
    SchemaTextStyle.LabelSmall -> MaterialTheme.typography.labelSmall
}

private fun SchemaArrangement.toHorizontal(): Arrangement.Horizontal = when (this) {
    SchemaArrangement.Start -> Arrangement.Start
    SchemaArrangement.Center -> Arrangement.Center
    SchemaArrangement.End -> Arrangement.End
    SchemaArrangement.SpaceBetween -> Arrangement.SpaceBetween
    SchemaArrangement.SpaceAround -> Arrangement.SpaceAround
    SchemaArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
}

private fun SchemaArrangement.toVertical(): Arrangement.Vertical = when (this) {
    SchemaArrangement.Start -> Arrangement.Top
    SchemaArrangement.Center -> Arrangement.Center
    SchemaArrangement.End -> Arrangement.Bottom
    SchemaArrangement.SpaceBetween -> Arrangement.SpaceBetween
    SchemaArrangement.SpaceAround -> Arrangement.SpaceAround
    SchemaArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
}

private fun SchemaHorizontalAlignment.toAlignment(): Alignment.Horizontal = when (this) {
    SchemaHorizontalAlignment.Start -> Alignment.Start
    SchemaHorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
    SchemaHorizontalAlignment.End -> Alignment.End
}

private fun SchemaVerticalAlignment.toAlignment(): Alignment.Vertical = when (this) {
    SchemaVerticalAlignment.Top -> Alignment.Top
    SchemaVerticalAlignment.CenterVertically -> Alignment.CenterVertically
    SchemaVerticalAlignment.Bottom -> Alignment.Bottom
}

private fun SchemaIconName.toImageVector(): ImageVector = when (this) {
    SchemaIconName.Home -> Icons.Filled.Home
    SchemaIconName.Settings -> Icons.Filled.Settings
    SchemaIconName.Star -> Icons.Filled.Star
    SchemaIconName.Favorite -> Icons.Filled.Favorite
    SchemaIconName.Search -> Icons.Filled.Search
    SchemaIconName.Menu -> Icons.Filled.Menu
    SchemaIconName.Close -> Icons.Filled.Close
    SchemaIconName.Add -> Icons.Filled.Add
    // AutoMirrored variants flip horizontally under RTL layouts — the
    // M3 deprecation note on Icons.Filled.ArrowBack/ArrowForward points
    // here. Semantics are identical for LTR (our only target today),
    // and free RTL correctness later.
    SchemaIconName.ArrowBack -> Icons.AutoMirrored.Filled.ArrowBack
    SchemaIconName.ArrowForward -> Icons.AutoMirrored.Filled.ArrowForward
    SchemaIconName.Person -> Icons.Filled.Person
    SchemaIconName.Notifications -> Icons.Filled.Notifications
    SchemaIconName.Email -> Icons.Filled.Email
    SchemaIconName.Phone -> Icons.Filled.Phone
    SchemaIconName.Lock -> Icons.Filled.Lock
    SchemaIconName.Edit -> Icons.Filled.Edit
    SchemaIconName.Delete -> Icons.Filled.Delete
    SchemaIconName.Check -> Icons.Filled.Check
    SchemaIconName.Info -> Icons.Filled.Info
    SchemaIconName.Warning -> Icons.Filled.Warning
}

// ============================================================================
// LayoutModifier translation
//
// Each Cmp* widget reads its `modifier` (the konduit chain set by the
// runtime) and walks `forEachUnscoped`, translating each schema modifier
// into a compose Modifier.
//
// Weight is intentionally NOT applied here — only Row/Column children
// can apply it, and they do so before invoking value(...) on the child.
// ============================================================================

@Composable
private fun KonduitModifier.applyToCompose(base: ComposeModifier): ComposeModifier {
    var m = base
    var bgSchemaColor: SchemaColor? = null
    // Background corner radius — added in the rounded-Background follow-up.
    // 0 = rectangular fill (the original behavior); >0 = rounded fill of
    // that radius. Same additive-with-default pattern as Border.
    var bgCornerRadiusDp: Int = 0
    // Border state hoisted same way as Background — `toComposeColor()`
    // is @Composable and can't run inside the (non-composable)
    // forEachUnscoped lambda. Latest Border in the chain wins (matches
    // Background semantics; documented in KDoc).
    var borderThicknessDp: Int = 0
    var borderSchemaColor: SchemaColor? = null
    // Border corner radius — added in the rounded-Border follow-up. 0 =
    // sharp rectangle (the original Border behavior); >0 = rounded
    // stroke matching a sibling Clip(cornerRadiusDp).
    var borderCornerRadiusDp: Int = 0
    forEachUnscoped { el ->
        when (el) {
            is MFillMaxSize -> m = m.fillMaxSize()
            is MFillMaxWidth -> m = m.fillMaxWidth()
            is MFillMaxHeight -> m = m.fillMaxHeight()
            is MPadding -> m = m.padding(
                start = el.start.dp,
                top = el.top.dp,
                end = el.end.dp,
                bottom = el.bottom.dp,
            )
            is MSize -> m = m.size(width = el.width.dp, height = el.height.dp)
            is MWidth -> m = m.width(el.value.dp)
            is MHeight -> m = m.height(el.value.dp)
            is MBackground -> {
                bgSchemaColor = el.color
                bgCornerRadiusDp = el.cornerRadiusDp
            }
            is MAlpha -> m = m.alpha(el.value.toFloat())
            is MWeight -> { /* applied by parent Row/Column */ }
            // Tier 3 modifier additions (tags 12–17). Clip / WrapContent /
            // AspectRatio apply inline; Border defers (composable color
            // resolution) and is applied after the loop along with bg.
            is MBorder -> {
                borderThicknessDp = el.thicknessDp
                borderSchemaColor = el.color
                borderCornerRadiusDp = el.cornerRadiusDp
            }
            is MClip -> if (el.cornerRadiusDp > 0) {
                m = m.clip(androidx.compose.foundation.shape.RoundedCornerShape(el.cornerRadiusDp.dp))
            }
            is MClipCircle -> m = m.clip(androidx.compose.foundation.shape.CircleShape)
            is MWrapContentWidth -> m = m.wrapContentWidth()
            is MWrapContentHeight -> m = m.wrapContentHeight()
            is MAspectRatio -> m = m.aspectRatio(el.ratio.toFloat())
        }
    }
    val bg = bgSchemaColor
    if (bg != null) {
        // RoundedCornerShape(0.dp) ≡ RectangleShape — single code path,
        // no branch needed for the rectangular case (same trick as Border).
        m = m.background(
            color = bg.toComposeColor(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(bgCornerRadiusDp.dp),
        )
    }
    val bColor = borderSchemaColor
    if (bColor != null) {
        // RoundedCornerShape(0.dp) is equivalent to RectangleShape, so we
        // can always pass a RoundedCornerShape — no need to branch on
        // whether cornerRadiusDp is zero.
        m = m.border(
            width = borderThicknessDp.dp,
            color = bColor.toComposeColor(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(borderCornerRadiusDp.dp),
        )
    }
    return m
}

private fun KonduitModifier.findWeight(): Float? {
    var w: Float? = null
    forEachUnscoped { el ->
        if (el is MWeight) w = el.value.toFloat()
    }
    return w
}

// ============================================================================
// Tier 1 — host Cmp* implementations
//
// `modifier` is state-backed so that the runtime's setter triggers
// recomposition of the value lambda, which re-reads the chain.
// ============================================================================

private class StateModifier {
    private val _modifier = mutableStateOf<KonduitModifier>(KonduitModifier)
    var value: KonduitModifier
        get() = _modifier.value
        set(v) { _modifier.value = v }
}

class CmpBox : Box<CmpRender> {
    private val mod = StateModifier()
    private var onClick by mutableStateOf<(() -> Unit)?>(null)
    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val click = onClick
        val composed = modifier.applyToCompose(incoming)
            .let { if (click != null) it.clickable { click() } else it }
        androidx.compose.foundation.layout.Box(modifier = composed) {
            (children as CmpChildren).render()
        }
    }

    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpColumn : com.example.serverdrivenui.schema.widget.Column<CmpRender> {
    private val mod = StateModifier()
    private var verticalArrangement by mutableStateOf(SchemaArrangement.Start)
    private var horizontalAlignment by mutableStateOf(SchemaHorizontalAlignment.Start)

    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.foundation.layout.Column(
            modifier = composed,
            verticalArrangement = verticalArrangement.toVertical(),
            horizontalAlignment = horizontalAlignment.toAlignment(),
        ) {
            (children as CmpChildren).renderInColumn(this)
        }
    }

    override fun verticalArrangement(verticalArrangement: SchemaArrangement) {
        this.verticalArrangement = verticalArrangement
    }
    override fun horizontalAlignment(horizontalAlignment: SchemaHorizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment
    }
}

class CmpRow : com.example.serverdrivenui.schema.widget.Row<CmpRender> {
    private val mod = StateModifier()
    private var horizontalArrangement by mutableStateOf(SchemaArrangement.Start)
    private var verticalAlignment by mutableStateOf(SchemaVerticalAlignment.Top)

    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.foundation.layout.Row(
            modifier = composed,
            horizontalArrangement = horizontalArrangement.toHorizontal(),
            verticalAlignment = verticalAlignment.toAlignment(),
        ) {
            (children as CmpChildren).renderInRow(this)
        }
    }

    override fun horizontalArrangement(horizontalArrangement: SchemaArrangement) {
        this.horizontalArrangement = horizontalArrangement
    }
    override fun verticalAlignment(verticalAlignment: SchemaVerticalAlignment) {
        this.verticalAlignment = verticalAlignment
    }
}

class CmpSpacer : com.example.serverdrivenui.schema.widget.Spacer<CmpRender> {
    private val mod = StateModifier()
    private var width by mutableStateOf(0)
    private var height by mutableStateOf(0)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        var m = modifier.applyToCompose(incoming)
        if (width > 0) m = m.width(width.dp)
        if (height > 0) m = m.height(height.dp)
        androidx.compose.foundation.layout.Spacer(modifier = m)
    }

    override fun width(width: Int) { this.width = width }
    override fun height(height: Int) { this.height = height }
}

class CmpLazyColumn : LazyColumn<CmpRender> {
    private val mod = StateModifier()
    override val items: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        ComposeLazyColumn(modifier = composed) {
            (items as CmpChildren).renderInLazyScope(this)
        }
    }
}

class CmpLazyRow : LazyRow<CmpRender> {
    private val mod = StateModifier()
    override val items: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        ComposeLazyRow(modifier = composed) {
            (items as CmpChildren).renderInLazyScope(this)
        }
    }
}

class CmpLazyItem : LazyItem<CmpRender> {
    private val mod = StateModifier()
    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.foundation.layout.Box(modifier = composed) {
            (children as CmpChildren).render()
        }
    }
}

class CmpText : com.example.serverdrivenui.schema.widget.Text<CmpRender> {
    private val mod = StateModifier()
    private var text by mutableStateOf("")
    private var color by mutableStateOf(SchemaColor.OnSurface)
    private var style by mutableStateOf(SchemaTextStyle.BodyMedium)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        ComposeText(
            text = text,
            color = color.toComposeColor(),
            style = style.toTextStyle(),
            modifier = composed,
        )
    }

    override fun text(text: String) { this.text = text }
    override fun color(color: SchemaColor) { this.color = color }
    override fun style(style: SchemaTextStyle) { this.style = style }
}

class CmpAsyncImage : AsyncImage<CmpRender> {
    private val mod = StateModifier()
    private var url by mutableStateOf("")
    private var contentDescription by mutableStateOf("")

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        if (url.isNotEmpty()) {
            val composed = modifier.applyToCompose(incoming)
            CoilAsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = composed,
                onState = { state ->
                    when (state) {
                        is coil3.compose.AsyncImagePainter.State.Loading ->
                            println("CmpAsyncImage: Loading $url")
                        is coil3.compose.AsyncImagePainter.State.Success ->
                            println("CmpAsyncImage: Success $url (${state.result.image.width}x${state.result.image.height})")
                        is coil3.compose.AsyncImagePainter.State.Error ->
                            println("CmpAsyncImage: Error $url: ${state.result.throwable}")
                        else -> {}
                    }
                },
            )
        }
    }

    override fun url(url: String) { this.url = url }
    override fun contentDescription(contentDescription: String) {
        this.contentDescription = contentDescription
    }
}

class CmpIcon : com.example.serverdrivenui.schema.widget.Icon<CmpRender> {
    private val mod = StateModifier()
    private var name by mutableStateOf(SchemaIconName.Star)
    private var tint by mutableStateOf(SchemaColor.OnSurface)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        ComposeIcon(
            imageVector = name.toImageVector(),
            contentDescription = name.name,
            tint = tint.toComposeColor(),
            modifier = composed,
        )
    }

    override fun name(name: SchemaIconName) { this.name = name }
    override fun tint(tint: SchemaColor) { this.tint = tint }
}

// ============================================================================
// Tier 2 — Buttons (IDs 21–28)
//
// Common pattern: each Cmp* state-backs `text` (where applicable),
// `enabled`, and `onClick`. The compose Modifier built from `applyToCompose`
// flows into the underlying Material3 button.
// ============================================================================

private class ButtonStateText {
    var text by mutableStateOf("")
    var enabled by mutableStateOf(true)
    var onClick by mutableStateOf<(() -> Unit)?>(null)
}

@Composable
private fun ButtonLabel(text: String) {
    ComposeText(text = text)
}

class CmpButton : com.example.serverdrivenui.schema.widget.Button<CmpRender> {
    private val mod = StateModifier()
    private val s = ButtonStateText()

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = s.onClick
        androidx.compose.material3.Button(
            onClick = { cb?.invoke() },
            enabled = s.enabled,
            modifier = composed,
        ) { ButtonLabel(s.text) }
    }

    override fun text(text: String) { s.text = text }
    override fun enabled(enabled: Boolean) { s.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { s.onClick = onClick }
}

class CmpOutlinedButton : com.example.serverdrivenui.schema.widget.OutlinedButton<CmpRender> {
    private val mod = StateModifier()
    private val s = ButtonStateText()

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = s.onClick
        androidx.compose.material3.OutlinedButton(
            onClick = { cb?.invoke() },
            enabled = s.enabled,
            modifier = composed,
        ) { ButtonLabel(s.text) }
    }

    override fun text(text: String) { s.text = text }
    override fun enabled(enabled: Boolean) { s.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { s.onClick = onClick }
}

class CmpTextButton : com.example.serverdrivenui.schema.widget.TextButton<CmpRender> {
    private val mod = StateModifier()
    private val s = ButtonStateText()

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = s.onClick
        androidx.compose.material3.TextButton(
            onClick = { cb?.invoke() },
            enabled = s.enabled,
            modifier = composed,
        ) { ButtonLabel(s.text) }
    }

    override fun text(text: String) { s.text = text }
    override fun enabled(enabled: Boolean) { s.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { s.onClick = onClick }
}

class CmpFilledTonalButton : com.example.serverdrivenui.schema.widget.FilledTonalButton<CmpRender> {
    private val mod = StateModifier()
    private val s = ButtonStateText()

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = s.onClick
        androidx.compose.material3.FilledTonalButton(
            onClick = { cb?.invoke() },
            enabled = s.enabled,
            modifier = composed,
        ) { ButtonLabel(s.text) }
    }

    override fun text(text: String) { s.text = text }
    override fun enabled(enabled: Boolean) { s.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { s.onClick = onClick }
}

class CmpElevatedButton : com.example.serverdrivenui.schema.widget.ElevatedButton<CmpRender> {
    private val mod = StateModifier()
    private val s = ButtonStateText()

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = s.onClick
        androidx.compose.material3.ElevatedButton(
            onClick = { cb?.invoke() },
            enabled = s.enabled,
            modifier = composed,
        ) { ButtonLabel(s.text) }
    }

    override fun text(text: String) { s.text = text }
    override fun enabled(enabled: Boolean) { s.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { s.onClick = onClick }
}

class CmpIconButton : com.example.serverdrivenui.schema.widget.IconButton<CmpRender> {
    private val mod = StateModifier()
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.material3.IconButton(
            onClick = { cb?.invoke() },
            enabled = enabled,
            modifier = composed,
        ) {
            (content as CmpChildren).render()
        }
    }

    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpFloatingActionButton : com.example.serverdrivenui.schema.widget.FloatingActionButton<CmpRender> {
    private val mod = StateModifier()
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.material3.FloatingActionButton(
            onClick = { cb?.invoke() },
            modifier = composed,
        ) {
            (content as CmpChildren).render()
        }
    }

    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpExtendedFloatingActionButton :
    com.example.serverdrivenui.schema.widget.ExtendedFloatingActionButton<CmpRender> {
    private val mod = StateModifier()
    private var text by mutableStateOf("")
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val icon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.material3.ExtendedFloatingActionButton(
            text = { ComposeText(text = text) },
            icon = { (icon as CmpChildren).render() },
            onClick = { cb?.invoke() },
            modifier = composed,
        )
    }

    override fun text(text: String) { this.text = text }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

// ============================================================================
// Tier 2 — Inputs (IDs 31–33)
// ============================================================================

class CmpTextField : com.example.serverdrivenui.schema.widget.TextField<CmpRender> {
    private val mod = StateModifier()
    private var fieldValue by mutableStateOf("")
    private var placeholder by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onValueChange by mutableStateOf<((String) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onValueChange
        androidx.compose.material3.TextField(
            value = fieldValue,
            onValueChange = { cb?.invoke(it) },
            placeholder = { ComposeText(text = placeholder) },
            enabled = enabled,
            singleLine = true,
            modifier = composed,
        )
    }

    override fun value(value: String) { this.fieldValue = value }
    override fun placeholder(placeholder: String) { this.placeholder = placeholder }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onValueChange(onValueChange: ((String) -> Unit)?) {
        this.onValueChange = onValueChange
    }
}

class CmpOutlinedTextField :
    com.example.serverdrivenui.schema.widget.OutlinedTextField<CmpRender> {
    private val mod = StateModifier()
    private var fieldValue by mutableStateOf("")
    private var placeholder by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onValueChange by mutableStateOf<((String) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onValueChange
        androidx.compose.material3.OutlinedTextField(
            value = fieldValue,
            onValueChange = { cb?.invoke(it) },
            placeholder = { ComposeText(text = placeholder) },
            enabled = enabled,
            singleLine = true,
            modifier = composed,
        )
    }

    override fun value(value: String) { this.fieldValue = value }
    override fun placeholder(placeholder: String) { this.placeholder = placeholder }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onValueChange(onValueChange: ((String) -> Unit)?) {
        this.onValueChange = onValueChange
    }
}

/**
 * Search bar = OutlinedTextField with a leading Search icon. We deliberately
 * stay away from `androidx.compose.material3.SearchBar` because that opens
 * an expandable suggestion sheet which doesn't fit the SDUI model yet.
 */
class CmpSearchBar : com.example.serverdrivenui.schema.widget.SearchBar<CmpRender> {
    private val mod = StateModifier()
    private var fieldValue by mutableStateOf("")
    private var placeholder by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onValueChange by mutableStateOf<((String) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onValueChange
        androidx.compose.material3.OutlinedTextField(
            value = fieldValue,
            onValueChange = { cb?.invoke(it) },
            placeholder = { ComposeText(text = placeholder) },
            enabled = enabled,
            singleLine = true,
            leadingIcon = {
                ComposeIcon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                )
            },
            modifier = composed,
        )
    }

    override fun value(value: String) { this.fieldValue = value }
    override fun placeholder(placeholder: String) { this.placeholder = placeholder }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onValueChange(onValueChange: ((String) -> Unit)?) {
        this.onValueChange = onValueChange
    }
}

// ============================================================================
// Tier 2 — Selection (IDs 41–46)
// ============================================================================

class CmpCheckbox : com.example.serverdrivenui.schema.widget.Checkbox<CmpRender> {
    private val mod = StateModifier()
    private var checked by mutableStateOf(false)
    private var enabled by mutableStateOf(true)
    private var onCheckedChange by mutableStateOf<((Boolean) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onCheckedChange
        androidx.compose.material3.Checkbox(
            checked = checked,
            onCheckedChange = { cb?.invoke(it) },
            enabled = enabled,
            modifier = composed,
        )
    }

    override fun checked(checked: Boolean) { this.checked = checked }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onCheckedChange(onCheckedChange: ((Boolean) -> Unit)?) {
        this.onCheckedChange = onCheckedChange
    }
}

class CmpRadioButton : com.example.serverdrivenui.schema.widget.RadioButton<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = { cb?.invoke() },
            enabled = enabled,
            modifier = composed,
        )
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpSwitch : com.example.serverdrivenui.schema.widget.Switch<CmpRender> {
    private val mod = StateModifier()
    private var checked by mutableStateOf(false)
    private var enabled by mutableStateOf(true)
    private var onCheckedChange by mutableStateOf<((Boolean) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onCheckedChange
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = { cb?.invoke(it) },
            enabled = enabled,
            modifier = composed,
        )
    }

    override fun checked(checked: Boolean) { this.checked = checked }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onCheckedChange(onCheckedChange: ((Boolean) -> Unit)?) {
        this.onCheckedChange = onCheckedChange
    }
}

class CmpSlider : com.example.serverdrivenui.schema.widget.Slider<CmpRender> {
    private val mod = StateModifier()
    private var sliderValue by mutableStateOf(0f)
    private var valueFrom by mutableStateOf(0f)
    private var valueTo by mutableStateOf(1f)
    private var steps by mutableStateOf(0)
    private var enabled by mutableStateOf(true)
    private var onValueChange by mutableStateOf<((Float) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onValueChange
        androidx.compose.material3.Slider(
            value = sliderValue,
            onValueChange = { cb?.invoke(it) },
            valueRange = valueFrom..valueTo,
            steps = steps,
            enabled = enabled,
            modifier = composed,
        )
    }

    override fun value(value: Float) { this.sliderValue = value }
    override fun valueFrom(valueFrom: Float) { this.valueFrom = valueFrom }
    override fun valueTo(valueTo: Float) { this.valueTo = valueTo }
    override fun steps(steps: Int) { this.steps = steps }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onValueChange(onValueChange: ((Float) -> Unit)?) {
        this.onValueChange = onValueChange
    }
}

class CmpRangeSlider : com.example.serverdrivenui.schema.widget.RangeSlider<CmpRender> {
    private val mod = StateModifier()
    private var rangeStart by mutableStateOf(0f)
    private var rangeEnd by mutableStateOf(1f)
    private var valueFrom by mutableStateOf(0f)
    private var valueTo by mutableStateOf(1f)
    private var steps by mutableStateOf(0)
    private var enabled by mutableStateOf(true)
    private var onRangeChange by mutableStateOf<((Float, Float) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onRangeChange
        androidx.compose.material3.RangeSlider(
            value = rangeStart..rangeEnd,
            onValueChange = { range -> cb?.invoke(range.start, range.endInclusive) },
            valueRange = valueFrom..valueTo,
            steps = steps,
            enabled = enabled,
            modifier = composed,
        )
    }

    override fun rangeStart(rangeStart: Float) { this.rangeStart = rangeStart }
    override fun rangeEnd(rangeEnd: Float) { this.rangeEnd = rangeEnd }
    override fun valueFrom(valueFrom: Float) { this.valueFrom = valueFrom }
    override fun valueTo(valueTo: Float) { this.valueTo = valueTo }
    override fun steps(steps: Int) { this.steps = steps }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onRangeChange(onRangeChange: ((Float, Float) -> Unit)?) {
        this.onRangeChange = onRangeChange
    }
}

/**
 * Compose Multiplatform 1.8 doesn't ship `material3.SegmentedButton` on iOS
 * yet, so we render the row as a custom Row of FilledTonal / Outlined
 * buttons. Visually similar enough; promote to the real M3 widget when
 * CMP catches up.
 */
class CmpSegmentedButtonRow :
    com.example.serverdrivenui.schema.widget.SegmentedButtonRow<CmpRender> {
    private val mod = StateModifier()
    private var labelsCsv by mutableStateOf("")
    private var selectedIndex by mutableStateOf(0)
    private var onSelectionChange by mutableStateOf<((Int) -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onSelectionChange
        val labels = labelsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (labels.isNotEmpty()) {
            androidx.compose.foundation.layout.Row(
                modifier = composed,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                labels.forEachIndexed { index, label ->
                    if (index == selectedIndex) {
                        androidx.compose.material3.FilledTonalButton(
                            onClick = { cb?.invoke(index) },
                        ) { ComposeText(text = label) }
                    } else {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { cb?.invoke(index) },
                        ) { ComposeText(text = label) }
                    }
                }
            }
        }
    }

    override fun labelsCsv(labelsCsv: String) { this.labelsCsv = labelsCsv }
    override fun selectedIndex(selectedIndex: Int) { this.selectedIndex = selectedIndex }
    override fun onSelectionChange(onSelectionChange: ((Int) -> Unit)?) {
        this.onSelectionChange = onSelectionChange
    }
}

// ============================================================================
// Tier 2 — Containers (IDs 51–54)
// ============================================================================

class CmpCard : com.example.serverdrivenui.schema.widget.Card<CmpRender> {
    private val mod = StateModifier()
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        if (cb != null) {
            androidx.compose.material3.Card(onClick = { cb() }, modifier = composed) {
                (content as CmpChildren).render()
            }
        } else {
            androidx.compose.material3.Card(modifier = composed) {
                (content as CmpChildren).render()
            }
        }
    }

    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpElevatedCard : com.example.serverdrivenui.schema.widget.ElevatedCard<CmpRender> {
    private val mod = StateModifier()
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        if (cb != null) {
            androidx.compose.material3.ElevatedCard(onClick = { cb() }, modifier = composed) {
                (content as CmpChildren).render()
            }
        } else {
            androidx.compose.material3.ElevatedCard(modifier = composed) {
                (content as CmpChildren).render()
            }
        }
    }

    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpOutlinedCard : com.example.serverdrivenui.schema.widget.OutlinedCard<CmpRender> {
    private val mod = StateModifier()
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        if (cb != null) {
            androidx.compose.material3.OutlinedCard(onClick = { cb() }, modifier = composed) {
                (content as CmpChildren).render()
            }
        } else {
            androidx.compose.material3.OutlinedCard(modifier = composed) {
                (content as CmpChildren).render()
            }
        }
    }

    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpSurface : com.example.serverdrivenui.schema.widget.Surface<CmpRender> {
    private val mod = StateModifier()
    private var tonalElevationDp by mutableStateOf(0)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        if (cb != null) {
            androidx.compose.material3.Surface(
                onClick = { cb() },
                tonalElevation = tonalElevationDp.dp,
                modifier = composed,
            ) { (content as CmpChildren).render() }
        } else {
            androidx.compose.material3.Surface(
                tonalElevation = tonalElevationDp.dp,
                modifier = composed,
            ) { (content as CmpChildren).render() }
        }
    }

    override fun tonalElevationDp(tonalElevationDp: Int) {
        this.tonalElevationDp = tonalElevationDp
    }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

// ============================================================================
// Tier 2 — Feedback (IDs 61–64)
// ============================================================================

class CmpLinearProgressIndicator :
    com.example.serverdrivenui.schema.widget.LinearProgressIndicator<CmpRender> {
    private val mod = StateModifier()
    private var progress by mutableStateOf(0f)
    private var indeterminate by mutableStateOf(true)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        if (indeterminate) {
            androidx.compose.material3.LinearProgressIndicator(modifier = composed)
        } else {
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = composed,
            )
        }
    }

    override fun progress(progress: Float) { this.progress = progress }
    override fun indeterminate(indeterminate: Boolean) {
        this.indeterminate = indeterminate
    }
}

class CmpCircularProgressIndicator :
    com.example.serverdrivenui.schema.widget.CircularProgressIndicator<CmpRender> {
    private val mod = StateModifier()
    private var progress by mutableStateOf(0f)
    private var indeterminate by mutableStateOf(true)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        if (indeterminate) {
            androidx.compose.material3.CircularProgressIndicator(modifier = composed)
        } else {
            androidx.compose.material3.CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = composed,
            )
        }
    }

    override fun progress(progress: Float) { this.progress = progress }
    override fun indeterminate(indeterminate: Boolean) {
        this.indeterminate = indeterminate
    }
}

class CmpBadge : com.example.serverdrivenui.schema.widget.Badge<CmpRender> {
    private val mod = StateModifier()
    private var text by mutableStateOf("")

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        if (text.isNotEmpty()) {
            androidx.compose.material3.Badge(modifier = composed) {
                ComposeText(text = text)
            }
        } else {
            androidx.compose.material3.Badge(modifier = composed)
        }
    }

    override fun text(text: String) { this.text = text }
}

class CmpSnackbar : com.example.serverdrivenui.schema.widget.Snackbar<CmpRender> {
    private val mod = StateModifier()
    private var message by mutableStateOf("")
    private var actionLabel by mutableStateOf("")
    private var onActionClick by mutableStateOf<(() -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onActionClick
        androidx.compose.material3.Snackbar(
            modifier = composed,
            action = if (actionLabel.isNotEmpty()) {
                {
                    androidx.compose.material3.TextButton(onClick = { cb?.invoke() }) {
                        ComposeText(text = actionLabel)
                    }
                }
            } else null,
        ) {
            ComposeText(text = message)
        }
    }

    override fun message(message: String) { this.message = message }
    override fun actionLabel(actionLabel: String) { this.actionLabel = actionLabel }
    override fun onActionClick(onActionClick: (() -> Unit)?) {
        this.onActionClick = onActionClick
    }
}

// ============================================================================
// Tier 2 — Navigation structure (IDs 71–78)
// ============================================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpScaffold : com.example.serverdrivenui.schema.widget.Scaffold<CmpRender> {
    private val mod = StateModifier()
    override val topBar: Widget.Children<CmpRender> = CmpChildren()
    override val bottomBar: Widget.Children<CmpRender> = CmpChildren()
    override val floatingActionButton: Widget.Children<CmpRender> = CmpChildren()
    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.Scaffold(
            modifier = composed,
            topBar = { (topBar as CmpChildren).render() },
            bottomBar = { (bottomBar as CmpChildren).render() },
            floatingActionButton = { (floatingActionButton as CmpChildren).render() },
        ) { padding ->
            androidx.compose.foundation.layout.Box(
                modifier = ComposeModifier.padding(padding),
            ) {
                (content as CmpChildren).render()
            }
        }
    }
}

@Composable
private fun renderTopAppBarSlot(slot: CmpChildren) {
    slot.render()
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpTopAppBar : com.example.serverdrivenui.schema.widget.TopAppBar<CmpRender> {
    private val mod = StateModifier()
    private var title by mutableStateOf("")
    override val navigationIcon: Widget.Children<CmpRender> = CmpChildren()
    override val actions: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.TopAppBar(
            title = { ComposeText(text = title) },
            navigationIcon = { renderTopAppBarSlot(navigationIcon as CmpChildren) },
            actions = {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) { renderTopAppBarSlot(actions as CmpChildren) }
            },
            modifier = composed,
        )
    }

    override fun title(title: String) { this.title = title }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpLargeTopAppBar : com.example.serverdrivenui.schema.widget.LargeTopAppBar<CmpRender> {
    private val mod = StateModifier()
    private var title by mutableStateOf("")
    override val navigationIcon: Widget.Children<CmpRender> = CmpChildren()
    override val actions: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.LargeTopAppBar(
            title = { ComposeText(text = title) },
            navigationIcon = { renderTopAppBarSlot(navigationIcon as CmpChildren) },
            actions = {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) { renderTopAppBarSlot(actions as CmpChildren) }
            },
            modifier = composed,
        )
    }

    override fun title(title: String) { this.title = title }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpMediumTopAppBar : com.example.serverdrivenui.schema.widget.MediumTopAppBar<CmpRender> {
    private val mod = StateModifier()
    private var title by mutableStateOf("")
    override val navigationIcon: Widget.Children<CmpRender> = CmpChildren()
    override val actions: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.MediumTopAppBar(
            title = { ComposeText(text = title) },
            navigationIcon = { renderTopAppBarSlot(navigationIcon as CmpChildren) },
            actions = {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) { renderTopAppBarSlot(actions as CmpChildren) }
            },
            modifier = composed,
        )
    }

    override fun title(title: String) { this.title = title }
}

class CmpNavigationBar : com.example.serverdrivenui.schema.widget.NavigationBar<CmpRender> {
    private val mod = StateModifier()
    override val items: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.NavigationBar(modifier = composed) {
            (items as CmpChildren).renderInRow(this)
        }
    }
}

class CmpNavigationBarItem :
    com.example.serverdrivenui.schema.widget.NavigationBarItem<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var label by mutableStateOf("")
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val icon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        // NavigationBar items provide their own RowScope inside the bar.
        // We can't access RowScope here, so render inside a Box and trust
        // the NavigationBar to lay out children sensibly.
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.foundation.layout.Box(modifier = composed) {
            // Material 3 NavigationBarItem requires RowScope, but we render
            // through the children loop in CmpNavigationBar (renderInRow)
            // which already provides one. Wrap our content here.
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = ComposeModifier
                    .clickable { cb?.invoke() }
                    .padding(8.dp),
            ) {
                (icon as CmpChildren).render()
                if (label.isNotEmpty()) {
                    ComposeText(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun label(label: String) { this.label = label }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpTabRow : com.example.serverdrivenui.schema.widget.TabRow<CmpRender> {
    private val mod = StateModifier()
    private var selectedTabIndex by mutableStateOf(0)

    override val tabs: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = composed,
        ) {
            (tabs as CmpChildren).render()
        }
    }

    override fun selectedTabIndex(selectedTabIndex: Int) {
        this.selectedTabIndex = selectedTabIndex
    }
}

class CmpTab : com.example.serverdrivenui.schema.widget.Tab<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var text by mutableStateOf("")
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val icon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.material3.Tab(
            selected = selected,
            onClick = { cb?.invoke() },
            text = if (text.isNotEmpty()) {
                { ComposeText(text = text) }
            } else null,
            icon = if ((icon as CmpChildren).widgets.isNotEmpty()) {
                { (icon as CmpChildren).render() }
            } else null,
            modifier = composed,
        )
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun text(text: String) { this.text = text }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

// ============================================================================
// Tier 2 — Misc (IDs 79–80)
// ============================================================================

class CmpHorizontalDivider :
    com.example.serverdrivenui.schema.widget.HorizontalDivider<CmpRender> {
    private val mod = StateModifier()
    private var thicknessDp by mutableStateOf(1)
    private var color by mutableStateOf(SchemaColor.OutlineVariant)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.HorizontalDivider(
            thickness = thicknessDp.dp,
            color = color.toComposeColor(),
            modifier = composed,
        )
    }

    override fun thicknessDp(thicknessDp: Int) { this.thicknessDp = thicknessDp }
    override fun color(color: SchemaColor) { this.color = color }
}

class CmpVerticalDivider :
    com.example.serverdrivenui.schema.widget.VerticalDivider<CmpRender> {
    private val mod = StateModifier()
    private var thicknessDp by mutableStateOf(1)
    private var color by mutableStateOf(SchemaColor.OutlineVariant)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        androidx.compose.material3.VerticalDivider(
            thickness = thicknessDp.dp,
            color = color.toComposeColor(),
            modifier = composed,
        )
    }

    override fun thicknessDp(thicknessDp: Int) { this.thicknessDp = thicknessDp }
    override fun color(color: SchemaColor) { this.color = color }
}

// ============================================================================
// Tier 3 — Chips (IDs 100–103)
//
// All four chips share a single leadingIcon @Children(1) slot. We render
// the slot as Material 3's `leadingIcon` lambda only when the guest
// actually placed widgets in it — otherwise we pass `null` so the chip
// uses its compact "label-only" layout. FilterChip auto-renders a check
// glyph when selected (M3 default) which subsumes leadingIcon, so we
// short-circuit to null in the selected branch to avoid icon double-up.
// InputChip's onClose maps to the trailingIcon lambda; null hides the X.
// ============================================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpFilterChip : com.example.serverdrivenui.schema.widget.FilterChip<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var label by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val leadingIcon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        val hasIcon = (leadingIcon as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.FilterChip(
            selected = selected,
            onClick = { cb?.invoke() },
            label = { ComposeText(text = label) },
            enabled = enabled,
            // M3 renders a check glyph when selected; surrender the
            // leadingIcon slot in that branch so we don't stack two icons.
            leadingIcon = if (hasIcon && !selected) {
                { (leadingIcon as CmpChildren).render() }
            } else null,
            modifier = composed,
        )
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun label(label: String) { this.label = label }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpAssistChip : com.example.serverdrivenui.schema.widget.AssistChip<CmpRender> {
    private val mod = StateModifier()
    private var label by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val leadingIcon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        val hasIcon = (leadingIcon as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.AssistChip(
            onClick = { cb?.invoke() },
            label = { ComposeText(text = label) },
            enabled = enabled,
            leadingIcon = if (hasIcon) {
                { (leadingIcon as CmpChildren).render() }
            } else null,
            modifier = composed,
        )
    }

    override fun label(label: String) { this.label = label }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpInputChip : com.example.serverdrivenui.schema.widget.InputChip<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var label by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)
    private var onClose by mutableStateOf<(() -> Unit)?>(null)

    override val leadingIcon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val click = onClick
        val close = onClose
        val hasIcon = (leadingIcon as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.InputChip(
            selected = selected,
            onClick = { click?.invoke() },
            label = { ComposeText(text = label) },
            enabled = enabled,
            // FilterChip's check-glyph branch logic doesn't apply to
            // InputChip — M3 InputChip doesn't auto-render a selected
            // glyph; the avatar/icon slot is always honored.
            leadingIcon = if (hasIcon) {
                { (leadingIcon as CmpChildren).render() }
            } else null,
            // Conventional X-to-dismiss; null hides the trailing slot.
            trailingIcon = if (close != null) {
                {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = ComposeModifier
                            .size(androidx.compose.material3.InputChipDefaults.IconSize)
                            .clickable { close.invoke() },
                    )
                }
            } else null,
            modifier = composed,
        )
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun label(label: String) { this.label = label }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
    override fun onClose(onClose: (() -> Unit)?) { this.onClose = onClose }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpSuggestionChip :
    com.example.serverdrivenui.schema.widget.SuggestionChip<CmpRender> {
    private val mod = StateModifier()
    private var label by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val leadingIcon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        val hasIcon = (leadingIcon as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.SuggestionChip(
            onClick = { cb?.invoke() },
            label = { ComposeText(text = label) },
            enabled = enabled,
            icon = if (hasIcon) {
                { (leadingIcon as CmpChildren).render() }
            } else null,
            modifier = composed,
        )
    }

    override fun label(label: String) { this.label = label }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

// ============================================================================
// Tier 3 — List + Menus (IDs 110–112)
//
// ListItem maps the three text properties (headline / supporting /
// overline) into M3's `*Content` slot lambdas; empty strings collapse
// to null to preserve M3's natural compact layout. Slots gate on
// `widgets.isNotEmpty()` exactly like the chips do.
//
// DropdownMenu wraps `material3.DropdownMenu`; the popup renders inside
// the parent layout with default position. Typical Caliclan usage is to
// wrap the trigger (e.g. an IconButton) and the menu in a Box so the
// menu anchors to the trigger; the host doesn't need to do anything
// special for this — Popup positioning handles it.
//
// DropdownMenuItem is a leaf — no popup machinery; it renders inside
// the DropdownMenu's ColumnScope content lambda.
// ============================================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpListItem : com.example.serverdrivenui.schema.widget.ListItem<CmpRender> {
    private val mod = StateModifier()
    private var headline by mutableStateOf("")
    private var supporting by mutableStateOf("")
    private var overline by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val leadingContent: Widget.Children<CmpRender> = CmpChildren()
    override val trailingContent: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        // ListItem doesn't have a native enabled flag; gate clickable
        // wiring on enabled so a disabled row visually still renders
        // but won't fire onClick.
        val rowMod = if (cb != null && enabled) {
            composed.clickable { cb.invoke() }
        } else composed
        val hasLeading = (leadingContent as CmpChildren).widgets.isNotEmpty()
        val hasTrailing = (trailingContent as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.ListItem(
            headlineContent = { ComposeText(text = headline) },
            // Empty-string short-circuit: surrender the slot rather
            // than render an empty Text that pushes the row taller.
            supportingContent = if (supporting.isNotEmpty()) {
                { ComposeText(text = supporting) }
            } else null,
            overlineContent = if (overline.isNotEmpty()) {
                { ComposeText(text = overline) }
            } else null,
            leadingContent = if (hasLeading) {
                { (leadingContent as CmpChildren).render() }
            } else null,
            trailingContent = if (hasTrailing) {
                { (trailingContent as CmpChildren).render() }
            } else null,
            modifier = rowMod,
        )
    }

    override fun headline(headline: String) { this.headline = headline }
    override fun supporting(supporting: String) { this.supporting = supporting }
    override fun overline(overline: String) { this.overline = overline }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpDropdownMenu : com.example.serverdrivenui.schema.widget.DropdownMenu<CmpRender> {
    private val mod = StateModifier()
    private var expanded by mutableStateOf(false)
    private var onDismissRequest by mutableStateOf<() -> Unit>({})

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        // We don't apply `incoming` (parent modifier) to DropdownMenu's
        // own modifier — the popup positions itself relative to its
        // parent's coordinates and a non-trivial parent modifier on the
        // menu itself would shift the popup, not the trigger. Apply
        // modifier to the menu surface only via mod.value where needed
        // (currently no callers want this; pass Modifier through plain).
        val cb = onDismissRequest
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { cb.invoke() },
            // No modifier propagation: the parent modifier is for the
            // anchoring layout (the Box wrapping trigger + menu), not
            // for the menu surface itself.
        ) {
            (content as CmpChildren).render()
        }
    }

    override fun expanded(expanded: Boolean) { this.expanded = expanded }
    override fun onDismissRequest(onDismissRequest: () -> Unit) {
        this.onDismissRequest = onDismissRequest
    }
}

class CmpDropdownMenuItem :
    com.example.serverdrivenui.schema.widget.DropdownMenuItem<CmpRender> {
    private val mod = StateModifier()
    private var text by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val leadingIcon: Widget.Children<CmpRender> = CmpChildren()
    override val trailingIcon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        val hasLeading = (leadingIcon as CmpChildren).widgets.isNotEmpty()
        val hasTrailing = (trailingIcon as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.DropdownMenuItem(
            text = { ComposeText(text = text) },
            onClick = { cb?.invoke() },
            enabled = enabled,
            leadingIcon = if (hasLeading) {
                { (leadingIcon as CmpChildren).render() }
            } else null,
            trailingIcon = if (hasTrailing) {
                { (trailingIcon as CmpChildren).render() }
            } else null,
            modifier = composed,
        )
    }

    override fun text(text: String) { this.text = text }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

// ============================================================================
// Tier 3 — Overlays (IDs 120–121)
//
// Visibility lives in the guest: when the guest stops emitting the
// widget, the host stops rendering it. The widget's onDismissRequest
// fires AFTER M3's internal hide animation completes (for
// ModalBottomSheet) / immediately (for AlertDialog), so the guest can
// safely cut the widget from the tree on dismiss without truncating
// animations.
//
// We deliberately do NOT thread the parent modifier chain into either
// overlay's surface — same reason as DropdownMenu (Batch 3.1 decisions
// log): these are popup-style overlays positioned by Compose's window
// machinery, not part of the parent's layout flow. fillMaxWidth /
// padding on the overlay would shift the overlay surface inside its
// own window, not the trigger.
// ============================================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpModalBottomSheet :
    com.example.serverdrivenui.schema.widget.ModalBottomSheet<CmpRender> {
    private val mod = StateModifier()
    private var onDismissRequest by mutableStateOf<() -> Unit>({})
    private var skipPartiallyExpanded by mutableStateOf(false)

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { _ ->
        val cb = onDismissRequest
        // rememberModalBottomSheetState recomposes when skipPartiallyExpanded
        // changes — keying it lets the guest flip the knob between sheet
        // openings without stale state. (Live mid-show changes are not
        // expected; M3 doesn't react to them either.)
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
        )
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { cb.invoke() },
            sheetState = sheetState,
            // No parent-modifier propagation — see header comment.
        ) {
            // M3 ModalBottomSheet's content lambda is ColumnScope; calling
            // render() from inside the lambda places children inside that
            // ColumnScope so they stack vertically by default. Layout-axis
            // overrides (rows etc.) work via children that are themselves
            // Row widgets.
            (content as CmpChildren).render()
        }
    }

    override fun onDismissRequest(onDismissRequest: () -> Unit) {
        this.onDismissRequest = onDismissRequest
    }
    override fun skipPartiallyExpanded(skipPartiallyExpanded: Boolean) {
        this.skipPartiallyExpanded = skipPartiallyExpanded
    }
}

class CmpAlertDialog : com.example.serverdrivenui.schema.widget.AlertDialog<CmpRender> {
    private val mod = StateModifier()
    private var title by mutableStateOf("")
    private var text by mutableStateOf("")
    private var onDismissRequest by mutableStateOf<() -> Unit>({})

    override val icon: Widget.Children<CmpRender> = CmpChildren()
    override val confirmButton: Widget.Children<CmpRender> = CmpChildren()
    override val dismissButton: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { _ ->
        val cb = onDismissRequest
        val hasIcon = (icon as CmpChildren).widgets.isNotEmpty()
        val hasDismiss = (dismissButton as CmpChildren).widgets.isNotEmpty()
        // confirmButton is M3-required; we always pass the lambda. If the
        // guest left it empty, M3 will render an empty action area —
        // that's a guest bug surfaced visually rather than a host crash.
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { cb.invoke() },
            confirmButton = { (confirmButton as CmpChildren).render() },
            dismissButton = if (hasDismiss) {
                { (dismissButton as CmpChildren).render() }
            } else null,
            icon = if (hasIcon) {
                { (icon as CmpChildren).render() }
            } else null,
            title = if (title.isNotEmpty()) {
                { ComposeText(text = title) }
            } else null,
            text = if (text.isNotEmpty()) {
                { ComposeText(text = text) }
            } else null,
            // No parent-modifier propagation — see header comment.
        )
    }

    override fun title(title: String) { this.title = title }
    override fun text(text: String) { this.text = text }
    override fun onDismissRequest(onDismissRequest: () -> Unit) {
        this.onDismissRequest = onDismissRequest
    }
}

// ============================================================================
// Tier 3 — Pagers (IDs 140–142)
//
// Each pager child = one page. The host uses
// `(pages as CmpChildren).widgets.size` for pageCount and renders only
// the i-th child for page i (no deep pre-composition; M3's Pager
// composes the active + adjacent pages by default and discards the rest).
//
// onPageChanged uses `PagerState.settledPage` (post-fling) rather than
// `currentPage` (during-swipe) — guests typically want the "user landed
// here" semantics, not "this is closest to center right now". The first
// settled-page emission after composition is initialPage, which is a
// no-op the guest can ignore.
//
// V1: programmatic page jumps from the guest aren't supported.
// `rememberPagerState` ignores subsequent initialPage changes; adding a
// `currentPage` @Property + LaunchedEffect-driven animateScrollToPage
// is an additive follow-up.
// ============================================================================

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun PagerHostShared(
    pages: CmpChildren,
    initialPage: Int,
    onPageChanged: ((Int) -> Unit)?,
    userScrollEnabled: Boolean,
    pageSpacingDp: Int,
    composedModifier: ComposeModifier,
    horizontal: Boolean,
) {
    val pageCount = pages.widgets.size
    // Edge case: empty pages list → render nothing (M3 Pager would
    // crash with pageCount=0). The guest is responsible for ensuring
    // at least one page; we just no-op defensively.
    if (pageCount == 0) return
    val state = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage.coerceIn(0, pageCount - 1),
        pageCount = { pageCount },
    )
    // Bridge settledPage → onPageChanged. snapshotFlow re-emits whenever
    // settledPage changes; the first emission is the initial value
    // (matches initialPage), which the guest can deduplicate. We don't
    // drop(1) here because that requires the kotlinx-coroutines flow
    // operators which add a dep just for this.
    androidx.compose.runtime.LaunchedEffect(state, onPageChanged) {
        val cb = onPageChanged ?: return@LaunchedEffect
        androidx.compose.runtime.snapshotFlow { state.settledPage }.collect { cb.invoke(it) }
    }
    if (horizontal) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = state,
            userScrollEnabled = userScrollEnabled,
            pageSpacing = pageSpacingDp.dp,
            modifier = composedModifier,
        ) { pageIndex ->
            // Render the i-th child as this page's content. Defensive
            // bounds-check: if the children list shrank between
            // recompositions (pageCount memoized), bail to a no-op page.
            pages.widgets.getOrNull(pageIndex)?.value?.invoke(ComposeModifier)
        }
    } else {
        androidx.compose.foundation.pager.VerticalPager(
            state = state,
            userScrollEnabled = userScrollEnabled,
            pageSpacing = pageSpacingDp.dp,
            modifier = composedModifier,
        ) { pageIndex ->
            pages.widgets.getOrNull(pageIndex)?.value?.invoke(ComposeModifier)
        }
    }
}

class CmpHorizontalPager :
    com.example.serverdrivenui.schema.widget.HorizontalPager<CmpRender> {
    private val mod = StateModifier()
    private var initialPage by mutableStateOf(0)
    private var onPageChanged by mutableStateOf<((Int) -> Unit)?>(null)
    private var userScrollEnabled by mutableStateOf(true)
    private var pageSpacingDp by mutableStateOf(0)

    override val pages: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        PagerHostShared(
            pages = pages as CmpChildren,
            initialPage = initialPage,
            onPageChanged = onPageChanged,
            userScrollEnabled = userScrollEnabled,
            pageSpacingDp = pageSpacingDp,
            composedModifier = composed,
            horizontal = true,
        )
    }

    override fun initialPage(initialPage: Int) { this.initialPage = initialPage }
    override fun onPageChanged(onPageChanged: ((Int) -> Unit)?) {
        this.onPageChanged = onPageChanged
    }
    override fun userScrollEnabled(userScrollEnabled: Boolean) {
        this.userScrollEnabled = userScrollEnabled
    }
    override fun pageSpacingDp(pageSpacingDp: Int) {
        this.pageSpacingDp = pageSpacingDp
    }
}

class CmpVerticalPager :
    com.example.serverdrivenui.schema.widget.VerticalPager<CmpRender> {
    private val mod = StateModifier()
    private var initialPage by mutableStateOf(0)
    private var onPageChanged by mutableStateOf<((Int) -> Unit)?>(null)
    private var userScrollEnabled by mutableStateOf(true)
    private var pageSpacingDp by mutableStateOf(0)

    override val pages: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        PagerHostShared(
            pages = pages as CmpChildren,
            initialPage = initialPage,
            onPageChanged = onPageChanged,
            userScrollEnabled = userScrollEnabled,
            pageSpacingDp = pageSpacingDp,
            composedModifier = composed,
            horizontal = false,
        )
    }

    override fun initialPage(initialPage: Int) { this.initialPage = initialPage }
    override fun onPageChanged(onPageChanged: ((Int) -> Unit)?) {
        this.onPageChanged = onPageChanged
    }
    override fun userScrollEnabled(userScrollEnabled: Boolean) {
        this.userScrollEnabled = userScrollEnabled
    }
    override fun pageSpacingDp(pageSpacingDp: Int) {
        this.pageSpacingDp = pageSpacingDp
    }
}

// ============================================================================
// Tier 3 — Large-screen navigation (IDs 160–163)
//
// NavigationRail / NavigationRailItem are vertical-axis siblings of
// NavigationBar / NavigationBarItem; the M3 widgets do the work, the host
// just bridges the schema properties + slots.
//
// ModalNavigationDrawer uses a controlled-component pattern for drawer
// state: the guest holds `drawerOpen: Boolean`, the host syncs M3's
// DrawerState to it via LaunchedEffect, and reports user-driven gesture
// changes back through onDrawerStateChange via snapshotFlow on
// `isOpen`. Feedback-loop check: when the guest sets drawerOpen=true,
// LaunchedEffect calls drawerState.open(); the snapshotFlow subsequently
// fires onDrawerStateChange(true); the guest re-receives drawerOpen=true
// (no-op). No infinite loop.
//
// drawerContent is wrapped in M3's ModalDrawerSheet by the host so the
// guest emits items directly (typically NavigationDrawerItem children)
// without worrying about surface styling.
// ============================================================================

class CmpNavigationRail :
    com.example.serverdrivenui.schema.widget.NavigationRail<CmpRender> {
    private val mod = StateModifier()

    override val header: Widget.Children<CmpRender> = CmpChildren()
    override val items: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val hasHeader = (header as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.NavigationRail(
            header = if (hasHeader) {
                {
                    // M3's header lambda is ColumnScope; render() works
                    // here because compose's normal scoping gives us a
                    // composable context to emit children into.
                    (header as CmpChildren).render()
                }
            } else null,
            modifier = composed,
        ) {
            // Items render in M3's NavigationRail content ColumnScope —
            // each child is expected to be a NavigationRailItem.
            (items as CmpChildren).render()
        }
    }
}

class CmpNavigationRailItem :
    com.example.serverdrivenui.schema.widget.NavigationRailItem<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var label by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val icon: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        androidx.compose.material3.NavigationRailItem(
            selected = selected,
            onClick = { cb?.invoke() },
            icon = { (icon as CmpChildren).render() },
            label = if (label.isNotEmpty()) {
                { ComposeText(text = label) }
            } else null,
            enabled = enabled,
            modifier = composed,
        )
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun label(label: String) { this.label = label }
    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpModalNavigationDrawer :
    com.example.serverdrivenui.schema.widget.ModalNavigationDrawer<CmpRender> {
    private val mod = StateModifier()
    private var drawerOpen by mutableStateOf(false)
    private var onDrawerStateChange by mutableStateOf<((Boolean) -> Unit)?>(null)
    private var gesturesEnabled by mutableStateOf(true)

    override val drawerContent: Widget.Children<CmpRender> = CmpChildren()
    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val initial = if (drawerOpen) {
            androidx.compose.material3.DrawerValue.Open
        } else {
            androidx.compose.material3.DrawerValue.Closed
        }
        // Initial state matches drawerOpen at first composition; later
        // changes flow through the LaunchedEffects below.
        val drawerState = androidx.compose.material3.rememberDrawerState(initial)
        // Guest → host: keep drawerState in sync with the guest's Boolean.
        // open() / close() are suspend functions; we run them in the
        // LaunchedEffect's coroutine.
        androidx.compose.runtime.LaunchedEffect(drawerOpen) {
            if (drawerOpen && !drawerState.isOpen) drawerState.open()
            else if (!drawerOpen && !drawerState.isClosed) drawerState.close()
        }
        // Host → guest: report user-driven open/close (swipe, scrim tap)
        // back to the guest. snapshotFlow re-emits when isOpen flips.
        // First emission matches initial state (no-op for the guest).
        val cb = onDrawerStateChange
        if (cb != null) {
            androidx.compose.runtime.LaunchedEffect(drawerState) {
                androidx.compose.runtime.snapshotFlow { drawerState.isOpen }
                    .collect { cb.invoke(it) }
            }
        }
        androidx.compose.material3.ModalNavigationDrawer(
            drawerContent = {
                androidx.compose.material3.ModalDrawerSheet {
                    (drawerContent as CmpChildren).render()
                }
            },
            drawerState = drawerState,
            gesturesEnabled = gesturesEnabled,
            modifier = composed,
        ) {
            (content as CmpChildren).render()
        }
    }

    override fun drawerOpen(drawerOpen: Boolean) { this.drawerOpen = drawerOpen }
    override fun onDrawerStateChange(onDrawerStateChange: ((Boolean) -> Unit)?) {
        this.onDrawerStateChange = onDrawerStateChange
    }
    override fun gesturesEnabled(gesturesEnabled: Boolean) {
        this.gesturesEnabled = gesturesEnabled
    }
}

// ============================================================================
// Tier 3 — Pickers (IDs 170–171)
//
// DatePickerDialog uses M3's first-class `material3.DatePickerDialog`
// + `material3.DatePicker`. TimePickerDialog has no native M3 wrapper —
// the host hand-rolls an AlertDialog scaffold around M3's TimePicker.
//
// Visibility model — same conditional-render pattern as the Tier 3
// overlays (Batch 3.2): the guest holds a Boolean and only emits the
// picker widget when visible.
//
// OK / Cancel labels are hardcoded ("OK" / "Cancel") in v1; add
// confirmLabel / dismissLabel @Property additions later for i18n.
// Initial-state knobs (initialSelectedDateMillis = 0L for "no preset",
// initialHour / initialMinute / is24Hour for time) are passed through
// to rememberDatePickerState / rememberTimePickerState.
//
// onConfirm semantics: fires AFTER the user taps OK with a valid
// selection (DatePicker requires a date; TimePicker always has a time).
// Tap Cancel or scrim → onDismissRequest only, no onConfirm.
// ============================================================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpDatePickerDialog :
    com.example.serverdrivenui.schema.widget.DatePickerDialog<CmpRender> {
    private val mod = StateModifier()
    private var initialSelectedDateMillis by mutableStateOf(0L)
    private var onConfirm by mutableStateOf<((Long) -> Unit)?>(null)
    private var onDismissRequest by mutableStateOf<() -> Unit>({})

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { _ ->
        val dismiss = onDismissRequest
        val confirm = onConfirm
        // 0L sentinel → null (no preset). Real dates are always > 0
        // (Unix epoch is 1970, so any real selection is positive).
        val initialMillis = if (initialSelectedDateMillis == 0L) {
            null
        } else initialSelectedDateMillis
        val state = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { dismiss.invoke() },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        // selectedDateMillis is null until the user
                        // picks a date — disable OK in that case
                        // (rather than firing onConfirm with a sentinel).
                        val picked = state.selectedDateMillis
                        if (picked != null) confirm?.invoke(picked)
                    },
                    enabled = state.selectedDateMillis != null,
                ) { ComposeText("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { dismiss.invoke() },
                ) { ComposeText("Cancel") }
            },
        ) {
            androidx.compose.material3.DatePicker(state = state)
        }
    }

    override fun initialSelectedDateMillis(initialSelectedDateMillis: Long) {
        this.initialSelectedDateMillis = initialSelectedDateMillis
    }
    override fun onConfirm(onConfirm: ((Long) -> Unit)?) {
        this.onConfirm = onConfirm
    }
    override fun onDismissRequest(onDismissRequest: () -> Unit) {
        this.onDismissRequest = onDismissRequest
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpTimePickerDialog :
    com.example.serverdrivenui.schema.widget.TimePickerDialog<CmpRender> {
    private val mod = StateModifier()
    private var initialHour by mutableStateOf(0)
    private var initialMinute by mutableStateOf(0)
    private var is24Hour by mutableStateOf(true)
    private var onConfirm by mutableStateOf<((Int) -> Unit)?>(null)
    private var onDismissRequest by mutableStateOf<() -> Unit>({})

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { _ ->
        val dismiss = onDismissRequest
        val confirm = onConfirm
        val state = androidx.compose.material3.rememberTimePickerState(
            initialHour = initialHour.coerceIn(0, 23),
            initialMinute = initialMinute.coerceIn(0, 59),
            is24Hour = is24Hour,
        )
        // No first-class TimePickerDialog in M3 — wrap TimePicker in an
        // AlertDialog scaffold ourselves. Title slot left empty (M3
        // convention: time pickers don't typically have titles).
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { dismiss.invoke() },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        // Pack hour + minute into a single Int (minutes
                        // since midnight) — see Schema doc for rationale.
                        confirm?.invoke(state.hour * 60 + state.minute)
                    },
                ) { ComposeText("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { dismiss.invoke() },
                ) { ComposeText("Cancel") }
            },
            text = {
                // Center the picker in the dialog's text slot.
                androidx.compose.foundation.layout.Box(
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                    modifier = ComposeModifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.TimePicker(state = state)
                }
            },
        )
    }

    override fun initialHour(initialHour: Int) { this.initialHour = initialHour }
    override fun initialMinute(initialMinute: Int) { this.initialMinute = initialMinute }
    override fun is24Hour(is24Hour: Boolean) { this.is24Hour = is24Hour }
    override fun onConfirm(onConfirm: ((Int) -> Unit)?) {
        this.onConfirm = onConfirm
    }
    override fun onDismissRequest(onDismissRequest: () -> Unit) {
        this.onDismissRequest = onDismissRequest
    }
}

class CmpNavigationDrawerItem :
    com.example.serverdrivenui.schema.widget.NavigationDrawerItem<CmpRender> {
    private val mod = StateModifier()
    private var selected by mutableStateOf(false)
    private var label by mutableStateOf("")
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val icon: Widget.Children<CmpRender> = CmpChildren()
    override val badge: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onClick
        val hasIcon = (icon as CmpChildren).widgets.isNotEmpty()
        val hasBadge = (badge as CmpChildren).widgets.isNotEmpty()
        androidx.compose.material3.NavigationDrawerItem(
            label = { ComposeText(text = label) },
            selected = selected,
            onClick = { cb?.invoke() },
            icon = if (hasIcon) {
                { (icon as CmpChildren).render() }
            } else null,
            badge = if (hasBadge) {
                { (badge as CmpChildren).render() }
            } else null,
            modifier = composed,
        )
    }

    override fun selected(selected: Boolean) { this.selected = selected }
    override fun label(label: String) { this.label = label }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class CmpPullToRefreshBox :
    com.example.serverdrivenui.schema.widget.PullToRefreshBox<CmpRender> {
    private val mod = StateModifier()
    private var isRefreshing by mutableStateOf(false)
    private var onRefresh by mutableStateOf<() -> Unit>({})

    override val content: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        val cb = onRefresh
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { cb.invoke() },
            modifier = composed,
        ) {
            // BoxScope content — children stack inside the box (M3
            // default contentAlignment = TopStart). Typical usage is
            // a single LazyColumn child filling the box; multiple
            // children stack at TopStart unless they bring their own
            // layout-axis modifiers.
            (content as CmpChildren).render()
        }
    }

    override fun isRefreshing(isRefreshing: Boolean) {
        this.isRefreshing = isRefreshing
    }
    override fun onRefresh(onRefresh: () -> Unit) {
        this.onRefresh = onRefresh
    }
}

class CmpPagerIndicator :
    com.example.serverdrivenui.schema.widget.PagerIndicator<CmpRender> {
    private val mod = StateModifier()
    private var pageCount by mutableStateOf(0)
    private var currentPage by mutableStateOf(0)
    private var activeColor by mutableStateOf(SchemaColor.Primary)
    private var inactiveColor by mutableStateOf(SchemaColor.OutlineVariant)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming)
        // Row of dots; the active dot is slightly larger to add a
        // second visual cue beyond color (helps colorblind users).
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement
                .spacedBy(6.dp, alignment = androidx.compose.ui.Alignment.CenterHorizontally),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = composed,
        ) {
            for (i in 0 until pageCount) {
                val isActive = i == currentPage
                androidx.compose.foundation.layout.Box(
                    modifier = ComposeModifier
                        .size(if (isActive) 10.dp else 8.dp)
                        .background(
                            color = (if (isActive) activeColor else inactiveColor)
                                .toComposeColor(),
                            shape = androidx.compose.foundation.shape.CircleShape,
                        ),
                )
            }
        }
    }

    override fun pageCount(pageCount: Int) { this.pageCount = pageCount }
    override fun currentPage(currentPage: Int) { this.currentPage = currentPage }
    override fun activeColor(activeColor: SchemaColor) { this.activeColor = activeColor }
    override fun inactiveColor(inactiveColor: SchemaColor) { this.inactiveColor = inactiveColor }
}

// ============================================================================
// Caliclan navigation primitives
// ============================================================================

class CmpScreenStack : ScreenStack<CmpRender> {
    private val mod = StateModifier()
    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { incoming ->
        val composed = modifier.applyToCompose(incoming).fillMaxSize()
        androidx.compose.foundation.layout.Box(modifier = composed) {
            (children as CmpChildren).render()
        }
    }
}

class CmpBackHandler : BackHandler<CmpRender> {
    private val mod = StateModifier()
    private var enabled by mutableStateOf(false)
    private var onBack by mutableStateOf<(() -> Unit)?>(null)

    override var modifier: KonduitModifier
        get() = mod.value
        set(v) { mod.value = v }

    override val value: CmpRender = { _ ->
        if (enabled && onBack != null) {
            androidx.compose.ui.backhandler.BackHandler(enabled = true) {
                onBack?.invoke()
            }
        }
    }

    override fun enabled(enabled: Boolean) { this.enabled = enabled }
    override fun onBack(onBack: () -> Unit) { this.onBack = onBack }
}

// ============================================================================
// Children container (reused by every widget with @Children)
// ============================================================================

class CmpChildren : Widget.Children<CmpRender> {
    private val _widgets = mutableStateListOf<Widget<CmpRender>>()

    override val widgets: List<Widget<CmpRender>> get() = _widgets

    override fun insert(index: Int, widget: Widget<CmpRender>) {
        _widgets.add(index, widget)
    }
    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
        for (i in 0 until count) {
            val element = _widgets.removeAt(fromIndex)
            val dest = if (fromIndex < toIndex) toIndex - 1 else toIndex
            _widgets.add(dest, element)
        }
    }
    override fun remove(index: Int, count: Int) {
        _widgets.removeRange(index, index + count)
    }
    override fun onModifierUpdated(index: Int, widget: Widget<CmpRender>) {}
    override fun detach() {}

    @Composable
    fun render() {
        _widgets.forEach { widget ->
            widget.value(ComposeModifier)
        }
    }

    /** Renders each child as a separate item() in a LazyColumn/Row scope. */
    fun renderInLazyScope(scope: LazyListScope) {
        _widgets.forEach { widget ->
            scope.item {
                widget.value(ComposeModifier)
            }
        }
    }

    /**
     * Render inside a compose RowScope. Extracts Weight from each child's
     * modifier chain and applies it via RowScope.weight() before delegating.
     */
    @Composable
    fun renderInRow(scope: androidx.compose.foundation.layout.RowScope) {
        _widgets.forEach { widget ->
            val weight = widget.modifier.findWeight()
            val base = if (weight != null) {
                with(scope) { ComposeModifier.weight(weight) }
            } else {
                ComposeModifier
            }
            widget.value(base)
        }
    }

    /** Same as [renderInRow] but for ColumnScope. */
    @Composable
    fun renderInColumn(scope: androidx.compose.foundation.layout.ColumnScope) {
        _widgets.forEach { widget ->
            val weight = widget.modifier.findWeight()
            val base = if (weight != null) {
                with(scope) { ComposeModifier.weight(weight) }
            } else {
                ComposeModifier
            }
            widget.value(base)
        }
    }
}

// ============================================================================
// Widget factory
//
// Modifier callbacks are no-ops: the modifier chain reaches each widget
// via `widget.modifier =` in the runtime's UiModifierChange handler. The
// state-backed `modifier` field then drives recomposition. The factory
// callbacks exist purely to satisfy the generated interface contract.
// ============================================================================

object CmpWidgetFactory : SduiSchemaWidgetFactory<CmpRender> {
    override fun Box() = CmpBox()
    override fun Column() = CmpColumn()
    override fun Row() = CmpRow()
    override fun Spacer() = CmpSpacer()
    override fun LazyColumn() = CmpLazyColumn()
    override fun LazyRow() = CmpLazyRow()
    override fun LazyItem() = CmpLazyItem()
    override fun Text() = CmpText()
    override fun AsyncImage() = CmpAsyncImage()
    override fun Icon() = CmpIcon()
    override fun Button() = CmpButton()
    override fun OutlinedButton() = CmpOutlinedButton()
    override fun TextButton() = CmpTextButton()
    override fun FilledTonalButton() = CmpFilledTonalButton()
    override fun ElevatedButton() = CmpElevatedButton()
    override fun IconButton() = CmpIconButton()
    override fun FloatingActionButton() = CmpFloatingActionButton()
    override fun ExtendedFloatingActionButton() = CmpExtendedFloatingActionButton()
    override fun TextField() = CmpTextField()
    override fun OutlinedTextField() = CmpOutlinedTextField()
    override fun SearchBar() = CmpSearchBar()
    override fun Checkbox() = CmpCheckbox()
    override fun RadioButton() = CmpRadioButton()
    override fun Switch() = CmpSwitch()
    override fun Slider() = CmpSlider()
    override fun RangeSlider() = CmpRangeSlider()
    override fun SegmentedButtonRow() = CmpSegmentedButtonRow()
    override fun Card() = CmpCard()
    override fun ElevatedCard() = CmpElevatedCard()
    override fun OutlinedCard() = CmpOutlinedCard()
    override fun Surface() = CmpSurface()
    override fun LinearProgressIndicator() = CmpLinearProgressIndicator()
    override fun CircularProgressIndicator() = CmpCircularProgressIndicator()
    override fun Badge() = CmpBadge()
    override fun Snackbar() = CmpSnackbar()
    override fun Scaffold() = CmpScaffold()
    override fun TopAppBar() = CmpTopAppBar()
    override fun LargeTopAppBar() = CmpLargeTopAppBar()
    override fun MediumTopAppBar() = CmpMediumTopAppBar()
    override fun NavigationBar() = CmpNavigationBar()
    override fun NavigationBarItem() = CmpNavigationBarItem()
    override fun TabRow() = CmpTabRow()
    override fun Tab() = CmpTab()
    override fun HorizontalDivider() = CmpHorizontalDivider()
    override fun VerticalDivider() = CmpVerticalDivider()
    override fun FilterChip() = CmpFilterChip()
    override fun AssistChip() = CmpAssistChip()
    override fun InputChip() = CmpInputChip()
    override fun SuggestionChip() = CmpSuggestionChip()
    override fun ListItem() = CmpListItem()
    override fun DropdownMenu() = CmpDropdownMenu()
    override fun DropdownMenuItem() = CmpDropdownMenuItem()
    override fun ModalBottomSheet() = CmpModalBottomSheet()
    override fun AlertDialog() = CmpAlertDialog()
    override fun HorizontalPager() = CmpHorizontalPager()
    override fun VerticalPager() = CmpVerticalPager()
    override fun PagerIndicator() = CmpPagerIndicator()
    override fun PullToRefreshBox() = CmpPullToRefreshBox()
    override fun NavigationRail() = CmpNavigationRail()
    override fun NavigationRailItem() = CmpNavigationRailItem()
    override fun ModalNavigationDrawer() = CmpModalNavigationDrawer()
    override fun NavigationDrawerItem() = CmpNavigationDrawerItem()
    override fun DatePickerDialog() = CmpDatePickerDialog()
    override fun TimePickerDialog() = CmpTimePickerDialog()
    override fun ScreenStack() = CmpScreenStack()
    override fun BackHandler() = CmpBackHandler()

    override fun Padding(value: CmpRender, modifier: MPadding) {}
    override fun Size(value: CmpRender, modifier: MSize) {}
    override fun Width(value: CmpRender, modifier: MWidth) {}
    override fun Height(value: CmpRender, modifier: MHeight) {}
    override fun Background(value: CmpRender, modifier: MBackground) {}
    override fun Weight(value: CmpRender, modifier: MWeight) {}
    override fun FillMaxWidth(value: CmpRender, modifier: MFillMaxWidth) {}
    override fun FillMaxHeight(value: CmpRender, modifier: MFillMaxHeight) {}
    override fun FillMaxSize(value: CmpRender, modifier: MFillMaxSize) {}
    override fun Alpha(value: CmpRender, modifier: MAlpha) {}
    // Tier 3 modifier additions (tags 12–17). Same no-op pattern as
    // every other modifier callback — actual application happens via
    // applyToCompose reading from the widget's modifier chain.
    override fun Border(value: CmpRender, modifier: MBorder) {}
    override fun Clip(value: CmpRender, modifier: MClip) {}
    override fun ClipCircle(value: CmpRender, modifier: MClipCircle) {}
    override fun WrapContentWidth(value: CmpRender, modifier: MWrapContentWidth) {}
    override fun WrapContentHeight(value: CmpRender, modifier: MWrapContentHeight) {}
    override fun AspectRatio(value: CmpRender, modifier: MAspectRatio) {}
}

// ============================================================================
// Services
// ============================================================================

// Deleted: `RealHostConsole` (commonMain HostConsole impl). Both platforms
// ship their own console impls — Android: AndroidRealHostConsole (logs via
// android.util.Log so messages land in logcat); iOS: IosRealHostConsole
// (logs via println which goes to stderr, captured by Xcode console). A
// single commonMain impl can't reach either platform's preferred log sink,
// so consolidation isn't a win here.

/**
 * Single shared SnackbarHostState that the host's root UI hooks into via
 * `SnackbarHost(SnackbarHub.state)` (see App.kt) and that the
 * [RealHostSnackbar] Zipline service writes into.
 *
 * Singleton because:
 *   1. The host's UI tree may recompose, but the snackbar queue must
 *      survive across recompositions.
 *   2. The Zipline service binding runs in `bindServices` BEFORE the
 *      Compose UI mounts; we need a stable reference to hand the queue
 *      to the service. A singleton sidesteps the lifecycle ordering
 *      problem entirely.
 *
 * Trade-off: only one root UI can host snackbars at a time. For multi-
 * window apps (desktop) we'd need a per-window state holder routed
 * through composition locals; not relevant for Caliclan today.
 */
object SnackbarHub {
    val state: androidx.compose.material3.SnackbarHostState =
        androidx.compose.material3.SnackbarHostState()
}

/**
 * Maps Zipline guest calls to M3 SnackbarHostState.showSnackbar. Owns
 * an internal coroutine scope so the suspending showSnackbar call runs
 * off the Zipline thread.
 *
 * SnackbarHostState.showSnackbar SUSPENDS until the snackbar is
 * dismissed (by next show, by timeout, or by action press). Each call
 * to RealHostSnackbar.show() spawns a coroutine; M3 internally
 * serializes them via a Mutex so calls naturally queue FIFO. No extra
 * queue state needed on our side.
 */
/**
 * @param ziplineDispatcher Zipline's thread-confined dispatcher
 *   ([dev.konduit.treehouse.TreehouseDispatchers.zipline]). REQUIRED at
 *   construction so the type system makes the wiring impossible to forget.
 *
 *   Why it must be passed in (gotcha #12 in docs/HANDOVER.md): Zipline's
 *   QuickJS instance is single-thread confined. Any outbound call to a
 *   guest ZiplineService — including `callback.onResult(...)` and
 *   `callback.close()` — MUST be issued from this dispatcher. Calling from
 *   any other thread (e.g. Dispatchers.Main, which our scope uses to drive
 *   the M3 Snackbar UI) crashes with `QuickJsException: stack overflow` on
 *   iOS Kotlin/Native. JVM-backed Zipline silently tolerates the wrong
 *   thread by luck. See cashapp/zipline#1429 + #1592.
 *
 *   Construct + bind from inside [TreehouseApp.Spec.bindServices], which
 *   is the first lifecycle hook that has access to `treehouseApp.dispatchers`.
 *   Hold the resulting instance as a `lateinit var` on the Spec to keep
 *   it alive (Zipline holds only a weak-ish ref; without a strong host-side
 *   ref the service GCs and emits `serviceLeaked`).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class RealHostSnackbar(
    private val ziplineDispatcher: kotlinx.coroutines.CoroutineDispatcher,
) : com.example.serverdrivenui.shared.HostSnackbar {
    // Lazy scope — defer Dispatchers.Main resolution until first show()
    // call, NOT at construction. On iOS Kotlin/Native, accessing
    // Dispatchers.Main during bindServices() (which runs before the
    // Compose UI mounts and may run before the main coroutine
    // dispatcher is fully wired) can throw IllegalStateException.
    // SupervisorJob keeps the scope alive across individual showSnackbar
    // coroutine completions; failures in one snackbar don't tear down
    // the whole queue.
    private val scope by lazy {
        kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main +
                kotlinx.coroutines.SupervisorJob(),
        )
    }

    override fun show(message: String, actionLabel: String?, durationMillis: Long) {
        // Fire-and-forget variant: result is discarded. Defensive
        // try/catch — a throw out of a Zipline-bound method on the host
        // side can propagate to the guest in confusing ways (silent UI
        // tear-down on iOS, observed empirically). Better to swallow +
        // log here and let subsequent calls keep working.
        try {
            scope.launch {
                SnackbarHub.state.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = mapDuration(durationMillis),
                )
            }
        } catch (t: Throwable) {
            println("RealHostSnackbar.show($message) failed: ${t.message}")
        }
    }

    override fun showWithResult(
        message: String,
        actionLabel: String?,
        durationMillis: Long,
        callback: com.example.serverdrivenui.shared.SnackbarResultCallback,
    ) {
        try {
            scope.launch {
                // Show the snackbar on Dispatchers.Main (current coroutine
                // context). M3 SnackbarHostState.showSnackbar is a suspend
                // that lives on the UI thread.
                val actionPerformed = try {
                    val result = SnackbarHub.state.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        duration = mapDuration(durationMillis),
                    )
                    result == androidx.compose.material3.SnackbarResult.ActionPerformed
                } catch (t: Throwable) {
                    println("RealHostSnackbar.showWithResult($message) showSnackbar threw: ${t.message}")
                    false
                }

                // Both callback.onResult AND callback.close are outbound
                // Zipline calls and MUST be issued from the zipline-confined
                // dispatcher (gotcha #12). If we forget to hop, iOS K/N
                // crashes with QuickJsException: stack overflow; JVM tolerates
                // it by luck. Group both ZiplineService touches inside a
                // single withContext to amortize the dispatch hop.
                kotlinx.coroutines.withContext(ziplineDispatcher) {
                    invokeAndClose(message, callback, actionPerformed)
                }
            }
        } catch (t: Throwable) {
            println("RealHostSnackbar.showWithResult($message) failed: ${t.message}")
            // We couldn't even launch — best-effort close on the calling
            // thread; if that crashes, so be it.
            try { callback.close() } catch (_: Throwable) {}
        }
    }

    /**
     * Single-pass invoke + close, intended to run on the zipline dispatcher.
     * Splits out so both the "happy path" and the "no dispatcher wired"
     * fallback share the same try/catch + close shape.
     */
    private fun invokeAndClose(
        message: String,
        callback: com.example.serverdrivenui.shared.SnackbarResultCallback,
        actionPerformed: Boolean,
    ) {
        try {
            callback.onResult(actionPerformed)
        } catch (t: Throwable) {
            println("RealHostSnackbar.showWithResult($message) onResult callback threw: ${t.message}")
        } finally {
            // Always close the proxy exactly once — otherwise it leaks the
            // Zipline service binding and we get spurious `serviceLeaked`
            // warnings.
            try { callback.close() } catch (_: Throwable) { /* already gone */ }
        }
    }

    /**
     * durationMillis → M3 SnackbarDuration. Same buckets the original
     * show() used; extracted so both methods agree exactly.
     */
    private fun mapDuration(durationMillis: Long): androidx.compose.material3.SnackbarDuration =
        when {
            durationMillis <= 0L -> androidx.compose.material3.SnackbarDuration.Indefinite
            durationMillis <= 6000L -> androidx.compose.material3.SnackbarDuration.Short
            else -> androidx.compose.material3.SnackbarDuration.Long
        }
}

// NOTE: there used to be an `SduiAppSpec` class here that bound HostConsole
// + HostSnackbar — but it was never referenced from either platform entry
// point. Both Android (MainActivity.kt) and iOS (MainViewController.kt)
// instantiate their own anonymous `TreehouseApp.Spec<SduiAppService>()`,
// each binding services platform-locally (Android: AndroidRealHostConsole;
// iOS: IosRealHostConsole; both: RealHostSnackbar from below). The dead
// class was deleted in the snackbar bind-site fix; if you re-add a shared
// Spec helper later, ensure both platforms switch to it in the same diff.
