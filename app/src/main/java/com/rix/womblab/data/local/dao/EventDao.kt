package com.rix.womblab.data.local.dao

import androidx.room.*
import com.rix.womblab.data.local.entities.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY startDate ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events WHERE startDate >= :currentDate ORDER BY startDate ASC")
    fun getUpcomingEvents(currentDate: LocalDateTime = LocalDateTime.now()): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE startDate < :currentDate ORDER BY startDate DESC")
    fun getPastEvents(currentDate: LocalDateTime = LocalDateTime.now()): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE featured = 1 ORDER BY startDate ASC")
    fun getFeaturedEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY startDate ASC")
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE lastUpdated < :cutoffDate")
    suspend fun deleteOldEvents(cutoffDate: LocalDateTime)

    @Query("DELETE FROM events")
    suspend fun clearAllEvents()

    @Query("SELECT * FROM events WHERE id IN (:eventIds) ORDER BY startDate ASC")
    suspend fun getEventsByIds(eventIds: List<String>): List<EventEntity>

    @Query("SELECT * FROM events WHERE id IN (:eventIds) ORDER BY startDate ASC")
    fun getEventsByIdsFlow(eventIds: List<String>): Flow<List<EventEntity>>

    @Query("SELECT id FROM events WHERE id IN (:eventIds)")
    suspend fun getExistingEventIds(eventIds: List<String>): List<String>

    @Query("SELECT id FROM events")
    suspend fun getAllEventIds(): List<String>

    @Query("""
        SELECT e.* FROM events e
        INNER JOIN favorites f ON e.id = f.eventId
        WHERE f.userId = :userId
        ORDER BY e.startDate ASC
    """)
    fun getFavoriteEventsByUser(userId: String): Flow<List<EventEntity>>
}