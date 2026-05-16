package com.example.serverdrivenui.presenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.serverdrivenui.presenter.HostExploreNavigatorBridge
import com.example.serverdrivenui.presenter.HostQuotesProviderBridge
import com.example.serverdrivenui.presenter.HostWallpapersProviderBridge
import com.example.serverdrivenui.presenter.Navigator
import com.example.serverdrivenui.presenter.Screen
import com.example.serverdrivenui.shared.Quote
import com.example.serverdrivenui.shared.Wallpaper
import com.example.serverdrivenui.schema.SchemaArrangement
import com.example.serverdrivenui.schema.SchemaBoxAlignment
import com.example.serverdrivenui.schema.SchemaColor
import com.example.serverdrivenui.schema.SchemaFontWeight
import com.example.serverdrivenui.schema.SchemaHorizontalAlignment
import com.example.serverdrivenui.schema.SchemaIconName
import com.example.serverdrivenui.schema.SchemaTextAlign
import com.example.serverdrivenui.schema.SchemaTextStyle
import com.example.serverdrivenui.schema.SchemaVerticalAlignment
import com.example.serverdrivenui.schema.compose.AsyncImage
import com.example.serverdrivenui.schema.compose.Box
import com.example.serverdrivenui.schema.compose.Card
import com.example.serverdrivenui.schema.compose.Column
import com.example.serverdrivenui.schema.compose.Icon
import com.example.serverdrivenui.schema.compose.LazyItem
import com.example.serverdrivenui.schema.compose.LazyVerticalGrid
import com.example.serverdrivenui.schema.compose.Row
import com.example.serverdrivenui.schema.compose.SearchBar
import com.example.serverdrivenui.schema.compose.Spacer
import com.example.serverdrivenui.schema.compose.SuggestionChip
import com.example.serverdrivenui.schema.compose.Text
import com.example.serverdrivenui.schema.compose.aspectRatio
import com.example.serverdrivenui.schema.compose.background
import com.example.serverdrivenui.schema.compose.clip
import com.example.serverdrivenui.schema.compose.fillMaxSize
import com.example.serverdrivenui.schema.compose.fillMaxWidth
import com.example.serverdrivenui.schema.compose.height
import com.example.serverdrivenui.schema.compose.linearGradient
import com.example.serverdrivenui.schema.compose.padding
import com.example.serverdrivenui.schema.compose.size
import com.example.serverdrivenui.schema.compose.statusBarsPadding
import dev.konduit.Modifier

/**
 * Server-driven version of DevoStatus's `ExploreScreen`.
 *
 * Surface:
 *   - Top header (white-tinted, status-bar-padded) with a non-functional
 *     search text field + three suggestion chips ("Trending" pre-selected
 *     in saffron, "Shiva", "Morning").
 *   - 2-column LazyVerticalGrid of cards. Each card pairs a quote
 *     ([HostQuotesProvider]) with a randomly-matched wallpaper
 *     ([HostWallpapersProvider]) — the quote's `tag` filters wallpapers
 *     to the matching deity first, then falls back to any wallpaper.
 *   - Card aspect 0.75 (taller than wide), with the wallpaper as the
 *     hero, a black scrim for text readability, the quote text centered
 *     in white, a heart icon top-right, and a white "Create" button
 *     across the bottom that fires [HostExploreNavigator.onExploreItemSelected].
 *
 * Schema features it exercises:
 *   - [LazyVerticalGrid] with `columns = 2`, content + item spacing
 *   - [aspectRatio] modifier on each card
 *   - [linearGradient] modifier for the no-wallpaper fallback
 *   - Stacked Box with multiple absolute-aligned children
 *   - Brand-color `CustomBackground` for the heart pill
 *   - `OutlinedTextField` (non-functional placeholder for the search)
 *   - `SuggestionChip` colored vs neutral states
 */
