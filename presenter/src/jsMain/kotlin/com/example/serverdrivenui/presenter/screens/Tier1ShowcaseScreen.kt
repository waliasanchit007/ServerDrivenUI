package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.SchemaArrangement
import com.example.serverdrivenui.schema.SchemaColor
import com.example.serverdrivenui.schema.SchemaHorizontalAlignment
import com.example.serverdrivenui.schema.SchemaIconName
import com.example.serverdrivenui.schema.SchemaTextStyle
import com.example.serverdrivenui.schema.SchemaVerticalAlignment
import com.example.serverdrivenui.schema.compose.AsyncImage
import com.example.serverdrivenui.schema.compose.Box
import com.example.serverdrivenui.schema.compose.Button
import com.example.serverdrivenui.schema.compose.Card
import com.example.serverdrivenui.schema.compose.Checkbox
import com.example.serverdrivenui.schema.compose.ElevatedCard
import com.example.serverdrivenui.schema.compose.Column
import com.example.serverdrivenui.schema.compose.ElevatedButton
import com.example.serverdrivenui.schema.compose.ExtendedFloatingActionButton
import com.example.serverdrivenui.schema.compose.FilledTonalButton
import com.example.serverdrivenui.schema.compose.FloatingActionButton
import com.example.serverdrivenui.schema.compose.Icon
import com.example.serverdrivenui.schema.compose.IconButton
import com.example.serverdrivenui.schema.compose.LazyColumn
import com.example.serverdrivenui.schema.compose.LazyItem
import com.example.serverdrivenui.schema.compose.LazyRow
import com.example.serverdrivenui.schema.compose.OutlinedButton
import com.example.serverdrivenui.schema.compose.OutlinedCard
import com.example.serverdrivenui.schema.compose.OutlinedTextField
import com.example.serverdrivenui.schema.compose.RadioButton
import com.example.serverdrivenui.schema.compose.RangeSlider
import com.example.serverdrivenui.schema.compose.Row
import com.example.serverdrivenui.schema.compose.SearchBar
import com.example.serverdrivenui.schema.compose.SegmentedButtonRow
import com.example.serverdrivenui.schema.compose.Slider
import com.example.serverdrivenui.schema.compose.Spacer
import com.example.serverdrivenui.schema.compose.Surface
import com.example.serverdrivenui.schema.compose.Switch
import com.example.serverdrivenui.schema.compose.Text
import com.example.serverdrivenui.schema.compose.TextButton
import com.example.serverdrivenui.schema.compose.TextField
import com.example.serverdrivenui.schema.compose.background
import com.example.serverdrivenui.schema.compose.fillMaxSize
import com.example.serverdrivenui.schema.compose.fillMaxWidth
import com.example.serverdrivenui.schema.compose.height
import com.example.serverdrivenui.schema.compose.padding
import com.example.serverdrivenui.schema.compose.size
import dev.konduit.Modifier

/**
 * Demonstrates every Tier 1 widget end-to-end. Phase 3 verification gate
 * per docs/KONDUIT_PLAN.md §4 + Batch 2.0 verification gate (§8.2).
 *
 * Top-level container is a LazyColumn so the showcase scrolls (it has more
 * content than fits on a phone screen). Each section is a LazyItem, which
 * itself can contain Box/Column/Row/Text/Icon/AsyncImage/etc. — exercising
 * every Tier 1 widget at least once between the outer scroll and inner
 * sections, plus every Tier 2.0 modifier (padding, background, size, fill).
 */
class Tier1ShowcaseScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        LazyColumn(modifier = Modifier.padding(16, 16, 16, 16).fillMaxSize()) {
            // --- Title block ---
            LazyItem {
                Column(
                    verticalArrangement = SchemaArrangement.Start,
                    horizontalAlignment = SchemaHorizontalAlignment.Start,
                ) {
                    Text(
                        text = "Konduit Tier 1",
                        color = SchemaColor.Primary,
                        style = SchemaTextStyle.HeadlineMedium,
                    )
                    Spacer(width = 0, height = 4)
                    Text(
                        text = "10 foundation widgets, 11 layout modifiers.",
                        color = SchemaColor.OnSurfaceVariant,
                        style = SchemaTextStyle.BodyMedium,
                    )
                }
            }

