package io.aura.android.domain.model

data class IncidentReport(
    val id: String,
    val type: IncidentType,
    val severity: SeverityLevel,
    val status: ReportStatus,
    val location: AuraLocation,
    val description: String?,
    val visibility: ReportVisibility,
    val isAnonymous: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
