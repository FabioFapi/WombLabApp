package com.rix.womblab.presentation.navigation

sealed class Screen(val route: String) {

    data object Splash : Screen("splash")

    data object Login : Screen("login")

    data object Register : Screen("register")

    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Notifications : Screen("notifications")
    data object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    data object Main : Screen("main")
}

object NavigationRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val EVENT_DETAIL = "event_detail"
    const val NOTIFICATIONS = "notifications"
}