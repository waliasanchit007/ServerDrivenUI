@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.serverdrivenui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape

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

// Caliclan Design Tokens
private object CaliclanTheme {
    val Background = androidx.compose.ui.graphics.Color(0xFF121212)
    val Surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    val SurfaceVariant = androidx.compose.ui.graphics.Color(0xFF2C2C2C)
    val Accent = androidx.compose.ui.graphics.Color(0xFFFFC107)
    val Success = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val Error = androidx.compose.ui.graphics.Color(0xFFE57373)
    val TextPrimary = androidx.compose.ui.graphics.Color.White
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFFB0B0B0)
    val Border = androidx.compose.ui.graphics.Color(0xFF2C2C2C)
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
 * StatusCard - Membership status with colored border
 */
class CmpStatusCard : StatusCard<@Composable (androidx.compose.ui.Modifier) -> Unit> {
    private var status by mutableStateOf("active")
    private var title by mutableStateOf("")
    private var subtitle by mutableStateOf("")
    private var daysLeft by mutableStateOf(0)
    private var onClick by mutableStateOf<(() -> Unit)?>(null)

    override val value: @Composable (androidx.compose.ui.Modifier) -> Unit = { modifier ->
        val borderColor = when (status) {
            "active" -> CaliclanTheme.Success
            "expiring" -> CaliclanTheme.Accent
            "expired" -> CaliclanTheme.Error
            else -> CaliclanTheme.Border
        }
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null) androidx.compose.ui.Modifier.clickable { onClick?.invoke() } else androidx.compose.ui.Modifier),
            colors = CardDefaults.cardColors(containerColor = CaliclanTheme.Surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CaliclanTheme.TextPrimary
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CaliclanTheme.TextSecondary
                    )
                }
                if (daysLeft > 0 && status == "expiring") {
                    Text(
                        text = "Expires in $daysLeft days",
                        style = MaterialTheme.typography.bodySmall,
                        color = CaliclanTheme.Accent
                    )
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

