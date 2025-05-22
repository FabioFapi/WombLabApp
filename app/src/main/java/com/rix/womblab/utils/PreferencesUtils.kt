package com.rix.womblab.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun setUserId(userId: String) {
        prefs.edit().putString(Constants.PREF_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(Constants.PREF_USER_ID, null)
    }

    fun setLastRefresh(dateTime: LocalDateTime) {
        val timeString = DateUtils.formatForApi(dateTime)
        prefs.edit().putString(Constants.PREF_LAST_REFRESH, timeString).apply()
    }

    fun getLastRefresh(): LocalDateTime? {
        val timeString = prefs.getString(Constants.PREF_LAST_REFRESH, null)
        return timeString?.let { DateUtils.parseFromApi(it) }
    }

    fun shouldRefresh(): Boolean {
        val lastRefresh = getLastRefresh() ?: return true
        val now = LocalDateTime.now()
        val thresholdMinutes = Constants.REFRESH_THRESHOLD_MINUTES
        return lastRefresh.plusMinutes(thresholdMinutes.toLong()).isBefore(now)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(Constants.PREF_ONBOARDING_COMPLETED, false)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}