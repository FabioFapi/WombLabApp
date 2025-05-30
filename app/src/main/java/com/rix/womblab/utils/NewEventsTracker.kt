package com.rix.womblab.utils

import android.content.Context
import android.content.SharedPreferences
import com.rix.womblab.domain.model.Event
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewEventsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "new_events_tracker", Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SEEN_EVENT_IDS = "seen_event_ids"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
    }

    fun findNewEvents(allEvents: List<Event>): List<Event> {
        val seenEventIds = getSeenEventIds()

        return allEvents.filter { event ->
            !seenEventIds.contains(event.id)
        }
    }

    fun markEventsAsSeen(events: List<Event>) {
        val currentSeenIds = getSeenEventIds().toMutableSet()
        val newIds = events.map { it.id }
        currentSeenIds.addAll(newIds)

        val recentIds = currentSeenIds.toList().takeLast(500).toSet()

        prefs.edit()
            .putStringSet(KEY_SEEN_EVENT_IDS, recentIds.toSet())
            .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
            .apply()
    }

    private fun getSeenEventIds(): Set<String> {
        return prefs.getStringSet(KEY_SEEN_EVENT_IDS, emptySet()) ?: emptySet()
    }

    fun isFirstTime(): Boolean {
        return prefs.getLong(KEY_LAST_CHECK_TIME, 0) == 0L
    }

    fun reset() {
        prefs.edit().clear().apply()
    }
}