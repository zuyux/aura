package io.aura.android.data.repository

import io.aura.android.data.local.dao.AlertDao
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstAlertRepository @Inject constructor(
    private val alertDao: AlertDao,
) : AlertRepository {
    override fun observeNearbyAlerts(): Flow<List<Alert>> =
        alertDao.observeAlerts().map { alerts -> alerts.map { it.toDomain() } }

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
