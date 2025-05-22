package com.rix.womblab.domain.usecase.events

import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(page: Int = 1): Flow<Resource<List<Event>>> {
        return eventRepository.getUpcomingEvents(page)
    }
}