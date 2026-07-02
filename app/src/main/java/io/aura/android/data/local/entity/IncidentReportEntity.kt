package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.model.ReportVisibility
import io.aura.android.domain.model.SeverityLevel

@Entity(tableName = "incident_reports")
data class IncidentReportEntity(
    @PrimaryKey val id: String,
    val type: IncidentType,
    val severity: SeverityLevel,
    val status: ReportStatus,
    val latitude: Double,
    val longitude: Double,
    val locationPrecision: LocationPrecision,
    val description: String?,
    val visibility: ReportVisibility,
    val isAnonymous: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
