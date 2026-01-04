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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person

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
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
            modifier.fillMaxWidth().clickable { it() } 
        } ?: modifier.fillMaxWidth()
        
        Card(
            modifier = cardModifier,
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(16.dp)
            ) {
                (children as CmpChildren).render()
            }
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

// ============= Caliclan Widgets =============

// Caliclan Design Tokens - matches web app neutral-950 palette
private object CaliclanTheme {
    // Backgrounds (from web tailwind neutral scale)
    val Background = androidx.compose.ui.graphics.Color(0xFF0A0A0A)  // neutral-950
    val Surface = androidx.compose.ui.graphics.Color(0xFF171717)     // neutral-900
    val SurfaceVariant = androidx.compose.ui.graphics.Color(0xFF262626)  // neutral-800
    val SurfaceElevated = androidx.compose.ui.graphics.Color(0xFF262626) // neutral-800
    
    // Accent colors (amber scale)
    val Accent = androidx.compose.ui.graphics.Color(0xFFF59E0B)      // amber-500
    val AccentDark = androidx.compose.ui.graphics.Color(0xFF92400E)  // amber-900/50 approx
    val AccentMuted = androidx.compose.ui.graphics.Color(0xFF78350F) // amber-950/40 approx
    
    // Status colors
    val Success = androidx.compose.ui.graphics.Color(0xFF059669)     // emerald-600
    val SuccessBg = androidx.compose.ui.graphics.Color(0xFF064E3B)   // emerald-950/40 approx
    val Error = androidx.compose.ui.graphics.Color(0xFFDC2626)       // red-600
    
    // Text
    val TextPrimary = androidx.compose.ui.graphics.Color(0xFFFAFAFA)   // neutral-50
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFFA3A3A3) // neutral-400
    val TextMuted = androidx.compose.ui.graphics.Color(0xFF737373)     // neutral-500
    
    // Borders
    val Border = androidx.compose.ui.graphics.Color(0xFF262626)        // neutral-800
    val BorderLight = androidx.compose.ui.graphics.Color(0xFF404040)   // neutral-700
}

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
 * StatusCard - Membership status with subtle left accent strip
 */
class CmpStatusCard : StatusCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var status by mutableStateOf("active")
    private var title by mutableStateOf("")
    private var subtitle by mutableStateOf("")
    private var daysLeft by mutableStateOf(0)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        // Subtle accent color (reduced intensity)
        val accentColor = when (status) {
            "active" -> CaliclanTheme.Success.copy(alpha = 0.7f)
            "expiring" -> CaliclanTheme.Accent.copy(alpha = 0.7f)
            "expired" -> CaliclanTheme.Error.copy(alpha = 0.7f)
            else -> CaliclanTheme.Border
        }
        
        // Card with left accent strip instead of full border
        Card(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null) androidx.compose.ui.Modifier.clickable { onClick?.invoke() } else androidx.compose.ui.Modifier),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row {
                // Left accent strip (subtle)
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accentColor)
                )
                Column(
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CaliclanTheme.TextPrimary
                    )
                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CaliclanTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun status(status: String) { this.status = status }
    override fun title(title: String) { this.title = title }
    override fun subtitle(subtitle: String) { this.subtitle = subtitle }
    override fun daysLeft(daysLeft: Int) { this.daysLeft = daysLeft }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

/**
 * ConsistencyStrip - Weekly attendance indicators (Mon-Sun)
 */
