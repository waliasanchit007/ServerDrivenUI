@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.serverdrivenui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import com.example.serverdrivenui.common.theme.CaliclanTheme


// ============= Existing Widgets =============

class CmpMyText : MyText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")

    init {
        println("CmpMyText: Widget created")
    }

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        println("CmpMyText: Rendering with text='$text'")
        Text(
            text = text, 
            modifier = modifier,
            color = CaliclanTheme.TextPrimary,
            style = MaterialTheme.typography.bodyLarge
        )
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
        Button(
            onClick = onClick, 
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = CaliclanTheme.Accent,
                contentColor = androidx.compose.ui.graphics.Color.Black
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
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
    private var spacing by mutableStateOf(0)
    private var padding by mutableStateOf(0)
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val baseMod = if (padding > 0) modifier.padding(padding.dp) else modifier
        
        // Use FlowRow for "Wrap" arrangement to allow chip wrapping
        if (horizontalArrangement == "Wrap") {
            androidx.compose.foundation.layout.FlowRow(
                modifier = baseMod,
                horizontalArrangement = Arrangement.spacedBy(spacing.dp),
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                (children as CmpChildren).render()
            }
        } else {
            // For SpaceBetween, SpaceEvenly, SpaceAround - use that arrangement, not spacedBy
            // spacing parameter is for items, but main arrangement should still work
            val isSpaceArrangement = horizontalArrangement in listOf("SpaceBetween", "SpaceEvenly", "SpaceAround")
            val arrangement = if (isSpaceArrangement) {
                parseHorizontalArrangement(horizontalArrangement)
            } else if (spacing > 0) {
                Arrangement.spacedBy(spacing.dp)
            } else {
                parseHorizontalArrangement(horizontalArrangement)
            }
            // Use fillMaxWidth for space-based arrangements to work properly
            val rowMod = if (isSpaceArrangement) {
                baseMod.fillMaxWidth()
            } else {
                baseMod
            }
            Row(
                modifier = rowMod,
                horizontalArrangement = arrangement,
                verticalAlignment = parseVerticalAlignment(verticalAlignment)
            ) {
                (children as CmpChildren).render()
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun horizontalArrangement(horizontalArrangement: String) {
        this.horizontalArrangement = horizontalArrangement
    }

    override fun verticalAlignment(verticalAlignment: String) {
        this.verticalAlignment = verticalAlignment
    }

    override fun spacing(spacing: Int) {
        this.spacing = spacing
    }

    override fun padding(padding: Int) {
        this.padding = padding
    }
}

class CmpFlexColumn : FlexColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var verticalArrangement by mutableStateOf("Top")
    private var horizontalAlignment by mutableStateOf("Start")
    private var spacing by mutableStateOf(0)
    private var padding by mutableStateOf(0)
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val arrangement = if (spacing > 0) {
            Arrangement.spacedBy(spacing.dp)
        } else {
            parseVerticalArrangement2(verticalArrangement)
        }
        // When horizontalAlignment is "Stretch", use fillMaxWidth and Start alignment
        val (colMod, hAlign) = if (horizontalAlignment == "Stretch") {
            val baseMod = if (padding > 0) modifier.padding(padding.dp) else modifier
            baseMod.fillMaxWidth() to Alignment.Start
        } else {
            val baseMod = if (padding > 0) modifier.padding(padding.dp) else modifier
            baseMod to parseHorizontalAlignment(horizontalAlignment)
        }
        Column(
            modifier = colMod,
            verticalArrangement = arrangement,
            horizontalAlignment = hAlign
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

    override fun spacing(spacing: Int) {
        this.spacing = spacing
    }

    override fun padding(padding: Int) {
        this.padding = padding
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
    private var backgroundColor by mutableStateOf("")
    private var borderColor by mutableStateOf("")
    private var borderWidth by mutableStateOf(0)
    private var borderRadius by mutableStateOf(0)
    private var cardPadding by mutableStateOf(0)
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val cardModifier = onClick?.let { 
            modifier.fillMaxWidth().clickable { it() } 
        } ?: modifier.fillMaxWidth()
        
        val bgColor = parseColor(backgroundColor, CaliclanTheme.Surface)
        val radius = if (borderRadius > 0) borderRadius.dp else 12.dp
        val border = if (borderWidth > 0 && borderColor.isNotEmpty()) {
            androidx.compose.foundation.BorderStroke(borderWidth.dp, parseColor(borderColor, CaliclanTheme.Border))
        } else null
        val padding = if (cardPadding > 0) cardPadding.dp else 16.dp
        
        Card(
            modifier = cardModifier,
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(radius),
            border = border
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                (children as CmpChildren).render()
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun onClick(onClick: (() -> Unit)?) {
        this.onClick = onClick
    }

    override fun backgroundColor(backgroundColor: String) {
        this.backgroundColor = backgroundColor
    }

    override fun borderColor(borderColor: String) {
        this.borderColor = borderColor
    }

    override fun borderWidth(borderWidth: Int) {
        this.borderWidth = borderWidth
    }

    override fun borderRadius(borderRadius: Int) {
        this.borderRadius = borderRadius
    }

    override fun padding(padding: Int) {
        this.cardPadding = padding
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

/**
 * Parse color from hex string "#RRGGBB" or semantic name.
 * Semantic colors match CaliclanTheme tokens.
 */
private fun parseColor(value: String, default: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    if (value.isEmpty()) return default
    
    // Hex color
    if (value.startsWith("#")) {
        return try {
            val colorLong = value.removePrefix("#").toLong(16)
            if (value.length == 7) { // #RRGGBB
                androidx.compose.ui.graphics.Color(0xFF000000 or colorLong)
            } else { // #AARRGGBB
                androidx.compose.ui.graphics.Color(colorLong)
            }
        } catch (e: Exception) {
            default
        }
    }
    
    // Semantic color tokens
    return when (value.lowercase()) {
        "primary" -> CaliclanTheme.TextPrimary
        "secondary" -> CaliclanTheme.TextSecondary
        "muted" -> CaliclanTheme.TextMuted
        "accent" -> CaliclanTheme.Accent
        "accentdark" -> CaliclanTheme.AccentDark
        "accentmuted" -> CaliclanTheme.AccentMuted
        "success" -> CaliclanTheme.Success
        "successbg" -> CaliclanTheme.SuccessBg
        "error" -> CaliclanTheme.Error
        "surface" -> CaliclanTheme.Surface
        "surfacevariant" -> CaliclanTheme.SurfaceVariant
        "background" -> CaliclanTheme.Background
        "border" -> CaliclanTheme.Border
        "borderlight" -> CaliclanTheme.BorderLight
        else -> default
    }
}

private fun parseContentAlignment(value: String): Alignment = when (value) {
    "TopStart" -> Alignment.TopStart
    "TopCenter" -> Alignment.TopCenter
    "TopEnd" -> Alignment.TopEnd
    "CenterStart" -> Alignment.CenterStart
    "Center" -> Alignment.Center
    "CenterEnd" -> Alignment.CenterEnd
    "BottomStart" -> Alignment.BottomStart
    "BottomCenter" -> Alignment.BottomCenter
    "BottomEnd" -> Alignment.BottomEnd
    else -> Alignment.TopStart
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

// ============= Caliclan Widgets =============

// CaliclanTheme is now imported from com.example.serverdrivenui.common.theme.CaliclanTheme

/**
 * LazyList - Scrollable vertical list
 */
class CmpLazyList : LazyList<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val widgets = (children as CmpChildren).widgets
            items(widgets.size) { index ->
                widgets[index].value(androidx.compose.ui.Modifier.fillMaxWidth())
            }
        }
    }

    override var modifier: Modifier = Modifier
}

/**
 * AsyncImage - URL-based image loading with Coil
 */
class CmpAsyncImage : AsyncImage<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var url by mutableStateOf("")
    private var contentDescription by mutableStateOf("")
    private var size by mutableStateOf(64)
    private var circular by mutableStateOf(false)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val imageModifier = modifier
            .size(size.dp)
            .then(
                if (circular) androidx.compose.ui.Modifier.then(
                    androidx.compose.foundation.shape.CircleShape.let { shape ->
                        androidx.compose.ui.Modifier
                    }
                ) else androidx.compose.ui.Modifier
            )
        
        coil3.compose.AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = if (circular) {
                modifier
                    .size(size.dp)
                    .clip(CircleShape)
            } else {
                modifier.size(size.dp)
            },
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }

    override var modifier: Modifier = Modifier

    override fun url(url: String) { this.url = url }
    override fun contentDescription(contentDescription: String) { this.contentDescription = contentDescription }
    override fun size(size: Int) { this.size = size }
    override fun circular(circular: Boolean) { this.circular = circular }
}

/**
 * BottomSheet - Modal overlay from bottom
 */
class CmpBottomSheet : BottomSheet<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var isVisible by mutableStateOf(false)
    private var onDismiss by mutableStateOf({})
    
    override val content: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        if (isVisible) {
            val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { onDismiss() },
                sheetState = sheetState,
                containerColor = CaliclanTheme.Surface,
                contentColor = CaliclanTheme.TextPrimary,
                dragHandle = {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(CaliclanTheme.TextSecondary, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    (content as CmpChildren).render()
                }
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun isVisible(isVisible: Boolean) { this.isVisible = isVisible }
    override fun onDismiss(onDismiss: () -> Unit) { this.onDismiss = onDismiss }
}

/**
 * ScrollableColumn - Scrollable vertical content with padding
 */
class CmpScrollableColumn : ScrollableColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var padding by mutableStateOf(16)
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = padding.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            val widgets = (children as CmpChildren).widgets
            items(widgets.size) { index ->
                widgets[index].value(androidx.compose.ui.Modifier.fillMaxWidth())
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun padding(padding: Int) { this.padding = padding }
}

/**
 * HeaderText - Large title text with size variants
 */
class CmpHeaderText : HeaderText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")
    private var size by mutableStateOf("large")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val style = when (size) {
            "large" -> MaterialTheme.typography.headlineMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            "medium" -> MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            "small" -> MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            else -> MaterialTheme.typography.titleLarge
        }
        
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = CaliclanTheme.TextPrimary
        )
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) { this.text = text }
    override fun size(size: String) { this.size = size }
}

