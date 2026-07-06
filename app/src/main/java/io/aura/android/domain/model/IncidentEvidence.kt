package io.aura.android.domain.model

data class IncidentEvidence(
    val id: String,
    val reportId: String,
    val type: EvidenceType,
    val localUri: String,
    val remoteUrl: String?,
    val sha256Hash: String,
    val visibility: EvidenceVisibility,
    val createdAtMillis: Long,
)
