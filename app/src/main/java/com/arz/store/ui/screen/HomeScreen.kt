package com.arz.store.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arz.store.ui.MainViewModel
import com.arz.store.R
import com.arz.store.model.*
import com.arz.store.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onGameClick: (GameProduct) -> Unit,
) {
    val banners by viewModel.banners.collectAsState()
    val games by viewModel.games.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll banner
    LaunchedEffect(banners.size) {
        if (banners.isNotEmpty()) {
            while (true) {
                delay(3000)
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategorySlug by remember { mutableStateOf("all") }

    val filteredGames = remember(searchQuery, selectedCategorySlug, games) {
        var result = games
        if (selectedCategorySlug != "all") {
            result = result.filter {
                it.categorySlug.equals(selectedCategorySlug, ignoreCase = true)
            }
        }
        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        TopBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            isSearchActive = isSearchActive,
            onSearchToggle = {
                isSearchActive = !isSearchActive
                if (!isSearchActive) searchQuery = ""
            },
        )

        // Banner Carousel
        if (!isSearchActive || searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            BannerCarousel(
                banners = banners,
                pagerState = pagerState,
                onBannerClick = { banner ->
                    val selectedGame = games.find { it.id == banner.id }
                    if (selectedGame != null) {
                        onGameClick(selectedGame)
                    }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Category Chips
            CategoryChips(
                categories = categories,
                selectedCategorySlug = selectedCategorySlug,
                onCategorySelect = { selectedCategorySlug = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isSearchActive && searchQuery.isNotEmpty())
                    "Hasil Pencarian" else "Game Populer",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            if (!isSearchActive) {
                Text(
                    text = "Lihat Semua",
                    color = AccentCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Game Grid
        GameGrid(
            games = filteredGames,
            onGameClick = onGameClick,
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun TopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), DarkBg)
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSearchActive) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    placeholder = {
                        Text("Cari game...", color = TextMuted, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = AccentCyan)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .clickable { onSearchToggle() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Brand + Search button
                Column {
                    Text(
                        text = "ARZ Store",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                    Text(
                        text = "Top Up Game Terpercaya",
                        color = AccentCyan,
                        fontSize = 11.sp,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Search Icon Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .border(1.dp, DarkCardBorder, CircleShape)
                            .clickable { onSearchToggle() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentPurple, PrimaryBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_arz),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannerCarousel(
    banners: List<BannerItem>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onBannerClick: (BannerItem) -> Unit,
) {
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp),
            pageSpacing = 12.dp,
        ) { page ->
            if (banners.isNotEmpty()) {
                BannerCard(
                    banner = banners[page],
                    onClick = { onBannerClick(banners[page]) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            banners.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                val dotWidth by animateIntAsState(
                    targetValue = if (isSelected) 24 else 8,
                    animationSpec = tween(300),
                    label = "dot_width",
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) AccentCyan else DarkCardBorder,
                    animationSpec = tween(300),
                    label = "dot_color",
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .width(dotWidth.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
        }
    }
}

@Composable
fun BannerCard(
    banner: BannerItem,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        if (banner.imageUrl != null) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = "Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = banner.iconResId),
                contentDescription = "Banner Local",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<CategoryItem>,
    selectedCategorySlug: String,
    onCategorySelect: (String) -> Unit
) {
    // Add "Semua" prepended if not already there, or handle it manually
    val allCategories = listOf(CategoryItem(0, "Semua", "all", null)) + categories

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(allCategories) { category ->
            val isSelected = category.slug == selectedCategorySlug
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) PrimaryBlue else DarkCard,
                label = "chip_bg",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) PrimaryBlue else DarkCardBorder,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable { onCategorySelect(category.slug) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = category.name,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun GameGrid(
    games: List<GameProduct>,
    onGameClick: (GameProduct) -> Unit,
) {
    val columns = 3
    val rows = (games.size + columns - 1) / columns

    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < games.size) {
                        GameCard(
                            game = games[index],
                            onClick = { onGameClick(games[index]) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: GameProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150),
        label = "card_scale",
    )

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(game.gradientStart, game.gradientEnd),
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        // Game icon as full background
        if (game.iconUrl != null) {
            AsyncImage(
                model = game.iconUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = game.iconResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        // Gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Background circles for depth
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopEnd)
                .offset(x = 15.dp, y = (-10).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                when {
                    game.isPopular -> BadgeChip("HOT", Color(0xFFFF6B35))
                    game.isNew -> BadgeChip("NEW", SuccessGreen)
                }
            }

            // Game info (moved to bottom, no center icon anymore)
            Column {
                Text(
                    text = game.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = game.category,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
fun BadgeChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.85f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