class CmpConsistencyStrip : ConsistencyStrip<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var monday by mutableStateOf("future")
    private var tuesday by mutableStateOf("future")
    private var wednesday by mutableStateOf("future")
    private var thursday by mutableStateOf("future")
    private var friday by mutableStateOf("future")
    private var saturday by mutableStateOf("future")
    private var sunday by mutableStateOf("future")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "M" to monday, "T" to tuesday, "W" to wednesday,
                "T" to thursday, "F" to friday, "S" to saturday, "S" to sunday
            ).forEach { (label, status) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = CaliclanTheme.TextSecondary
                    )
                    androidx.compose.foundation.Canvas(
                        modifier = androidx.compose.ui.Modifier.size(12.dp)
                    ) {
                        when (status) {
                            "attended" -> drawCircle(color = CaliclanTheme.Success)
                            "today" -> drawCircle(color = CaliclanTheme.Accent)
                            "missed" -> {
                                drawCircle(
                                    color = CaliclanTheme.TextSecondary,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            }
                            "rest" -> drawLine(
                                color = CaliclanTheme.TextSecondary,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                                strokeWidth = 2f
                            )
                            else -> drawCircle(color = CaliclanTheme.Border)
                        }
                    }
                }
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun monday(monday: String) { this.monday = monday }
    override fun tuesday(tuesday: String) { this.tuesday = tuesday }
    override fun wednesday(wednesday: String) { this.wednesday = wednesday }
    override fun thursday(thursday: String) { this.thursday = thursday }
    override fun friday(friday: String) { this.friday = friday }
    override fun saturday(saturday: String) { this.saturday = saturday }
    override fun sunday(sunday: String) { this.sunday = sunday }
}

/**
 * CoachCard - Coach preview with photo, name, role
 */
class CmpCoachCard : CoachCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var name by mutableStateOf("")
    private var role by mutableStateOf("")
    private var photoUrl by mutableStateOf("")
    private var onClick by mutableStateOf({})

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Card(
            modifier = modifier
                .width(140.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface)
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                coil3.compose.AsyncImage(
                    model = photoUrl,
                    contentDescription = name,
                    modifier = androidx.compose.ui.Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = CaliclanTheme.TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = CaliclanTheme.Accent,
                    maxLines = 1
                )
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun name(name: String) { this.name = name }
    override fun role(role: String) { this.role = role }
    override fun photoUrl(photoUrl: String) { this.photoUrl = photoUrl }
    override fun onClick(onClick: () -> Unit) { this.onClick = onClick }
}

/**
 * ScheduleItem - Training day row
 */
class CmpScheduleItem : ScheduleItem<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var dayName by mutableStateOf("")
    private var date by mutableStateOf("")
    private var focus by mutableStateOf("")
    private var isToday by mutableStateOf(false)
    private var isAttended by mutableStateOf(false)
    private var isRestDay by mutableStateOf(false)
    private var onClick by mutableStateOf({})

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val borderColor = when {
            isToday -> CaliclanTheme.Accent
            else -> CaliclanTheme.Border
        }
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isToday) CaliclanTheme.Surface else CaliclanTheme.Background
            ),
            border = if (isToday) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
        ) {
            Row(
                modifier = androidx.compose.ui.Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isToday) CaliclanTheme.Accent else CaliclanTheme.TextSecondary
                    )
                    Text(
                        text = if (isRestDay) "Rest & Recovery" else focus,
                        style = MaterialTheme.typography.titleMedium,
                        color = CaliclanTheme.TextPrimary
                    )
                }
                
                if (isAttended) {
                    Text(text = "✓", color = CaliclanTheme.Success)
                } else if (isRestDay) {
                    Text(text = "—", color = CaliclanTheme.TextSecondary)
                }
            }
        }
    }

    override var modifier: Modifier = Modifier

    override fun dayName(dayName: String) { this.dayName = dayName }
    override fun date(date: String) { this.date = date }
    override fun focus(focus: String) { this.focus = focus }
    override fun isToday(isToday: Boolean) { this.isToday = isToday }
    override fun isAttended(isAttended: Boolean) { this.isAttended = isAttended }
    override fun isRestDay(isRestDay: Boolean) { this.isRestDay = isRestDay }
    override fun onClick(onClick: () -> Unit) { this.onClick = onClick }
}

/**
 * BottomNavigationBar - Tab bar with Home, Training, Membership
 */
