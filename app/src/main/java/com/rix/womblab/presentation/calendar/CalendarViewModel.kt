package com.rix.womblab.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.usecase.events.GetUpcomingEventsUseCase
import com.rix.womblab.domain.usecase.favorites.ToggleFavoriteUseCase
import com.rix.womblab.domain.usecase.favorites.GetFavoritesUseCase
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val eventsForSelectedDate: List<Event> = emptyList(),
    val error: String? = null,
    val userId: String? = null,
    val favoriteIds: Set<String> = emptySet()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getUpcomingEventsUseCase: GetUpcomingEventsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val eventsByDateCache = mutableMapOf<LocalDate, List<Event>>()
    private var cacheBuilt = false

    private val wombLabPattern = Regex("""Dal\s+(\d{1,2})-(\d{1,2})-(\d{4})""")
    private val slashPattern = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")
    private val italianPattern = Regex(
        """(\d{1,2})\s+(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\s+(\d{4})""",
        RegexOption.IGNORE_CASE
    )
    private val monthNames = mapOf(
        "gennaio" to 1, "febbraio" to 2, "marzo" to 3, "aprile" to 4,
        "maggio" to 5, "giugno" to 6, "luglio" to 7, "agosto" to 8,
        "settembre" to 9, "ottobre" to 10, "novembre" to 11, "dicembre" to 12
    )

    init {
        setupUser()
        loadEvents()
    }

    private fun setupUser() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { firebaseUser ->
                    _uiState.value = _uiState.value.copy(
                        userId = firebaseUser?.uid
                    )
                    firebaseUser?.uid?.let { userId ->
                        loadFavorites(userId)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore configurazione utente: ${e.message}"
                )
            }
        }
    }

    private fun loadFavorites(userId: String) {
        viewModelScope.launch {
            getFavoritesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val favoriteIds = resource.data?.map { it.id }?.toSet() ?: emptySet()
                        _uiState.value = _uiState.value.copy(favoriteIds = favoriteIds)
                        updateEventsWithFavorites()
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    private fun updateEventsWithFavorites() {
        val favoriteIds = _uiState.value.favoriteIds

        val updatedEvents = _uiState.value.events.map { event ->
            event.copy(isFavorite = favoriteIds.contains(event.id))
        }

        val updatedEventsForSelectedDate = _uiState.value.eventsForSelectedDate.map { event ->
            event.copy(isFavorite = favoriteIds.contains(event.id))
        }

        _uiState.value = _uiState.value.copy(
            events = updatedEvents,
            eventsForSelectedDate = updatedEventsForSelectedDate
        )

        if (cacheBuilt) {
            rebuildCacheWithUpdatedEvents(updatedEvents)
        }
    }

    private fun rebuildCacheWithUpdatedEvents(events: List<Event>) {
        eventsByDateCache.clear()
        buildOptimizedCache(events)
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                getUpcomingEventsUseCase().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val events = resource.data ?: emptyList()
                            val eventsWithFavorites = updateEventsWithFavoriteStatus(events)

                            buildOptimizedCache(eventsWithFavorites)

                            val eventsForSelectedDate = getEventsForDate(_uiState.value.selectedDate)

                            _uiState.value = _uiState.value.copy(
                                events = eventsWithFavorites,
                                eventsForSelectedDate = eventsForSelectedDate,
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
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore caricamento eventi: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun buildOptimizedCache(events: List<Event>) {
        try {
            eventsByDateCache.clear()

            events.forEach { event ->
                try {
                    val parsedDate = extractDateFromDescription(event.description)

                    val eventDate = parsedDate ?: event.startDate.toLocalDate()

                    val existingEvents = eventsByDateCache[eventDate] ?: emptyList()
                    eventsByDateCache[eventDate] = existingEvents + event

                } catch (_: Exception) {
                    try {
                        val fallbackDate = event.startDate.toLocalDate()
                        val existingEvents = eventsByDateCache[fallbackDate] ?: emptyList()
                        eventsByDateCache[fallbackDate] = existingEvents + event
                    } catch (_: Exception) {
                    }
                }
            }

            cacheBuilt = true

        } catch (_: Exception) {
            buildSimpleCache(events)
        }
    }

    private fun buildSimpleCache(events: List<Event>) {
        eventsByDateCache.clear()
        events.forEach { event ->
            try {
                val date = event.startDate.toLocalDate()
                val existingEvents = eventsByDateCache[date] ?: emptyList()
                eventsByDateCache[date] = existingEvents + event
            } catch (_: Exception) {
            }
        }
        cacheBuilt = true
    }

    private fun updateEventsWithFavoriteStatus(events: List<Event>): List<Event> {
        val favoriteIds = _uiState.value.favoriteIds
        return events.map { event ->
            event.copy(isFavorite = favoriteIds.contains(event.id))
        }
    }

    fun onDateSelected(date: LocalDate) {
        try {
            val eventsForDate = getEventsForDate(date)
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                eventsForSelectedDate = eventsForDate
            )
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                eventsForSelectedDate = emptyList(),
                error = "Problema caricamento eventi per data selezionata"
            )
        }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = yearMonth)
    }

    fun onToggleFavorite(eventId: String) {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            try {
                when (val result = toggleFavoriteUseCase(eventId, userId)) {
                    is Resource.Success -> {
                        val isFavorite = result.data ?: false

                        val updatedFavoriteIds = if (isFavorite) {
                            _uiState.value.favoriteIds + eventId
                        } else {
                            _uiState.value.favoriteIds - eventId
                        }

                        _uiState.value = _uiState.value.copy(favoriteIds = updatedFavoriteIds)
                        updateEventFavoriteStatus(eventId, isFavorite)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore aggiornamento preferito: ${e.message}"
                )
            }
        }
    }

    private fun updateEventFavoriteStatus(eventId: String, isFavorite: Boolean) {
        try {
            val updatedEvents = _uiState.value.events.map { event ->
                if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
            }

            val updatedEventsForSelectedDate = _uiState.value.eventsForSelectedDate.map { event ->
                if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
            }

            _uiState.value = _uiState.value.copy(
                events = updatedEvents,
                eventsForSelectedDate = updatedEventsForSelectedDate
            )

            if (cacheBuilt) {
                eventsByDateCache.forEach { (date, events) ->
                    val updatedEvents = events.map { event ->
                        if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
                    }
                    eventsByDateCache[date] = updatedEvents
                }
            }
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(error = "Errore aggiornamento stato preferito")
        }
    }

    fun getEventsForDate(date: LocalDate): List<Event> {
        return try {
            if (cacheBuilt) {
                eventsByDateCache[date] ?: emptyList()
            } else {
                _uiState.value.events.filter { event ->
                    try {
                        event.startDate.toLocalDate() == date
                    } catch (_: Exception) {
                        false
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun hasEventsOnDate(date: LocalDate): Boolean {
        return try {
            if (cacheBuilt) {
                eventsByDateCache.containsKey(date)
            } else {
                _uiState.value.events.any { event ->
                    try {
                        event.startDate.toLocalDate() == date
                    } catch (_: Exception) {
                        false
                    }
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun extractDateFromDescription(description: String): LocalDate? {
        return try {
            parseWombLabDate(description)?.let { return it }

            parseSlashDate(description)?.let { return it }

            parseItalianDate(description)?.let { return it }

            null

        } catch (_: Exception) {
            null
        }
    }

    private fun parseWombLabDate(description: String): LocalDate? {
        return try {
            val match = wombLabPattern.find(description) ?: return null
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year = match.groupValues[3].toInt()
            LocalDate.of(year, month, day)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSlashDate(description: String): LocalDate? {
        return try {
            val match = slashPattern.find(description) ?: return null
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year = match.groupValues[3].toInt()
            LocalDate.of(year, month, day)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseItalianDate(description: String): LocalDate? {
        return try {
            val match = italianPattern.find(description) ?: return null
            val day = match.groupValues[1].toInt()
            val monthName = match.groupValues[2].lowercase()
            val year = match.groupValues[3].toInt()
            val month = monthNames[monthName] ?: return null
            LocalDate.of(year, month, day)
        } catch (_: Exception) {
            null
        }
    }

    fun onRefresh() {
        cacheBuilt = false
        eventsByDateCache.clear()

        _uiState.value.userId?.let { userId ->
            loadFavorites(userId)
        }
        loadEvents()
    }

}