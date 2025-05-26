package com.rix.womblab.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rix.womblab.presentation.navigation.WombLabNavigation
import com.rix.womblab.presentation.theme.WombLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WombLabTheme {
                WombLabNavigation()
            }
        }
    }
}