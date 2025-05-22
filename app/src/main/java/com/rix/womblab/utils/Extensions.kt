package com.rix.womblab.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Date Extensions
fun LocalDateTime.toDisplayString(): String {
    val formatter = DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT_DISPLAY, Locale.getDefault())
    return this.format(formatter)
}

fun LocalDateTime.toDateString(): String {
    val formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
    return this.format(formatter)
}

fun LocalDateTime.toTimeString(): String {
    val formatter = DateTimeFormatter.ofPattern(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault())
    return this.format(formatter)
}

fun LocalDateTime.isToday(): Boolean {
    return this.toLocalDate() == LocalDate.now()
}

fun LocalDateTime.isTomorrow(): Boolean {
    return this.toLocalDate() == LocalDate.now().plusDays(1)
}

fun LocalDateTime.isThisWeek(): Boolean {
    val now = LocalDate.now()
    val startOfWeek = now.minusDays(now.dayOfWeek.value - 1L)
    val endOfWeek = startOfWeek.plusDays(6)
    val eventDate = this.toLocalDate()
    return eventDate in startOfWeek..endOfWeek
}

fun LocalDateTime.toRelativeString(): String {
    return when {
        isToday() -> "Oggi, ${toTimeString()}"
        isTomorrow() -> "Domani, ${toTimeString()}"
        isThisWeek() -> {
            val dayOfWeek = this.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
            "$dayOfWeek, ${toTimeString()}"
        }
        else -> toDisplayString()
    }
}

// String Extensions
fun String.removeHtmlTags(): String {
    return this.replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#8217;", "'")
        .replace("&nbsp;", " ")
        .trim()
}

fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else "${substring(0, maxLength)}..."
}

fun String.toSlug(): String {
    return this.lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("\\s+"), "-")
        .trim('-')
}

// List Extensions
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in 0 until size) this[index] else null
}

fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return if (isEmpty()) emptyList()
    else indices.step(size).map { i ->
        subList(i, minOf(i + size, this.size))
    }
}