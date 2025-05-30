package com.rix.womblab.presentation

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rix.womblab.presentation.navigation.WombLabNavigation
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.utils.NotificationPermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: NotificationPermissionManager

    private var permissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            WombLabTheme {
                WombLabNavigation()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (!permissionManager.hasNotificationPermission() && !permissionRequested) {
            permissionRequested = true
            permissionManager.requestNotificationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NotificationPermissionManager.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "✅ Permesso notifiche concesso")
                } else {
                    Log.w("MainActivity", "❌ Permesso notifiche negato")
                }
            }
        }
    }
}