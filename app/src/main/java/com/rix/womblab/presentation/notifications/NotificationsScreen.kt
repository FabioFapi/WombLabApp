package com.rix.womblab.presentation.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rix.womblab.R
import com.rix.womblab.domain.model.Notification
import com.rix.womblab.domain.model.NotificationType
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.notifications_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (uiState.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AnimatedBadge(count = uiState.unreadCount)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.onMarkAllAsRead() }
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = stringResource(id = R.string.notifications_mark_all_read),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.options))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { stringResource(id = R.string.notifications_clear_old) },
                            onClick = {
                                viewModel.clearOldNotifications()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    AnimatedLoadingState()
                }

                uiState.notifications.isEmpty() -> {
                    AnimatedEmptyNotificationsState()
                }

                else -> {
                    AnimatedNotificationsList(
                        groupedNotifications = uiState.groupedNotifications,
                        onNotificationClick = { notification ->
                            viewModel.onNotificationClick(notification)
                            notification.eventId?.let { eventId ->
                                onNavigateToEvent(eventId)
                            }
                        },
                        onMarkAsRead = viewModel::onMarkAsRead,
                        onDeleteNotification = viewModel::onDeleteNotification
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedBadge(count: Int) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(count) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Badge(
            containerColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun AnimatedLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = stringResource(id = R.string.loading))
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing)
                ),
                label = stringResource(id = R.string.notifications_animated_rotation)
            )

            Text(
                text = stringResource(id = R.string.notifications_emoji),
                fontSize = 48.sp,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )

            Text(
                text = stringResource(id = R.string.notifications_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AnimatedEmptyNotificationsState() {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.notifications_emoji),
                        fontSize = 64.sp
                    )

                    Text(
                        text = stringResource(id = R.string.notifications_nothing),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(id = R.string.notifications_no_new_events),                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedNotificationsList(
    groupedNotifications: Map<String, List<Notification>>,
    onNotificationClick: (Notification) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onDeleteNotification: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedNotifications.forEach { (dateGroup, notifications) ->
            item(key = "header_$dateGroup") {
                AnimatedDateGroupHeader(title = dateGroup)
            }

            items(
                items = notifications,
                key = { notification -> notification.id }
            ) { notification ->
                AnimatedNotificationItem(
                    notification = notification,
                    onClick = { onNotificationClick(notification) },
                    onMarkAsRead = { onMarkAsRead(notification.id) },
                    onDelete = { onDeleteNotification(notification.id) }
                )
            }

            item(key = "spacer_$dateGroup") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AnimatedDateGroupHeader(title: String) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(title) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AnimatedNotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    LaunchedEffect(notification.id) {
        delay(150)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn()
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            NotificationCard(
                notification = notification,
                onClick = onClick,
                onShowOptions = { showOptions = true }
            )

            // Options Menu
            DropdownMenu(
                expanded = showOptions,
                onDismissRequest = { showOptions = false }
            ) {
                if (!notification.isRead) {
                    DropdownMenuItem(
                        text = { stringResource(id = R.string.notifications_dropmenu) },
                        onClick = {
                            onMarkAsRead()
                            showOptions = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Done, contentDescription = null)
                        }
                    )
                }

                DropdownMenuItem(
                    text = { stringResource(id = R.string.delete) },
                    onClick = {
                        onDelete()
                        showOptions = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onShowOptions: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        getNotificationTypeColor(notification.type).copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.type.emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatNotificationTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (notification.isRead) 0.7f else 0.9f
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.eventTitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📚 ${notification.eventTitle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onShowOptions,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.options),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun getNotificationTypeColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.NEW_EVENTS -> Color(0xFF4CAF50)
        NotificationType.EVENT_REMINDER_24H -> Color(0xFF2196F3)
        NotificationType.EVENT_REMINDER_1H -> Color(0xFFFF9800)
        NotificationType.EVENT_REMINDER_15M -> Color(0xFFf44336)
        NotificationType.FAVORITE_ADDED -> Color(0xFFE91E63)
        NotificationType.FAVORITE_REMOVED -> Color(0xFF9E9E9E)
        NotificationType.EVENT_UPDATED -> Color(0xFF9C27B0)
        NotificationType.SYSTEM -> Color(0xFF607D8B)
    }
}

@Composable
private fun formatNotificationTime(timestamp: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val duration = java.time.Duration.between(timestamp, now)

    return when {
        duration.toMinutes() < 1 -> "Ora"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
        duration.toHours() < 24 -> "${duration.toHours()}h"
        duration.toDays() < 7 -> "${duration.toDays()}g"
        else -> timestamp.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
}