package io.aura.android.domain.model

data class Alert(
    val id: String,
    val reportId: String?,
    val type: IncidentType,
    val severity: SeverityLevel,
    val status: AlertStatus,
    val location: AuraLocation,
    val summary: String?,
    val distanceMeters: Int?,
    val reportedAtMillis: Long,
)
