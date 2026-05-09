@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.serverdrivenui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn as ComposeLazyColumn
import androidx.compose.foundation.lazy.LazyRow as ComposeLazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon as ComposeIcon
import androidx.compose.material3.Text as ComposeText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier as ComposeModifier
import androidx.compose.ui.draw.alpha
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
import com.example.serverdrivenui.schema.modifier.Background as MBackground
import com.example.serverdrivenui.schema.modifier.FillMaxHeight as MFillMaxHeight
import com.example.serverdrivenui.schema.modifier.FillMaxSize as MFillMaxSize
import com.example.serverdrivenui.schema.modifier.FillMaxWidth as MFillMaxWidth
import com.example.serverdrivenui.schema.modifier.Height as MHeight
import com.example.serverdrivenui.schema.modifier.Padding as MPadding
import com.example.serverdrivenui.schema.modifier.Size as MSize
import com.example.serverdrivenui.schema.modifier.Weight as MWeight
import com.example.serverdrivenui.schema.modifier.Width as MWidth
import kotlinx.coroutines.flow.Flow

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
    SchemaIconName.ArrowBack -> Icons.Filled.ArrowBack
    SchemaIconName.ArrowForward -> Icons.Filled.ArrowForward
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
            is MBackground -> bgSchemaColor = el.color
            is MAlpha -> m = m.alpha(el.value.toFloat())
            is MWeight -> { /* applied by parent Row/Column */ }
        }
    }
    val bg = bgSchemaColor
    if (bg != null) m = m.background(bg.toComposeColor())
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
}

// ============================================================================
// Services
// ============================================================================

class RealHostConsole : HostConsole {
    override fun log(message: String) {
        println("JS: $message")
    }
}

class SduiAppSpec(
    override val manifestUrl: Flow<String>,
    override val name: String = "sdui",
) : TreehouseApp.Spec<SduiAppService>() {
    override val serializersModule = com.example.serverdrivenui.schema.SduiSerializersModule

    override suspend fun bindServices(treehouseApp: TreehouseApp<SduiAppService>, zipline: Zipline) {
        zipline.bind<HostConsole>("console", RealHostConsole())
    }

    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}
