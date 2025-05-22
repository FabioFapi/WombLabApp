package com.rix.womblab.domain.usecase.events

import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class RefreshEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return eventRepository.refreshEvents()
    }
}