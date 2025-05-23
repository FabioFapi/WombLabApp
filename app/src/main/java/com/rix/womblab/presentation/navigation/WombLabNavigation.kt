package com.rix.womblab.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rix.womblab.presentation.auth.login.LoginScreen
import com.rix.womblab.presentation.auth.login.LoginViewModel
import com.rix.womblab.presentation.auth.register.RegisterScreen
import kotlinx.coroutines.delay

@Composable
fun WombLabNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { needsRegistration ->
                    if (needsRegistration) {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    println("🚀 Navigation: onNavigateBack called")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegistrationSuccess = {
                    println("🚀 Navigation: onRegistrationSuccess called")
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Main App con Bottom Navigation
        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}

@Composable
private fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // Aspetta 2 secondi per mostrare lo splash
        delay(2000)

        // Naviga in base allo stato dell'autenticazione
        when {
            // Utente non autenticato -> Login
            !loginState.isLoggedIn -> {
                println("🚀 Splash: Utente non autenticato -> Login")
                onNavigateToLogin()
            }
            // Utente autenticato ma registrazione non completata -> Register
            loginState.isLoggedIn && !loginState.isRegistrationComplete -> {
                println("🚀 Splash: Utente autenticato ma registrazione incompleta -> Register")
                onNavigateToRegister()
            }
            // Utente autenticato e registrazione completata -> Main
            loginState.isLoggedIn && loginState.isRegistrationComplete -> {
                println("🚀 Splash: Utente pronto -> Main")
                onNavigateToMain()
            }
            // Fallback -> Login
            else -> {
                println("🚀 Splash: Fallback -> Login")
                onNavigateToLogin()
            }
        }
    }

    // Mostra messaggio di debug se c'è un errore
    LaunchedEffect(loginState.error) {
        loginState.error?.let { error ->
            println("🚀 Splash Error: $error")
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF006B5B),
                        androidx.compose.ui.graphics.Color(0xFF004D42)
                    )
                )
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            // Logo
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier
                    .size(120.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(60.dp)),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "🔬",
                        fontSize = 48.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))

            androidx.compose.material3.Text(
                text = "WombLab",
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            // Stato di debug
            androidx.compose.material3.Text(
                text = when {
                    loginState.isLoading -> "Controllo autenticazione..."
                    loginState.error != null -> "Errore: ${loginState.error}"
                    loginState.isLoggedIn && loginState.isRegistrationComplete -> "Benvenuto!"
                    loginState.isLoggedIn && !loginState.isRegistrationComplete -> "Completa registrazione..."
                    else -> "Caricamento..."
                },
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))

            androidx.compose.material3.CircularProgressIndicator(
                color = androidx.compose.ui.graphics.Color.White,
                strokeWidth = 2.dp
            )
        }
    }
}