/**
 * SecondaryText - Grey caption/secondary text
 */
class CmpSecondaryText : SecondaryText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Text(
            text = text,
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            color = CaliclanTheme.TextSecondary
        )
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) { this.text = text }
}

/**
 * IconButton - Circular button with icon
 */
class CmpIconButton : IconButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var icon by mutableStateOf("home")
    private var onClick by mutableStateOf({})
    private var isSelected by mutableStateOf(false)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val iconText = when (icon) {
            "home" -> "🏠"
            "calendar" -> "📅"
            "card" -> "💳"
            "arrow_back" -> "←"
            "close" -> "✕"
            "instagram" -> "📸"
            else -> "•"
        }
        
        androidx.compose.material3.IconButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(
                text = iconText,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) CaliclanTheme.Accent else CaliclanTheme.TextPrimary
            )
        }
    }

    override var modifier: Modifier = Modifier

    override fun icon(icon: String) { this.icon = icon }
    override fun onClick(onClick: () -> Unit) { this.onClick = onClick }
    override fun isSelected(isSelected: Boolean) { this.isSelected = isSelected }
}

/**
 * Chip - Compact category tag
 */
class CmpChip : Chip<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var label by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .background(
                    CaliclanTheme.SurfaceVariant,
                    androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = CaliclanTheme.TextSecondary
            )
        }
    }

    override var modifier: Modifier = Modifier

    override fun label(label: String) { this.label = label }
}

