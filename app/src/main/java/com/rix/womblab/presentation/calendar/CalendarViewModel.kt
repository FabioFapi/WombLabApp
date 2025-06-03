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
import java.time.LocalDateTime
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

    private val parsedDatesCache = mutableMapOf<String, LocalDate?>()
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

        val eventsByDate = mutableMapOf<LocalDate, MutableList<Event>>()

        events.forEach { event ->
            parsedDatesCache[event.id]?.let { parsedDate ->
                eventsByDate.getOrPut(parsedDate) { mutableListOf() }.add(event)
            }
        }

        eventsByDate.forEach { (date, eventList) ->
            eventsByDateCache[date] = eventList.toList()
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                getUpcomingEventsUseCase().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val events = resource.data ?: emptyList()

                            val favoriteIds = _uiState.value.favoriteIds
                            val eventsWithFavorites = events.map { event ->
                                event.copy(isFavorite = favoriteIds.contains(event.id))
                            }

                            buildCacheSafe(eventsWithFavorites)

                            val eventsForSelectedDate = getEventsForDateSafe(_uiState.value.selectedDate, eventsWithFavorites)

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

    private fun buildCacheSafe(events: List<Event>) {
        try {
            if (cacheBuilt && parsedDatesCache.size == events.size) {
                return
            }

            parsedDatesCache.clear()
            eventsByDateCache.clear()

            events.forEach { event ->
                try {
                    val extractedDate = extractDateFromDescriptionSafe(event.description)
                    parsedDatesCache[event.id] = extractedDate
                } catch (e: Exception) {
                    parsedDatesCache[event.id] = null
                }
            }

            val eventsByDate = mutableMapOf<LocalDate, MutableList<Event>>()

            events.forEach { event ->
                try {
                    parsedDatesCache[event.id]?.let { parsedDate ->
                        eventsByDate.getOrPut(parsedDate) { mutableListOf() }.add(event)
                    }
                } catch (e: Exception) {
                }
            }

            eventsByDate.forEach { (date, eventList) ->
                eventsByDateCache[date] = eventList.toList()
            }

            cacheBuilt = true
        } catch (e: Exception) {
            parsedDatesCache.clear()
            eventsByDateCache.clear()
            cacheBuilt = false
        }
    }

    fun onDateSelected(date: LocalDate) {
        try {
            val eventsForDate = if (cacheBuilt && eventsByDateCache.containsKey(date)) {
                eventsByDateCache[date] ?: emptyList()
            } else {
                getEventsForDateSafe(date, _uiState.value.events)
            }

            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                eventsForSelectedDate = eventsForDate
            )
        } catch (e: Exception) {
            val fallbackEvents = _uiState.value.events.filter { event ->
                try {
                    event.startDate.toLocalDate() == date
                } catch (ex: Exception) {
                    false
                }
            }

            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                eventsForSelectedDate = fallbackEvents,
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

                        updateEventFavoriteStatusSafe(eventId, isFavorite)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message
                        )
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

    private fun updateEventFavoriteStatusSafe(eventId: String, isFavorite: Boolean) {
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
                val eventDate = parsedDatesCache[eventId]
                eventDate?.let { date ->
                    val cachedEvents = eventsByDateCache[date]
                    if (cachedEvents != null) {
                        val updatedCachedEvents = cachedEvents.map { event ->
                            if (event.id == eventId) event.copy(isFavorite = isFavorite) else event
                        }
                        eventsByDateCache[date] = updatedCachedEvents
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Errore aggiornamento stato preferito"
            )
        }
    }

    private fun getEventsForDateSafe(date: LocalDate, events: List<Event>): List<Event> {
        return try {
            if (cacheBuilt) {
                eventsByDateCache[date] ?: emptyList()
            } else {
                events.filter { event ->
                    try {
                        val cachedDate = parsedDatesCache[event.id]
                        cachedDate == date
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            events.filter { event ->
                try {
                    event.startDate.toLocalDate() == date
                } catch (ex: Exception) {
                    false
                }
            }
        }
    }

    fun getEventsForDate(date: LocalDate): List<Event> {
        return getEventsForDateSafe(date, _uiState.value.events)
    }

    fun hasEventsOnDate(date: LocalDate): Boolean {
        return try {
            if (cacheBuilt) {
                eventsByDateCache.containsKey(date)
            } else {
                _uiState.value.events.any { event ->
                    try {
                        parsedDatesCache[event.id] == date
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value.events.any { event ->
                try {
                    event.startDate.toLocalDate() == date
                } catch (ex: Exception) {
                    false
                }
            }
        }
    }

    private fun extractDateFromDescriptionSafe(description: String): LocalDate? {
        return try {
            when {
                description.contains("Dal") -> parseWombLabDateSafe(description)
                description.contains("/") -> parseSlashDateSafe(description)
                monthNames.keys.any { description.contains(it, ignoreCase = true) } ->
                    parseItalianDateSafe(description)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseWombLabDateSafe(description: String): LocalDate? {
        return try {
            val match = wombLabPattern.find(description) ?: return null
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year = match.groupValues[3].toInt()
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSlashDateSafe(description: String): LocalDate? {
        return try {
            val match = slashPattern.find(description) ?: return null
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year = match.groupValues[3].toInt()
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseItalianDateSafe(description: String): LocalDate? {
        return try {
            val match = italianPattern.find(description) ?: return null
            val day = match.groupValues[1].toInt()
            val monthName = match.groupValues[2].lowercase()
            val year = match.groupValues[3].toInt()
            val month = monthNames[monthName] ?: return null
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    fun onRefresh() {
        cacheBuilt = false
        parsedDatesCache.clear()
        eventsByDateCache.clear()

        _uiState.value.userId?.let { userId ->
            loadFavorites(userId)
        }
        loadEvents()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}