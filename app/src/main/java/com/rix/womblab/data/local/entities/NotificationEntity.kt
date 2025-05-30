package com.rix.womblab.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.rix.womblab.domain.model.Notification
import com.rix.womblab.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val eventId: String?,
    val eventTitle: String?,
    val timestamp: String,
    val isRead: Boolean = false,
    val isVisible: Boolean = true
)

class DateTimeConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? {
        return dateString?.let { LocalDateTime.parse(it, formatter) }
    }
}

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        type = NotificationType.valueOf(type),
        title = title,
        message = message,
        eventId = eventId,
        eventTitle = eventTitle,
        timestamp = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        isRead = isRead,
        isVisible = isVisible
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        type = type.name,
        title = title,
        message = message,
        eventId = eventId,
        eventTitle = eventTitle,
        timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        isRead = isRead,
        isVisible = isVisible
    )
}