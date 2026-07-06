package io.aura.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType
import io.aura.android.domain.repository.GuardianRepository
import io.aura.android.domain.repository.ProfileSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeNotificationsViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository,
    private val profileSettingsRepository: ProfileSettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeNotificationsUiState> = guardianRepository.observeNotifications()
        .combine(profileSettingsRepository.observeSettings()) { notifications, settings ->
            val visibleNotifications = if (!settings.notificationsEnabled) {
                emptyList()
            } else {
                notifications.filter { notification ->
                    when (notification.type) {
                        GuardianNotificationType.GUARDIAN_INVITE -> settings.guardianInviteNotificationsEnabled
                        GuardianNotificationType.SOS_ALERT -> settings.sosAlertNotificationsEnabled
                    }
                }
            }

            HomeNotificationsUiState(
                notifications = visibleNotifications,
                unreadCount = visibleNotifications.count {
                    it.isActionable || it.status == GuardianNotificationStatus.UNREAD
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeNotificationsUiState(),
        )

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            guardianRepository.markNotificationRead(notificationId)
        }
    }

    fun acceptInvite(notificationId: String) {
        viewModelScope.launch {
            guardianRepository.acceptGuardianInvite(notificationId)
        }
    }

    fun declineInvite(notificationId: String) {
        viewModelScope.launch {
            guardianRepository.declineGuardianInvite(notificationId)
        }
    }
}

data class HomeNotificationsUiState(
    val notifications: List<GuardianNotification> = emptyList(),
    val unreadCount: Int = 0,
) {
    val pendingInvite: GuardianNotification? =
        notifications.firstOrNull { it.isActionable }
}

val GuardianNotification.isActionable: Boolean
    get() = type == GuardianNotificationType.GUARDIAN_INVITE &&
        (status == GuardianNotificationStatus.UNREAD || status == GuardianNotificationStatus.READ)
