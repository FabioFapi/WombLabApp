package com.rix.womblab.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rix.womblab.domain.model.Event
import com.rix.womblab.presentation.components.ErrorMessage
import com.rix.womblab.presentation.components.EventCard
import com.rix.womblab.presentation.components.LoadingIndicator
import com.rix.womblab.presentation.components.WombLabTopBar
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.utils.HomeScreenImagePreloader
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var isSearchVisible by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val density = LocalDensity.current
    var scrollY by remember { mutableStateOf(0f) }

    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        scrollY = with(density) { listState.firstVisibleItemScrollOffset.toDp().value }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0
                val totalItemsCount = listState.layoutInfo.totalItemsCount

                if (lastVisibleItemIndex >= totalItemsCount - 3 &&
                    uiState.hasMoreEvents &&
                    !uiState.isLoading) {
                    viewModel.loadMoreEvents()
                }
            }
    }

    Scaffold(
        topBar = {
            WombLabTopBar(
                title = "Eventi",
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSearchToggle = {
                    isSearchVisible = !isSearchVisible
                    if (!isSearchVisible) {
                        viewModel.clearSearch()
                    }
                },
                isSearchVisible = isSearchVisible,
                hasNotifications = false,
                onNotificationClick = { }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.upcomingEvents.isEmpty() && uiState.favoriteEvents.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedLoadingIndicator()
                    }
                }

                uiState.error != null && uiState.upcomingEvents.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SlideInErrorMessage(
                            message = uiState.error ?: "Errore sconosciuto",
                            onRetryClick = { viewModel.onRefresh() }
                        )
                    }
                }

                else -> {
                    AnimatedMainContent(
                        uiState = uiState,
                        onEventClick = onEventClick,
                        onFavoriteClick = viewModel::onToggleFavorite,
                        isSearchVisible = isSearchVisible,
                        listState = listState,
                        scrollOffset = scrollY
                    )

                    HomeScreenImagePreloader(
                        favoriteEvents = uiState.favoriteEvents,
                        featuredEvents = uiState.featuredEvents,
                        upcomingEvents = uiState.upcomingEvents
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedMainContent(
    uiState: HomeUiState,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    isSearchVisible: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scrollOffset: Float
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (isSearchVisible && uiState.searchQuery.isNotEmpty()) {
            item {
                AnimatedSearchSection(
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    searchQuery = uiState.searchQuery,
                    onEventClick = onEventClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        } else {
            if (uiState.favoriteEvents.isNotEmpty()) {
                item {
                    AnimatedFavoritesSection(
                        favoriteEvents = uiState.favoriteEvents,
                        onEventClick = onEventClick,
                        onFavoriteClick = onFavoriteClick,
                        scrollOffset = scrollOffset
                    )
                }
            }

            if (uiState.featuredEvents.isNotEmpty()) {
                item {
                    AnimatedFeaturedSection(
                        featuredEvents = uiState.featuredEvents,
                        onEventClick = onEventClick,
                        onFavoriteClick = onFavoriteClick,
                        scrollOffset = scrollOffset
                    )
                }
            }

            item {
                AnimatedUpcomingSection(
                    upcomingEvents = uiState.upcomingEvents,
                    onEventClick = onEventClick,
                    onFavoriteClick = onFavoriteClick,
                    isLoading = uiState.isLoading,
                    hasMoreEvents = uiState.hasMoreEvents
                )
            }
        }
    }
}

@Composable
private fun AnimatedSearchSection(
    searchResults: List<Event>,
    isSearching: Boolean,
    searchQuery: String,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { -it / 4 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            AnimatedHeaderSection(
                title = "Risultati per \"$searchQuery\"",
                subtitle = if (searchResults.isNotEmpty()) "${searchResults.size} eventi trovati" else null,
                animationDelay = 0
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedLoadingIndicator()
                    }
                }

                searchResults.isEmpty() -> {
                    AnimatedEmptyState(
                        title = "Nessun evento trovato",
                        description = "Prova con parole chiave diverse",
                        emoji = "🔍"
                    )
                }

                else -> {
                    searchResults.forEachIndexed { index, event ->
                        EventCard(
                            event = event,
                            onEventClick = onEventClick,
                            onFavoriteClick = onFavoriteClick,
                            isCompact = true,
                            animationDelay = index * 100,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedFavoritesSection(
    favoriteEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    scrollOffset: Float
) {
    val parallaxOffset = scrollOffset * 0.3f

    Column(
        modifier = Modifier.graphicsLayer {
            translationY = -parallaxOffset
        }
    ) {
        AnimatedHeaderSection(
            title = "I tuoi eventi salvati",
            subtitle = "${favoriteEvents.size} eventi",
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(favoriteEvents) { index, event ->
                EventCard(
                    event = event,
                    onEventClick = onEventClick,
                    onFavoriteClick = onFavoriteClick,
                    animationDelay = 0,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedFeaturedSection(
    featuredEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    scrollOffset: Float
) {
    val parallaxOffset = scrollOffset * 0.5f

    Column(
        modifier = Modifier.graphicsLayer {
            translationY = -parallaxOffset
        }
    ) {
        AnimatedHeaderSection(
            title = "Eventi in evidenza",
            subtitle = "Selezionati per te",
            animationDelay = 300
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(featuredEvents) { index, event ->
                EventCard(
                    event = event,
                    onEventClick = onEventClick,
                    onFavoriteClick = onFavoriteClick,
                    animationDelay = 600 + (index * 150),
                    modifier = Modifier.width(320.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedUpcomingSection(
    upcomingEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    isLoading: Boolean,
    hasMoreEvents: Boolean
) {
    Column {
        AnimatedHeaderSection(
            title = "Prossimi eventi",
            subtitle = if (upcomingEvents.isNotEmpty()) "${upcomingEvents.size} eventi disponibili" else "",
            animationDelay = 400
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            upcomingEvents.isEmpty() && !isLoading -> {
                AnimatedEmptyState(
                    title = "Nessun evento in programma",
                    description = "Controlla più tardi per nuovi eventi",
                    emoji = "📅"
                )
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    upcomingEvents.forEachIndexed { index, event ->
                        EventCard(
                            event = event,
                            onEventClick = onEventClick,
                            onFavoriteClick = onFavoriteClick,
                            animationDelay = 800 + (index * 100),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (isLoading && upcomingEvents.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedLoadingIndicator(size = 32.dp)
                        }
                    }

                    if (!hasMoreEvents && upcomingEvents.isNotEmpty()) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(800))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Hai visualizzato tutti gli eventi!",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedHeaderSection(
    title: String,
    subtitle: String? = null,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            subtitle?.let {
                var isSubtitleVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(200)
                    isSubtitleVisible = true
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isSubtitleVisible,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedLoadingIndicator(
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🔬",
            fontSize = (size.value * 0.6f).sp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
        )
    }
}

@Composable
private fun SlideInErrorMessage(
    message: String,
    onRetryClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
        ErrorMessage(
            message = message,
            onRetryClick = onRetryClick
        )
    }
}

@Composable
private fun AnimatedEmptyState(
    title: String,
    description: String,
    emoji: String
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 32.sp
                    )
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    WombLabTheme {
        HomeScreen()
    }
}