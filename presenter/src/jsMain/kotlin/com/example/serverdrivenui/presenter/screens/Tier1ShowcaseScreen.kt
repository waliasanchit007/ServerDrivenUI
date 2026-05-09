package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.schema.SchemaArrangement
import com.example.serverdrivenui.schema.SchemaColor
import com.example.serverdrivenui.schema.SchemaHorizontalAlignment
import com.example.serverdrivenui.schema.SchemaIconName
import com.example.serverdrivenui.schema.SchemaTextStyle
import com.example.serverdrivenui.schema.SchemaVerticalAlignment
import com.example.serverdrivenui.schema.compose.AsyncImage
import com.example.serverdrivenui.schema.compose.Badge
import com.example.serverdrivenui.schema.compose.Box
import com.example.serverdrivenui.schema.compose.Button
import com.example.serverdrivenui.schema.compose.Card
import com.example.serverdrivenui.schema.compose.Checkbox
import com.example.serverdrivenui.schema.compose.CircularProgressIndicator
import com.example.serverdrivenui.schema.compose.ElevatedCard
import com.example.serverdrivenui.schema.compose.Column
import com.example.serverdrivenui.schema.compose.ElevatedButton
import com.example.serverdrivenui.schema.compose.ExtendedFloatingActionButton
import com.example.serverdrivenui.schema.compose.FilledTonalButton
import com.example.serverdrivenui.schema.compose.AlertDialog
import com.example.serverdrivenui.schema.compose.AssistChip
import com.example.serverdrivenui.schema.compose.DropdownMenu
import com.example.serverdrivenui.schema.compose.DropdownMenuItem
import com.example.serverdrivenui.schema.compose.FilterChip
import com.example.serverdrivenui.schema.compose.HorizontalDivider
import com.example.serverdrivenui.schema.compose.HorizontalPager
import com.example.serverdrivenui.schema.compose.InputChip
import com.example.serverdrivenui.schema.compose.ListItem
import com.example.serverdrivenui.schema.compose.ModalBottomSheet
import com.example.serverdrivenui.schema.compose.PagerIndicator
import com.example.serverdrivenui.schema.compose.PullToRefreshBox
import com.example.serverdrivenui.schema.compose.SuggestionChip
import com.example.serverdrivenui.schema.compose.VerticalDivider
import com.example.serverdrivenui.schema.compose.VerticalPager
import com.example.serverdrivenui.schema.compose.FloatingActionButton
import com.example.serverdrivenui.schema.compose.Icon
import com.example.serverdrivenui.schema.compose.IconButton
import com.example.serverdrivenui.schema.compose.LazyColumn
import com.example.serverdrivenui.schema.compose.LazyItem
import com.example.serverdrivenui.schema.compose.LazyRow
import com.example.serverdrivenui.schema.compose.LargeTopAppBar
import com.example.serverdrivenui.schema.compose.LinearProgressIndicator
import com.example.serverdrivenui.schema.compose.MediumTopAppBar
import com.example.serverdrivenui.schema.compose.NavigationBar
import com.example.serverdrivenui.schema.compose.NavigationBarItem
import com.example.serverdrivenui.schema.compose.OutlinedButton
import com.example.serverdrivenui.schema.compose.OutlinedCard
import com.example.serverdrivenui.schema.compose.OutlinedTextField
import com.example.serverdrivenui.schema.compose.RadioButton
import com.example.serverdrivenui.schema.compose.RangeSlider
import com.example.serverdrivenui.schema.compose.Row
import com.example.serverdrivenui.schema.compose.SearchBar
import com.example.serverdrivenui.schema.compose.SegmentedButtonRow
import com.example.serverdrivenui.schema.compose.Slider
import com.example.serverdrivenui.schema.compose.Snackbar
import com.example.serverdrivenui.schema.compose.Spacer
import com.example.serverdrivenui.schema.compose.Surface
import com.example.serverdrivenui.schema.compose.Switch
import com.example.serverdrivenui.schema.compose.Tab
import com.example.serverdrivenui.schema.compose.TabRow
import com.example.serverdrivenui.schema.compose.Text
import com.example.serverdrivenui.schema.compose.TopAppBar
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

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Feedback (Batch 2.5) ---
            LazyItem {
                Text(
                    text = "Feedback",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // Determinate + indeterminate linear
            LazyItem {
                LinearProgressIndicator(
                    progress = 0.65f,
                    indeterminate = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                LinearProgressIndicator(
                    progress = 0f,
                    indeterminate = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 12) }
            // Circular + Badges row
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CircularProgressIndicator(
                        progress = 0.4f,
                        indeterminate = false,
                    )
                    CircularProgressIndicator(
                        progress = 0f,
                        indeterminate = true,
                    )
                    Badge(text = "9")
                    Badge(text = "99+")
                    Badge(text = "")
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }
            // Snackbar
            LazyItem {
                Snackbar(
                    message = "Saved to drafts",
                    actionLabel = "Undo",
                    onActionClick = null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Nav structure (Batch 2.6) ---
            LazyItem {
                Text(
                    text = "Navigation",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // Compact TopAppBar preview
            LazyItem {
                TopAppBar(
                    title = "TopAppBar",
                    navigationIcon = {
                        Icon(name = SchemaIconName.Menu, tint = SchemaColor.OnSurface)
                    },
                    actions = {
                        Icon(name = SchemaIconName.Search, tint = SchemaColor.OnSurface)
                        Spacer(width = 8, height = 0)
                        Icon(name = SchemaIconName.Settings, tint = SchemaColor.OnSurface)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // MediumTopAppBar
            LazyItem {
                MediumTopAppBar(
                    title = "MediumTopAppBar",
                    navigationIcon = {
                        Icon(name = SchemaIconName.ArrowBack, tint = SchemaColor.OnSurface)
                    },
                    actions = {
                        Icon(name = SchemaIconName.Edit, tint = SchemaColor.OnSurface)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // LargeTopAppBar
            LazyItem {
                LargeTopAppBar(
                    title = "LargeTopAppBar",
                    navigationIcon = {
                        Icon(name = SchemaIconName.ArrowBack, tint = SchemaColor.OnSurface)
                    },
                    actions = {
                        Icon(name = SchemaIconName.Notifications, tint = SchemaColor.OnSurface)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 12) }
            // TabRow
            LazyItem {
                var tab by remember { mutableStateOf(0) }
                TabRow(
                    selectedTabIndex = tab,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        selected = tab == 0,
                        text = "Home",
                        onClick = { tab = 0 },
                    ) { Icon(name = SchemaIconName.Home, tint = SchemaColor.Primary) }
                    Tab(
                        selected = tab == 1,
                        text = "Search",
                        onClick = { tab = 1 },
                    ) { Icon(name = SchemaIconName.Search, tint = SchemaColor.Primary) }
                    Tab(
                        selected = tab == 2,
                        text = "Profile",
                        onClick = { tab = 2 },
                    ) { Icon(name = SchemaIconName.Person, tint = SchemaColor.Primary) }
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }
            // NavigationBar
            LazyItem {
                var nav by remember { mutableStateOf(0) }
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    NavigationBarItem(
                        selected = nav == 0,
                        label = "Home",
                        onClick = { nav = 0 },
                    ) { Icon(name = SchemaIconName.Home, tint = SchemaColor.OnSurface) }
                    NavigationBarItem(
                        selected = nav == 1,
                        label = "Inbox",
                        onClick = { nav = 1 },
                    ) { Icon(name = SchemaIconName.Email, tint = SchemaColor.OnSurface) }
                    NavigationBarItem(
                        selected = nav == 2,
                        label = "Settings",
                        onClick = { nav = 2 },
                    ) { Icon(name = SchemaIconName.Settings, tint = SchemaColor.OnSurface) }
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Misc — Dividers (Batch 2.7) ---
            LazyItem {
                Text(
                    text = "Dividers",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            // HorizontalDivider — default thickness (1dp), default color
            LazyItem {
                HorizontalDivider(
                    thicknessDp = 1,
                    color = SchemaColor.OutlineVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 12) }
            // Thicker accent divider
            LazyItem {
                HorizontalDivider(
                    thicknessDp = 4,
                    color = SchemaColor.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyItem { Spacer(width = 0, height = 12) }
            // VerticalDividers in a Row
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(48),
                ) {
                    Text(
                        text = "Left",
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                    )
                    VerticalDivider(
                        thicknessDp = 1,
                        color = SchemaColor.OutlineVariant,
                        modifier = Modifier.height(32),
                    )
                    Text(
                        text = "Center",
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                    )
                    VerticalDivider(
                        thicknessDp = 1,
                        color = SchemaColor.OutlineVariant,
                        modifier = Modifier.height(32),
                    )
                    Text(
                        text = "Right",
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                    )
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Tier 3 — Chips (Batch 3.0) ---
            LazyItem {
                Text(
                    text = "Chips (Tier 3)",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }

            // FilterChip row — each chip toggles independently. M3
            // auto-renders a check glyph when selected, so we omit the
            // leading icon in the selected branch (matches the host's
            // hasIcon && !selected gate in CmpFilterChip).
            LazyItem {
                var fav by remember { mutableStateOf(false) }
                var price by remember { mutableStateOf(true) }
                var open by remember { mutableStateOf(false) }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FilterChip(
                        selected = fav,
                        label = "Favorites",
                        enabled = true,
                        onClick = { fav = !fav },
                    ) { Icon(name = SchemaIconName.Favorite, tint = SchemaColor.OnSurface) }
                    FilterChip(
                        selected = price,
                        label = "Under \$50",
                        enabled = true,
                        onClick = { price = !price },
                    ) { /* no leading icon */ }
                    FilterChip(
                        selected = open,
                        label = "Open now",
                        enabled = true,
                        onClick = { open = !open },
                    ) { /* no leading icon */ }
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }

            // AssistChip row — non-toggleable action shortcuts. Two with
            // a leading icon, one without, plus a disabled state.
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AssistChip(
                        label = "Edit",
                        enabled = true,
                        onClick = null,
                    ) { Icon(name = SchemaIconName.Edit, tint = SchemaColor.OnSurface) }
                    AssistChip(
                        label = "Share",
                        enabled = true,
                        onClick = null,
                    ) { /* no leading icon */ }
                    AssistChip(
                        label = "Disabled",
                        enabled = false,
                        onClick = null,
                    ) { Icon(name = SchemaIconName.Lock, tint = SchemaColor.OnSurface) }
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }

            // InputChip row — exercises onClose dismissal. Tapping the
            // trailing X removes the chip from the list, demonstrating
            // that the host correctly hides the X when onClose is null
            // (the third chip below has no close affordance).
            LazyItem {
                var tokens by remember {
                    mutableStateOf(listOf("alice@example.com", "bob@example.com", "carol@example.com"))
                }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    tokens.forEachIndexed { idx, email ->
                        InputChip(
                            selected = false,
                            label = email,
                            enabled = true,
                            onClick = null,
                            // Last chip is "pinned" — no close affordance.
                            onClose = if (idx < tokens.lastIndex) {
                                { tokens = tokens.toMutableList().also { it.removeAt(idx) } }
                            } else null,
                        ) { Icon(name = SchemaIconName.Person, tint = SchemaColor.OnSurface) }
                    }
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }

            // SuggestionChip row — hint-style affordances. Real apps wire
            // onClick to populate a query / shortcut a flow; here we just
            // show the visual.
            LazyItem {
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceEvenly,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SuggestionChip(
                        label = "Recent",
                        enabled = true,
                        onClick = null,
                    ) { /* no leading icon */ }
                    SuggestionChip(
                        label = "Trending",
                        enabled = true,
                        onClick = null,
                    ) { Icon(name = SchemaIconName.Star, tint = SchemaColor.OnSurface) }
                    SuggestionChip(
                        label = "Nearby",
                        enabled = true,
                        onClick = null,
                    ) { /* no leading icon */ }
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Tier 3 — List + Menus (Batch 3.1) ---
            LazyItem {
                Text(
                    text = "ListItem + DropdownMenu (Tier 3)",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }

            // Three-line ListItem with leading avatar + trailing icon —
            // the densest M3 list shape.
            LazyItem {
                ListItem(
                    headline = "Alice Carter",
                    supporting = "alice@example.com · Online",
                    overline = "Recent",
                    enabled = true,
                    onClick = null,
                    leadingContent = {
                        Icon(name = SchemaIconName.Person, tint = SchemaColor.Primary)
                    },
                    trailingContent = {
                        Icon(name = SchemaIconName.ArrowForward, tint = SchemaColor.OnSurfaceVariant)
                    },
                )
            }
            // Two-line ListItem (no overline).
            LazyItem {
                ListItem(
                    headline = "Bob Diaz",
                    supporting = "bob@example.com",
                    overline = "",
                    enabled = true,
                    onClick = null,
                    leadingContent = {
                        Icon(name = SchemaIconName.Person, tint = SchemaColor.Tertiary)
                    },
                    trailingContent = { /* none */ },
                )
            }
            // Single-line ListItem — no supporting/overline, no leading.
            // Trailing-only content (e.g. a chevron) keeps the row tappable
            // looking even though we don't wire onClick here.
            LazyItem {
                ListItem(
                    headline = "Single-line row",
                    supporting = "",
                    overline = "",
                    enabled = true,
                    onClick = null,
                    leadingContent = { /* none */ },
                    trailingContent = {
                        Icon(name = SchemaIconName.ArrowForward, tint = SchemaColor.OnSurfaceVariant)
                    },
                )
            }
            // Disabled clickable ListItem — the host wires clickable only
            // when both onClick != null AND enabled, so this shows the
            // "looks tappable but isn't" path. (We pass a no-op onClick to
            // exercise the disabled-click gate; tap should do nothing.)
            LazyItem {
                ListItem(
                    headline = "Disabled row (won't fire)",
                    supporting = "Locked / not yet available",
                    overline = "",
                    enabled = false,
                    onClick = { /* should never fire because enabled=false */ },
                    leadingContent = {
                        Icon(name = SchemaIconName.Lock, tint = SchemaColor.OnSurfaceVariant)
                    },
                    trailingContent = { /* none */ },
                )
            }
            LazyItem { Spacer(width = 0, height = 12) }

            // DropdownMenu — typical anchoring pattern: wrap the trigger
            // (IconButton) and the menu in a Box. The popup positions
            // itself relative to the Box's coordinates.
            LazyItem {
                var expanded by remember { mutableStateOf(false) }
                var lastChoice by remember { mutableStateOf("(no menu choice yet)") }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceBetween,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(0, 8, 0, 8),
                ) {
                    Text(
                        text = lastChoice,
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                    )
                    Box(onClick = null) {
                        IconButton(enabled = true, onClick = { expanded = true }) {
                            Icon(name = SchemaIconName.Menu, tint = SchemaColor.OnSurface)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            DropdownMenuItem(
                                text = "Edit",
                                enabled = true,
                                onClick = {
                                    lastChoice = "Picked: Edit"
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(name = SchemaIconName.Edit, tint = SchemaColor.OnSurface)
                                },
                                trailingIcon = { /* none */ },
                            )
                            DropdownMenuItem(
                                text = "Share",
                                enabled = true,
                                onClick = {
                                    lastChoice = "Picked: Share"
                                    expanded = false
                                },
                                leadingIcon = { /* none */ },
                                trailingIcon = { /* none */ },
                            )
                            DropdownMenuItem(
                                text = "Delete",
                                enabled = false,
                                onClick = {
                                    // Disabled — won't fire even if reached.
                                    lastChoice = "Picked: Delete"
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(name = SchemaIconName.Delete, tint = SchemaColor.Error)
                                },
                                trailingIcon = { /* none */ },
                            )
                        }
                    }
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Tier 3 — Overlays (Batch 3.2) ---
            // ModalBottomSheet + AlertDialog. Both are CONDITIONALLY
            // composed: the guest holds a Boolean and only emits the
            // overlay widget when visible. M3 runs the dismiss animation
            // internally before firing onDismissRequest, so cutting the
            // widget on dismiss doesn't truncate the animation.
            LazyItem {
                Text(
                    text = "Overlays (Tier 3)",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }

            // ModalBottomSheet trigger + last-action mirror.
            LazyItem {
                var showSheet by remember { mutableStateOf(false) }
                var lastSheetAction by remember { mutableStateOf("(no sheet action yet)") }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceBetween,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(0, 4, 0, 4),
                ) {
                    Text(
                        text = lastSheetAction,
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                    )
                    Button(
                        text = "Show sheet",
                        enabled = true,
                        onClick = { showSheet = true },
                    )
                }
                if (showSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            // Fired AFTER M3's hide animation runs. Safe
                            // to remove the widget from the tree here.
                            lastSheetAction = "Sheet dismissed (scrim/back/swipe)"
                            showSheet = false
                        },
                        // skipPartiallyExpanded = true → opens fully,
                        // no half-state. Switch to false for content
                        // that should peek (media / map sheets).
                        skipPartiallyExpanded = true,
                    ) {
                        Column(
                            verticalArrangement = SchemaArrangement.Start,
                            horizontalAlignment = SchemaHorizontalAlignment.Start,
                            modifier = Modifier.fillMaxWidth().padding(24, 16, 24, 32),
                        ) {
                            Text(
                                text = "Bottom sheet",
                                color = SchemaColor.OnSurface,
                                style = SchemaTextStyle.TitleLarge,
                            )
                            Spacer(width = 0, height = 8)
                            Text(
                                text = "Sheet content rendered inside M3's ColumnScope. Tap a button or swipe down / tap the scrim / press back to dismiss.",
                                color = SchemaColor.OnSurfaceVariant,
                                style = SchemaTextStyle.BodyMedium,
                            )
                            Spacer(width = 0, height = 16)
                            Row(
                                horizontalArrangement = SchemaArrangement.SpaceBetween,
                                verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                TextButton(
                                    text = "Cancel",
                                    enabled = true,
                                    onClick = {
                                        lastSheetAction = "Sheet: Cancel"
                                        showSheet = false
                                    },
                                )
                                Button(
                                    text = "Save",
                                    enabled = true,
                                    onClick = {
                                        lastSheetAction = "Sheet: Save"
                                        showSheet = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
            LazyItem { Spacer(width = 0, height = 12) }

            // AlertDialog trigger + last-action mirror.
            LazyItem {
                var showDialog by remember { mutableStateOf(false) }
                var lastDialogAction by remember { mutableStateOf("(no dialog action yet)") }
                Row(
                    horizontalArrangement = SchemaArrangement.SpaceBetween,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(0, 4, 0, 4),
                ) {
                    Text(
                        text = lastDialogAction,
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.BodyMedium,
                    )
                    Button(
                        text = "Show dialog",
                        enabled = true,
                        onClick = { showDialog = true },
                    )
                }
                if (showDialog) {
                    AlertDialog(
                        title = "Confirm action",
                        text = "Are you sure you want to delete this item? This cannot be undone.",
                        onDismissRequest = {
                            lastDialogAction = "Dialog dismissed (scrim/back)"
                            showDialog = false
                        },
                        icon = {
                            Icon(name = SchemaIconName.Warning, tint = SchemaColor.Error)
                        },
                        confirmButton = {
                            TextButton(
                                text = "Delete",
                                enabled = true,
                                onClick = {
                                    lastDialogAction = "Dialog: Delete confirmed"
                                    showDialog = false
                                },
                            )
                        },
                        dismissButton = {
                            TextButton(
                                text = "Cancel",
                                enabled = true,
                                onClick = {
                                    lastDialogAction = "Dialog: Cancel"
                                    showDialog = false
                                },
                            )
                        },
                    )
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Tier 3 — Pagers (Batch 3.3) ---
            LazyItem {
                Text(
                    text = "Pagers (Tier 3)",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }

            // HorizontalPager — three pages, each a colored card with
            // a label. PagerIndicator below mirrors the current page.
            // The guest holds currentPage in `remember`; onPageChanged
            // updates it after each fling settles.
            LazyItem {
                val pageColors = listOf(
                    SchemaColor.PrimaryContainer,
                    SchemaColor.SecondaryContainer,
                    SchemaColor.Tertiary,
                )
                val pageOnColors = listOf(
                    SchemaColor.OnPrimaryContainer,
                    SchemaColor.OnSecondaryContainer,
                    SchemaColor.OnTertiary,
                )
                var currentPage by remember { mutableStateOf(0) }
                Column(
                    verticalArrangement = SchemaArrangement.Start,
                    horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HorizontalPager(
                        initialPage = 0,
                        onPageChanged = { newPage -> currentPage = newPage },
                        userScrollEnabled = true,
                        pageSpacingDp = 12,
                        modifier = Modifier.fillMaxWidth().height(160),
                    ) {
                        // Three pages — each a colored Box with a label.
                        for (i in 0 until 3) {
                            Box(
                                onClick = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(pageColors[i])
                                    .padding(16, 16, 16, 16),
                            ) {
                                Text(
                                    text = "Page ${i + 1} of 3",
                                    color = pageOnColors[i],
                                    style = SchemaTextStyle.HeadlineSmall,
                                )
                            }
                        }
                    }
                    Spacer(width = 0, height = 12)
                    PagerIndicator(
                        pageCount = 3,
                        currentPage = currentPage,
                        activeColor = SchemaColor.Primary,
                        inactiveColor = SchemaColor.OutlineVariant,
                    )
                }
            }
            LazyItem { Spacer(width = 0, height = 16) }

            // VerticalPager — two pages, swipe up/down. Smaller height
            // since vertical pages eat screen space; this is more of a
            // "demonstrates the API exists" item than a rich demo.
            LazyItem {
                Text(
                    text = "Vertical pager (swipe up/down)",
                    color = SchemaColor.OnSurfaceVariant,
                    style = SchemaTextStyle.BodySmall,
                )
            }
            LazyItem { Spacer(width = 0, height = 4) }
            LazyItem {
                var currentVPage by remember { mutableStateOf(0) }
                Column(
                    verticalArrangement = SchemaArrangement.Start,
                    horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    VerticalPager(
                        initialPage = 0,
                        onPageChanged = { newPage -> currentVPage = newPage },
                        userScrollEnabled = true,
                        pageSpacingDp = 8,
                        modifier = Modifier.fillMaxWidth().height(120),
                    ) {
                        Box(
                            onClick = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SchemaColor.SurfaceVariant)
                                .padding(16, 16, 16, 16),
                        ) {
                            Text(
                                text = "Top page — swipe up",
                                color = SchemaColor.OnSurfaceVariant,
                                style = SchemaTextStyle.BodyMedium,
                            )
                        }
                        Box(
                            onClick = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SchemaColor.PrimaryContainer)
                                .padding(16, 16, 16, 16),
                        ) {
                            Text(
                                text = "Bottom page — swipe down",
                                color = SchemaColor.OnPrimaryContainer,
                                style = SchemaTextStyle.BodyMedium,
                            )
                        }
                    }
                    Spacer(width = 0, height = 8)
                    PagerIndicator(
                        pageCount = 2,
                        currentPage = currentVPage,
                        activeColor = SchemaColor.Primary,
                        inactiveColor = SchemaColor.OutlineVariant,
                    )
                }
            }

            LazyItem { Spacer(width = 0, height = 24) }

            // --- Tier 3 — PullToRefresh (Batch 3.4) ---
            LazyItem {
                Text(
                    text = "PullToRefresh (Tier 3)",
                    color = SchemaColor.OnSurface,
                    style = SchemaTextStyle.TitleMedium,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                Text(
                    text = "Pull the colored panel below downward to refresh. Spinner runs for 1.2s, then settles.",
                    color = SchemaColor.OnSurfaceVariant,
                    style = SchemaTextStyle.BodySmall,
                )
            }
            LazyItem { Spacer(width = 0, height = 8) }
            LazyItem {
                var refreshing by remember { mutableStateOf(false) }
                var refreshCount by remember { mutableStateOf(0) }
                // Simulated async work: when the user pulls, we flip
                // refreshing=true, then a LaunchedEffect keyed on the
                // count waits 1.2s and flips it back. Real apps would
                // kick off a network call instead.
                LaunchedEffect(refreshCount) {
                    if (refreshCount > 0) {
                        delay(1200)
                        refreshing = false
                    }
                }
                PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = {
                        refreshing = true
                        refreshCount += 1
                    },
                    modifier = Modifier.fillMaxWidth().height(200),
                ) {
                    Column(
                        verticalArrangement = SchemaArrangement.Start,
                        horizontalAlignment = SchemaHorizontalAlignment.Start,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SchemaColor.SecondaryContainer)
                            .padding(16, 16, 16, 16),
                    ) {
                        Text(
                            text = "Refreshes triggered: $refreshCount",
                            color = SchemaColor.OnSecondaryContainer,
                            style = SchemaTextStyle.TitleMedium,
                        )
                        Spacer(width = 0, height = 8)
                        Text(
                            text = if (refreshing) "Refreshing…" else "Idle. Pull down to refresh.",
                            color = SchemaColor.OnSecondaryContainer,
                            style = SchemaTextStyle.BodyMedium,
                        )
                    }
                }
            }

            LazyItem { Spacer(width = 0, height = 32) }
        }
    }
}
