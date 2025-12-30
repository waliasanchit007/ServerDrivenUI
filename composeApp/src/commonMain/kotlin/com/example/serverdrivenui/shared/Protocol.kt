@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.serverdrivenui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import app.cash.redwood.Modifier
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.AppLifecycle
import app.cash.redwood.widget.Widget
import app.cash.redwood.protocol.RedwoodVersion
import app.cash.zipline.Zipline
import com.example.serverdrivenui.schema.widget.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import coil3.compose.AsyncImage

// ============= Existing Widgets =============

class CmpMyText : MyText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")

    init {
        println("CmpMyText: Widget created")
    }

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        println("CmpMyText: Rendering with text='$text'")
        Text(text = text, modifier = modifier)
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) {
        println("CmpMyText: text() called with '$text'")
        this.text = text
    }
}

class CmpMyButton : MyButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")
    private var onClick by mutableStateOf({})

    init {
        println("CmpMyButton: Widget created")
    }

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        println("CmpMyButton: Rendering with text='$text'")
        Button(onClick = onClick, modifier = modifier) {
            Text(text = text)
        }
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) {
        println("CmpMyButton: text() called with '$text'")
        this.text = text
    }

    override fun onClick(onClick: () -> Unit) {
        println("CmpMyButton: onClick() handler set")
        this.onClick = onClick
    }
}

class CmpMyColumn : MyColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier
}

// ============= Layout Widgets =============

class CmpFlexRow : FlexRow<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var horizontalArrangement by mutableStateOf("Start")
    private var verticalAlignment by mutableStateOf("Top")
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Row(
            modifier = modifier,
            horizontalArrangement = parseHorizontalArrangement(horizontalArrangement),
            verticalAlignment = parseVerticalAlignment(verticalAlignment)
        ) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier

    override fun horizontalArrangement(horizontalArrangement: String) {
        this.horizontalArrangement = horizontalArrangement
    }

    override fun verticalAlignment(verticalAlignment: String) {
        this.verticalAlignment = verticalAlignment
    }
}

class CmpFlexColumn : FlexColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var verticalArrangement by mutableStateOf("Top")
    private var horizontalAlignment by mutableStateOf("Start")
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier,
            verticalArrangement = parseVerticalArrangement2(verticalArrangement),
            horizontalAlignment = parseHorizontalAlignment(horizontalAlignment)
        ) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier

    override fun verticalArrangement(verticalArrangement: String) {
        this.verticalArrangement = verticalArrangement
    }

    override fun horizontalAlignment(horizontalAlignment: String) {
        this.horizontalAlignment = horizontalAlignment
    }
}

class CmpBox : Box<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.layout.Box(modifier = modifier) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier
}

class CmpSpacer : Spacer<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var width by mutableStateOf(0)
    private var height by mutableStateOf(0)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.layout.Spacer(
            modifier = modifier
                .width(width.dp)
                .height(height.dp)
        )
    }

    override var modifier: Modifier = Modifier

    override fun width(width: Int) {
        this.width = width
    }

    override fun height(height: Int) {
        this.height = height
    }
}

// ============= Input Widgets =============

class CmpSduiTextField : SduiTextField<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var textValue by mutableStateOf("")
    private var labelText by mutableStateOf("")
    private var placeholderText by mutableStateOf("")
    private var onValueChangeCallback: (String) -> Unit = {}

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                onValueChangeCallback(newValue)
            },
            label = if (labelText.isNotEmpty()) {{ Text(labelText) }} else null,
            placeholder = if (placeholderText.isNotEmpty()) {{ Text(placeholderText) }} else null,
            modifier = modifier.fillMaxWidth()
        )
    }

    override var modifier: Modifier = Modifier

    override fun value(value: String) {
        this.textValue = value
    }

    override fun label(label: String) {
        this.labelText = label
    }

    override fun placeholder(placeholder: String) {
        this.placeholderText = placeholder
    }

    override fun onValueChange(onValueChange: (String) -> Unit) {
        this.onValueChangeCallback = onValueChange
    }
}

class CmpSduiSwitch : SduiSwitch<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var checked by mutableStateOf(false)
    private var onCheckedChange: (Boolean) -> Unit = {}

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                onCheckedChange(newValue)
            },
            modifier = modifier
        )
    }

    override var modifier: Modifier = Modifier

    override fun checked(checked: Boolean) {
        this.checked = checked
    }

    override fun onCheckedChange(onCheckedChange: (Boolean) -> Unit) {
        this.onCheckedChange = onCheckedChange
    }
}

// ============= Display Widgets =============

class CmpSduiImage : SduiImage<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var url by mutableStateOf("")
    private var contentDescription by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        if (url.isNotEmpty()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
    }

    override var modifier: Modifier = Modifier

    override fun url(url: String) {
        this.url = url
    }

    override fun contentDescription(contentDescription: String) {
        this.contentDescription = contentDescription
    }
}

class CmpSduiCard : SduiCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var onClick: (() -> Unit)? = null
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val cardModifier = onClick?.let { 
            modifier.clickable { it() } 
        } ?: modifier
        
        Card(modifier = cardModifier) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier

    override fun onClick(onClick: (() -> Unit)?) {
        this.onClick = onClick
    }
}

