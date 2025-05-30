package com.rix.womblab.domain.model

import java.time.LocalDateTime

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val eventId: String? = null,
    val eventTitle: String? = null,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false,
    val isVisible: Boolean = true
)

enum class NotificationType(
    val emoji: String,
    val displayName: String,
    val priority: NotificationPriority
) {
    NEW_EVENTS("🆕", "Nuovi Eventi", NotificationPriority.NORMAL),
    EVENT_REMINDER_24H("📅", "Promemoria", NotificationPriority.HIGH),
    EVENT_REMINDER_1H("⏰", "Promemoria", NotificationPriority.HIGH),
    EVENT_REMINDER_15M("🔔", "Promemoria", NotificationPriority.URGENT),
    FAVORITE_ADDED("⭐", "Preferiti", NotificationPriority.LOW),
    FAVORITE_REMOVED("💔", "Preferiti", NotificationPriority.LOW),
    EVENT_UPDATED("📝", "Aggiornamenti", NotificationPriority.NORMAL),
    SYSTEM("⚙️", "Sistema", NotificationPriority.LOW)
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}