package com.rix.womblab.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rix.womblab.presentation.splash.SplashScreen
import com.rix.womblab.presentation.theme.WombLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WombLabTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
private fun MainAppContainer() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onNavigateToMain = {
                showSplash = false
            }
        )
    } else {
        com.rix.womblab.presentation.navigation.MainScreen(
            onLogout = {
                showSplash = true
            }
        )
    }
}