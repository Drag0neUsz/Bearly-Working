package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoastCategory
import com.example.data.SavageQuotes
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageUiState

private data class CategoryVisualStyle(
    val iconSymbol: String,
    val iconColor: Color,
    val subtitle: String,
    val iconBg: Color,
    val iconBorder: Color,
    val iconShadow: Color,
    val barColor: Color
)

private fun getCategoryVisualStyle(categoryId: String): CategoryVisualStyle {
    return when (categoryId) {
        SavageQuotes.CAT_BEAR_POKES -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = Color(0xFFB45309),
            subtitle = "Active schedule companion taps",
            iconBg = Color(0xFFFEF3C7),
            iconBorder = Color(0xFFFBBF24),
            iconShadow = Color(0xFFF59E0B),
            barColor = SoftGreen
        )
        SavageQuotes.CAT_FINISHED_DAY_POKES -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = SoftBlueText,
            subtitle = "End-of-day wrapup reflections",
            iconBg = Color(0xFFE0F2FE),
            iconBorder = Color(0xFF93C5FD),
            iconShadow = Color(0xFF60A5FA),
            barColor = SoftBlue
        )
        SavageQuotes.CAT_IGNORED_PRIORITY -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = SoftRed,
            subtitle = "High priority task neglect",
            iconBg = Color(0xFFFEE2E2),
            iconBorder = Color(0xFFFCA5A5),
            iconShadow = Color(0xFFF87171),
            barColor = SoftRed
        )
        SavageQuotes.CAT_TIME_RUNNING_OUT -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = Color(0xFFEA580C),
            subtitle = "Late afternoon panic reminders",
            iconBg = Color(0xFFFFEDD5),
            iconBorder = Color(0xFFFDBA74),
            iconShadow = Color(0xFFFB923C),
            barColor = SoftOrange
        )
        SavageQuotes.CAT_BROKEN_PROMISES -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = Color(0xFF9333EA),
            subtitle = "Self-commitment dropoffs",
            iconBg = Color(0xFFF3E8FF),
            iconBorder = Color(0xFFD8B4FE),
            iconShadow = Color(0xFFC084FC),
            barColor = SoftPurple
        )
        SavageQuotes.CAT_MIDDAY_SLUMP -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = Color(0xFF2563EB),
            subtitle = "Afternoon procrastination peaks",
            iconBg = Color(0xFFDBEAFE),
            iconBorder = Color(0xFF93C5FD),
            iconShadow = Color(0xFF3B82F6),
            barColor = SoftBlue
        )
        SavageQuotes.CAT_MORNING_SLACKING -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = Color(0xFFCA8A04),
            subtitle = "Morning slacker callouts",
            iconBg = Color(0xFFFEF9C3),
            iconBorder = Color(0xFFFDE047),
            iconShadow = Color(0xFFEAB308),
            barColor = SoftYellow
        )
        SavageQuotes.CAT_ALL_COMPLETED -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = SoftGreenText,
            subtitle = "Rare stunned respect",
            iconBg = Color(0xFFDCFCE7),
            iconBorder = Color(0xFF86EFAC),
            iconShadow = SoftGreen,
            barColor = SoftGreen
        )
        else -> CategoryVisualStyle(
            iconSymbol = "☒",
            iconColor = SoftRed,
            subtitle = "Hidden easter egg roasts",
            iconBg = Color(0xFFFEE2E2),
            iconBorder = Color(0xFFFCA5A5),
            iconShadow = SoftRed,
            barColor = SoftRed
        )
    }
}

