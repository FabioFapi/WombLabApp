package com.rix.womblab.utils

object Constants {

    const val WOMBLAB_BASE_URL = "https://www.womblab.com/"
    const val EVENTS_ENDPOINT = "wp-json/tribe/events/v1/events"

    const val DEFAULT_PAGE_SIZE = 15
    const val DEFAULT_PAGE = 1

    const val CACHE_EXPIRY_HOURS = 24
    const val REFRESH_THRESHOLD_MINUTES = 30

    const val DATABASE_NAME = "womblab_database"
    const val DATABASE_VERSION = 1

    const val PREF_NAME = "womblab_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_LAST_REFRESH = "last_refresh"
    const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
    const val PREF_REGISTRATION_COMPLETED = "registration_completed"

    const val DATE_FORMAT_API = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val TIME_FORMAT_DISPLAY = "HH:mm"
    const val DATETIME_FORMAT_DISPLAY = "dd MMM yyyy, HH:mm"

    const val ANIMATION_DURATION_SHORT = 300
    const val ANIMATION_DURATION_MEDIUM = 500
    const val ANIMATION_DURATION_LONG = 800

    val POPULAR_CATEGORIES
        get() = listOf(
            "WOMBLAB",
            "Chirurgia Vascolare",
            "Anestesia",
            "Cardiologia",
            "Medicina"
        )

    val MEDICAL_PROFESSIONS
        get() = listOf(
            "Medico Chirurgo",
            "Odontoiatra",
            "Veterinario",
            "Farmacista",
            "Biologo",
            "Infermiere",
            "Ostetrica/o",
            "Fisioterapista",
            "Tecnico di Radiologia",
            "Tecnico di Laboratorio",
            "Dietista",
            "Logopedista",
            "Psicologo",
            "Assistente Sanitario",
            "Tecnico della Prevenzione",
            "Igienista Dentale",
            "Ortottista",
            "Terapista Occupazionale",
            "Tecnico Ortopedico",
            "Tecnico Audiometrista",
            "Tecnico Neurofisiopatologia",
            "Tecnico della Riabilitazione Psichiatrica",
            "Educatore Professionale",
            "Altro"
        )

    val MEDICAL_SPECIALIZATIONS
        get() = listOf(
            "Anestesia e Rianimazione",
            "Cardiologia",
            "Chirurgia Generale",
            "Chirurgia Vascolare",
            "Dermatologia",
            "Endocrinologia",
            "Gastroenterologia",
            "Ginecologia e Ostetricia",
            "Medicina Interna",
            "Neurologia",
            "Oncologia",
            "Ortopedia",
            "Pediatria",
            "Pneumologia",
            "Psichiatria",
            "Radiologia",
            "Urologia",
            "Medicina d'Urgenza",
            "Medicina del Lavoro",
            "Medicina Legale",
            "Igiene e Sanità Pubblica",
            "Medicina Fisica e Riabilitativa",
            "Altro"
        )
}