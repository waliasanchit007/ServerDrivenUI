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
import com.example.serverdrivenui.schema.compose.BackHandler
import com.example.serverdrivenui.schema.compose.Box
import com.example.serverdrivenui.schema.compose.Button
import com.example.serverdrivenui.schema.compose.Column
import com.example.serverdrivenui.schema.compose.HorizontalDivider
import com.example.serverdrivenui.schema.compose.Icon
import com.example.serverdrivenui.schema.compose.IconButton
import com.example.serverdrivenui.schema.compose.ModalNavigationDrawer
import com.example.serverdrivenui.schema.compose.NavigationDrawerItem
import com.example.serverdrivenui.schema.compose.Row
import com.example.serverdrivenui.schema.compose.Spacer
import com.example.serverdrivenui.schema.compose.Text
import com.example.serverdrivenui.schema.compose.background
import com.example.serverdrivenui.schema.compose.fillMaxSize
import com.example.serverdrivenui.schema.compose.fillMaxWidth
import com.example.serverdrivenui.schema.compose.padding
import dev.konduit.Modifier

/**
 * Standalone demo for ModalNavigationDrawer (Tier 3 Batch 3.5).
 *
 * Why a separate Screen rather than a section in [Tier1ShowcaseScreen]:
 * ModalNavigationDrawer wraps the WHOLE app surface, not a region of
 * a scroll container. Putting it inside a LazyItem would try to anchor
 * the drawer to the item's edge, not the screen's edge — visually
 * broken. Pushing a dedicated screen via the Navigator gives the
 * drawer a real top-level surface to anchor to.
 *
 * Demonstrates the controlled-component drawer pattern: the guest holds
 * `drawerOpen` state, flips it from a button or NavigationDrawerItem
 * onClick, and mirrors host-driven gesture changes via
 * `onDrawerStateChange`.
 */
class NavDrawerDemoScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        // BackHandler so the system back button pops the demo screen
        // instead of exiting the app — and closes the drawer first if
        // it's open (parity with M3's typical drawer back-handling).
        var drawerOpen by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf("Home") }

        BackHandler(enabled = true, onBack = {
            if (drawerOpen) drawerOpen = false else navigator.pop()
        })

        ModalNavigationDrawer(
            drawerOpen = drawerOpen,
            onDrawerStateChange = { open ->
                // Host fires this on user gestures (swipe to open/close,
                // tap scrim). Keep our state in sync.
                drawerOpen = open
            },
            gesturesEnabled = true,
            modifier = Modifier.fillMaxSize(),
            drawerContent = {
                // Host wraps this in M3's ModalDrawerSheet automatically.
                // We just emit the items directly.
                Column(
                    verticalArrangement = SchemaArrangement.Start,
                    horizontalAlignment = SchemaHorizontalAlignment.Start,
                    modifier = Modifier.fillMaxWidth().padding(0, 24, 0, 0),
                ) {
                    Text(
                        text = "Caliclan",
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.HeadlineSmall,
                        modifier = Modifier.padding(28, 8, 28, 16),
                    )
                    HorizontalDivider(
                        thicknessDp = 1,
                        color = SchemaColor.OutlineVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(width = 0, height = 12)
                    NavigationDrawerItem(
                        selected = selectedItem == "Home",
                        label = "Home",
                        onClick = {
                            selectedItem = "Home"
                            drawerOpen = false
                        },
                        icon = {
                            Icon(name = SchemaIconName.Home, tint = SchemaColor.OnSurface)
                        },
                        badge = { /* none */ },
                        modifier = Modifier.padding(12, 0, 12, 0),
                    )
                    NavigationDrawerItem(
                        selected = selectedItem == "Inbox",
                        label = "Inbox",
                        onClick = {
                            selectedItem = "Inbox"
                            drawerOpen = false
                        },
                        icon = {
                            Icon(name = SchemaIconName.Email, tint = SchemaColor.OnSurface)
                        },
                        // Badge shows an unread count; uses the existing
                        // Badge widget (Tier 2). Demonstrates that the
                        // badge slot accepts any composable.
                        badge = {
                            Text(
                                text = "12",
                                color = SchemaColor.OnSurface,
                                style = SchemaTextStyle.LabelSmall,
                            )
                        },
                        modifier = Modifier.padding(12, 0, 12, 0),
                    )
                    NavigationDrawerItem(
                        selected = selectedItem == "Settings",
                        label = "Settings",
                        onClick = {
                            selectedItem = "Settings"
                            drawerOpen = false
                        },
                        icon = {
                            Icon(name = SchemaIconName.Settings, tint = SchemaColor.OnSurface)
                        },
                        badge = { /* none */ },
                        modifier = Modifier.padding(12, 0, 12, 0),
                    )
                }
            },
        ) {
            // Main content behind the drawer. Tap menu icon to open;
            // tap scrim or swipe drawer left to close. Back button
            // also closes the drawer if open, otherwise pops the screen.
            Column(
                verticalArrangement = SchemaArrangement.Start,
                horizontalAlignment = SchemaHorizontalAlignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .background(SchemaColor.Background)
                    .padding(0, 16, 0, 16),
            ) {
                Row(
                    horizontalArrangement = SchemaArrangement.Start,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(8, 0, 16, 0),
                ) {
                    IconButton(enabled = true, onClick = { drawerOpen = true }) {
                        Icon(name = SchemaIconName.Menu, tint = SchemaColor.OnSurface)
                    }
                    Spacer(width = 8, height = 0)
                    Text(
                        text = "Drawer demo",
                        color = SchemaColor.OnSurface,
                        style = SchemaTextStyle.TitleLarge,
                    )
                }
                Spacer(width = 0, height = 16)
                Box(
                    onClick = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16, 0, 16, 0),
                ) {
                    Column(
                        verticalArrangement = SchemaArrangement.Start,
                        horizontalAlignment = SchemaHorizontalAlignment.Start,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Selected: $selectedItem",
                            color = SchemaColor.OnSurface,
                            style = SchemaTextStyle.TitleMedium,
                        )
                        Spacer(width = 0, height = 8)
                        Text(
                            text = "Open the drawer with the menu icon (or swipe from the left edge), pick an item — the drawer auto-closes via the controlled-component pattern (the item's onClick sets drawerOpen=false).",
                            color = SchemaColor.OnSurfaceVariant,
                            style = SchemaTextStyle.BodyMedium,
                        )
                        Spacer(width = 0, height = 24)
                        Button(
                            text = "Back to showcase",
                            enabled = true,
                            onClick = { navigator.pop() },
                        )
                    }
                }
            }
        }
    }
}
