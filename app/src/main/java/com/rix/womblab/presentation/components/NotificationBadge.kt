package com.rix.womblab.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rix.womblab.domain.repository.NotificationRepository
import javax.inject.Inject

@Composable
fun NotificationIconWithBadge(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificationRepository: NotificationRepository
) {
    val unreadCount by notificationRepository.getUnreadCount().collectAsState(initial = 0)

    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifiche",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (unreadCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}