package com.rix.womblab.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {

    private val apiFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_API)
    private val displayFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())

    fun formatForApi(dateTime: LocalDateTime): String {
        return dateTime.format(apiFormatter)
    }

    fun formatForDisplay(dateTime: LocalDateTime): String {
        return dateTime.format(displayFormatter)
    }

    fun parseFromApi(dateString: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateString, apiFormatter)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(dateString.replace(" ", "T"))
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun getCurrentWeekRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
        val endOfWeek = startOfWeek.plusDays(6)
        return Pair(startOfWeek, endOfWeek)
    }

    fun getCurrentMonthRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        return Pair(startOfMonth, endOfMonth)
    }

    fun isEventUpcoming(eventDateTime: LocalDateTime): Boolean {
        return eventDateTime.isAfter(LocalDateTime.now())
    }

    fun isEventToday(eventDateTime: LocalDateTime): Boolean {
        return eventDateTime.toLocalDate() == LocalDate.now()
    }

    fun getEventStatus(startDate: LocalDateTime, endDate: LocalDateTime): EventStatus {
        val now = LocalDateTime.now()
        return when {
            now.isBefore(startDate) -> EventStatus.UPCOMING
            now.isAfter(endDate) -> EventStatus.PAST
            else -> EventStatus.ONGOING
        }
    }

    enum class EventStatus {
        UPCOMING, ONGOING, PAST
    }
}