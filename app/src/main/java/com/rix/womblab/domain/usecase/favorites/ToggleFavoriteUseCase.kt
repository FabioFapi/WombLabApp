package com.rix.womblab.domain.usecase.favorites

import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, userId: String): Resource<Boolean> {
        return try {
            val isFavorite = eventRepository.isFavorite(eventId, userId)

            if (isFavorite) {
                when (val result = eventRepository.removeFromFavorites(eventId, userId)) {
                    is Resource.Success -> Resource.Success(false)
                    is Resource.Error -> Resource.Error(result.message ?: "Errore nella rimozione")
                    is Resource.Loading -> Resource.Loading()
                }
            } else {
                when (val result = eventRepository.addToFavorites(eventId, userId)) {
                    is Resource.Success -> Resource.Success(true)
                    is Resource.Error -> Resource.Error(result.message ?: "Errore nell'aggiunta")
                    is Resource.Loading -> Resource.Loading()
                }
            }
        } catch (e: Exception) {
            Resource.Error("Errore nel toggle favorito: ${e.message}")
        }
    }
}