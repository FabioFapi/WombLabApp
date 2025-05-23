package com.rix.womblab.domain.usecase.favorites

import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class RemoveFromFavoritesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, userId: String): Resource<Unit> {
        return eventRepository.removeFromFavorites(eventId, userId)
    }
}