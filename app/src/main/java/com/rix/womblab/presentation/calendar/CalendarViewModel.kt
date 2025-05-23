package com.rix.womblab.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.usecase.events.GetUpcomingEventsUseCase
import com.rix.womblab.domain.usecase.favorites.ToggleFavoriteUseCase
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
    val userId: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getUpcomingEventsUseCase: GetUpcomingEventsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        setupUser()
        loadEvents()
    }

    private fun setupUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { firebaseUser ->
                _uiState.value = _uiState.value.copy(
                    userId = firebaseUser?.uid
                )
            }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getUpcomingEventsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val events = resource.data ?: emptyList()

                        events.forEach { event ->
                            val extractedDate = extractDateFromDescription(event.description)
                        }

                        val eventsForSelectedDate = getEventsForDate(_uiState.value.selectedDate, events)

                        _uiState.value = _uiState.value.copy(
                            events = events,
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
        }
    }

    fun onDateSelected(date: LocalDate) {
        val eventsForDate = getEventsForDate(date, _uiState.value.events)
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            eventsForSelectedDate = eventsForDate
        )
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = yearMonth)
    }

    fun onToggleFavorite(eventId: String) {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(eventId, userId)) {
                is Resource.Success -> {
                    val isFavorite = result.data ?: false
                    updateEventFavoriteStatus(eventId, isFavorite)
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
    }

    private fun getEventsForDate(date: LocalDate, events: List<Event>): List<Event> {
        return events.filter { event ->
            val extractedDate = extractDateFromDescription(event.description)
            if (extractedDate != null) {
                return@filter extractedDate == date
            }

            val eventDate = event.startDate.toLocalDate()
            eventDate == date
        }
    }

    fun getEventsForDate(date: LocalDate): List<Event> {
        return getEventsForDate(date, _uiState.value.events)
    }

    fun hasEventsOnDate(date: LocalDate): Boolean {
        return getEventsForDate(date).isNotEmpty()
    }

    private fun extractDateFromDescription(description: String): LocalDate? {
        return try {
            val parsedInfo = com.rix.womblab.utils.DescriptionParser.parseEventDescription(description)

            parsedInfo.extractedDateTime?.toLocalDate()
                ?: parseItalianDateFromString(parsedInfo.eventDate)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseItalianDateFromString(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null

        return try {
            // Pattern per "29/05/2025"
            val slashPattern = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")
            val slashMatch = slashPattern.find(dateString)

            if (slashMatch != null) {
                val day = slashMatch.groupValues[1].toInt()
                val month = slashMatch.groupValues[2].toInt()
                val year = slashMatch.groupValues[3].toInt()

                return LocalDate.of(year, month, day)
            }

            val monthNames = mapOf(
                "gennaio" to 1, "febbraio" to 2, "marzo" to 3, "aprile" to 4,
                "maggio" to 5, "giugno" to 6, "luglio" to 7, "agosto" to 8,
                "settembre" to 9, "ottobre" to 10, "novembre" to 11, "dicembre" to 12
            )

            val italianPattern = Regex("""(\d{1,2})\s+(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\s+(\d{4})""", RegexOption.IGNORE_CASE)
            val italianMatch = italianPattern.find(dateString)

            if (italianMatch != null) {
                val day = italianMatch.groupValues[1].toInt()
                val monthName = italianMatch.groupValues[2].lowercase()
                val year = italianMatch.groupValues[3].toInt()
                val month = monthNames[monthName] ?: return null

                return LocalDate.of(year, month, day)
            }

            val dashPattern = Regex("""(\d{1,2})-(\d{1,2})-(\d{4})""")
            val dashMatch = dashPattern.find(dateString)

            if (dashMatch != null) {
                val day = dashMatch.groupValues[1].toInt()
                val month = dashMatch.groupValues[2].toInt()
                val year = dashMatch.groupValues[3].toInt()

                return LocalDate.of(year, month, day)
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    fun onRefresh() {
        loadEvents()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}