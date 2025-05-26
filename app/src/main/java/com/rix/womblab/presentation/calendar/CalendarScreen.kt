package com.rix.womblab.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rix.womblab.domain.model.Event
import com.rix.womblab.presentation.components.EmptyState
import com.rix.womblab.presentation.components.ErrorMessage
import com.rix.womblab.presentation.components.EventCard
import com.rix.womblab.presentation.components.LoadingIndicator
import com.rix.womblab.presentation.theme.WombLabTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onEventClick: (String) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Calendario Eventi",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onRefresh() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.error != null && uiState.events.isEmpty() -> {
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
                    CalendarContent(
                        uiState = uiState,
                        onDateSelected = viewModel::onDateSelected,
                        onMonthChanged = viewModel::onMonthChanged,
                        onEventClick = onEventClick,
                        onFavoriteClick = viewModel::onToggleFavorite,
                        hasEventsOnDate = viewModel::hasEventsOnDate
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    hasEventsOnDate: (LocalDate) -> Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            CalendarWidget(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected,
                onMonthChanged = onMonthChanged,
                hasEventsOnDate = hasEventsOnDate
            )
        }

        item {
            SelectedDateSection(
                selectedDate = uiState.selectedDate,
                eventsForDate = uiState.eventsForSelectedDate,
                onEventClick = onEventClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun CalendarWidget(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    hasEventsOnDate: (LocalDate) -> Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            CalendarHeader(
                currentMonth = currentMonth,
                onMonthChanged = onMonthChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeekDaysHeader()

            Spacer(modifier = Modifier.height(8.dp))

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                hasEventsOnDate = hasEventsOnDate
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onMonthChanged(currentMonth.minusMonths(1)) }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Mese precedente"
            )
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALIAN)),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = { onMonthChanged(currentMonth.plusMonths(1)) }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Mese successivo"
            )
        }
    }
}

@Composable
private fun WeekDaysHeader() {
    val weekDays = listOf("L", "M", "M", "G", "V", "S", "D")

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekDays.forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    hasEventsOnDate: (LocalDate) -> Boolean
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    val totalCells = firstDayOfWeek + daysInMonth
    val weeks = (totalCells + 6) / 7

    Column {
        repeat(weeks) { week ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayOfMonth)
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            CalendarDay(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                hasEvents = hasEventsOnDate(date),
                                onClick = { onDateSelected(date) }
                            )
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun SelectedDateSection(
    selectedDate: LocalDate,
    eventsForDate: List<Event>,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.ITALIAN)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                eventsForDate.isEmpty() -> {
                    EmptyState(
                        title = "Nessun evento",
                        description = "Non ci sono eventi programmati per questa data",
                        emoji = "📅",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    Text(
                        text = "${eventsForDate.size} evento${if (eventsForDate.size > 1) "i" else ""}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    eventsForDate.forEach { event ->
                        EventCard(
                            event = event,
                            onEventClick = onEventClick,
                            onFavoriteClick = onFavoriteClick,
                            isCompact = true,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    WombLabTheme {
        CalendarScreen()
    }
}