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
import com.example.serverdrivenui.shared.HostQuotesObserver
import com.example.serverdrivenui.shared.Quote
import com.example.serverdrivenui.schema.SchemaArrangement
import com.example.serverdrivenui.schema.SchemaBoxAlignment
import com.example.serverdrivenui.schema.SchemaColor
import com.example.serverdrivenui.schema.SchemaFontFamily
import com.example.serverdrivenui.schema.SchemaFontWeight
import com.example.serverdrivenui.schema.SchemaHorizontalAlignment
import com.example.serverdrivenui.schema.SchemaIconName
import com.example.serverdrivenui.schema.SchemaTextAlign
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
import com.example.serverdrivenui.schema.compose.alpha
import com.example.serverdrivenui.schema.compose.background
import com.example.serverdrivenui.schema.compose.border
import com.example.serverdrivenui.schema.compose.clip
import com.example.serverdrivenui.schema.compose.fillMaxSize
import com.example.serverdrivenui.schema.compose.fillMaxWidth
import com.example.serverdrivenui.schema.compose.height
import com.example.serverdrivenui.schema.compose.offset
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
        // Bumped by the host-side observer; participates in the
        // getQuotes() LaunchedEffect's key list so a change-notification
        // forces a re-fetch with the current language filter.
        var refreshTick by remember { mutableStateOf(0) }

        // Subscribe to host change-notifications exactly once per provider.
        // Wrapped in try/catch so older hosts (no observe() impl) gracefully
        // degrade to snapshot mode rather than crashing.
        LaunchedEffect(provider) {
            if (provider == null) return@LaunchedEffect
            val observer = object : HostQuotesObserver {
                override fun onQuotesChanged() {
                    refreshTick++
                }
            }
            try {
                provider.observe(observer)
            } catch (t: Throwable) {
                // Old host without observe() — snapshot-only mode is fine.
                println("Konduit-Guest: HostQuotesProvider.observe() unavailable: ${t.message}")
            }
        }

        LaunchedEffect(selectedFilter, refreshTick, provider) {
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
                                // Brand the selection state to match
                                // DevoStatus's native chip: saffron
                                // container with maroon label, saffron
                                // border in both states.
                                selectedContainerColor = SchemaColor.Tertiary,
                                selectedLabelColor = SchemaColor.Primary,
                                borderColor = SchemaColor.Tertiary,
                                selectedBorderColor = SchemaColor.Tertiary,
                                // Pill shape — native used CircleShape on
                                // the M3 32dp-tall chip, so a corner
                                // radius >= height/2 collapses to a pill.
                                // 50dp comfortably overshoots.
                                cornerRadiusDp = 50,
                            ) {
                                if (selectedFilter == value) {
                                    // Leading check on selected chip,
                                    // tinted with the same SchemaColor
                                    // the M3 chip uses for its label so
                                    // the icon contrasts against the
                                    // saffron fill the same way the text
                                    // does. The host now always renders
                                    // guest-supplied leadingIcons (no
                                    // longer suppressed when selected),
                                    // so this glyph actually paints.
                                    Icon(
                                        name = SchemaIconName.Check,
                                        tint = SchemaColor.Primary,
                                        modifier = Modifier.size(18, 18),
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
 * Single quote card — full visual parity with DevoStatus's original
 * native `FeedQuoteCard`:
 *
 *   ┌──────────────────────────────────────────┐
 *   │ rounded white card, faint tertiary border │
 *   │   ┌─────────────── faded "  ┐ overhang ──┤
 *   │   │            watermark   │              │
 *   │   │   "Quote text in serif bold maroon,   │
 *   │   │              centered."               │
 *   │   └───────────────────────┘              │
 *   ├───────────────────────────────────────────┤
 *   │ ░  brush   Tap to create status   tinted ░│
 *   └───────────────────────────────────────────┘
 *
 * Uses the schema features added in upstream commit b62d366:
 *  - Modifier.alpha(0.1)        — watermark fade
 *  - Modifier.offset(12, -12)   — corner overhang
 *  - Modifier.border(...)       — rounded saffron stroke
 *  - Modifier.background(c, r, a) — tinted action-bar fill
 *  - Box(contentAlignment=TopEnd) — watermark anchor
 *  - Text(textAlign+fontWeight+fontFamily) — serif bold center
 */
@Composable
private fun QuoteCard(quote: Quote, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        // Explicitly request a clean white container — M3's default
        // surfaceContainerHighest gives a pale lavender tint in DevoStatus's
        // theme, which doesn't match native's `Color.White`. Background
        // slot is conventionally the integrator's "true white" in M3.
        containerColor = SchemaColor.Background,
        contentColor = SchemaColor.OnBackground,
        modifier = Modifier
            .fillMaxWidth()
            // Rounded saffron-tinted outline matching native:
            // `BorderStroke(1.dp, Saffron.copy(alpha=0.2f))`. SchemaColor.Tertiary
            // is the slot the integrator's MaterialTheme maps to Saffron.
            .border(
                thicknessDp = 1,
                color = SchemaColor.Tertiary,
                cornerRadiusDp = 16,
            )
            // Clip rounds the entire card content so the watermark
            // overhang is clipped to the card edge instead of bleeding
            // outside the M3 Card's shape.
            .clip(cornerRadiusDp = 16),
    ) {
        Column(
            verticalArrangement = SchemaArrangement.Start,
            horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Top section: watermark + centered quote text in a single
            // Box, mirroring the native FeedQuoteCard's layered Box.
            Box(
                onClick = null,
                contentAlignment = SchemaBoxAlignment.TopEnd,
                modifier = Modifier.fillMaxWidth().padding(24, 24, 24, 24),
            ) {
                // Decorative watermark icon — TopEnd anchored, 64dp,
                // saffron @ 10% alpha, nudged out 12dp toward the
                // corner for the overhang look.
                Icon(
                    name = SchemaIconName.FormatQuote,
                    tint = SchemaColor.Tertiary,
                    modifier = Modifier
                        .size(64, 64)
                        .alpha(0.1)
                        .offset(x = 12, y = -12),
                )
                // Centered quote text. Serif bold maroon, exact match
                // with the native FeedQuoteCard's headlineSmall.copy(...).
                Column(
                    verticalArrangement = SchemaArrangement.Center,
                    horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "\"${quote.text}\"",
                        style = SchemaTextStyle.HeadlineSmall,
                        color = SchemaColor.Primary,
                        textAlign = SchemaTextAlign.Center,
                        fontWeight = SchemaFontWeight.Bold,
                        fontFamily = SchemaFontFamily.Serif,
                    )
                }
            }

            // Action bar — saffron tint matching native's
            // `Saffron.copy(alpha = 0.1f)`. Pinned to 40dp tall so it
            // anchors the card's bottom edge (otherwise the Card's
            // intrinsic height ends at the Row's content + padding,
            // which leaves a thin white sliver visible between the
            // action bar and the card's rounded-bottom border in some
            // resolutions). 0.12 nudges saturation up slightly so the
            // tint reads as "intentional" rather than a render artifact
            // at common pixel densities.
            Row(
                horizontalArrangement = SchemaArrangement.Center,
                verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40)
                    .background(
                        color = SchemaColor.Tertiary,
                        alpha = 0.12,
                    ),
            ) {
                Icon(
                    name = SchemaIconName.Brush,
                    tint = SchemaColor.Tertiary,
                    modifier = Modifier.size(16, 16),
                )
                Box(
                    onClick = null,
                    modifier = Modifier.size(4, 0),
                ) {}
                Text(
                    text = "Tap to create status",
                    style = SchemaTextStyle.LabelSmall,
                    color = SchemaColor.Tertiary,
                    fontWeight = SchemaFontWeight.Bold,
                )
            }
        }
    }
}
