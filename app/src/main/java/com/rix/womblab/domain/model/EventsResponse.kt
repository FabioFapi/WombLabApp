package com.rix.womblab.domain.model

data class EventsResponse(
    val events: List<Event>,
    val total: Int,
    val totalPages: Int,
    val restUrl: String
)