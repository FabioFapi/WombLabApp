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
        if (eventId.isNotBlank()) {
            loadEventDetail()
        } else {
            _uiState.value = _uiState.value.copy(
                error = "ID evento non valido",
                isLoading = false
            )
        }
    }

    private fun setupUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { firebaseUser ->
                _uiState.value = _uiState.value.copy(
                    userId = firebaseUser?.uid
                )

                // Quando otteniamo l'userId e abbiamo già caricato l'evento,
                // controlliamo se è nei preferiti
                if (firebaseUser?.uid != null && _uiState.value.event != null) {
                    checkIfFavorite(firebaseUser.uid)
                }
            }
        }
    }

    private fun loadEventDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = getEventDetailUseCase(eventId)) {
                is Resource.Success -> {
                    result.data?.let { eventDetail ->
                        // Se l'evento dal repository ha già info sui preferiti, usiamola
                        val isFavorite = eventDetail.event.isFavorite

                        _uiState.value = _uiState.value.copy(
                            event = eventDetail.event,
                            eventDetail = eventDetail,
                            isFavorite = isFavorite,
                            isLoading = false
                        )

                        // Se abbiamo l'userId ma isFavorite è false, facciamo un controllo
                        // per essere sicuri (nel caso il repository non abbia ancora l'info aggiornata)
                        _uiState.value.userId?.let { userId ->
                            if (!isFavorite) {
                                checkIfFavorite(userId)
                            }
                        }
                    } ?: run {
                        _uiState.value = _uiState.value.copy(
                            error = "Evento non trovato",
                            isLoading = false
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Errore nel caricamento evento",
                        isLoading = false
                    )
                }

                is Resource.Loading -> {
                    // Non fare nulla durante il caricamento
                }
            }
        }
    }

    private fun checkIfFavorite(userId: String) {
        viewModelScope.launch {
            try {
                val isFav = isFavoriteUseCase(eventId, userId)
                _uiState.value = _uiState.value.copy(isFavorite = isFav)

                // Aggiorna anche l'evento se presente
                _uiState.value.event?.let { event ->
                    if (event.isFavorite != isFav) {
                        _uiState.value = _uiState.value.copy(
                            event = event.copy(isFavorite = isFav)
                        )
                    }
                }
            } catch (e: Exception) {
                // Log dell'errore ma non mostrare all'utente
                android.util.Log.e("EventDetail", "Errore controllo preferiti", e)
            }
        }
    }

    fun onToggleFavorite() {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTogglingFavorite = true)

            when (val result = toggleFavoriteUseCase(eventId, userId)) {
                is Resource.Success -> {
                    val newFavoriteState = result.data ?: false

                    // Aggiorna sia isFavorite che l'evento stesso
                    _uiState.value = _uiState.value.copy(
                        isFavorite = newFavoriteState,
                        isTogglingFavorite = false,
                        event = _uiState.value.event?.copy(isFavorite = newFavoriteState)
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isTogglingFavorite = false
                    )
                }

                is Resource.Loading -> {
                    // Non fare nulla durante il caricamento
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