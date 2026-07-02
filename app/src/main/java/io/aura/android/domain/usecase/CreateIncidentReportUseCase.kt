package io.aura.android.domain.usecase

import io.aura.android.domain.location.LocationPrivacyService
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.IncidentReport
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.model.ReportVisibility
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.repository.IncidentReportRepository
import java.util.UUID
import javax.inject.Inject

class CreateIncidentReportUseCase @Inject constructor(
    private val incidentReportRepository: IncidentReportRepository,
    private val locationPrivacyService: LocationPrivacyService,
) {
    suspend operator fun invoke(input: CreateIncidentReportInput): IncidentReport {
        require(input.description.length <= MAX_DESCRIPTION_LENGTH) {
            "La descripcion debe tener $MAX_DESCRIPTION_LENGTH caracteres o menos."
        }
        require(input.isLocationConfirmed) {
            "Confirma la ubicacion aproximada del incidente."
        }

        val now = System.currentTimeMillis()
        val publicLocation = locationPrivacyService.applyPrecision(
            location = input.location,
            precision = input.locationPrecision,
        )
        val report = IncidentReport(
            id = UUID.randomUUID().toString(),
            type = input.type,
            severity = input.severity,
            status = ReportStatus.PENDING_SYNC,
            location = publicLocation,
            description = input.description.trim().ifBlank { null },
            visibility = ReportVisibility.COMMUNITY,
            isAnonymous = input.isAnonymous,
            createdAtMillis = now,
            updatedAtMillis = now,
        )

        incidentReportRepository.createLocalReport(report)
        return report
    }

    companion object {
        const val MAX_DESCRIPTION_LENGTH = 500
    }
}

data class CreateIncidentReportInput(
    val type: IncidentType,
    val severity: SeverityLevel,
    val location: AuraLocation,
    val locationPrecision: LocationPrecision,
    val isLocationConfirmed: Boolean,
    val description: String,
    val isAnonymous: Boolean,
)
