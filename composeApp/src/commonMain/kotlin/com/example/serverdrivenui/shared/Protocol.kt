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
import kotlinx.coroutines.flow.Flow

private typealias CmpRender = @Composable (androidx.compose.ui.Modifier) -> Unit

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
// Tier 1 — host Cmp* implementations
// ============================================================================

class CmpBox : Box<CmpRender> {
    private var padding by mutableStateOf(0)
    private var background by mutableStateOf(SchemaColor.Transparent)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        val bg = background.toComposeColor()
        val click = onClick
        val composed = mod
            .let { if (click != null) it.clickable { click() } else it }
            .let { if (background != SchemaColor.Transparent) it.background(bg) else it }
            .let { if (padding > 0) it.padding(padding.dp) else it }
        androidx.compose.foundation.layout.Box(modifier = composed) {
            (children as CmpChildren).render()
        }
    }

    override fun padding(padding: Int) { this.padding = padding }
    override fun background(background: SchemaColor) { this.background = background }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

class CmpColumn : com.example.serverdrivenui.schema.widget.Column<CmpRender> {
    private var padding by mutableStateOf(0)
    private var background by mutableStateOf(SchemaColor.Transparent)
    private var verticalArrangement by mutableStateOf(SchemaArrangement.Start)
    private var horizontalAlignment by mutableStateOf(SchemaHorizontalAlignment.Start)
    private var fillMaxSize by mutableStateOf(false)

    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        val bg = background.toComposeColor()
        val composed = mod
            .let { if (fillMaxSize) it.fillMaxSize() else it }
            .let { if (background != SchemaColor.Transparent) it.background(bg) else it }
            .let { if (padding > 0) it.padding(padding.dp) else it }
        androidx.compose.foundation.layout.Column(
            modifier = composed,
            verticalArrangement = verticalArrangement.toVertical(),
            horizontalAlignment = horizontalAlignment.toAlignment(),
        ) {
            (children as CmpChildren).render()
        }
    }

    override fun padding(padding: Int) { this.padding = padding }
    override fun background(background: SchemaColor) { this.background = background }
    override fun verticalArrangement(verticalArrangement: SchemaArrangement) {
        this.verticalArrangement = verticalArrangement
    }
    override fun horizontalAlignment(horizontalAlignment: SchemaHorizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment
    }
    override fun fillMaxSize(fillMaxSize: Boolean) { this.fillMaxSize = fillMaxSize }
}

class CmpRow : com.example.serverdrivenui.schema.widget.Row<CmpRender> {
    private var padding by mutableStateOf(0)
    private var background by mutableStateOf(SchemaColor.Transparent)
    private var horizontalArrangement by mutableStateOf(SchemaArrangement.Start)
    private var verticalAlignment by mutableStateOf(SchemaVerticalAlignment.Top)
    private var fillMaxWidth by mutableStateOf(false)

    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        val bg = background.toComposeColor()
        val composed = mod
            .let { if (fillMaxWidth) it.fillMaxWidth() else it }
            .let { if (background != SchemaColor.Transparent) it.background(bg) else it }
            .let { if (padding > 0) it.padding(padding.dp) else it }
        androidx.compose.foundation.layout.Row(
            modifier = composed,
            horizontalArrangement = horizontalArrangement.toHorizontal(),
            verticalAlignment = verticalAlignment.toAlignment(),
        ) {
            (children as CmpChildren).render()
        }
    }

    override fun padding(padding: Int) { this.padding = padding }
    override fun background(background: SchemaColor) { this.background = background }
    override fun horizontalArrangement(horizontalArrangement: SchemaArrangement) {
        this.horizontalArrangement = horizontalArrangement
    }
    override fun verticalAlignment(verticalAlignment: SchemaVerticalAlignment) {
        this.verticalAlignment = verticalAlignment
    }
    override fun fillMaxWidth(fillMaxWidth: Boolean) { this.fillMaxWidth = fillMaxWidth }
}

class CmpSpacer : com.example.serverdrivenui.schema.widget.Spacer<CmpRender> {
    private var width by mutableStateOf(0)
    private var height by mutableStateOf(0)

    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        var m = mod
        if (width > 0) m = m.width(width.dp)
        if (height > 0) m = m.height(height.dp)
        androidx.compose.foundation.layout.Spacer(modifier = m)
    }

    override fun width(width: Int) { this.width = width }
    override fun height(height: Int) { this.height = height }
}

