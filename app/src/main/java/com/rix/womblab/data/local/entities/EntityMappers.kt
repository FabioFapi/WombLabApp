package com.rix.womblab.data.local.entities

import com.rix.womblab.domain.model.*

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        title = title,
        description = description,
        excerpt = excerpt,
        url = url,
        image = imageUrl?.let {
            EventImage(
                url = it,
                id = id,
                width = 600,
                height = 400,
                filesize = 0,
                thumbnailUrl = thumbnailUrl,
                mediumUrl = imageUrl
            )
        },
        startDate = startDate,
        endDate = endDate,
        allDay = allDay,
        timezone = timezone,
        cost = cost,
        website = website,
        venue = if (venueName != null) {
            EventVenue(
                id = "cached_venue",
                name = venueName,
                address = venueAddress ?: "",
                city = venueCity ?: "",
                country = "",
                province = null,
                zip = null,
                website = null,
                showMap = true
            )
        } else null,
        organizer = if (organizerName != null) {
            listOf(
                EventOrganizer(
                    id = "cached_organizer",
                    name = organizerName,
                    email = organizerEmail,
                    phone = null
                )
            )
        } else emptyList(),
        categories = categories.map {
            EventCategory(
                id = "category_$it",
                name = it,
                slug = it.lowercase().replace(" ", "-")
            )
        },
        tags = emptyList(),
        featured = featured,
        status = status,
        isFavorite = false
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        title = title,
        description = description,
        excerpt = excerpt,
        url = url,
        imageUrl = image?.url,
        thumbnailUrl = image?.thumbnailUrl,
        startDate = startDate,
        endDate = endDate,
        allDay = allDay,
        timezone = timezone,
        cost = cost,
        website = website,
        venueName = venue?.name,
        venueAddress = venue?.address,
        venueCity = venue?.city,
        organizerName = organizer.firstOrNull()?.name,
        organizerEmail = organizer.firstOrNull()?.email,
        categories = categories.map { it.name },
        featured = featured,
        status = status
    )
}