/**
 * AppScaffold - Layout with fixed bottom navigation bar.
 * Uses Compose Scaffold for proper layout with bottom slot.
 */
class CmpAppScaffold : AppScaffold<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var showBottomBar by mutableStateOf(true)
    private var selectedTab by mutableStateOf("home")
    private var onTabSelected by mutableStateOf<(String) -> Unit>({})
    
    override val content: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = CaliclanTheme.Background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                    ) {
                        // Home tab
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { 
                                Text(
                                    "Home", 
                                    color = if (selectedTab == "home") CaliclanTheme.Accent else CaliclanTheme.TextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            selected = selectedTab == "home",
                            onClick = { onTabSelected("home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CaliclanTheme.Accent,
                                unselectedIconColor = CaliclanTheme.TextSecondary,
                                indicatorColor = CaliclanTheme.SurfaceVariant
                            )
                        )
                        // Training tab
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.DateRange, contentDescription = "Training") },
                            label = { 
                                Text(
                                    "Training", 
                                    color = if (selectedTab == "training") CaliclanTheme.Accent else CaliclanTheme.TextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            selected = selectedTab == "training",
                            onClick = { onTabSelected("training") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CaliclanTheme.Accent,
                                unselectedIconColor = CaliclanTheme.TextSecondary,
                                indicatorColor = CaliclanTheme.SurfaceVariant
                            )
                        )
                        // Membership tab
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Star, contentDescription = "Membership") },
                            label = { 
                                Text(
                                    "Membership", 
                                    color = if (selectedTab == "membership") CaliclanTheme.Accent else CaliclanTheme.TextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            selected = selectedTab == "membership",
                            onClick = { onTabSelected("membership") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CaliclanTheme.Accent,
                                unselectedIconColor = CaliclanTheme.TextSecondary,
                                indicatorColor = CaliclanTheme.SurfaceVariant
                            )
                        )
                        // Profile tab
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                            label = { 
                                Text(
                                    "Profile", 
                                    color = if (selectedTab == "profile") CaliclanTheme.Accent else CaliclanTheme.TextSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            selected = selectedTab == "profile",
                            onClick = { onTabSelected("profile") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CaliclanTheme.Accent,
                                unselectedIconColor = CaliclanTheme.TextSecondary,
                                indicatorColor = CaliclanTheme.SurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                (content as CmpChildren).render()
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun showBottomBar(showBottomBar: Boolean) { this.showBottomBar = showBottomBar }
    override fun selectedTab(selectedTab: String) { this.selectedTab = selectedTab }
    override fun onTabSelected(onTabSelected: (String) -> Unit) { this.onTabSelected = onTabSelected }
}

/**
 * ActionButton - Full-width button with icon
 */
class CmpActionButton : ActionButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var icon by mutableStateOf("whatsapp")
    private var text by mutableStateOf("")
    private var variant by mutableStateOf("secondary")
    private var onClick by mutableStateOf({})

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val (bgColor, textColor) = when (variant) {
            "primary" -> CaliclanTheme.Accent to CaliclanTheme.Background
            "ghost" -> androidx.compose.ui.graphics.Color.Transparent to CaliclanTheme.TextPrimary
            else -> CaliclanTheme.Surface to CaliclanTheme.TextPrimary
        }
        val iconEmoji = when (icon) {
            "whatsapp" -> "💬"
            "arrow_right" -> "→"
            else -> "•"
        }
        
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = bgColor),
            border = if (variant == "secondary") androidx.compose.foundation.BorderStroke(1.dp, CaliclanTheme.Border) else null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(iconEmoji, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
            Text(text, color = textColor, style = MaterialTheme.typography.bodyLarge)
        }
    }

    override var modifier: Modifier = Modifier
    override fun icon(icon: String) { this.icon = icon }
    override fun text(text: String) { this.text = text }
    override fun variant(variant: String) { this.variant = variant }
    override fun onClick(onClick: () -> Unit) { this.onClick = onClick }
}

