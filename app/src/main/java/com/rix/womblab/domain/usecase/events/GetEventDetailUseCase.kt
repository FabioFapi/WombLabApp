package com.rix.womblab.domain.usecase.events

import com.rix.womblab.domain.model.EventDetail
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class GetEventDetailUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Resource<EventDetail> {
        return eventRepository.getEventById(eventId)
    }
}