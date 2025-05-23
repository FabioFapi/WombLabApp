package com.rix.womblab.domain.usecase.favorites

import com.rix.womblab.domain.repository.EventRepository
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, userId: String): Boolean {
        return eventRepository.isFavorite(eventId, userId)
    }
}