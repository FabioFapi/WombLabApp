package com.rix.womblab.domain.usecase.events

import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.model.EventFilter
import com.rix.womblab.domain.model.EventsResponse
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(filter: EventFilter = EventFilter()): Flow<Resource<EventsResponse>> {
        return eventRepository.getEvents(filter)
    }
}