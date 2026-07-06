package io.aura.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.aura.android.data.notification.AuraNotificationChannelSetup
import io.aura.android.data.notification.GuardianSessionNotifier
import io.aura.android.data.notification.NearbyAlertNotifier
import io.aura.android.data.notification.PendingSyncNotifier
import io.aura.android.data.sync.SyncQueueManager
import io.aura.android.data.sync.SyncScheduler
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.repository.DeviceIdentityRepository
import io.aura.android.domain.repository.GuardianRepository
import io.aura.android.domain.repository.AlertRepository
import io.aura.android.domain.repository.ProfileSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltAndroidApp
class AuraApp : Application() {
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var syncQueueManager: SyncQueueManager
    @Inject lateinit var deviceIdentityRepository: DeviceIdentityRepository
    @Inject lateinit var alertRepository: AlertRepository
    @Inject lateinit var guardianRepository: GuardianRepository
    @Inject lateinit var profileSettingsRepository: ProfileSettingsRepository
    @Inject lateinit var notificationChannelSetup: AuraNotificationChannelSetup
    @Inject lateinit var guardianSessionNotifier: GuardianSessionNotifier
    @Inject lateinit var nearbyAlertNotifier: NearbyAlertNotifier
    @Inject lateinit var pendingSyncNotifier: PendingSyncNotifier

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var knownNearbyAlertIds: Set<String>? = null

    override fun onCreate() {
        super.onCreate()
        notificationChannelSetup.createChannels()
        syncScheduler.scheduleAll()
        applicationScope.launch {
            runCatching { deviceIdentityRepository.getOrCreateIdentity() }
        }
        applicationScope.launch {
            guardianRepository.observeSessions()
                .map { sessions -> sessions.firstOrNull { it.isLiveSession } }
                .distinctUntilChangedBy { session -> session?.id to session?.status }
                .collect { session ->
                    if (session == null) {
                        guardianSessionNotifier.cancelActiveSession()
                    } else {
                        guardianSessionNotifier.showActiveSession(session)
                    }
                }
        }
        applicationScope.launch {
            alertRepository.observeNearbyAlerts()
                .combine(profileSettingsRepository.observeSettings()) { alerts, settings ->
                    if (settings.notificationsEnabled) alerts else emptyList()
                }
                .collect { alerts ->
                    val activeAlerts = alerts.filter { alert -> alert.isNotifiable }
                    val activeAlertIds = activeAlerts.mapTo(mutableSetOf()) { alert -> alert.id }
                    val previousIds = knownNearbyAlertIds
                    knownNearbyAlertIds = activeAlertIds

                    if (previousIds == null) return@collect
                    activeAlerts
                        .filterNot { alert -> alert.id in previousIds }
                        .maxByOrNull { alert -> alert.reportedAtMillis }
                        ?.let(nearbyAlertNotifier::showNearbyAlert)
                }
        }
        applicationScope.launch {
            syncQueueManager.observePendingCount()
                .distinctUntilChangedBy { count -> count }
                .collect { pendingCount ->
                    if (pendingCount > 0) {
                        pendingSyncNotifier.showPendingSync(pendingCount)
                    } else {
                        pendingSyncNotifier.cancelPendingSync()
                    }
                }
        }
    }
}

private val io.aura.android.domain.model.SafetySession.isLiveSession: Boolean
    get() = status == io.aura.android.domain.model.SafetySessionStatus.ACTIVE ||
        status == io.aura.android.domain.model.SafetySessionStatus.SOS_TRIGGERED

private val Alert.isNotifiable: Boolean
    get() = status != AlertStatus.RESOLVED && status != AlertStatus.DISMISSED
