package com.rix.womblab.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.events.GetUpcomingEventsUseCase
import com.rix.womblab.domain.usecase.events.GetFeaturedEventsUseCase
import com.rix.womblab.domain.usecase.events.RefreshEventsUseCase
import com.rix.womblab.domain.usecase.events.SearchEventsUseCase
import com.rix.womblab.domain.usecase.favorites.GetFavoritesUseCase
import com.rix.womblab.domain.usecase.favorites.ToggleFavoriteUseCase
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val featuredEvents: List<Event> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val favoriteEvents: List<Event> = emptyList(),
    val searchResults: List<Event> = emptyList(),
    val error: String? = null,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val userId: String? = null,
    val currentPage: Int = 1,
    val hasMoreEvents: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUpcomingEventsUseCase: GetUpcomingEventsUseCase,
    private val getFeaturedEventsUseCase: GetFeaturedEventsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val refreshEventsUseCase: RefreshEventsUseCase,
    private val searchEventsUseCase: SearchEventsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        setupUser()
        loadInitialData()
    }

    private fun setupUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { firebaseUser ->
                _uiState.value = _uiState.value.copy(
                    userId = firebaseUser?.uid
                )

                firebaseUser?.uid?.let { userId ->
                    loadFavoriteEvents(userId)
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                launch { loadFeaturedEvents() }

                launch { loadUpcomingEvents() }
            } catch (e: Exception) {
            }
        }
    }

    private fun loadFeaturedEvents() {
        viewModelScope.launch {
            getFeaturedEventsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            featuredEvents = resource.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = resource.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    private fun loadUpcomingEvents(page: Int = 1) {
        viewModelScope.launch {
            getUpcomingEventsUseCase(page).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val newEvents = resource.data ?: emptyList()

                        _uiState.value = _uiState.value.copy(
                            upcomingEvents = if (page == 1) {
                                newEvents
                            } else {
                                _uiState.value.upcomingEvents + newEvents
                            },
                            isLoading = false,
                            hasMoreEvents = newEvents.size >= 15,
                            currentPage = page
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = resource.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        if (page == 1) {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    private fun loadFavoriteEvents(userId: String) {
        viewModelScope.launch {
            getFavoritesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            favoriteEvents = resource.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            when (val result = refreshEventsUseCase()) {
                is Resource.Success -> {
                    loadInitialData()
                    _uiState.value.userId?.let { loadFavoriteEvents(it) }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isRefreshing = false
                    )
                }
                is Resource.Loading -> {
                    // Già gestito
                }
            }

            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false
            )
        } else {
            searchEvents(query)
        }
    }

    private fun searchEvents(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)

            when (val result = searchEventsUseCase(query)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        searchResults = result.data ?: emptyList(),
                        isSearching = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isSearching = false
                    )
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    fun onToggleFavorite(eventId: String) {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(eventId, userId)) {
                is Resource.Success -> {
                    updateEventFavoriteStatus(eventId, result.data ?: false)

                    loadFavoriteEvents(userId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    private fun updateEventFavoriteStatus(eventId: String, isFavorite: Boolean) {
        _uiState.value = _uiState.value.copy(
            featuredEvents = _uiState.value.featuredEvents.map { event ->
                if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
            },
            upcomingEvents = _uiState.value.upcomingEvents.map { event ->
                if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
            },
            searchResults = _uiState.value.searchResults.map { event ->
                if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
            }
        )
    }

    fun loadMoreEvents() {
        if (_uiState.value.hasMoreEvents && !_uiState.value.isLoading) {
            loadUpcomingEvents(_uiState.value.currentPage + 1)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false
        )
    }
}