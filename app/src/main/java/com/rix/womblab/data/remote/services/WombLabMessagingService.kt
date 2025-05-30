package com.rix.womblab.data.remote.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rix.womblab.utils.WombLabNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WombLabMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "WombLabMessaging"
    }

    @Inject
    lateinit var notificationManager: WombLabNotificationManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📨 Messaggio FCM ricevuto da: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let {
            handleNotificationMessage(it)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        saveTokenLocally(token)

    }

    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "new_event" -> {
                val eventId = data["event_id"]
                val eventTitle = data["event_title"] ?: "Nuovo evento"
            }

            "event_reminder" -> {
                val eventId = data["event_id"]
                val eventTitle = data["event_title"] ?: "Promemoria evento"
            }

            "event_updated" -> {
                val eventId = data["event_id"]
            }

            else -> {
            }
        }
    }

    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
    }

    private fun saveTokenLocally(token: String) {
        try {
            val prefs = getSharedPreferences("womblab_prefs", MODE_PRIVATE)
            prefs.edit()
                .putString("fcm_token", token)
                .putLong("token_timestamp", System.currentTimeMillis())
                .apply()

        } catch (e: Exception) {
        }
    }

    fun getSavedFcmToken(): String? {
        return try {
            val prefs = getSharedPreferences("womblab_prefs", MODE_PRIVATE)
            prefs.getString("fcm_token", null)
        } catch (e: Exception) {
            null
        }
    }
}