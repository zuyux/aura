package io.aura.android.data.repository

import androidx.room.withTransaction
import io.aura.android.data.local.dao.AlertDao
import io.aura.android.data.local.dao.ReportVerificationDao
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.data.network.NetworkMonitor
import io.aura.android.data.network.mapNetworkErrors
import io.aura.android.data.remote.mapper.toDomain
import io.aura.android.data.sync.SyncEntityTypes
import io.aura.android.data.sync.SyncScheduler
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.ReportVerification
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.model.VerificationAction
import io.aura.android.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import io.aura.android.data.remote.IncidentRemoteDataSource

class OfflineFirstAlertRepository @Inject constructor(
    private val database: AuraDatabase,
    private val alertDao: AlertDao,
    private val reportVerificationDao: ReportVerificationDao,
    private val syncQueueDao: SyncQueueDao,
    private val incidentRemoteDataSource: IncidentRemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    private val syncScheduler: SyncScheduler,
) : AlertRepository {
    override fun observeNearbyAlerts(): Flow<List<Alert>> =
        alertDao.observeAlerts().map { alerts -> alerts.map { it.toDomain() } }

    override fun observeAlert(alertId: String): Flow<Alert?> =
        alertDao.observeAlert(alertId).map { alert -> alert?.toDomain() }

    override suspend fun recordVerification(alertId: String, action: VerificationAction) {
        val alert = alertDao.getAlert(alertId) ?: return
        val verificationTargetId = alert.reportId ?: alert.id

        val now = System.currentTimeMillis()
        val verification = ReportVerification(
            id = UUID.randomUUID().toString(),
            reportId = verificationTargetId,
            action = action,
            deviceId = null,
            createdAtMillis = now,
        )

        database.withTransaction {
            reportVerificationDao.upsert(verification.toEntity())
            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = "${SyncEntityTypes.REPORT_VERIFICATION}:${verification.id}",
                    entityType = SyncEntityTypes.REPORT_VERIFICATION,
                    entityId = verification.id,
                    operation = SyncOperation.CREATE,
                    priority = SyncPriority.NORMAL,
                    status = SyncStatus.PENDING,
                    attempts = 0,
                    lastError = null,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                ),
            )

            when (action) {
                VerificationAction.ALSO_SEEN -> alertDao.updateStatus(alertId, AlertStatus.COMMUNITY_CONFIRMED)
                VerificationAction.SEEMS_FALSE -> alertDao.updateStatus(alertId, AlertStatus.DISMISSED)
                VerificationAction.RESOLVED -> alertDao.updateStatus(alertId, AlertStatus.RESOLVED)
                VerificationAction.HIDE_ALERT -> alertDao.updateStatus(alertId, AlertStatus.DISMISSED)
            }
        }
        syncScheduler.scheduleAll()
    }

    override suspend fun refreshNearbyAlerts(location: AuraLocation, radiusMeters: Int) {
        if (!networkMonitor.isOnline()) return

        runCatching {
            mapNetworkErrors {
                incidentRemoteDataSource.getCommunityReports()
            }
        }.onSuccess { remoteAlerts ->
            alertDao.upsertAll(remoteAlerts.map { alert -> alert.toDomain().toEntity() })
        }
    }

}
