package io.aura.android.domain.repository

import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.IncidentEvidence
import kotlinx.coroutines.flow.Flow

interface IncidentEvidenceRepository {
    fun observeEvidenceForReport(reportId: String): Flow<List<IncidentEvidence>>

    suspend fun addEvidence(
        reportId: String,
        type: EvidenceType,
        sourceUri: String,
    ): IncidentEvidence

    suspend fun deleteLocalEvidence(evidenceId: String)
}