// ============= Helper Functions =============

private fun parseHorizontalArrangement(value: String): Arrangement.Horizontal = when (value) {
    "Start" -> Arrangement.Start
    "Center" -> Arrangement.Center
    "End" -> Arrangement.End
    "SpaceBetween" -> Arrangement.SpaceBetween
    "SpaceAround" -> Arrangement.SpaceAround
    "SpaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.Start
}

private fun parseVerticalAlignment(value: String): Alignment.Vertical = when (value) {
    "Top" -> Alignment.Top
    "CenterVertically" -> Alignment.CenterVertically
    "Bottom" -> Alignment.Bottom
    else -> Alignment.Top
}

private fun parseVerticalArrangement2(value: String): Arrangement.Vertical = when (value) {
    "Top" -> Arrangement.Top
    "Center" -> Arrangement.Center
    "Bottom" -> Arrangement.Bottom
    "SpaceBetween" -> Arrangement.SpaceBetween
    "SpaceAround" -> Arrangement.SpaceAround
    "SpaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.Top
}

private fun parseHorizontalAlignment(value: String): Alignment.Horizontal = when (value) {
    "Start" -> Alignment.Start
    "CenterHorizontally" -> Alignment.CenterHorizontally
    "End" -> Alignment.End
    else -> Alignment.Start
}

// ============= Children Container =============

class CmpChildren : Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private val _widgets = mutableStateListOf<Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>>()

    override val widgets: List<Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>>
        get() = _widgets

    override fun insert(index: Int, widget: Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>) {
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

    override fun onModifierUpdated(index: Int, widget: Widget<@Composable (androidx.compose.ui.Modifier) -> Unit>) {
    }

    override fun detach() {
    }

    @Composable
    fun render() {
        _widgets.forEach { widget ->
            widget.value(androidx.compose.ui.Modifier)
        }
    }
}

// ============= Navigation Widgets =============

/**
 * ScreenStack widget - renders screen content.
 * In a more advanced implementation, this could use AnimatedContent for transitions.
 */
class CmpScreenStack : ScreenStack<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.layout.Box(
            modifier = modifier.fillMaxSize()
        ) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier
}

/**
 * BackHandler widget - intercepts back press events.
 * Uses Compose Multiplatform's BackHandler which works on both Android and iOS.
 */
class CmpBackHandler : BackHandler<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var enabled by mutableStateOf(false)
    private var onBack by mutableStateOf<(() -> Unit)?>(null)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { _ ->
        // Use CMP's BackHandler - works on both Android and iOS
        if (enabled && onBack != null) {
            androidx.compose.ui.backhandler.BackHandler(enabled = true) {
                println("CmpBackHandler: Back event received, invoking callback")
                onBack?.invoke()
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun enabled(enabled: Boolean) {
        println("CmpBackHandler: enabled set to $enabled")
        this.enabled = enabled
    }

    override fun onBack(onBack: () -> Unit) {
        println("CmpBackHandler: onBack handler registered")
        this.onBack = onBack
    }
}

// ============= Widget Factory =============

object CmpWidgetFactory : SduiSchemaWidgetFactory<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override fun MyText(): MyText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating MyText widget")
        return CmpMyText()
    }
    override fun MyButton(): MyButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating MyButton widget")
        return CmpMyButton()
    }
    override fun MyColumn(): MyColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating MyColumn widget")
        return CmpMyColumn()
    }
    override fun FlexRow(): FlexRow<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating FlexRow widget")
        return CmpFlexRow()
    }
    override fun FlexColumn(): FlexColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating FlexColumn widget")
        return CmpFlexColumn()
    }
    override fun Box(): Box<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating Box widget")
        return CmpBox()
    }
    override fun Spacer(): Spacer<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating Spacer widget")
        return CmpSpacer()
    }
    override fun SduiTextField(): SduiTextField<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating SduiTextField widget")
        return CmpSduiTextField()
    }
    override fun SduiSwitch(): SduiSwitch<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating SduiSwitch widget")
        return CmpSduiSwitch()
    }
    override fun SduiImage(): SduiImage<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating SduiImage widget")
        return CmpSduiImage()
    }
    override fun SduiCard(): SduiCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating SduiCard widget")
        return CmpSduiCard()
    }
    override fun ScreenStack(): ScreenStack<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating ScreenStack widget")
        return CmpScreenStack()
    }
    override fun BackHandler(): BackHandler<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        println("CmpWidgetFactory: Creating BackHandler widget")
        return CmpBackHandler()
    }
}

// ============= Services =============

class RealHostConsole : HostConsole {
    init {
        println("HOST: RealHostConsole initialized")
    }
    override fun log(message: String) {
        println("JS: $message")
    }
}

class SduiAppSpec(
    override val manifestUrl: Flow<String>,
    override val name: String = "sdui",
) : TreehouseApp.Spec<SduiAppService>() {
    override suspend fun bindServices(treehouseApp: TreehouseApp<SduiAppService>, zipline: Zipline) {
        println("HOST: bindServices called")
        zipline.bind<HostConsole>("console", RealHostConsole())
        println("HOST: console service bound")
    }
    
    override fun create(zipline: Zipline): SduiAppService {
        return zipline.take<SduiAppService>("app")
    }
}