class CmpBottomNavigationBar : BottomNavigationBar<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var selectedTab by mutableStateOf("home")
    private var onTabSelected by mutableStateOf<(String) -> Unit>({})

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        androidx.compose.material3.NavigationBar(
            modifier = modifier,
            containerColor = CaliclanTheme.Surface,
            contentColor = CaliclanTheme.TextPrimary
        ) {
            // Home tab
            NavigationBarItem(
                icon = { Text("🏠", style = MaterialTheme.typography.titleMedium) },
                label = { Text("Home", color = if (selectedTab == "home") CaliclanTheme.Accent else CaliclanTheme.TextSecondary) },
                selected = selectedTab == "home",
                onClick = { onTabSelected("home") },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = CaliclanTheme.Accent,
                    indicatorColor = CaliclanTheme.SurfaceVariant
                )
            )
            // Training tab
            NavigationBarItem(
                icon = { Text("📅", style = MaterialTheme.typography.titleMedium) },
                label = { Text("Training", color = if (selectedTab == "training") CaliclanTheme.Accent else CaliclanTheme.TextSecondary) },
                selected = selectedTab == "training",
                onClick = { onTabSelected("training") },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = CaliclanTheme.Accent,
                    indicatorColor = CaliclanTheme.SurfaceVariant
                )
            )
            // Membership tab
            NavigationBarItem(
                icon = { Text("💳", style = MaterialTheme.typography.titleMedium) },
                label = { Text("Membership", color = if (selectedTab == "membership") CaliclanTheme.Accent else CaliclanTheme.TextSecondary) },
                selected = selectedTab == "membership",
                onClick = { onTabSelected("membership") },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = CaliclanTheme.Accent,
                    indicatorColor = CaliclanTheme.SurfaceVariant
                )
            )
        }
    }

    override var modifier: Modifier = Modifier

    override fun selectedTab(selectedTab: String) { this.selectedTab = selectedTab }
    override fun onTabSelected(onTabSelected: (String) -> Unit) { this.onTabSelected = onTabSelected }
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            icon = { Icon(Icons.Filled.AccountBox, contentDescription = "Membership") },
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
 * GreetingHeader - Two-line greeting matching web app
 */
class CmpGreetingHeader : GreetingHeader<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var subtitle by mutableStateOf("Welcome back,")
    private var title by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Column(modifier = modifier) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = CaliclanTheme.TextSecondary
            )
            Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = CaliclanTheme.TextPrimary
            )
        }
    }

    override var modifier: Modifier = Modifier
    override fun subtitle(subtitle: String) { this.subtitle = subtitle }
    override fun title(title: String) { this.title = title }
}

/**
 * TrainingSessionCard - Today's session card with focus + goals
 */
