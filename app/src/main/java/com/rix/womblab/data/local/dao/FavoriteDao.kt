package com.rix.womblab.data.local.dao

import androidx.room.*
import com.rix.womblab.data.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserFavorites(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT eventId FROM favorites WHERE userId = :userId")
    fun getUserFavoriteIds(userId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE eventId = :eventId AND userId = :userId")
    suspend fun isFavorite(eventId: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE eventId = :eventId AND userId = :userId")
    suspend fun removeFromFavorites(eventId: String, userId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearUserFavorites(userId: String)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}