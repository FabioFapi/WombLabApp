package com.rix.womblab.data.repository

import com.rix.womblab.data.local.dao.EventDao
import com.rix.womblab.data.local.dao.FavoriteDao
import com.rix.womblab.data.local.entities.FavoriteEntity
import com.rix.womblab.data.local.entities.toDomain
import com.rix.womblab.data.local.entities.toEntity
import com.rix.womblab.data.remote.api.WordPressApi
import com.rix.womblab.data.remote.dto.toDomain
import com.rix.womblab.domain.model.*
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.domain.repository.EventRepository
import com.rix.womblab.utils.NewEventsTracker
import com.rix.womblab.utils.Resource
import com.rix.womblab.utils.WombLabNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val wordPressApi: WordPressApi,
    private val eventDao: EventDao,
    private val favoriteDao: FavoriteDao,
    private val authRepository: AuthRepository,
    private val newEventsTracker: NewEventsTracker,
    private val notificationManager: WombLabNotificationManager
) : EventRepository {

    companion object {
        private const val TAG = "EventRepositoryImpl"
    }

    private suspend fun enrichEventsWithFavorites(
        events: List<Event>,
        userId: String?
    ): List<Event> {
        if (userId == null) return events

        return try {
            val favoriteIds = favoriteDao.getUserFavoriteIds(userId).first().toSet()

            events.map { event ->
                event.copy(isFavorite = favoriteIds.contains(event.id))
            }
        } catch (e: Exception) {
            events
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return authRepository.getCurrentUser()?.uid
    }

    override fun getEvents(filter: EventFilter): Flow<Resource<EventsResponse>> = flow {
        emit(Resource.Loading())

        try {
            val userId = getCurrentUserId()

            val cachedEvents = eventDao.getAllEvents()
            cachedEvents.collect { entities ->
                if (entities.isNotEmpty()) {
                    var events = entities.map { entity -> entity.toDomain() }
                    events = enrichEventsWithFavorites(events, userId)

                    val response = EventsResponse(
                        events = events,
                        total = events.size,
                        totalPages = 1,
                        restUrl = ""
                    )
                    emit(Resource.Success(response))
                }
            }

            val startDate = filter.startDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val endDate = filter.endDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val categoriesString = filter.categories.joinToString(",")

            val response = wordPressApi.getEvents(
                page = filter.page,
                perPage = filter.perPage,
                startDate = startDate,
                endDate = endDate,
                search = filter.searchQuery,
                categories = categoriesString.ifEmpty { null },
                featured = filter.featured
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    try {
                        val domainResponse = apiResponse.toDomain()
                        var newEvents = domainResponse.events

                        newEvents = enrichEventsWithFavorites(newEvents, userId)

                        if (!newEventsTracker.isFirstTime()) {
                            val unseenEvents = newEventsTracker.findNewEvents(newEvents)
                            if (unseenEvents.isNotEmpty()) {
                                notificationManager.showNewEventsNotification(unseenEvents)
                            }
                        }

                        newEventsTracker.markEventsAsSeen(newEvents)

                        try {
                            val entities = newEvents.map { event -> event.toEntity() }
                            eventDao.insertEvents(entities)

                            val enrichedResponse = domainResponse.copy(events = newEvents)
                            emit(Resource.Success(enrichedResponse))
                        } catch (e: Exception) {
                            emit(Resource.Error("Errore salvataggio: ${e.message}"))
                        }
                    } catch (e: Exception) {
                        emit(Resource.Error("Errore mapping: ${e.message}"))
                    }
                } ?: run {
                    emit(Resource.Error("Response vuota"))
                }
            } else {
                emit(Resource.Error("Errore nel caricamento eventi: ${response.message()}"))
            }

        } catch (e: Exception) {
            emit(Resource.Error("Errore di rete: ${e.message}"))
        }
    }

    override fun getUpcomingEvents(page: Int): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())

        try {
            val userId = getCurrentUserId()

            eventDao.getUpcomingEvents().collect { entities ->
                if (entities.isNotEmpty()) {
                    var events = entities.map { entity -> entity.toDomain() }
                    events = enrichEventsWithFavorites(events, userId)
                    emit(Resource.Success(events))
                }
            }

            val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val response = wordPressApi.getUpcomingEvents(
                page = page,
                perPage = 15,
                startDate = currentDate
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    try {
                        var events = apiResponse.events.map { eventDto -> eventDto.toDomain() }

                        events = enrichEventsWithFavorites(events, userId)

                        val entities = events.map { event -> event.toEntity() }
                        eventDao.insertEvents(entities)

                        emit(Resource.Success(events))
                    } catch (e: Exception) {
                        emit(Resource.Error("Errore processing: ${e.message}"))
                    }
                } ?: run {
                    emit(Resource.Error("Response vuota"))
                }
            } else {
                emit(Resource.Error("Errore nel caricamento eventi: ${response.message()}"))
            }

        } catch (e: Exception) {
            emit(Resource.Error("Errore di rete: ${e.message}"))
        }
    }

    override fun getFeaturedEvents(): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())

        try {
            val userId = getCurrentUserId()

            val cachedEntities = eventDao.getFeaturedEvents().first()

            if (cachedEntities.isNotEmpty()) {
                var events = cachedEntities.map { entity -> entity.toDomain() }
                events = enrichEventsWithFavorites(events, userId)
                emit(Resource.Success(events))
                return@flow
            }

            val response = wordPressApi.getFeaturedEvents()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    var events = apiResponse.events.map { eventDto -> eventDto.toDomain() }

                    events = enrichEventsWithFavorites(events, userId)

                    val entities = events.map { event -> event.toEntity() }
                    eventDao.insertEvents(entities)

                    emit(Resource.Success(events))
                } ?: run {
                    emit(Resource.Error("Response vuota"))
                }
            } else {
                val errorMsg = "Errore API: ${response.code()} - ${response.message()}"
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Errore di rete: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }

    override fun getUserFavoriteEvents(userId: String): Flow<Resource<List<Event>>> {
        return favoriteDao.getUserFavoriteIds(userId).map { favoriteIds ->
            try {
                val events = mutableListOf<Event>()
                favoriteIds.forEach { eventId ->
                    eventDao.getEventById(eventId)?.let { entity ->
                        events.add(entity.toDomain().copy(isFavorite = true))
                    }
                }
                Resource.Success(events)
            } catch (e: Exception) {
                Resource.Error("Errore nel caricamento preferiti: ${e.message}")
            }
        }
    }

    override suspend fun getEventById(eventId: String): Resource<EventDetail> {
        return try {
            val userId = getCurrentUserId()

            val cachedEvent = eventDao.getEventById(eventId)
            if (cachedEvent != null) {
                var event = cachedEvent.toDomain()

                if (userId != null) {
                    event = event.copy(isFavorite = isFavorite(eventId, userId))
                }

                return Resource.Success(EventDetail(event = event, customFields = null))
            }

            val response = wordPressApi.getEventById(eventId)

            if (response.isSuccessful) {
                response.body()?.let { eventDto ->
                    var event = eventDto.toDomain()

                    if (userId != null) {
                        event = event.copy(isFavorite = isFavorite(eventId, userId))
                    }

                    eventDao.insertEvent(event.toEntity())

                    Resource.Success(
                        EventDetail(
                            event = event,
                            customFields = eventDto.customFields
                        )
                    )
                } ?: Resource.Error("Evento non trovato")
            } else {
                Resource.Error("Errore nel caricamento evento: ${response.message()}")
            }

        } catch (e: Exception) {
            Resource.Error("Errore di rete: ${e.message}")
        }
    }

    override suspend fun addToFavorites(eventId: String, userId: String): Resource<Unit> {
        return try {
            val favorite = FavoriteEntity(
                eventId = eventId,
                userId = userId,
                addedAt = LocalDateTime.now()
            )
            favoriteDao.addToFavorites(favorite)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore nell'aggiungere ai preferiti: ${e.message}")
        }
    }

    override suspend fun removeFromFavorites(eventId: String, userId: String): Resource<Unit> {
        return try {
            favoriteDao.removeFromFavorites(eventId, userId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Errore nella rimozione dai preferiti: ${e.message}")
        }
    }

    override suspend fun isFavorite(eventId: String, userId: String): Boolean {
        return try {
            favoriteDao.isFavorite(eventId, userId) > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchEvents(query: String, page: Int): Resource<List<Event>> {
        return try {
            val userId = getCurrentUserId()

            val response = wordPressApi.searchEvents(
                searchQuery = query,
                page = page,
                perPage = 15
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    var events = apiResponse.events.map { eventDto -> eventDto.toDomain() }

                    // Arricchisci con info preferiti
                    events = enrichEventsWithFavorites(events, userId)

                    val entities = events.map { event -> event.toEntity() }
                    eventDao.insertEvents(entities)

                    Resource.Success(events)
                } ?: Resource.Error("Nessun risultato trovato")
            } else {
                Resource.Error("Errore nella ricerca: ${response.message()}")
            }

        } catch (e: Exception) {
            Resource.Error("Errore di rete: ${e.message}")
        }
    }

    override suspend fun refreshEvents(): Resource<Unit> {
        return try {
            val userId = getCurrentUserId()

            eventDao.deleteOldEvents(LocalDateTime.now().minusDays(1))

            val response = wordPressApi.getUpcomingEvents(
                page = 1,
                perPage = 50,
                startDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    var events = apiResponse.events.map { eventDto -> eventDto.toDomain() }

                    // Arricchisci con info preferiti
                    events = enrichEventsWithFavorites(events, userId)

                    if (!newEventsTracker.isFirstTime()) {
                        val unseenEvents = newEventsTracker.findNewEvents(events)
                        if (unseenEvents.isNotEmpty()) {
                            notificationManager.showNewEventsNotification(unseenEvents)
                        }
                    }

                    newEventsTracker.markEventsAsSeen(events)

                    val entities = events.map { event -> event.toEntity() }
                    eventDao.insertEvents(entities)
                }
                Resource.Success(Unit)
            } else {
                Resource.Error("Errore nell'aggiornamento: ${response.message()}")
            }

        } catch (e: Exception) {
            Resource.Error("Errore di rete: ${e.message}")
        }
    }
}