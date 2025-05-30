package com.rix.womblab.utils

import android.content.Context
import androidx.work.*
import com.rix.womblab.domain.model.Event
import com.rix.womblab.workers.EventReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleEventReminders(event: Event) {
        val reminders = listOf(24, 1, 0)

        reminders.forEach { hoursBeforeEvent ->
            scheduleReminder(event, hoursBeforeEvent)
        }
    }

    private fun scheduleReminder(event: Event, hoursBeforeEvent: Int) {
        val now = LocalDateTime.now()
        val eventTime = event.startDate

        val reminderTime = when (hoursBeforeEvent) {
            24 -> eventTime.minusHours(24)
            1 -> eventTime.minusHours(1)
            else -> eventTime.minusMinutes(15)
        }

        if (reminderTime.isBefore(now)) {
            return
        }

        val delayDuration = Duration.between(now, reminderTime)

        val inputData = workDataOf(
            "eventId" to event.id,
            "hoursBeforeEvent" to hoursBeforeEvent
        )

        val workRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delayDuration.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("event_reminder_${event.id}")
            .build()

        workManager.enqueueUniqueWork(
            "reminder_${event.id}_${hoursBeforeEvent}h",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelEventReminders(eventId: String) {
        workManager.cancelAllWorkByTag("event_reminder_$eventId")
    }
}