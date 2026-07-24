package io.aura.android.domain.usecase

import io.aura.android.domain.repository.IncidentEvidenceRepository
import javax.inject.Inject

class DeleteLocalEvidenceUseCase @Inject constructor(
    private val incidentEvidenceRepository: IncidentEvidenceRepository,
) {
    suspend operator fun invoke(evidenceId: String) {
        require(evidenceId.isNotBlank()) { "No se encontró la evidencia local." }
        incidentEvidenceRepository.deleteLocalEvidence(evidenceId)
    }
}
