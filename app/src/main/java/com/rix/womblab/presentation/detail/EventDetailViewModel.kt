package com.rix.womblab.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.model.EventDetail
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.events.GetEventDetailUseCase
import com.rix.womblab.domain.usecase.favorites.IsFavoriteUseCase
import com.rix.womblab.domain.usecase.favorites.ToggleFavoriteUseCase
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val eventDetail: EventDetail? = null,
    val isFavorite: Boolean = false,
    val error: String? = null,
    val userId: String? = null,
    val isTogglingFavorite: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val getEventDetailUseCase: GetEventDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle.get<String>("eventId") ?: ""

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        setupUser()
        loadEventDetail()
    }

    private fun setupUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { firebaseUser ->
                _uiState.value = _uiState.value.copy(
                    userId = firebaseUser?.uid
                )

                firebaseUser?.uid?.let { userId ->
                    checkIfFavorite(userId)
                }
            }
        }
    }

    private fun loadEventDetail() {
        if (eventId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "ID evento non valido",
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = getEventDetailUseCase(eventId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        event = result.data?.event,
                        eventDetail = result.data,
                        isLoading = false
                    )

                    _uiState.value.userId?.let { userId ->
                        checkIfFavorite(userId)
                    }
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Errore nel caricamento evento",
                        isLoading = false
                    )
                }

                is Resource.Loading -> {
                }
            }
        }
    }

    private fun checkIfFavorite(userId: String) {
        viewModelScope.launch {
            val isFav = isFavoriteUseCase(eventId, userId)
            _uiState.value = _uiState.value.copy(isFavorite = isFav)
        }
    }

    fun onToggleFavorite() {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTogglingFavorite = true)

            when (val result = toggleFavoriteUseCase(eventId, userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isFavorite = result.data ?: false,
                        isTogglingFavorite = false
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isTogglingFavorite = false
                    )
                }

                is Resource.Loading -> {
                    // Già gestito
                }
            }
        }
    }

    fun onRetry() {
        loadEventDetail()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun shareEvent() {
        // TODO: Implementare condivisione evento
        val event = _uiState.value.event
        if (event != null) {
            android.util.Log.d("EventDetail", "Condivisione evento: ${event.title}")
        }
    }

    fun openWebsite() {
        val event = _uiState.value.event
        if (event?.website != null) {
            // TODO: Aprire browser con website
            android.util.Log.d("EventDetail", "Apertura website: ${event.website}")
        }
    }
}