package io.aura.android.domain.usecase

import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.IncidentEvidence
import io.aura.android.domain.repository.IncidentEvidenceRepository
import javax.inject.Inject

class AddIncidentEvidenceUseCase @Inject constructor(
    private val incidentEvidenceRepository: IncidentEvidenceRepository,
) {
    suspend operator fun invoke(input: AddIncidentEvidenceInput): IncidentEvidence {
        require(input.reportId.isNotBlank()) { "No se encontro el reporte para adjuntar evidencia." }
        require(input.sourceUri.isNotBlank()) { "Selecciona un archivo de evidencia." }

        return incidentEvidenceRepository.addEvidence(
            reportId = input.reportId,
            type = input.type,
            sourceUri = input.sourceUri,
        )
    }
}

data class AddIncidentEvidenceInput(
    val reportId: String,
    val type: EvidenceType,
    val sourceUri: String,
)
