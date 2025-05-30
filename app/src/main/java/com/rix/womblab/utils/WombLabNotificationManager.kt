package com.rix.womblab.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rix.womblab.R
import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.model.NotificationType
import com.rix.womblab.domain.repository.NotificationRepository
import com.rix.womblab.presentation.MainActivity
import com.rix.womblab.utils.toDateString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WombLabNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    companion object {
        const val CHANNEL_ID_REMINDERS = "womblab_reminders"
        const val CHANNEL_ID_NEW_EVENTS = "womblab_new_events"
        const val NOTIFICATION_ID_BASE = 2000
        const val NOTIFICATION_ID_NEW_EVENTS = 3000
    }

    private val notificationManager = NotificationManagerCompat.from(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Promemoria Eventi",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Promemoria per eventi salvati nei preferiti"
                    enableLights(true)
                    enableVibration(true)
                    setVibrationPattern(longArrayOf(0, 1000, 500, 1000))
                },

                NotificationChannel(
                    CHANNEL_ID_NEW_EVENTS,
                    "Nuovi Eventi",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifiche quando vengono pubblicati nuovi eventi"
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                    enableVibration(true)
                }
            )

            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { systemManager.createNotificationChannel(it) }
        }
    }

    fun showEventReminderNotification(event: Event, hoursBeforeEvent: Int) {
        if (!hasNotificationPermission()) {
            Log.w("NotificationManager", "❌ Permesso notifiche non concesso")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventId", event.id)
            putExtra("openDetail", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode() + hoursBeforeEvent,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message, notificationType) = when (hoursBeforeEvent) {
            24 -> Triple(
                "Evento domani",
                "📅 ${event.title} inizia domani",
                NotificationType.EVENT_REMINDER_24H
            )
            1 -> Triple(
                "Evento tra 1 ora",
                "⏰ ${event.title} inizia tra 1 ora",
                NotificationType.EVENT_REMINDER_1H
            )
            else -> Triple(
                "Evento tra 15 minuti",
                "🔔 ${event.title} inizia tra 15 minuti",
                NotificationType.EVENT_REMINDER_15M
            )
        }

        // Save to database
        scope.launch {
            notificationRepository.addNotification(
                type = notificationType,
                title = title,
                message = message,
                eventId = event.id,
                eventTitle = event.title
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_womblab_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$message\n📍 ${event.venue?.name ?: "Sede da definire"}\n📅 ${event.startDate.toDateString()}"
            ))
            .build()

        notificationManager.notify(
            NOTIFICATION_ID_BASE + event.id.hashCode() + hoursBeforeEvent,
            notification
        )
    }

    fun showNewEventsNotification(newEvents: List<Event>) {
        if (!hasNotificationPermission() || newEvents.isEmpty()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_NEW_EVENTS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message) = if (newEvents.size == 1) {
            "🆕 Nuovo evento disponibile" to "📚 ${newEvents.first().title}"
        } else {
            "🆕 Nuovi eventi disponibili" to "${newEvents.size} nuovi eventi formativi"
        }

        // Save to database
        scope.launch {
            notificationRepository.addNotification(
                type = NotificationType.NEW_EVENTS,
                title = title,
                message = message,
                eventId = null,
                eventTitle = if (newEvents.size == 1) newEvents.first().title else null
            )
        }

        val notification = if (newEvents.size == 1) {
            createSingleNewEventNotification(newEvents.first(), pendingIntent)
        } else {
            createMultipleNewEventsNotification(newEvents, pendingIntent)
        }

        notificationManager.notify(NOTIFICATION_ID_NEW_EVENTS, notification)
    }

    fun showFavoriteNotification(event: Event, isAdded: Boolean) {
        if (!hasNotificationPermission()) return

        val (title, message, type) = if (isAdded) {
            Triple(
                "⭐ Evento salvato",
                "📚 ${event.title} aggiunto ai preferiti",
                NotificationType.FAVORITE_ADDED
            )
        } else {
            Triple(
                "💔 Evento rimosso",
                "📚 ${event.title} rimosso dai preferiti",
                NotificationType.FAVORITE_REMOVED
            )
        }

        // Save to database only
        scope.launch {
            notificationRepository.addNotification(
                type = type,
                title = title,
                message = message,
                eventId = event.id,
                eventTitle = event.title
            )
        }
    }

    private fun createSingleNewEventNotification(event: Event, pendingIntent: PendingIntent): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID_NEW_EVENTS)
            .setContentTitle("🆕 Nuovo evento disponibile")
            .setContentText("📚 ${event.title}")
            .setSmallIcon(R.drawable.ic_womblab_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("📚 ${event.title}\n📍 ${event.venue?.name ?: "Online"}\n📅 ${event.startDate.toDateString()}"))
            .build()
    }

    private fun createMultipleNewEventsNotification(events: List<Event>, pendingIntent: PendingIntent): android.app.Notification {
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("🆕 ${events.size} nuovi eventi disponibili")

        events.take(5).forEach { event ->
            inboxStyle.addLine("📚 ${event.title}")
        }

        if (events.size > 5) {
            inboxStyle.addLine("... e altri ${events.size - 5} eventi")
        }

        return NotificationCompat.Builder(context, CHANNEL_ID_NEW_EVENTS)
            .setContentTitle("🆕 Nuovi eventi disponibili")
            .setContentText("${events.size} nuovi eventi formativi")
            .setSmallIcon(R.drawable.ic_womblab_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setNumber(events.size)
            .setStyle(inboxStyle)
            .build()
    }

    fun cancelEventReminders(eventId: String) {
        listOf(24, 1, 0).forEach { hours ->
            notificationManager.cancel(NOTIFICATION_ID_BASE + eventId.hashCode() + hours)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}