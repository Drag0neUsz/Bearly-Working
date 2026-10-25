package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoastCategory
import com.example.data.SavageQuotes
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState

@Composable
fun QuotesScreen(
    uiState: SavageUiState,
    onPreviewQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<RoastCategory?>(null) }

    // Intercept back button when inside a category subpage
    if (selectedCategory != null) {
        BackHandler {
            selectedCategory = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComicWhiteBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Crossfade(
            targetState = selectedCategory,
            label = "categorySubpageTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { category ->
            if (category == null) {
                // ==========================================
                // MAIN PAGE: TILES OVERVIEW
                // ==========================================
                CategoryTilesView(
                    uiState = uiState,
                    onSelectCategory = { selectedCategory = it }
                )
            } else {
                // ==========================================
                // SUBPAGE: SPECIFIC CATEGORY ROASTS LIST
                // ==========================================
                CategorySubpageView(
                    category = category,
                    uiState = uiState,
                    onBack = { selectedCategory = null },
                    onPreviewQuote = onPreviewQuote
                )
            }
        }
    }
}

/**
 * Main Overview: Grid/List of Category Tiles.
 * Secret category only appears after at least one secret roast is discovered!
 */
@Composable
private fun CategoryTilesView(
    uiState: SavageUiState,
    onSelectCategory: (RoastCategory) -> Unit
) {
    // Only show secret category if at least one secret roast has been discovered!
    val visibleCategories = remember(uiState.unlockedQuotes) {
        SavageQuotes.ALL_CATEGORIES.filter { cat ->
            if (cat.isSecret) {
                cat.quotes.any { uiState.unlockedQuotes.contains(it) }
            } else {
                true
            }
        }
    }

    val visibleTotalQuotes = visibleCategories.sumOf { it.quotes.size }
    val totalUnlocked = visibleCategories.sumOf { cat ->
        cat.quotes.count { uiState.unlockedQuotes.contains(it) }
    }
    val completionPercent = if (visibleTotalQuotes == 0) 0 else ((totalUnlocked.toFloat() / visibleTotalQuotes) * 100).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header Frame
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .padding(12.dp)
                    .testTag("card_completion_book_header")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROAST COMPLETION BOOK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            ),
                            color = ComicInkBlack
                        )
                        Text(
                            text = "$totalUnlocked / $visibleTotalQuotes",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = ComicAccentRed
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Choose a tile below to inspect roasts. Items remain '???' until pulled through your actions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(ComicSurfaceSubtle)
                            .border(width = 1.5.dp, color = ComicInkBlack)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (totalUnlocked.toFloat() / visibleTotalQuotes.coerceAtLeast(1)).coerceIn(0f, 1f))
                                .background(ComicAccentRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TOTAL DISCOVERED: $completionPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = ComicInkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Category Tiles
        items(visibleCategories, key = { it.id }) { cat ->
            val unlockedInCat = cat.quotes.count { uiState.unlockedQuotes.contains(it) }
            val catTotal = cat.quotes.size
            val catRatio = if (catTotal == 0) 0f else unlockedInCat.toFloat() / catTotal

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .background(if (cat.isSecret) Color(0xFFFEF2F2) else ComicSurfaceWhite)
                    .border(
                        width = 2.dp,
                        color = if (cat.isSecret) ComicAccentRed else ComicInkBlack
                    )
                    .clickable { onSelectCategory(cat) }
                    .padding(16.dp)
                    .testTag("tile_category_${cat.id}")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (cat.isSecret) {
                                Box(
                                    modifier = Modifier
                                        .background(ComicAccentRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SECRET UNLOCKED",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = cat.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = ComicInkBlack
                            )
                        }

                        Box(
                            modifier = Modifier
                                .border(width = 1.dp, color = ComicInkBlack)
                                .background(ComicSurfaceSubtle)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unlockedInCat / $catTotal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = cat.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ComicTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar inside tile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(ComicSurfaceSubtle)
                            .border(width = 1.dp, color = ComicInkBlack)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = catRatio.coerceIn(0f, 1f))
                                .background(if (cat.isSecret) ComicAccentRed else ComicInkBlack)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VIEW ROASTS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (cat.isSecret) ComicAccentRed else ComicInkBlack
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (cat.isSecret) ComicAccentRed else ComicInkBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Subpage for a selected Category Tile.
 */
@Composable
private fun CategorySubpageView(
    category: RoastCategory,
    uiState: SavageUiState,
    onBack: () -> Unit,
    onPreviewQuote: (String) -> Unit
) {
    val unlockedInCat = category.quotes.count { uiState.unlockedQuotes.contains(it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Navigation Back Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComicSurfaceWhite)
                    .border(width = 2.dp, color = ComicInkBlack)
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("btn_back_to_tiles")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ComicInkBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BACK TO CATEGORIES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        ),
                        color = ComicInkBlack
                    )
                }
            }
        }

        // Subpage Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(ComicInkBlack)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$unlockedInCat / ${category.quotes.size} UNLOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = ComicInkBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // List of Roasts in this category
        items(category.quotes) { quote ->
            val isUnlocked = uiState.unlockedQuotes.contains(quote)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(if (isUnlocked) ComicSurfaceWhite else ComicSurfaceSubtle)
                    .border(
                        width = 1.5.dp,
                        color = if (isUnlocked) ComicInkBlack else ComicBorderSubtle
                    )
                    .then(
                        if (isUnlocked) {
                            Modifier.clickable { onPreviewQuote(quote) }
                        } else Modifier
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Unlocked",
                            tint = ComicAccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "\"$quote\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            ),
                            color = ComicInkBlack,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = ComicTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "???",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = ComicTextMuted
                            )
                            Text(
                                text = "LOCKED • HURL FACTOR: ${category.title}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = ComicTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
