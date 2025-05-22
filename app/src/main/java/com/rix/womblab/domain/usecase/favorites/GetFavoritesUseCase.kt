package com.rix.womblab.domain.usecase.favorites

import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<Event>>> {
        return eventRepository.getUserFavoriteEvents(userId)
    }
}