@Composable
fun QuotesScreen(
    uiState: SavageUiState,
    onPreviewQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<RoastCategory?>(null) }

    if (selectedCategory != null) {
        BackHandler {
            selectedCategory = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftCanvasBg,
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
                CategoryTilesView(
                    uiState = uiState,
                    onSelectCategory = { selectedCategory = it }
                )
            } else {
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

@Composable
private fun CategoryTilesView(
    uiState: SavageUiState,
    onSelectCategory: (RoastCategory) -> Unit
) {
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
    val collectionLevel = (totalUnlocked / 5) + 1

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftSurfaceWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Roast Book",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "BEARLY FINISHED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Golden Discovered Pill Badge with Ballot Box
                Tactile3DBadge(
                    containerColor = Color(0xFFFFFBEB),
                    borderColor = Color(0xFFFBBF24),
                    bottomLipColor = Color(0xFFF59E0B),
                    shape = RoundedCornerShape(16.dp),
                    lipHeight = 3.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "☒",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD97706)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$totalUnlocked / $visibleTotalQuotes",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFFD97706)
                    )
                }
            }

            HorizontalDivider(thickness = 2.dp, color = SoftBorder)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Hero Sticker & Global Progress Card
            item {
                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_completion_book_header"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = SoftBorder,
                    bottomLipColor = SoftBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.5.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Sticker Book 3D Icon Box with Ballot Box
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color(0xFFD97706), shape = RoundedCornerShape(16.dp))
                                .padding(bottom = 3.5.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "☒",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Roast Collection",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF1F2937)
                                )

                                Tactile3DBadge(
                                    containerColor = Color(0xFFFFEDD5),
                                    borderColor = Color(0xFFFDBA74),
                                    bottomLipColor = Color(0xFFFDBA74),
                                    shape = CircleShape,
                                    lipHeight = 2.5.dp,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LEVEL $collectionLevel",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        ),
                                        color = Color(0xFFEA580C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Tap cards to inspect roasts. Hidden roasts remain '???' until earned by your habits!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "☒ Total Discovered",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF4B5563)
                        )
                        Text(
                            text = "$completionPercent% COMPLETED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TactileGlossyProgressBar(
                        progress = totalUnlocked.toFloat() / visibleTotalQuotes.coerceAtLeast(1),
                        modifier = Modifier.fillMaxWidth(),
                        height = 18.dp,
                        trackColor = Color(0xFFE5E7EB),
                        trackBorderColor = Color(0xFFE5E7EB),
                        fillBrush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFFACC15))),
                        minVisualFraction = 0.08f
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Title Divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROAST CATEGORIES (${visibleCategories.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 2.dp,
                        color = Color(0xFFE5E7EB)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Category Cards List
            items(visibleCategories, key = { it.id }) { cat ->
                val unlockedInCat = cat.quotes.count { uiState.unlockedQuotes.contains(it) }
                val catTotal = cat.quotes.size
                val catRatio = if (catTotal == 0) 0f else unlockedInCat.toFloat() / catTotal
                val hasUnlocked = unlockedInCat > 0
                val visual = getCategoryVisualStyle(cat.id)

                val displayTitle = remember(cat.title) {
                    cat.title.lowercase().split(" ").joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    }
                }

                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("tile_category_${cat.id}"),
                    containerColor = SoftSurfaceWhite,
                    borderColor = if (hasUnlocked) Color(0xFFFCD34D) else SoftBorder,
                    bottomLipColor = if (hasUnlocked) Color(0xFFFBBF24) else SoftBorder,
                    cornerRadius = 24.dp,
                    lipHeight = 3.5.dp,
                    onClick = { onSelectCategory(cat) },
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Header Row: Icon Box + Title/Subtitle + Counter Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(visual.iconShadow, shape = RoundedCornerShape(15.dp))
                                    .padding(bottom = 3.dp)
                                    .background(visual.iconBg, shape = RoundedCornerShape(15.dp))
                                    .border(width = 2.dp, color = visual.iconBorder, shape = RoundedCornerShape(15.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = visual.iconSymbol,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = visual.iconColor
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    ),
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = visual.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Counter Chip
                        Tactile3DBadge(
                            containerColor = if (hasUnlocked) Color(0xFFFEF3C7) else Color(0xFFF3F4F6),
                            borderColor = if (hasUnlocked) Color(0xFFFBBF24) else Color(0xFFD1D5DB),
                            bottomLipColor = if (hasUnlocked) Color(0xFFF59E0B) else Color(0xFFD1D5DB),
                            shape = RoundedCornerShape(12.dp),
                            lipHeight = 2.5.dp,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$unlockedInCat / $catTotal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                ),
                                color = if (hasUnlocked) Color(0xFF92400E) else Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = cat.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Progress Bar
                    TactileGlossyProgressBar(
                        progress = catRatio,
                        modifier = Modifier.fillMaxWidth(),
                        height = 14.dp,
                        trackColor = Color(0xFFF3F4F6),
                        trackBorderColor = Color(0xFFE5E7EB),
                        fillBrush = Brush.horizontalGradient(listOf(visual.barColor, visual.barColor)),
                        minVisualFraction = if (hasUnlocked) 0.1f else 0f
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Footer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasUnlocked) {
                            Tactile3DBadge(
                                containerColor = SoftGreenLight,
                                borderColor = SoftGreen,
                                bottomLipColor = SoftGreenShadow,
                                shape = RoundedCornerShape(12.dp),
                                lipHeight = 2.5.dp,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SoftGreen
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$unlockedInCat Roast${if (unlockedInCat == 1) "" else "s"} Found",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = SoftGreen
                                )
                            }
                        } else {
                            Tactile3DBadge(
                                containerColor = Color(0xFFF3F4F6),
                                borderColor = Color(0xFFD1D5DB),
                                bottomLipColor = Color(0xFFD1D5DB),
                                shape = RoundedCornerShape(12.dp),
                                lipHeight = 2.5.dp,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "☒",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6B7280)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "All $catTotal Locked",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Tactile3DButton(
                            onClick = { onSelectCategory(cat) },
                            containerColor = if (hasUnlocked) SoftGreen else Color(0xFFF3F4F6),
                            bottomLipColor = if (hasUnlocked) SoftGreenShadow else Color(0xFFD1D5DB),
                            borderColor = if (hasUnlocked) SoftGreenShadow else Color(0xFFE5E7EB),
                            cornerRadius = 12.dp,
                            lipHeight = 3.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "VIEW ROASTS →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                color = if (hasUnlocked) Color.White else Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }

            // Motivator Quote Footer Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7).copy(alpha = 0.75f), shape = RoundedCornerShape(24.dp))
                        .border(width = 2.dp, color = Color(0xFFFCD34D), shape = RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"Procrastinate more to unlock the rest... or actually do your work!\" — Grumpy Bear",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF92400E),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CategorySubpageView(
    category: RoastCategory,
    uiState: SavageUiState,
    onBack: () -> Unit,
    onPreviewQuote: (String) -> Unit
) {
    val unlockedInCat = category.quotes.count { uiState.unlockedQuotes.contains(it) }
    val visual = getCategoryVisualStyle(category.id)

    Column(modifier = Modifier.fillMaxSize()) {
        // Navigation Back Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftSurfaceWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("btn_back_to_tiles"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SoftSurfaceSubtle, shape = RoundedCornerShape(10.dp))
                        .border(width = 1.5.dp, color = SoftBorder, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SoftTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "BACK TO CATEGORIES",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    ),
                    color = SoftTextPrimary
                )
            }
            HorizontalDivider(thickness = 2.dp, color = SoftBorder)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Subpage Category Hero Banner
            item {
                Tactile3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SoftSurfaceWhite,
                    borderColor = visual.iconBorder,
                    bottomLipColor = visual.iconShadow,
                    cornerRadius = 24.dp,
                    lipHeight = 3.5.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(visual.iconBg, shape = RoundedCornerShape(14.dp))
                                    .border(width = 2.dp, color = visual.iconBorder, shape = RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = visual.iconSymbol,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = visual.iconColor
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = SoftTextPrimary
                            )
                        }

                        Tactile3DBadge(
                            containerColor = SoftGreenLight,
                            borderColor = SoftGreenBorder,
                            bottomLipColor = SoftGreenBorder,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$unlockedInCat / ${category.quotes.size} UNLOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = SoftGreenText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // List of Roasts in this Category
            items(category.quotes) { quote ->
                val isUnlocked = uiState.unlockedQuotes.contains(quote)

                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    containerColor = if (isUnlocked) SoftSurfaceWhite else SoftSurfaceSubtle,
                    borderColor = if (isUnlocked) SoftGreenBorder else SoftBorder,
                    bottomLipColor = if (isUnlocked) Color(0xFF86EFAC) else SoftBorder,
                    cornerRadius = 20.dp,
                    lipHeight = 3.dp,
                    onClick = if (isUnlocked) ({ onPreviewQuote(quote) }) else null,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SoftGreenLight, shape = CircleShape)
                                    .border(width = 1.5.dp, color = SoftGreenBorder, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "Unlocked",
                                    tint = SoftGreen,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "\"$quote\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 20.sp
                                    ),
                                    color = SoftTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "TAP TO PREVIEW ON BEAR →",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    ),
                                    color = SoftGreenText
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFE5E7EB), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = SoftTextMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "???",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ),
                                    color = SoftTextMuted
                                )
                                Text(
                                    text = "LOCKED • ${category.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = SoftTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