            LazyItem { Spacer(width = 0, height = 16) }

            // --- Icon row ---
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(name = SchemaIconName.Home, tint = SchemaColor.Primary, modifier = Modifier.size(32, 32))
                    Icon(name = SchemaIconName.Star, tint = SchemaColor.Tertiary, modifier = Modifier.size(32, 32))
                    Icon(name = SchemaIconName.Favorite, tint = SchemaColor.Error, modifier = Modifier.size(32, 32))
                    Icon(name = SchemaIconName.Settings, tint = SchemaColor.OnSurfaceVariant, modifier = Modifier.size(32, 32))
                    Icon(name = SchemaIconName.Notifications, tint = SchemaColor.Secondary, modifier = Modifier.size(32, 32))
                }
            }

            LazyItem { Spacer(width = 0, height = 16) }

            // --- LazyRow section ---
            LazyItem {
                Text(
                    text = "LazyRow",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    repeat(12) { index ->
                        LazyItem {
                            Box(
                                onClick = null,
                                modifier = Modifier
                                    .background(SchemaColor.PrimaryContainer)
                                    .padding(12, 12, 12, 12),
                            ) {
                                Text(
                                    text = "Chip $index",
                                    color = SchemaColor.OnPrimaryContainer,
                                    style = SchemaTextStyle.LabelLarge,
                                )
                            }
                            Spacer(width = 8, height = 0)
                        }
                    }
                }
            }

            LazyItem { Spacer(width = 0, height = 16) }

            // --- List rows section (each row uses Row + Icon + Text) ---
            LazyItem {
                Text(
                    text = "List rows",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            val items = listOf(
                SchemaIconName.Home to "Home",
                SchemaIconName.Search to "Search",
                SchemaIconName.Email to "Inbox",
                SchemaIconName.Person to "Profile",
                SchemaIconName.Settings to "Settings",
            )
            items.forEach { (icon, label) ->
                LazyItem {
                    Row(
                        horizontalArrangement = SchemaArrangement.Start,
                        verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SchemaColor.SurfaceVariant)
                            .padding(12, 12, 12, 12),
                    ) {
                        Icon(name = icon, tint = SchemaColor.OnSurfaceVariant, modifier = Modifier.size(24, 24))
                        Spacer(width = 12, height = 0)
                        Text(
                            text = label,
                            color = SchemaColor.OnSurfaceVariant,
                            style = SchemaTextStyle.BodyLarge,
                        )
                    }
                    Spacer(width = 0, height = 4)
                }
            }

            LazyItem { Spacer(width = 0, height = 16) }

            // --- AsyncImage ---
            LazyItem {
                Text(
                    text = "AsyncImage",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                AsyncImage(
                    url = "https://picsum.photos/600/300",
                    contentDescription = "Random landscape from picsum.photos",
                    modifier = Modifier.fillMaxWidth().height(200),
                )
            }
            LazyItem { Spacer(width = 0, height = 24) }

            // --- Buttons (Batch 2.1) ---
            LazyItem {
                Text(
                    text = "Buttons",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceBetween,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(text = "Filled", enabled = true, onClick = null)
                    OutlinedButton(text = "Outlined", enabled = true, onClick = null)
                    TextButton(text = "Text", enabled = true, onClick = null)
                }
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceBetween,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FilledTonalButton(text = "Tonal", enabled = true, onClick = null)
                    ElevatedButton(text = "Elevated", enabled = true, onClick = null)
                    Button(text = "Disabled", enabled = false, onClick = null)
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(enabled = true, onClick = null) {
                        Icon(name = SchemaIconName.Favorite, tint = SchemaColor.Error)
                    }
                    FloatingActionButton(onClick = null) {
                        Icon(name = SchemaIconName.Add, tint = SchemaColor.OnPrimaryContainer)
                    }
                    ExtendedFloatingActionButton(text = "Compose", onClick = null) {
                        Icon(name = SchemaIconName.Edit, tint = SchemaColor.OnPrimaryContainer)
                    }
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Inputs (Batch 2.2) ---
            LazyItem {
                Text(
                    text = "Inputs",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                var name by remember { mutableStateOf("") }
                TextField(
                    value = name,
                    placeholder = "Your name",
                    enabled = true,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                var email by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = email,
                    placeholder = "you@example.com",
                    enabled = true,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                var query by remember { mutableStateOf("") }
                SearchBar(
                    value = query,
                    placeholder = "Search…",
                    enabled = true,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Selection (Batch 2.3) ---
            LazyItem {
                Text(
                    text = "Selection",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // Checkbox + Switch row
            LazyItem {
                var checked by remember { mutableStateOf(true) }
                var switched by remember { mutableStateOf(false) }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(checked = checked, enabled = true, onCheckedChange = { checked = it })
                    Switch(checked = switched, enabled = true, onCheckedChange = { switched = it })
                    Checkbox(checked = false, enabled = false, onCheckedChange = null)
                    Switch(checked = true, enabled = false, onCheckedChange = null)
                }
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // Radio group
            LazyItem {
                var pick by remember { mutableStateOf(0) }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(selected = pick == 0, enabled = true, onClick = { pick = 0 })
                    RadioButton(selected = pick == 1, enabled = true, onClick = { pick = 1 })
                    RadioButton(selected = pick == 2, enabled = true, onClick = { pick = 2 })
                    RadioButton(selected = false, enabled = false, onClick = null)
                }
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // Slider
            LazyItem {
                var v by remember { mutableStateOf(0.4f) }
                Slider(
                    value = v,
                    valueFrom = 0f,
                    valueTo = 1f,
                    steps = 0,
                    enabled = true,
                    onValueChange = { v = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            // RangeSlider
            LazyItem {
                var lo by remember { mutableStateOf(0.2f) }
                var hi by remember { mutableStateOf(0.8f) }
                RangeSlider(
                    rangeStart = lo,
                    rangeEnd = hi,
                    valueFrom = 0f,
                    valueTo = 1f,
                    steps = 0,
                    enabled = true,
                    onRangeChange = { s, e -> lo = s; hi = e },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // SegmentedButtonRow
            LazyItem {
                var seg by remember { mutableStateOf(1) }
                SegmentedButtonRow(
                    labelsCsv = "Day, Week, Month",
                    selectedIndex = seg,
                    onSelectionChange = { seg = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Containers (Batch 2.4) ---
            LazyItem {
                Text(
                    text = "Containers",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                Card(onClick = null, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = SchemaArrangement.Start,
                        horizontalAlignment = SchemaHorizontalAlignment.Start,
                        modifier = Modifier.padding(16, 16, 16, 16),
                    ) {
                        Text(
                            text = "Card",
                            color = SchemaColor.OnSurface,
                            style = SchemaTextStyle.TitleSmall,
                        )
                        Text(
                            text = "Filled card with default tonal elevation.",
                            color = SchemaColor.OnSurfaceVariant,
                            style = SchemaTextStyle.BodyMedium,
                        )
                    }
                }
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                ElevatedCard(onClick = null, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = SchemaArrangement.Start,
                        horizontalAlignment = SchemaHorizontalAlignment.Start,
                        modifier = Modifier.padding(16, 16, 16, 16),
                    ) {
                        Text(
                            text = "ElevatedCard",
                            color = SchemaColor.OnSurface,
                            style = SchemaTextStyle.TitleSmall,
                        )
                        Text(
                            text = "Card with shadow.",
                            color = SchemaColor.OnSurfaceVariant,
                            style = SchemaTextStyle.BodyMedium,
                        )
                    }
                }
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                OutlinedCard(onClick = null, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = SchemaArrangement.Start,
                        horizontalAlignment = SchemaHorizontalAlignment.Start,
                        modifier = Modifier.padding(16, 16, 16, 16),
                    ) {
                        Text(
                            text = "OutlinedCard",
                            color = SchemaColor.OnSurface,
                            style = SchemaTextStyle.TitleSmall,
                        )
                        Text(
                            text = "Card with outline border.",
                            color = SchemaColor.OnSurfaceVariant,
                            style = SchemaTextStyle.BodyMedium,
                        )
                    }
                }
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                Surface(
                    tonalElevationDp = 4,
                    onClick = null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "  Surface (tonalElevation=4dp)",
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                        modifier = Modifier.padding(16, 16, 16, 16),
                    )
                }
            }

            LazyItem { Spacer(width = 0, height = 32) }
        }
    }
}
