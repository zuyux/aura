package io.aura.android.data.repository

import androidx.room.withTransaction
import io.aura.android.data.local.dao.AlertDao
import io.aura.android.data.local.dao.IncidentReportDao
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.AlertEntity
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.mapper.toEntity
import io.aura.android.data.sync.SyncEntityTypes
import io.aura.android.data.sync.SyncScheduler
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.IncidentReport
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.repository.IncidentReportRepository
import java.util.UUID
import javax.inject.Inject

class OfflineFirstIncidentReportRepository @Inject constructor(
    private val database: AuraDatabase,
    private val incidentReportDao: IncidentReportDao,
    private val alertDao: AlertDao,
    private val syncQueueDao: SyncQueueDao,
    private val syncScheduler: SyncScheduler,
) : IncidentReportRepository {
    override suspend fun createLocalReport(report: IncidentReport, queueForSync: Boolean) {
        database.withTransaction {
            incidentReportDao.upsert(report.toEntity())
            if (queueForSync) {
                alertDao.upsertAll(
                    listOf(
                        AlertEntity(
                            id = report.id,
                            reportId = report.id,
                            type = report.type,
                            severity = report.severity,
                            status = AlertStatus.UNVERIFIED,
                            latitude = report.location.latitude,
                            longitude = report.location.longitude,
                            locationPrecision = report.location.precision,
                            summary = report.description,
                            distanceMeters = 0,
                            reportedAtMillis = report.createdAtMillis,
                        ),
                    ),
                )
                syncQueueDao.upsert(
                    SyncQueueEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = SyncEntityTypes.INCIDENT_REPORT,
                        entityId = report.id,
                        operation = SyncOperation.CREATE,
                        priority = SyncPriority.HIGH,
                        status = SyncStatus.PENDING,
                        attempts = 0,
                        lastError = null,
                        createdAtMillis = report.createdAtMillis,
                        updatedAtMillis = report.updatedAtMillis,
                    ),
                )
            }
        }
        if (queueForSync) syncScheduler.scheduleAll()
    }
}
