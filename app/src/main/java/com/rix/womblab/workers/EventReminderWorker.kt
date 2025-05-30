package com.rix.womblab.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rix.womblab.data.local.dao.EventDao
import com.rix.womblab.data.local.entities.toDomain
import com.rix.womblab.utils.WombLabNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EventReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventDao: EventDao,
    private val notificationManager: WombLabNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val eventId = inputData.getString("eventId") ?: return Result.failure()
            val hoursBeforeEvent = inputData.getInt("hoursBeforeEvent", 24)

            val eventEntity = eventDao.getEventById(eventId)
            if (eventEntity == null) {
                return Result.success()
            }

            val event = eventEntity.toDomain()

            notificationManager.showEventReminderNotification(event, hoursBeforeEvent)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}