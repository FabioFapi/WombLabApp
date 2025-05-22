package com.rix.womblab.domain.model

data class EventDetail(
    val event: Event,
    val customFields: Map<String, Any>?,
    val relatedEvents: List<Event> = emptyList()
)