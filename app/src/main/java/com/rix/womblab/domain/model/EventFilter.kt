package com.rix.womblab.domain.model

import java.time.LocalDateTime

data class EventFilter(
    val searchQuery: String? = null,
    val categories: List<String> = emptyList(),
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val city: String? = null,
    val featured: Boolean? = null,
    val page: Int = 1,
    val perPage: Int = 15
)