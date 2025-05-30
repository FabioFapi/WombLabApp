package com.rix.womblab.data.repository

import android.util.Log
import com.rix.womblab.data.local.dao.NotificationDao
import com.rix.womblab.data.local.entities.toDomain
import com.rix.womblab.data.local.entities.toEntity
import com.rix.womblab.domain.model.Notification
import com.rix.womblab.domain.model.NotificationType
import com.rix.womblab.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    companion object {
        private const val TAG = "NotificationRepo"
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    override fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    null
                }
            }.filterNotNull()
        }
    }

    override fun getUnreadNotifications(): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications().map { entities ->
            entities.map { entity ->
                try {
                    entity.toDomain()
                } catch (e: Exception) {
                    null
                }
            }.filterNotNull()
        }
    }

    override fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    override suspend fun addNotification(
        type: NotificationType,
        title: String,
        message: String,
        eventId: String?,
        eventTitle: String?
    ): String {
        return try {
            val notification = Notification(
                id = UUID.randomUUID().toString(),
                type = type,
                title = title,
                message = message,
                eventId = eventId,
                eventTitle = eventTitle,
                timestamp = LocalDateTime.now(),
                isRead = false,
                isVisible = true
            )

            notificationDao.insertNotification(notification.toEntity())
            notification.id
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun markAsRead(id: String) {
        try {
            notificationDao.markAsRead(id, true)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun markAllAsRead() {
        try {
            notificationDao.markAllAsRead()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun hideNotification(id: String) {
        try {
            notificationDao.hideNotification(id)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteNotification(id: String) {
        try {
            notificationDao.deleteNotification(id)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun clearOldNotifications(daysToKeep: Int) {
        try {
            val cutoffDate = LocalDateTime.now().minusDays(daysToKeep.toLong())
            val cutoffDateString = cutoffDate.format(formatter)
            notificationDao.deleteOldNotifications(cutoffDateString)

            val remainingCount = notificationDao.getTotalNotificationsCount()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getNotificationStats(): NotificationStats {
        return try {
            val total = notificationDao.getTotalNotificationsCount()
            val today = notificationDao.getTodayNotificationsCount()
            val unreadCount = notificationDao.getUnreadCount().let { flow ->
                var count = 0
                flow.collect { count = it }
                count
            }

            NotificationStats(
                total = total,
                today = today,
                unread = unreadCount
            )
        } catch (e: Exception) {
            NotificationStats(0, 0, 0)
        }
    }

    suspend fun getNotificationsByType(type: NotificationType): List<Notification> {
        return try {
            val allNotifications = notificationDao.getAllNotifications()
            allNotifications.map { entities ->
                entities.filter { entity ->
                    try {
                        entity.type == type.name
                    } catch (e: Exception) {
                        false
                    }
                }.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        null
                    }
                }
            }.let { flow ->
                var result = emptyList<Notification>()
                flow.collect { result = it }
                result
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun hasUnreadNotifications(): Boolean {
        return try {
            val count = notificationDao.getUnreadCount()
            count.let { flow ->
                var result = false
                flow.collect { result = it > 0 }
                result
            }
        } catch (e: Exception) {
            false
        }
    }
}

data class NotificationStats(
    val total: Int,
    val today: Int,
    val unread: Int
)