/**
 * CoachGrid - 2-column grid of coach cards
 */
class CmpCoachGrid : CoachGrid<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val widgets = (children as CmpChildren).widgets
        // 2-column grid layout
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in widgets.indices step 2) {
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                        widgets[i].value(androidx.compose.ui.Modifier.fillMaxWidth())
                    }
                    if (i + 1 < widgets.size) {
                        Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                            widgets[i + 1].value(androidx.compose.ui.Modifier.fillMaxWidth())
                        }
                    } else {
                        Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
                    }
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
}

// ============= Enhanced Styling Primitives =============

/**
 * StyledText - Text with full styling control
 */
class CmpStyledText : StyledText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var text by mutableStateOf("")
    private var style by mutableStateOf("body")
    private var color by mutableStateOf("")
    private var fontWeight by mutableStateOf("")
    private var letterSpacing by mutableStateOf(0)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val typography = when (style) {
            "headline" -> MaterialTheme.typography.headlineLarge
            "headlineMedium" -> MaterialTheme.typography.headlineMedium
            "title" -> MaterialTheme.typography.titleLarge
            "titleMedium" -> MaterialTheme.typography.titleMedium
            "titleSmall" -> MaterialTheme.typography.titleSmall
            "body" -> MaterialTheme.typography.bodyMedium
            "bodySmall" -> MaterialTheme.typography.bodySmall
            "label" -> MaterialTheme.typography.labelMedium
            "labelSmall" -> MaterialTheme.typography.labelSmall
            else -> MaterialTheme.typography.bodyMedium
        }
        
        val weight = when (fontWeight) {
            "bold" -> androidx.compose.ui.text.font.FontWeight.Bold
            "semibold" -> androidx.compose.ui.text.font.FontWeight.SemiBold
            "medium" -> androidx.compose.ui.text.font.FontWeight.Medium
            "normal" -> androidx.compose.ui.text.font.FontWeight.Normal
            else -> null
        }
        
        val textColor = parseColor(color, CaliclanTheme.TextPrimary)
        val finalStyle = if (weight != null) typography.copy(fontWeight = weight) else typography
        val styledFinal = if (letterSpacing > 0) finalStyle.copy(letterSpacing = letterSpacing.sp) else finalStyle
        
        Text(
            text = text,
            modifier = modifier,
            style = styledFinal,
            color = textColor
        )
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) { this.text = text }
    override fun style(style: String) { this.style = style }
    override fun color(color: String) { this.color = color }
    override fun fontWeight(fontWeight: String) { this.fontWeight = fontWeight }
    override fun letterSpacing(letterSpacing: Int) { this.letterSpacing = letterSpacing }
}