class CmpLazyColumn : LazyColumn<CmpRender> {
    private var padding by mutableStateOf(0)
    private var fillMaxSize by mutableStateOf(false)

    override val items: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        val composed = mod
            .let { if (fillMaxSize) it.fillMaxSize() else it }
            .let { if (padding > 0) it.padding(padding.dp) else it }
        ComposeLazyColumn(modifier = composed) {
            (items as CmpChildren).renderInLazyScope(this)
        }
    }

    override fun padding(padding: Int) { this.padding = padding }
    override fun fillMaxSize(fillMaxSize: Boolean) { this.fillMaxSize = fillMaxSize }
}

class CmpLazyRow : LazyRow<CmpRender> {
    private var padding by mutableStateOf(0)
    private var fillMaxWidth by mutableStateOf(false)

    override val items: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        val composed = mod
            .let { if (fillMaxWidth) it.fillMaxWidth() else it }
            .let { if (padding > 0) it.padding(padding.dp) else it }
        ComposeLazyRow(modifier = composed) {
            (items as CmpChildren).renderInLazyScope(this)
        }
    }

    override fun padding(padding: Int) { this.padding = padding }
    override fun fillMaxWidth(fillMaxWidth: Boolean) { this.fillMaxWidth = fillMaxWidth }
}

class CmpLazyItem : LazyItem<CmpRender> {
    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        // Inside a LazyColumn/Row scope, LazyItem children render directly; the
        // lazy parent wraps each child in its own item slot via renderInLazyScope.
        androidx.compose.foundation.layout.Box(modifier = mod) {
            (children as CmpChildren).render()
        }
    }
}

class CmpText : com.example.serverdrivenui.schema.widget.Text<CmpRender> {
    private var text by mutableStateOf("")
    private var color by mutableStateOf(SchemaColor.OnSurface)
    private var style by mutableStateOf(SchemaTextStyle.BodyMedium)

    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        ComposeText(
            text = text,
            color = color.toComposeColor(),
            style = style.toTextStyle(),
            modifier = mod,
        )
    }

    override fun text(text: String) { this.text = text }
    override fun color(color: SchemaColor) { this.color = color }
    override fun style(style: SchemaTextStyle) { this.style = style }
}

class CmpAsyncImage : AsyncImage<CmpRender> {
    private var url by mutableStateOf("")
    private var contentDescription by mutableStateOf("")
    private var width by mutableStateOf(0)
    private var height by mutableStateOf(0)

    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        if (url.isNotEmpty()) {
            var m = mod
            m = if (width > 0) m.width(width.dp) else m.fillMaxWidth()
            if (height > 0) m = m.height(height.dp)
            CoilAsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = m,
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
    override fun width(width: Int) { this.width = width }
    override fun height(height: Int) { this.height = height }
}

class CmpIcon : com.example.serverdrivenui.schema.widget.Icon<CmpRender> {
    private var name by mutableStateOf(SchemaIconName.Star)
    private var tint by mutableStateOf(SchemaColor.OnSurface)
    private var size by mutableStateOf(0)

    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        val m = if (size > 0) mod.size(size.dp) else mod
        ComposeIcon(
            imageVector = name.toImageVector(),
            contentDescription = name.name,
            tint = tint.toComposeColor(),
            modifier = m,
        )
    }

    override fun name(name: SchemaIconName) { this.name = name }
    override fun tint(tint: SchemaColor) { this.tint = tint }
    override fun size(size: Int) { this.size = size }
}

// ============================================================================
// Caliclan navigation primitives
// ============================================================================

class CmpScreenStack : ScreenStack<CmpRender> {
    override val children: Widget.Children<CmpRender> = CmpChildren()
    override var modifier: KonduitModifier = KonduitModifier

    override val value: CmpRender = { mod ->
        androidx.compose.foundation.layout.Box(modifier = mod.fillMaxSize()) {
            (children as CmpChildren).render()
        }
    }
}

class CmpBackHandler : BackHandler<CmpRender> {
    private var enabled by mutableStateOf(false)
    private var onBack by mutableStateOf<(() -> Unit)?>(null)

    override var modifier: KonduitModifier = KonduitModifier

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
            widget.value(androidx.compose.ui.Modifier)
        }
    }

    /** Renders each child as a separate item() in a LazyColumn/Row scope. */
    fun renderInLazyScope(scope: LazyListScope) {
        _widgets.forEach { widget ->
            scope.item {
                widget.value(androidx.compose.ui.Modifier)
            }
        }
    }
}

// ============================================================================
// Widget factory
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
