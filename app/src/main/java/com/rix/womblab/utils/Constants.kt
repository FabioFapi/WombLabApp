package com.rix.womblab.utils

object Constants {

    // API
    const val WOMBLAB_BASE_URL = "https://www.womblab.com/"
    const val EVENTS_ENDPOINT = "wp-json/tribe/events/v1/events"

    // Pagination
    const val DEFAULT_PAGE_SIZE = 15
    const val DEFAULT_PAGE = 1

    // Cache
    const val CACHE_EXPIRY_HOURS = 24
    const val REFRESH_THRESHOLD_MINUTES = 30

    // Database
    const val DATABASE_NAME = "womblab_database"
    const val DATABASE_VERSION = 1

    // SharedPreferences
    const val PREF_NAME = "womblab_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_LAST_REFRESH = "last_refresh"
    const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"

    // Date Formats
    const val DATE_FORMAT_API = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val TIME_FORMAT_DISPLAY = "HH:mm"
    const val DATETIME_FORMAT_DISPLAY = "dd MMM yyyy, HH:mm"

    // UI
    const val ANIMATION_DURATION_SHORT = 300
    const val ANIMATION_DURATION_MEDIUM = 500
    const val ANIMATION_DURATION_LONG = 800

    // Event Categories
    val POPULAR_CATEGORIES
        get() = listOf(
            "WOMBLAB",
            "Chirurgia Vascolare",
            "Anestesia",
            "Cardiologia",
            "Medicina"
        )
}