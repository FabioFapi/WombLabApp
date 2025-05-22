package com.rix.womblab.domain.repository

import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.model.EventDetail
import com.rix.womblab.domain.model.EventFilter
import com.rix.womblab.domain.model.EventsResponse
import com.rix.womblab.utils.Resource
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEvents(filter: EventFilter): Flow<Resource<EventsResponse>>
    fun getUpcomingEvents(page: Int = 1): Flow<Resource<List<Event>>>
    fun getFeaturedEvents(): Flow<Resource<List<Event>>>
    fun getUserFavoriteEvents(userId: String): Flow<Resource<List<Event>>>
    suspend fun getEventById(eventId: String): Resource<EventDetail>
    suspend fun addToFavorites(eventId: String, userId: String): Resource<Unit>
    suspend fun removeFromFavorites(eventId: String, userId: String): Resource<Unit>
    suspend fun isFavorite(eventId: String, userId: String): Boolean
    suspend fun searchEvents(query: String, page: Int = 1): Resource<List<Event>>
    suspend fun refreshEvents(): Resource<Unit>
}