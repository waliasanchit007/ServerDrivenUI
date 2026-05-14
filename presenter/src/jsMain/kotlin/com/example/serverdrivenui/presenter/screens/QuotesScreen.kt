package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.HostQuotesProviderBridge
import com.example.serverdrivenui.presenter.HostQuoteNavigatorBridge
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.shared.Quote
import com.example.serverdrivenui.schema.SchemaArrangement
import com.example.serverdrivenui.schema.SchemaColor
import com.example.serverdrivenui.schema.SchemaHorizontalAlignment
import com.example.serverdrivenui.schema.SchemaIconName
import com.example.serverdrivenui.schema.SchemaTextStyle
import com.example.serverdrivenui.schema.SchemaVerticalAlignment
import com.example.serverdrivenui.schema.compose.Box
import com.example.serverdrivenui.schema.compose.Card
import com.example.serverdrivenui.schema.compose.CircularProgressIndicator
import com.example.serverdrivenui.schema.compose.Column
import com.example.serverdrivenui.schema.compose.FilterChip
import com.example.serverdrivenui.schema.compose.Icon
import com.example.serverdrivenui.schema.compose.LazyColumn
import com.example.serverdrivenui.schema.compose.LazyItem
import com.example.serverdrivenui.schema.compose.LazyRow
import com.example.serverdrivenui.schema.compose.Row
import com.example.serverdrivenui.schema.compose.Text
import com.example.serverdrivenui.schema.compose.fillMaxSize
import com.example.serverdrivenui.schema.compose.fillMaxWidth
import com.example.serverdrivenui.schema.compose.height
import com.example.serverdrivenui.schema.compose.padding
import com.example.serverdrivenui.schema.compose.size
import dev.konduit.Modifier

/**
 * Server-driven version of DevoStatus's `QuotesScreen`.
 *
 * Data is provided by the host through [HostQuotesProviderBridge.instance]
 * (a `HostQuotesProvider` ZiplineService). Selection callbacks go back
 * to the host via [HostQuoteNavigatorBridge.instance] (a `HostQuoteNavigator`
 * service) — the host typically responds by navigating to its own
 * creation flow.
 *
 * The presenter routes to this screen automatically when the host has
 * bound a HostQuotesProvider (see [com.example.serverdrivenui.presenter
 * .RootUi]).
 */
class QuotesScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        val provider = HostQuotesProviderBridge.instance
        val navService = HostQuoteNavigatorBridge.instance

        var selectedFilter by remember { mutableStateOf<String?>(null) }
        var quotes by remember { mutableStateOf<List<Quote>?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(selectedFilter, provider) {
            if (provider == null) {
                error = "HostQuotesProvider not bound by host"
                return@LaunchedEffect
            }
            error = null
            try {
                // Non-suspend: see comment on HostQuotesProvider.
                quotes = provider.getQuotes(selectedFilter)
            } catch (t: Throwable) {
                error = "Failed to load quotes: ${t.message}"
            }
        }

        Column(
            verticalArrangement = SchemaArrangement.Start,
            horizontalAlignment = SchemaHorizontalAlignment.Start,
            modifier = Modifier.fillMaxSize(),
        ) {
            // ─── Header ──────────────────────────────────────────────
            Row(
                horizontalArrangement = SchemaArrangement.Center,
                verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16, 16, 16, 12),
            ) {
                Text(
                    text = "Select Quote",
                    style = SchemaTextStyle.TitleLarge,
                    color = SchemaColor.Primary,
                )
            }

            // ─── Language filter chips ───────────────────────────────
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(8, 0, 8, 8),
            ) {
                listOf<Pair<String, String?>>(
                    "All" to null,
                    "Hindi" to "hi",
                    "English" to "en",
                    "Sanskrit" to "sa",
                ).forEach { (label, value) ->
                    LazyItem {
                        Box(
                            onClick = null,
                            modifier = Modifier.padding(4, 0, 4, 0),
                        ) {
                            FilterChip(
                                selected = selectedFilter == value,
                                label = label,
                                enabled = true,
                                onClick = { selectedFilter = value },
                            ) {
                                if (selectedFilter == value) {
                                    Icon(
                                        name = SchemaIconName.Check,
                                        tint = SchemaColor.OnPrimary,
                                        modifier = Modifier.size(16, 16),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── Content: loading / error / list ─────────────────────
            val errorSnap = error
            val quotesSnap = quotes
            when {
                errorSnap != null -> {
                    Column(
                        verticalArrangement = SchemaArrangement.Center,
                        horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(24, 24, 24, 24),
                    ) {
                        Text(
                            text = errorSnap,
                            style = SchemaTextStyle.BodyMedium,
                            color = SchemaColor.Error,
                        )
                    }
                }
                quotesSnap == null -> {
                    Column(
                        verticalArrangement = SchemaArrangement.Center,
                        horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(32, 32, 32, 32),
                    ) {
                        CircularProgressIndicator(
                            progress = 0f,
                            indeterminate = true,
                        )
                    }
                }
                quotesSnap.isEmpty() -> {
                    Column(
                        verticalArrangement = SchemaArrangement.Center,
                        horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(32, 32, 32, 32),
                    ) {
                        Text(
                            text = "No quotes for this language.",
                            style = SchemaTextStyle.BodyMedium,
                            color = SchemaColor.OnSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16, 8, 16, 16),
                    ) {
                        quotesSnap.forEach { q ->
                            LazyItem {
                                Box(
                                    onClick = null,
                                    modifier = Modifier.padding(0, 8, 0, 8),
                                ) {
                                    QuoteCard(quote = q) {
                                        navService?.onQuoteSelected(q.id, q.tag)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single quote card. Visually matches DevoStatus's original
 * `FeedQuoteCard` Compose code: rounded-corner card, decorative
 * FormatQuote icon top-right, centered quote text, and a "Tap to
 * create status" action row at the bottom.
 */
@Composable
private fun QuoteCard(quote: Quote, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = SchemaArrangement.Start,
            horizontalAlignment = SchemaHorizontalAlignment.Start,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                onClick = null,
                modifier = Modifier.fillMaxWidth().padding(24, 24, 24, 24),
            ) {
                // Decorative quote icon — corner accent.
                Row(
                    horizontalArrangement = SchemaArrangement.End,
                    verticalAlignment = SchemaVerticalAlignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        name = SchemaIconName.FormatQuote,
                        tint = SchemaColor.Tertiary,
                        modifier = Modifier.size(48, 48),
                    )
                }

                Column(
                    verticalArrangement = SchemaArrangement.Center,
                    horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(0, 8, 0, 0),
                ) {
                    Text(
                        text = "\"${quote.text}\"",
                        style = SchemaTextStyle.HeadlineSmall,
                        color = SchemaColor.Primary,
                    )
                }
            }

            // Action bar
            Row(
                horizontalArrangement = SchemaArrangement.Center,
                verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(40).padding(0, 8, 0, 8),
            ) {
                Icon(
                    name = SchemaIconName.Brush,
                    tint = SchemaColor.Secondary,
                    modifier = Modifier.size(16, 16),
                )
                Box(
                    onClick = null,
                    modifier = Modifier.size(6, 0),
                ) {}
                Text(
                    text = "Tap to create status",
                    style = SchemaTextStyle.LabelMedium,
                    color = SchemaColor.Secondary,
                )
            }
        }
    }
}