class ExploreScreen : Screen {
    @Composable
    override fun Content(navigator: Navigator) {
        val quotesProvider = HostQuotesProviderBridge.instance
        val wallpapersProvider = HostWallpapersProviderBridge.instance
        val exploreNav = HostExploreNavigatorBridge.instance

        var quotes by remember { mutableStateOf<List<Quote>?>(null) }
        var wallpapers by remember { mutableStateOf<List<Wallpaper>?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(quotesProvider, wallpapersProvider) {
            error = null
            try {
                quotes = quotesProvider?.getQuotes(null) ?: emptyList()
            } catch (t: Throwable) {
                error = "Failed to load quotes: ${t.message}"
            }
            try {
                wallpapers = wallpapersProvider?.getWallpapers(null) ?: emptyList()
            } catch (t: Throwable) {
                // Wallpapers missing is non-fatal; cards just fall back
                // to gradient backgrounds.
                wallpapers = emptyList()
            }
        }

        Column(
            verticalArrangement = SchemaArrangement.Start,
            horizontalAlignment = SchemaHorizontalAlignment.Start,
            modifier = Modifier.fillMaxSize(),
        ) {
            // ─── Header (status-bar-padded, near-white background) ──
            Column(
                verticalArrangement = SchemaArrangement.Start,
                horizontalAlignment = SchemaHorizontalAlignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SchemaColor.Background,
                        alpha = 0.95,
                    )
                    .statusBarsPadding()
                    .padding(16, 16, 16, 16),
            ) {
                // Search box. Schema `SearchBar` bakes in the leading
                // search icon + placeholder semantics — `OutlinedTextField`
                // would work too but doesn't have a slot for the leading
                // icon. Non-functional in v1 (onValueChange = null); the
                // visual renders correctly.
                SearchBar(
                    value = "",
                    placeholder = "Search quotes, deities, or mood...",
                    enabled = true,
                    onValueChange = null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(width = 0, height = 12)
                // ─── Trending / Shiva / Morning chips ────────────
                Row(
                    horizontalArrangement = SchemaArrangement.Start,
                    verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SuggestionChip(
                        label = "Trending",
                        enabled = true,
                        onClick = null,
                    ) {}
                    Spacer(width = 8, height = 0)
                    SuggestionChip(
                        label = "Shiva",
                        enabled = true,
                        onClick = null,
                    ) {}
                    Spacer(width = 8, height = 0)
                    SuggestionChip(
                        label = "Morning",
                        enabled = true,
                        onClick = null,
                    ) {}
                }
            }

            // ─── Grid of cards ─────────────────────────────────────
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
                        // Loading: the LazyVerticalGrid would normally
                        // render here but we keep the screen empty
                        // until the snapshot arrives.
                    }
                }
                quotesSnap.isEmpty() -> {
                    Column(
                        verticalArrangement = SchemaArrangement.Center,
                        horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(32, 32, 32, 32),
                    ) {
                        Text(
                            text = "No quotes available.",
                            style = SchemaTextStyle.BodyMedium,
                            color = SchemaColor.OnSurfaceVariant,
                        )
                    }
                }
                else -> {
                    val featured = quotesSnap.take(20)
                    val wallpaperSnap = wallpapers ?: emptyList()
                    LazyVerticalGrid(
                        columns = 2,
                        contentPaddingDp = 16,
                        itemSpacingDp = 16,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        featured.forEachIndexed { idx, quote ->
                            // Random matching wallpaper, seeded by the
                            // pair (quote.id, item index) so the grid
                            // is stable across recompositions but
                            // distinct per card.
                            val wp = pickWallpaperFor(
                                quote = quote,
                                wallpapers = wallpaperSnap,
                                seed = idx,
                            )
                            LazyItem {
                                ExploreCard(
                                    quote = quote,
                                    wallpaper = wp,
                                    onClick = {
                                        exploreNav?.onExploreItemSelected(
                                            quoteId = quote.id,
                                            wallpaperId = wp?.id,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun pickWallpaperFor(
    quote: Quote,
    wallpapers: List<Wallpaper>,
    seed: Int,
): Wallpaper? {
    if (wallpapers.isEmpty()) return null
    val tag = quote.tag
    val byTag = if (tag != null) wallpapers.filter { it.tag == tag } else emptyList()
    val pool = if (byTag.isNotEmpty()) byTag else wallpapers
    // Deterministic per-card pick — no kotlin.random.Random with
    // shared state. We just index by (quote.id.hashCode + seed)
    // modulo pool size. This gives variety across cards while staying
    // stable across recompositions (same key set → same selection).
    val idx = ((quote.id.hashCode() xor seed) and Int.MAX_VALUE) % pool.size
    return pool[idx]
}

/**
 * Single Explore card. Matches DevoStatus's native `ExploreCard`:
 *   - aspect 0.75 (taller than wide)
 *   - AsyncImage hero with ContentScale.Crop semantics (host widget
 *     handles the scale mode); falls back to a saffron→maroon linear
 *     gradient when no wallpaper is available
 *   - Black scrim @ 0.4 alpha for text readability
 *   - Heart pill top-right (visual toggle only — the save-to-gallery
 *     half is too host-specific to ship through the schema)
 *   - Centered quote text in white-bold serif
 *   - White "Create" button across the bottom that fires the
 *     `onClick` (host nav → Preview)
 */
@Composable
private fun ExploreCard(
    quote: Quote,
    wallpaper: Wallpaper?,
    onClick: () -> Unit,
) {
    // Local "liked" state — visual only; the schema can express the
    // toggle but the actual save-to-gallery action lives in the host.
    var liked by remember { mutableStateOf(false) }

    // Saffron 0xFFFF6F00, PrimaryMaroon 0xFF7A1F1F — DevoStatus brand
    // colors, expressed as raw ARGB so the gradient works regardless of
    // the host's MaterialTheme.
    val saffronArgb = 0xFFFF6F00L
    val maroonArgb = 0xFF7A1F1FL
    val blackScrimArgb = 0xFF000000L
    val saffronPillArgb = saffronArgb

    Card(
        onClick = onClick,
        containerColor = SchemaColor.Background,
        contentColor = SchemaColor.OnBackground,
        cornerRadiusDp = 16,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio = 0.75),
    ) {
        Box(
            onClick = null,
            contentAlignment = SchemaBoxAlignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // ─── Background layer: image OR gradient fallback ────
            if (wallpaper != null) {
                AsyncImage(
                    url = wallpaper.imageUrl,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    onClick = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .linearGradient(
                            startArgb = saffronArgb,
                            endArgb = maroonArgb,
                            angleDegrees = 45,
                            startAlpha = 0.8,
                            endAlpha = 0.8,
                        ),
                ) {}
            }

            // ─── Scrim for text readability ──────────────────────
            Box(
                onClick = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = SchemaColor.Background,  // placeholder, overridden by alpha
                        alpha = 0.0,
                    )
                    .background(
                        color = SchemaColor.OnBackground,
                        alpha = 0.4,
                    ),
            ) {}

            // ─── Heart pill (top-right) ──────────────────────────
            Box(
                onClick = { liked = !liked },
                contentAlignment = SchemaBoxAlignment.TopEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8, 8, 8, 8),
            ) {
                Box(
                    onClick = { liked = !liked },
                    contentAlignment = SchemaBoxAlignment.Center,
                    modifier = Modifier
                        .size(36, 36)
                        .clip(cornerRadiusDp = 18),
                ) {
                    Box(
                        onClick = null,
                        contentAlignment = SchemaBoxAlignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(cornerRadiusDp = 18)
                            .background(
                                color = if (liked) SchemaColor.Tertiary else SchemaColor.OnBackground,
                                alpha = if (liked) 0.9 else 0.2,
                                cornerRadiusDp = 18,
                            ),
                    ) {
                        Icon(
                            name = if (liked) SchemaIconName.Favorite else SchemaIconName.FavoriteBorder,
                            tint = SchemaColor.Background,  // white-on-tinted-bg
                            modifier = Modifier.size(20, 20),
                        )
                    }
                }
            }

            // ─── Centered quote text ─────────────────────────────
            Column(
                verticalArrangement = SchemaArrangement.Center,
                horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12, 12, 12, 12),
            ) {
                Text(
                    text = "\"${quote.text}\"",
                    style = SchemaTextStyle.BodyLarge,
                    color = SchemaColor.Background,  // white in DevoStatus theme
                    fontWeight = SchemaFontWeight.Bold,
                    textAlign = SchemaTextAlign.Center,
                    maxLines = 4,
                )
            }

            // ─── Create button (bottom) ──────────────────────────
            Column(
                verticalArrangement = SchemaArrangement.End,
                horizontalAlignment = SchemaHorizontalAlignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12, 12, 12, 12),
            ) {
                Card(
                    onClick = onClick,
                    containerColor = SchemaColor.Background,
                    contentColor = SchemaColor.Tertiary,
                    cornerRadiusDp = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36),
                ) {
                    Row(
                        horizontalArrangement = SchemaArrangement.Center,
                        verticalAlignment = SchemaVerticalAlignment.CenterVertically,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            name = SchemaIconName.Share,
                            tint = SchemaColor.Tertiary,
                            modifier = Modifier.size(16, 16),
                        )
                        Spacer(width = 4, height = 0)
                        Text(
                            text = "Create",
                            style = SchemaTextStyle.LabelSmall,
                            color = SchemaColor.Tertiary,
                            fontWeight = SchemaFontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
