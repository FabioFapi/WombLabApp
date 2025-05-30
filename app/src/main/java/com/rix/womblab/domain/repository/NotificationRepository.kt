package com.rix.womblab.domain.repository

import com.rix.womblab.domain.model.Notification
import com.rix.womblab.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<Notification>>
    fun getUnreadNotifications(): Flow<List<Notification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun addNotification(
        type: NotificationType,
        title: String,
        message: String,
        eventId: String? = null,
        eventTitle: String? = null
    ): String
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun hideNotification(id: String)
    suspend fun deleteNotification(id: String)
    suspend fun clearOldNotifications(daysToKeep: Int = 30)
}