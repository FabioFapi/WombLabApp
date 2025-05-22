package com.rix.womblab.domain.usecase.events

import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class SearchEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1): Resource<List<Event>> {
        return if (query.isBlank()) {
            Resource.Error("Query di ricerca vuota")
        } else {
            eventRepository.searchEvents(query, page)
        }
    }
}