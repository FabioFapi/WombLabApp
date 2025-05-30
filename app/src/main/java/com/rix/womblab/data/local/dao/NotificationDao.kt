package com.rix.womblab.data.local.dao

import androidx.room.*
import com.rix.womblab.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE isVisible = 1 ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isVisible = 1 AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isVisible = 1 AND isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE isVisible = 1 AND DATE(timestamp) = DATE(:date) ORDER BY timestamp DESC")
    fun getNotificationsByDate(date: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :id")
    suspend fun markAsRead(id: String, isRead: Boolean = true)

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("UPDATE notifications SET isVisible = 0 WHERE id = :id")
    suspend fun hideNotification(id: String)

    @Query("DELETE FROM notifications WHERE timestamp < :cutoffDate")
    suspend fun deleteOldNotifications(cutoffDate: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    @Query("SELECT COUNT(*) FROM notifications WHERE isVisible = 1")
    suspend fun getTotalNotificationsCount(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE isVisible = 1 AND DATE(timestamp) = DATE('now')")
    suspend fun getTodayNotificationsCount(): Int
}