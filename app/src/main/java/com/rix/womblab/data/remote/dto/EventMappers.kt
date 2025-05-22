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
        startDate = parseDate(startDate),
        endDate = parseDate(endDate),
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
private fun parseDate(dateString: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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