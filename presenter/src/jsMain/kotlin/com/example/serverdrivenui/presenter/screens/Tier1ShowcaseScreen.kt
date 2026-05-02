package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
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
import com.example.serverdrivenui.schema.compose.Column
import com.example.serverdrivenui.schema.compose.Icon
import com.example.serverdrivenui.schema.compose.LazyColumn
import com.example.serverdrivenui.schema.compose.LazyItem
import com.example.serverdrivenui.schema.compose.LazyRow
import com.example.serverdrivenui.schema.compose.Row
import com.example.serverdrivenui.schema.compose.Spacer
import com.example.serverdrivenui.schema.compose.Text

/**
 * Demonstrates every Tier 1 widget end-to-end. Phase 3 verification gate
 * per docs/KONDUIT_PLAN.md §4.
 *
 * Layout (top to bottom):
 *   - Headline Text + body Text + spacer
 *   - Row of Icons (3 colors)
 *   - LazyRow of 12 horizontal "chip" Boxes
 *   - LazyColumn of 5 list-row Boxes (each Row of Icon + Text)
 *   - AsyncImage at the bottom
 */
class Tier1ShowcaseScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        Column(
            padding = 16,
            background = SchemaColor.Background,
            verticalArrangement = SchemaArrangement.Start,
            horizontalAlignment = SchemaHorizontalAlignment.Start,
            fillMaxSize = true,
        ) {
            // Title block
            Text(
                text = "Konduit Tier 1",
                color = SchemaColor.Primary,
                style = SchemaTextStyle.HeadlineMedium,
            )
            Spacer(width = 0, height = 4)
            Text(
                text = "10 foundation widgets, 0 legacy widgets.",
                color = SchemaColor.OnSurfaceVariant,
                style = SchemaTextStyle.BodyMedium,
            )
            Spacer(width = 0, height = 16)

            // Icon row — exercises Icon at three different tints
            Row(
                padding = 0,
                background = SchemaColor.Transparent,
                horizontalArrangement = SchemaArrangement.SpaceEvenly,
                verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                fillMaxWidth = true,
            ) {
                Icon(name = SchemaIconName.Home, tint = SchemaColor.Primary, size = 32)
                Icon(name = SchemaIconName.Star, tint = SchemaColor.Tertiary, size = 32)
                Icon(name = SchemaIconName.Favorite, tint = SchemaColor.Error, size = 32)
                Icon(name = SchemaIconName.Settings, tint = SchemaColor.OnSurfaceVariant, size = 32)
                Icon(name = SchemaIconName.Notifications, tint = SchemaColor.Secondary, size = 32)
            }
            Spacer(width = 0, height = 16)

            // Section label
            Text(
                text = "LazyRow",
                color = SchemaColor.OnSurface,
                style = SchemaTextStyle.TitleMedium,
            )
            Spacer(width = 0, height = 8)

            // LazyRow of "chip" boxes — exercises Box, LazyRow, LazyItem
            LazyRow(padding = 0, fillMaxWidth = true) {
                repeat(12) { index ->
                    LazyItem {
                        Box(
                            padding = 12,
                            background = SchemaColor.PrimaryContainer,
                            onClick = null,
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
            Spacer(width = 0, height = 16)

            // Section label
            Text(
                text = "LazyColumn",
                color = SchemaColor.OnSurface,
                style = SchemaTextStyle.TitleMedium,
            )
            Spacer(width = 0, height = 8)

            // LazyColumn of list rows — exercises LazyColumn, LazyItem, Row, Icon, Text
            LazyColumn(padding = 0, fillMaxSize = false) {
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
                            padding = 12,
                            background = SchemaColor.SurfaceVariant,
                            horizontalArrangement = SchemaArrangement.Start,
                            verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                            fillMaxWidth = true,
                        ) {
                            Icon(name = icon, tint = SchemaColor.OnSurfaceVariant, size = 24)
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
            }
            Spacer(width = 0, height = 16)

            // AsyncImage — exercises image fetch
            Text(
                text = "AsyncImage",
                color = SchemaColor.OnSurface,
                style = SchemaTextStyle.TitleMedium,
            )
            Spacer(width = 0, height = 8)
            AsyncImage(
                url = "https://picsum.photos/600/300",
                contentDescription = "Random landscape from picsum.photos",
                width = 0,
                height = 160,
            )
        }
    }
}