class CmpTrainingSessionCard : TrainingSessionCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var label by mutableStateOf("Today's Session")
    private var focus by mutableStateOf("")
    private var goals by mutableStateOf(listOf<String>())
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Card(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null) androidx.compose.ui.Modifier.clickable { onClick?.invoke() } else androidx.compose.ui.Modifier),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CaliclanTheme.Border),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(24.dp)) {
                // Header row
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CaliclanTheme.TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                        Text(
                            text = focus,
                            style = MaterialTheme.typography.titleLarge,
                            color = CaliclanTheme.TextPrimary
                        )
                    }
                    Text("›", style = MaterialTheme.typography.titleLarge, color = CaliclanTheme.TextMuted)
                }
                
                // Goals section
                if (goals.isNotEmpty()) {
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    Text(
                        text = "Focus Areas",
                        style = MaterialTheme.typography.labelSmall,
                        color = CaliclanTheme.TextSecondary
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        goals.forEach { goal ->
                            Box(
                                modifier = androidx.compose.ui.Modifier
                                    .background(CaliclanTheme.SurfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(goal, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun label(label: String) { this.label = label }
    override fun focus(focus: String) { this.focus = focus }
    override fun goals(goals: List<String>) { this.goals = goals }
    override fun onClick(onClick: (() -> Unit)?) { this.onClick = onClick }
}

/**
 * AnnouncementCard - Amber-bordered coach update
 */
class CmpAnnouncementCard : AnnouncementCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var label by mutableStateOf("Coach Update")
    private var title by mutableStateOf("")
    private var message by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CaliclanTheme.AccentDark),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(24.dp)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = CaliclanTheme.Accent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CaliclanTheme.TextPrimary
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CaliclanTheme.TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun label(label: String) { this.label = label }
    override fun title(title: String) { this.title = title }
    override fun message(message: String) { this.message = message }
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

/**
 * WeeklyAttendance - 7-day visual strip matching web app
 */
class CmpWeeklyAttendance : WeeklyAttendance<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var streak by mutableStateOf(0)
    private var days by mutableStateOf(listOf<String>())
    private var summary by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CaliclanTheme.Border),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(24.dp)) {
                // Streak header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                    Text(
                        "$streak-day streak",
                        style = MaterialTheme.typography.titleMedium,
                        color = CaliclanTheme.TextPrimary
                    )
                }
                
                Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                // Weekly visual
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        val status = days.getOrElse(index) { "future" }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = CaliclanTheme.TextMuted
                            )
                            Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                            Box(
                                modifier = androidx.compose.ui.Modifier
                                    .size(32.dp)
                                    .background(
                                        when (status) {
                                            "today" -> CaliclanTheme.Accent
                                            "attended" -> CaliclanTheme.SuccessBg
                                            else -> CaliclanTheme.SurfaceVariant
                                        },
                                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (status) {
                                    "attended" -> Box(
                                        modifier = androidx.compose.ui.Modifier
                                            .size(8.dp)
                                            .background(CaliclanTheme.Success, CircleShape)
                                    )
                                    "today" -> Box(
                                        modifier = androidx.compose.ui.Modifier
                                            .size(6.dp)
                                            .background(CaliclanTheme.Background, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Summary
                if (summary.isNotEmpty()) {
                    Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                    HorizontalDivider(color = CaliclanTheme.Border)
                    Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                    Text(
                        summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = CaliclanTheme.TextSecondary
                    )
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun streak(streak: Int) { this.streak = streak }
    override fun days(days: List<String>) { this.days = days }
    override fun summary(summary: String) { this.summary = summary }
}

/**
 * TrainingDayCard - Full training day card matching web app exactly
 */
class CmpTrainingDayCard : TrainingDayCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var day by mutableStateOf("")
    private var date by mutableStateOf("")
    private var focus by mutableStateOf("")
    private var goals by mutableStateOf(listOf<String>())
    private var supporting by mutableStateOf(listOf<String>())
    private var isToday by mutableStateOf(false)
    private var attended by mutableStateOf(false)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val bgColor = if (isToday) CaliclanTheme.AccentMuted else CaliclanTheme.Surface
        val borderColor = if (isToday) CaliclanTheme.Accent else CaliclanTheme.Border
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(24.dp)) {
                // Day Header
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                day,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday) CaliclanTheme.Accent else CaliclanTheme.TextSecondary
                            )
                            if (isToday) {
                                Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                                Box(
                                    modifier = androidx.compose.ui.Modifier
                                        .background(CaliclanTheme.Accent, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("TODAY", style = MaterialTheme.typography.labelSmall, color = CaliclanTheme.Background)
                                }
                            }
                            if (attended && !isToday) {
                                Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                                Box(
                                    modifier = androidx.compose.ui.Modifier
                                        .size(20.dp)
                                        .background(CaliclanTheme.SuccessBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", style = MaterialTheme.typography.labelSmall, color = CaliclanTheme.Success)
                                }
                            }
                        }
                        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                        Text(focus, style = MaterialTheme.typography.titleLarge, color = CaliclanTheme.TextPrimary)
                    }
                    Text(date, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextMuted)
                }
                
                Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                // Primary Goals
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                    Text("Primary Goals", style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                }
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    goals.forEach { goal ->
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .background(
                                    if (isToday) CaliclanTheme.AccentDark else CaliclanTheme.SurfaceVariant,
                                    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                goal,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isToday) CaliclanTheme.Accent else CaliclanTheme.TextPrimary
                            )
                        }
                    }
                }
                
                // Supporting
                if (supporting.isNotEmpty()) {
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕐", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        Text("Supporting", style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextMuted)
                    }
                    Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                    Text(
                        supporting.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = CaliclanTheme.TextSecondary
                    )
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun day(day: String) { this.day = day }
    override fun date(date: String) { this.date = date }
    override fun focus(focus: String) { this.focus = focus }
    override fun goals(goals: List<String>) { this.goals = goals }
    override fun supporting(supporting: List<String>) { this.supporting = supporting }
    override fun isToday(isToday: Boolean) { this.isToday = isToday }
    override fun attended(attended: Boolean) { this.attended = attended }
}

