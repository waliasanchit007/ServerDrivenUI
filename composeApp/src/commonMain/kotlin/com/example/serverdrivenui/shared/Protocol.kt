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
    override suspend fun bindServices(treehouseApp: TreehouseApp<SduiAppService>, zipline: Zipline) {
        zipline.bind<HostConsole>("console", RealHostConsole())
    }

    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}