/**
 * StyledBox - Container with full styling control
 */
class CmpStyledBox : StyledBox<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var backgroundColor by mutableStateOf("")
    private var borderRadius by mutableStateOf(0)
    private var padding by mutableStateOf(0)
    private var paddingHorizontal by mutableStateOf(0)
    private var paddingVertical by mutableStateOf(0)
    private var width by mutableStateOf(-1)
    private var height by mutableStateOf(-1)
    private var contentAlignment by mutableStateOf("")
    
    override val children: Widget.Children<@Composable (androidx.compose.ui.Modifier) -> Unit> = 
        CmpChildren()

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        var boxModifier = modifier
        
        // Size - wrap content explicitly to prevent vertical text rendering
        boxModifier = when (width) {
            -1 -> boxModifier.wrapContentWidth() // wrap content explicitly
            -2 -> boxModifier.fillMaxWidth() // fill
            else -> boxModifier.width(width.dp)
        }
        boxModifier = when (height) {
            -1 -> boxModifier.wrapContentHeight() // wrap content explicitly
            -2 -> boxModifier.fillMaxHeight() // fill
            else -> boxModifier.height(height.dp)
        }
        
        // Background
        if (backgroundColor.isNotEmpty()) {
            val bgColor = parseColor(backgroundColor, CaliclanTheme.Surface)
            val shape = if (borderRadius > 0) {
                androidx.compose.foundation.shape.RoundedCornerShape(borderRadius.dp)
            } else {
                androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
            }
            boxModifier = boxModifier.background(bgColor, shape)
        }
        
        // Padding
        boxModifier = when {
            paddingHorizontal > 0 || paddingVertical > 0 -> 
                boxModifier.padding(horizontal = paddingHorizontal.dp, vertical = paddingVertical.dp)
            padding > 0 -> boxModifier.padding(padding.dp)
            else -> boxModifier
        }
        
        val alignment = parseContentAlignment(contentAlignment)
        
        androidx.compose.foundation.layout.Box(
            modifier = boxModifier,
            contentAlignment = alignment
        ) {
            (children as CmpChildren).render()
        }
    }

    override var modifier: Modifier = Modifier

    override fun backgroundColor(backgroundColor: String) { this.backgroundColor = backgroundColor }
    override fun borderRadius(borderRadius: Int) { this.borderRadius = borderRadius }
    override fun padding(padding: Int) { this.padding = padding }
    override fun paddingHorizontal(paddingHorizontal: Int) { this.paddingHorizontal = paddingHorizontal }
    override fun paddingVertical(paddingVertical: Int) { this.paddingVertical = paddingVertical }
    override fun width(width: Int) { this.width = width }
    override fun height(height: Int) { this.height = height }
    override fun contentAlignment(contentAlignment: String) { this.contentAlignment = contentAlignment }
}

/**
 * Divider - Horizontal line separator
 */
class CmpDivider : Divider<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var color by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val dividerColor = parseColor(color, CaliclanTheme.Border)
        HorizontalDivider(
            modifier = modifier,
            color = dividerColor
        )
    }

    override var modifier: Modifier = Modifier

    override fun color(color: String) { this.color = color }
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
    // Caliclan widgets
    override fun LazyList(): LazyList<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpLazyList()
    }
    override fun AsyncImage(): AsyncImage<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpAsyncImage()
    }
    // Premium UI widgets
    override fun BottomSheet(): BottomSheet<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpBottomSheet()
    }
    override fun ScrollableColumn(): ScrollableColumn<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpScrollableColumn()
    }
    override fun HeaderText(): HeaderText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpHeaderText()
    }
    override fun SecondaryText(): SecondaryText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpSecondaryText()
    }
    override fun IconButton(): IconButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpIconButton()
    }
    override fun Chip(): Chip<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpChip()
    }
    override fun AppScaffold(): AppScaffold<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpAppScaffold()
    }
    override fun ActionButton(): ActionButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpActionButton()
    }
    override fun CoachGrid(): CoachGrid<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpCoachGrid()
    }
    // Enhanced styling primitives
    override fun StyledText(): StyledText<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpStyledText()
    }
    override fun StyledBox(): StyledBox<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpStyledBox()
    }
    override fun Divider(): Divider<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpDivider()
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

