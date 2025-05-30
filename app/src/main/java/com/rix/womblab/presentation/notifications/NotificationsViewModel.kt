package com.rix.womblab.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.repository.NotificationRepository
import com.rix.womblab.domain.model.Notification
import com.rix.womblab.domain.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val groupedNotifications: Map<String, List<Notification>> = emptyMap(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL
)

enum class NotificationFilter(val displayName: String) {
    ALL("Tutte"),
    UNREAD("Non lette"),
    NEW_EVENTS("Nuovi Eventi"),
    REMINDERS("Promemoria"),
    FAVORITES("Preferiti"),
    SYSTEM("Sistema")
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            notificationRepository.getAllNotifications().collect { notifications ->
                val grouped = groupNotificationsByDate(notifications)
                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    groupedNotifications = grouped,
                    isLoading = false
                )
            }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
    }

    private fun groupNotificationsByDate(notifications: List<Notification>): Map<String, List<Notification>> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)

        return notifications.groupBy { notification ->
            val notificationDate = notification.timestamp.toLocalDate()
            when {
                notificationDate == today -> "Oggi"
                notificationDate == yesterday -> "Ieri"
                notificationDate.isAfter(today.minusDays(7)) -> "Questa settimana"
                notificationDate.isAfter(today.minusDays(30)) -> "Questo mese"
                else -> "Più vecchie"
            }
        }
    }

    fun onNotificationClick(notification: Notification) {
        viewModelScope.launch {
            if (!notification.isRead) {
                notificationRepository.markAsRead(notification.id)
            }

            notification.eventId?.let { eventId ->
            }
        }
    }

    fun onMarkAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun onMarkAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun onDeleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.hideNotification(notificationId)
        }
    }

    fun onFilterChanged(filter: NotificationFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun clearOldNotifications() {
        viewModelScope.launch {
            notificationRepository.clearOldNotifications(30)
        }
    }
}