package io.aura.android.data.repository

import io.aura.android.data.local.dao.AlertDao
import io.aura.android.data.local.dao.ReportVerificationDao
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportVerification
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.model.VerificationAction
import io.aura.android.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class OfflineFirstAlertRepository @Inject constructor(
    private val alertDao: AlertDao,
    private val reportVerificationDao: ReportVerificationDao,
) : AlertRepository {
    override fun observeNearbyAlerts(): Flow<List<Alert>> =
        alertDao.observeAlerts().map { alerts -> alerts.map { it.toDomain() } }

    override fun observeAlert(alertId: String): Flow<Alert?> =
        alertDao.observeAlert(alertId).map { alert -> alert?.toDomain() }

    override suspend fun recordVerification(alertId: String, action: VerificationAction) {
        val alert = alertDao.getAlert(alertId) ?: return
        val verificationTargetId = alert.reportId ?: alert.id

        reportVerificationDao.upsert(
            ReportVerification(
                id = UUID.randomUUID().toString(),
                reportId = verificationTargetId,
                action = action,
                deviceId = null,
                createdAtMillis = System.currentTimeMillis(),
            ).toEntity(),
        )

        when (action) {
            VerificationAction.ALSO_SEEN -> alertDao.updateStatus(alertId, AlertStatus.COMMUNITY_CONFIRMED)
            VerificationAction.SEEMS_FALSE -> alertDao.updateStatus(alertId, AlertStatus.DISMISSED)
            VerificationAction.RESOLVED -> alertDao.updateStatus(alertId, AlertStatus.RESOLVED)
            VerificationAction.HIDE_ALERT -> alertDao.updateStatus(alertId, AlertStatus.DISMISSED)
        }
    }

    override suspend fun seedDemoAlertsIfEmpty() {
        if (alertDao.countAlerts() > 0) return

        val now = System.currentTimeMillis()
        alertDao.upsertAll(
            listOf(
                Alert(
                    id = "demo-alert-theft-miraflores",
                    reportId = null,
                    type = IncidentType.THEFT,
                    severity = SeverityLevel.HIGH,
                    status = AlertStatus.UNVERIFIED,
                    location = AuraLocation(
                        latitude = -12.1196,
                        longitude = -77.0365,
                        precision = LocationPrecision.APPROXIMATE,
                    ),
                    summary = "Robo de celular reportado cerca de una avenida concurrida.",
                    distanceMeters = 450,
                    reportedAtMillis = now - 12 * MINUTE_MILLIS,
                ),
                Alert(
                    id = "demo-alert-dangerous-area-barranco",
                    reportId = null,
                    type = IncidentType.DANGEROUS_AREA,
                    severity = SeverityLevel.MEDIUM,
                    status = AlertStatus.COMMUNITY_CONFIRMED,
                    location = AuraLocation(
                        latitude = -12.1437,
                        longitude = -77.0206,
                        precision = LocationPrecision.DISTRICT_ONLY,
                    ),
                    summary = "Vecinos reportan poca iluminacion y actividad sospechosa.",
                    distanceMeters = 1200,
                    reportedAtMillis = now - 48 * MINUTE_MILLIS,
                ),
                Alert(
                    id = "demo-alert-accident-surco",
                    reportId = null,
                    type = IncidentType.ACCIDENT,
                    severity = SeverityLevel.LOW,
                    status = AlertStatus.RESOLVED,
                    location = AuraLocation(
                        latitude = -12.1286,
                        longitude = -76.9847,
                        precision = LocationPrecision.APPROXIMATE,
                    ),
                    summary = "Accidente menor ya despejado por serenazgo.",
                    distanceMeters = 2600,
                    reportedAtMillis = now - 2 * HOUR_MILLIS,
                ),
            ).map { it.toEntity() },
        )
    }
}

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
