package com.rix.womblab.utils

import android.content.Context
import android.content.SharedPreferences
import com.rix.womblab.presentation.auth.register.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    private val json = Json { ignoreUnknownKeys = true }

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

    // Nuovi metodi per il profilo utente
    fun setUserProfile(profile: UserProfile) {
        try {
            val profileJson = json.encodeToString(profile)
            prefs.edit().putString(PREF_USER_PROFILE, profileJson).apply()
            println("🚀 PreferencesUtils: Profile saved: $profileJson")
        } catch (e: Exception) {
            println("❌ PreferencesUtils: Error saving profile: ${e.message}")
        }
    }

    fun getUserProfile(): UserProfile? {
        return try {
            val profileJson = prefs.getString(PREF_USER_PROFILE, null)
            if (profileJson != null) {
                json.decodeFromString<UserProfile>(profileJson)
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ PreferencesUtils: Error loading profile: ${e.message}")
            null
        }
    }

    fun setRegistrationCompleted(completed: Boolean) {
        prefs.edit().putBoolean(Constants.PREF_REGISTRATION_COMPLETED, completed).apply()
        println("🚀 PreferencesUtils: Registration completed set to: $completed")
    }

    fun isRegistrationCompleted(): Boolean {
        val completed = prefs.getBoolean(Constants.PREF_REGISTRATION_COMPLETED, false)
        println("🚀 PreferencesUtils: Registration completed: $completed")
        return completed
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_USER_PROFILE = "user_profile"
    }
}