package com.rix.womblab.domain.model

import java.time.LocalDateTime

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val excerpt: String,
    val url: String,
    val image: EventImage?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val allDay: Boolean,
    val timezone: String,
    val cost: String,
    val website: String?,
    val venue: EventVenue?,
    val organizer: List<EventOrganizer>,
    val categories: List<EventCategory>,
    val tags: List<EventTag>,
    val isFavorite: Boolean = false,
    val featured: Boolean,
    val status: String
)

data class EventImage(
    val url: String,
    val id: String,
    val width: Int,
    val height: Int,
    val filesize: Int,
    val thumbnailUrl: String?,
    val mediumUrl: String?
)

data class EventVenue(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String?,
    val zip: String?,
    val website: String?,
    val showMap: Boolean
)

data class EventOrganizer(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?
)

data class EventCategory(
    val id: String,
    val name: String,
    val slug: String
)

data class EventTag(
    val id: String,
    val name: String,
    val slug: String
)