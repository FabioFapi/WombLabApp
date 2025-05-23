package com.rix.womblab.data.remote.dto

import com.rix.womblab.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun EventsResponseDto.toDomain(): EventsResponse {
    return EventsResponse(
        events = events.map { it.toDomain() },
        total = total,
        totalPages = totalPages,
        restUrl = restUrl
    )
}

fun EventDto.toDomain(): Event {
    return Event(
        id = id.toString(),
        title = title.cleanHtml(),
        description = description.cleanHtml(),
        excerpt = excerpt.cleanHtml(),
        url = url,
        image = image?.toDomain(),
        startDate = parseEventDateFromDetails(utcStartDate, startDate, startDateDetails, utcStartDateDetails, timezone),
        endDate = parseEventDateFromDetails(utcEndDate, endDate, endDateDetails, utcEndDateDetails, timezone),
        allDay = allDay,
        timezone = timezone,
        cost = cost,
        website = website,
        venue = venue?.toDomain(),
        organizer = organizer.map { it.toDomain() },
        categories = categories.map { it.toDomain() },
        tags = tags.map { it.toDomain() },
        featured = featured,
        status = status
    )
}

fun EventImageDto.toDomain(): EventImage {
    val thumbnail = sizes["thumbnail"]
    val medium = sizes["medium"]

    return EventImage(
        url = url,
        id = id.toString(),
        width = width,
        height = height,
        filesize = filesize,
        thumbnailUrl = thumbnail?.url,
        mediumUrl = medium?.url
    )
}

fun EventVenueDto.toDomain(): EventVenue {
    return EventVenue(
        id = id.toString(),
        name = venue,
        address = address,
        city = city,
        country = country,
        province = province ?: stateprovince,
        zip = zip,
        website = website,
        showMap = showMap
    )
}

fun EventOrganizerDto.toDomain(): EventOrganizer {
    return EventOrganizer(
        id = id.toString(),
        name = organizer,
        email = email,
        phone = phone
    )
}

fun EventCategoryDto.toDomain(): EventCategory {
    return EventCategory(
        id = id.toString(),
        name = name,
        slug = slug
    )
}

fun EventTagDto.toDomain(): EventTag {
    return EventTag(
        id = id.toString(),
        name = name,
        slug = slug
    )
}

// Utility functions
private fun parseEventDateFromDetails(
    utcDateString: String?,
    localDateString: String?,
    dateDetails: DateDetailsDto?,
    utcDateDetails: DateDetailsDto?,
    timezoneString: String
): LocalDateTime {
    if (dateDetails == null) {
        return LocalDateTime.now().plusDays(1)
    }
    return try {
        when {
            !utcDateString.isNullOrBlank() -> {
                parseUtcDateString(utcDateString, timezoneString)
            }

            !localDateString.isNullOrBlank() -> {
                parseLocalDate(localDateString)
            }

            utcDateDetails != null -> {
                val utcDateTime = parseDateFromDetails(utcDateDetails)
                convertUtcToLocal(utcDateTime, timezoneString)
            }

            else -> {
                parseDateFromDetails(dateDetails)
            }
        }
    } catch (e: Exception) {
        LocalDateTime.now().plusDays(1)
    }
}

private fun parseDateFromDetails(details: DateDetailsDto): LocalDateTime {
    return try {
        val year = details.year.toIntOrNull() ?: 2025
        val month = details.month.toIntOrNull() ?: 1
        val day = details.day.toIntOrNull() ?: 1
        val hour = details.hour.toIntOrNull() ?: 0
        val minute = details.minutes.toIntOrNull() ?: 0
        val second = details.seconds.toIntOrNull() ?: 0

        val validMonth = month.coerceIn(1, 12)
        val validDay = day.coerceIn(1, 31)
        val validHour = hour.coerceIn(0, 23)
        val validMinute = minute.coerceIn(0, 59)
        val validSecond = second.coerceIn(0, 59)

        val result = LocalDateTime.of(year, validMonth, validDay, validHour, validMinute, validSecond)
        result
    } catch (e: Exception) {
        LocalDateTime.now().plusDays(1)
    }
}

private fun parseUtcDateString(utcDateString: String, timezoneString: String): LocalDateTime {
    return try {
        val utcDateTime = LocalDateTime.parse(utcDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        convertUtcToLocal(utcDateTime, timezoneString)
    } catch (e: Exception) {
        LocalDateTime.now()
    }
}

private fun convertUtcToLocal(utcDateTime: LocalDateTime, timezoneString: String): LocalDateTime {
    return try {
        val utcZoned = utcDateTime.atZone(java.time.ZoneId.of("UTC"))
        val localZoned = utcZoned.withZoneSameInstant(java.time.ZoneId.of(timezoneString))
        val result = localZoned.toLocalDateTime()

        result
    } catch (e: Exception) {
        utcDateTime
    }
}

private fun parseLocalDate(dateString: String): LocalDateTime {
    return try {
        val result = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        result
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(dateString.replace(" ", "T"))
        } catch (e2: Exception) {
            LocalDateTime.now()
        }
    }
}

private fun String.cleanHtml(): String {
    return this
        .replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#8217;", "'")
        .replace("&nbsp;", " ")
        .trim()
}