/**
 * MembershipPlanCard - Plan card matching web app exactly
 */
class CmpMembershipPlanCard : MembershipPlanCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var name by mutableStateOf("")
    private var duration by mutableStateOf("")
    private var price by mutableStateOf("")
    private var priceLabel by mutableStateOf("")
    private var features by mutableStateOf(listOf<String>())
    private var isCurrent by mutableStateOf(false)
    private var isRecommended by mutableStateOf(false)
    private var billingDate by mutableStateOf("")
    private var onSelect by mutableStateOf<(() -> Unit)?>(null)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val borderColor = when {
            isCurrent -> CaliclanTheme.Accent
            isRecommended -> CaliclanTheme.AccentDark
            else -> CaliclanTheme.Border
        }
        val bgColor = if (isCurrent) CaliclanTheme.AccentMuted else CaliclanTheme.Surface
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(24.dp)) {
                // Label
                if (isCurrent) {
                    Text(
                        "CURRENT PLAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = CaliclanTheme.Accent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                }
                if (isRecommended && !isCurrent) {
                    Text(
                        "RECOMMENDED",
                        style = MaterialTheme.typography.labelSmall,
                        color = CaliclanTheme.Accent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                }
                
                // Header
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge, color = CaliclanTheme.TextPrimary)
                        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                        Text(duration, style = MaterialTheme.typography.bodyMedium, color = CaliclanTheme.TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(price, style = MaterialTheme.typography.titleLarge, color = CaliclanTheme.TextPrimary)
                        Text(priceLabel, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                    }
                }
                
                Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                // Features
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCurrent) CaliclanTheme.Accent else CaliclanTheme.TextMuted,
                            modifier = androidx.compose.ui.Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        Text(
                            feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCurrent) CaliclanTheme.TextPrimary else CaliclanTheme.TextSecondary
                        )
                    }
                    Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                }
                
                // Billing date (current plan)
                if (isCurrent && billingDate.isNotEmpty()) {
                    Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    HorizontalDivider(color = CaliclanTheme.BorderLight)
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    Text("Next billing date", style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                    Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                    Text(billingDate, style = MaterialTheme.typography.bodyMedium, color = CaliclanTheme.TextPrimary)
                }
                
                // Select button (non-current plans)
                if (!isCurrent && onSelect != null) {
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    Button(
                        onClick = { onSelect?.invoke() },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecommended) CaliclanTheme.Accent else CaliclanTheme.SurfaceVariant
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Select Plan",
                            color = if (isRecommended) CaliclanTheme.Background else CaliclanTheme.TextPrimary
                        )
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        Text("→", color = if (isRecommended) CaliclanTheme.Background else CaliclanTheme.TextPrimary)
                    }
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun name(name: String) { this.name = name }
    override fun duration(duration: String) { this.duration = duration }
    override fun price(price: String) { this.price = price }
    override fun priceLabel(priceLabel: String) { this.priceLabel = priceLabel }
    override fun features(features: List<String>) { this.features = features }
    override fun isCurrent(isCurrent: Boolean) { this.isCurrent = isCurrent }
    override fun isRecommended(isRecommended: Boolean) { this.isRecommended = isRecommended }
    override fun billingDate(billingDate: String) { this.billingDate = billingDate }
    override fun onSelect(onSelect: (() -> Unit)?) { this.onSelect = onSelect }
}

/**
 * ProfileInfoCard - Member info card matching web app exactly
 */
