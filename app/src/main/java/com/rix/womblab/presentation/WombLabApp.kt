package com.rix.womblab.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rix.womblab.presentation.navigation.WombLabNavigation
import com.rix.womblab.presentation.theme.WombLabTheme

@Composable
fun WombLabApp() {
    WombLabTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            WombLabNavigation(navController = navController)
        }
    }
}