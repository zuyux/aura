package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel

@Entity(
    tableName = "alerts",
    indices = [Index(value = ["reportedAtMillis"]), Index(value = ["status"])],
)
data class AlertEntity(
    @PrimaryKey val id: String,
    val reportId: String?,
    val type: IncidentType,
    val severity: SeverityLevel,
    val status: AlertStatus,
    val latitude: Double,
    val longitude: Double,
    val locationPrecision: LocationPrecision,
    val summary: String?,
    val distanceMeters: Int?,
    val reportedAtMillis: Long,
)
