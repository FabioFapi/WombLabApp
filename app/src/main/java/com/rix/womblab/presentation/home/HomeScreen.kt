package com.rix.womblab.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    // Infinite scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0
                val totalItemsCount = listState.layoutInfo.totalItemsCount

                if (lastVisibleItemIndex >= totalItemsCount - 3 && uiState.hasMoreEvents && !uiState.isLoading) {
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
                        LoadingIndicator()
                    }
                }

                uiState.error != null && uiState.upcomingEvents.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorMessage(
                            message = uiState.error ?: "Errore sconosciuto",
                            onRetryClick = { viewModel.onRefresh() }
                        )
                    }
                }

                else -> {
                    MainContent(
                        uiState = uiState,
                        onEventClick = onEventClick,
                        onFavoriteClick = viewModel::onToggleFavorite,
                        isSearchVisible = isSearchVisible,
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: HomeUiState,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    isSearchVisible: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (isSearchVisible && uiState.searchQuery.isNotEmpty()) {
            item {
                SearchSection(
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
                    FavoritesSection(
                        favoriteEvents = uiState.favoriteEvents,
                        onEventClick = onEventClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }

            if (uiState.featuredEvents.isNotEmpty()) {
                item {
                    FeaturedSection(
                        featuredEvents = uiState.featuredEvents,
                        onEventClick = onEventClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }

            item {
                UpcomingSection(
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
private fun SearchSection(
    searchResults: List<Event>,
    isSearching: Boolean,
    searchQuery: String,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Risultati per \"$searchQuery\"",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isSearching -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }

            searchResults.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun evento trovato",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                searchResults.forEach { event ->
                    EventCard(
                        event = event,
                        onEventClick = onEventClick,
                        onFavoriteClick = onFavoriteClick,
                        isCompact = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FavoritesSection(
    favoriteEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Column {
        HeaderSection(
            title = "I tuoi eventi salvati",
            subtitle = "${favoriteEvents.size} eventi"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favoriteEvents) { event ->
                EventCard(
                    event = event,
                    onEventClick = onEventClick,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedSection(
    featuredEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Column {
        HeaderSection(
            title = "Eventi in evidenza",
            subtitle = "Selezionati per te"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(featuredEvents) { event ->
                EventCard(
                    event = event,
                    onEventClick = onEventClick,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.width(320.dp)
                )
            }
        }
    }
}

@Composable
private fun UpcomingSection(
    upcomingEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    isLoading: Boolean,
    hasMoreEvents: Boolean
) {
    Column {
        HeaderSection(
            title = "Prossimi eventi",
            subtitle = if (upcomingEvents.isNotEmpty()) "${upcomingEvents.size} eventi disponibili" else ""
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (upcomingEvents.isEmpty() && !isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun evento in programma al momento",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                upcomingEvents.forEach { event ->
                    EventCard(
                        event = event,
                        onEventClick = onEventClick,
                        onFavoriteClick = onFavoriteClick,
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
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }

                if (!hasMoreEvents && upcomingEvents.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hai visualizzato tutti gli eventi",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        subtitle?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
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