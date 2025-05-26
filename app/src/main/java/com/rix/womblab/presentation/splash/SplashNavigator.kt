package com.rix.womblab.presentation.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SPLASH_ROUTE = "splash"
const val MAIN_ROUTE = "main"
const val AUTH_ROUTE = "auth"

fun NavGraphBuilder.splashScreen(
    onNavigateToMain: () -> Unit
) {
    composable(route = SPLASH_ROUTE) {
        SplashScreen(
            onNavigateToMain = onNavigateToMain
        )
    }
}

object SplashNavigation {

    fun createMainRoute(): String = MAIN_ROUTE

    fun createAuthRoute(): String = AUTH_ROUTE

    fun createSplashRoute(): String = SPLASH_ROUTE
}