class CmpProfileInfoCard : ProfileInfoCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var name by mutableStateOf("")
    private var email by mutableStateOf("")
    private var phone by mutableStateOf("")
    private var batch by mutableStateOf("")
    private var memberSince by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CaliclanTheme.Border),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(modifier = androidx.compose.ui.Modifier.padding(24.dp)) {
                // Avatar + Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .size(64.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(CaliclanTheme.Accent, CaliclanTheme.AccentDark)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = CaliclanTheme.Background
                        )
                    }
                    Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge, color = CaliclanTheme.TextPrimary)
                        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                        Text(
                            "Member since $memberSince",
                            style = MaterialTheme.typography.bodySmall,
                            color = CaliclanTheme.TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                HorizontalDivider(color = CaliclanTheme.Border)
                Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                // Info rows
                Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Email", style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextPrimary)
                }
                Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Phone", style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                    Text(phone, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextPrimary)
                }
                Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Batch", style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                    Text(batch, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextPrimary)
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun name(name: String) { this.name = name }
    override fun email(email: String) { this.email = email }
    override fun phone(phone: String) { this.phone = phone }
    override fun batch(batch: String) { this.batch = batch }
    override fun memberSince(memberSince: String) { this.memberSince = memberSince }
}

/**
 * HistoryItem - Payment/Membership history item
 */
class CmpHistoryItem : HistoryItem<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var title by mutableStateOf("")
    private var subtitle by mutableStateOf("")
    private var status by mutableStateOf("")
    private var amount by mutableStateOf("")

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CaliclanTheme.Border),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.bodyMedium, color = CaliclanTheme.TextPrimary)
                    Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CaliclanTheme.TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (amount.isNotEmpty()) {
                        Text(amount, style = MaterialTheme.typography.bodyMedium, color = CaliclanTheme.TextPrimary)
                    }
                    val statusColor = when (status) {
                        "active" -> CaliclanTheme.Success
                        "completed" -> CaliclanTheme.Success
                        else -> CaliclanTheme.TextSecondary
                    }
                    val statusBg = when (status) {
                        "active" -> CaliclanTheme.SuccessBg
                        else -> androidx.compose.ui.graphics.Color.Transparent
                    }
                    Box(
                        modifier = if (status == "active") {
                            androidx.compose.ui.Modifier
                                .background(statusBg, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        } else androidx.compose.ui.Modifier
                    ) {
                        Text(
                            status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }

    override var modifier: Modifier = Modifier
    override fun title(title: String) { this.title = title }
    override fun subtitle(subtitle: String) { this.subtitle = subtitle }
    override fun status(status: String) { this.status = status }
    override fun amount(amount: String) { this.amount = amount }
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
    override fun StatusCard(): StatusCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpStatusCard()
    }
    override fun ConsistencyStrip(): ConsistencyStrip<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpConsistencyStrip()
    }
    override fun CoachCard(): CoachCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpCoachCard()
    }
    override fun ScheduleItem(): ScheduleItem<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpScheduleItem()
    }
    // Premium UI widgets
    override fun BottomNavigationBar(): BottomNavigationBar<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpBottomNavigationBar()
    }
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
    // New Home screen widgets
    override fun GreetingHeader(): GreetingHeader<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpGreetingHeader()
    }
    override fun TrainingSessionCard(): TrainingSessionCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpTrainingSessionCard()
    }
    override fun AnnouncementCard(): AnnouncementCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpAnnouncementCard()
    }
    override fun ActionButton(): ActionButton<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpActionButton()
    }
    override fun CoachGrid(): CoachGrid<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpCoachGrid()
    }
    override fun WeeklyAttendance(): WeeklyAttendance<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpWeeklyAttendance()
    }
    // Pixel-perfect screen widgets
    override fun TrainingDayCard(): TrainingDayCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpTrainingDayCard()
    }
    override fun MembershipPlanCard(): MembershipPlanCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpMembershipPlanCard()
    }
    override fun ProfileInfoCard(): ProfileInfoCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpProfileInfoCard()
    }
    override fun HistoryItem(): HistoryItem<@Composable (androidx.compose.ui.Modifier) -> Unit> {
        return CmpHistoryItem()
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

