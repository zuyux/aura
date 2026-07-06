package io.aura.android.domain.usecase

import io.aura.android.domain.location.LocationPrivacyService
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.EvidenceVisibility
import io.aura.android.domain.model.IncidentEvidence
import io.aura.android.domain.model.IncidentReport
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.model.ReportVisibility
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.repository.IncidentEvidenceRepository
import io.aura.android.domain.repository.IncidentReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentUseCasesTest {
    private val locationPrivacyService = LocationPrivacyService()

    @Test
    fun `create incident report stores pending sync community report`() = runBlocking {
        val repository = FakeIncidentReportRepository()
        val useCase = CreateIncidentReportUseCase(repository, locationPrivacyService)

        val report = useCase(
            CreateIncidentReportInput(
                type = IncidentType.THEFT,
                severity = SeverityLevel.HIGH,
                location = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.EXACT),
                locationPrecision = LocationPrecision.APPROXIMATE,
                isLocationConfirmed = true,
                description = "  Bike stolen near the park  ",
                isAnonymous = true,
            ),
        )

        assertEquals(report, repository.createdReport)
        assertTrue(repository.queueForSync)
        assertNotNull(report.id)
        assertEquals(ReportStatus.PENDING_SYNC, report.status)
        assertEquals(ReportVisibility.COMMUNITY, report.visibility)
        assertEquals(AuraLocation(latitude = -12.046, longitude = -77.043, precision = LocationPrecision.APPROXIMATE), report.location)
        assertEquals("Bike stolen near the park", report.description)
        assertTrue(report.isAnonymous)
        assertEquals(report.createdAtMillis, report.updatedAtMillis)
        assertTrue(report.createdAtMillis > 0)
    }

    @Test
    fun `create incident report never stores exact public location`() = runBlocking {
        val repository = FakeIncidentReportRepository()
        val useCase = CreateIncidentReportUseCase(repository, locationPrivacyService)

        val report = useCase(
            CreateIncidentReportInput(
                type = IncidentType.THEFT,
                severity = SeverityLevel.HIGH,
                location = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.EXACT),
                locationPrecision = LocationPrecision.EXACT,
                isLocationConfirmed = true,
                description = "Bike stolen near the park",
                isAnonymous = true,
            ),
        )

        assertEquals(ReportVisibility.COMMUNITY, report.visibility)
        assertEquals(AuraLocation(latitude = -12.046, longitude = -77.043, precision = LocationPrecision.APPROXIMATE), report.location)
    }

    @Test
    fun `create incident report rejects unconfirmed location`() {
        val repository = FakeIncidentReportRepository()
        val useCase = CreateIncidentReportUseCase(repository, locationPrivacyService)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(
                    CreateIncidentReportInput(
                        type = IncidentType.THEFT,
                        severity = SeverityLevel.MEDIUM,
                        location = AuraLocation(latitude = -12.046374, longitude = -77.042793),
                        locationPrecision = LocationPrecision.APPROXIMATE,
                        isLocationConfirmed = false,
                        description = "Report",
                        isAnonymous = false,
                    ),
                )
            }
        }
        assertNull(repository.createdReport)
    }

    @Test
    fun `create incident report rejects too long description`() {
        val repository = FakeIncidentReportRepository()
        val useCase = CreateIncidentReportUseCase(repository, locationPrivacyService)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(
                    CreateIncidentReportInput(
                        type = IncidentType.THEFT,
                        severity = SeverityLevel.MEDIUM,
                        location = AuraLocation(latitude = -12.046374, longitude = -77.042793),
                        locationPrecision = LocationPrecision.APPROXIMATE,
                        isLocationConfirmed = true,
                        description = "x".repeat(CreateIncidentReportUseCase.MAX_DESCRIPTION_LENGTH + 1),
                        isAnonymous = false,
                    ),
                )
            }
        }
        assertNull(repository.createdReport)
    }

    @Test
    fun `save incident report draft stores private draft without queueing sync`() = runBlocking {
        val repository = FakeIncidentReportRepository()
        val useCase = SaveIncidentReportDraftUseCase(repository, locationPrivacyService)

        val report = useCase(
            SaveIncidentReportDraftInput(
                type = IncidentType.SUSPICIOUS_PERSON,
                severity = SeverityLevel.LOW,
                location = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.EXACT),
                locationPrecision = LocationPrecision.DISTRICT_ONLY,
                description = "   ",
                isAnonymous = false,
            ),
        )

        assertEquals(report, repository.createdReport)
        assertFalse(repository.queueForSync)
        assertEquals(ReportStatus.DRAFT, report.status)
        assertEquals(ReportVisibility.PRIVATE, report.visibility)
        assertEquals(AuraLocation(latitude = -12.05, longitude = -77.04, precision = LocationPrecision.DISTRICT_ONLY), report.location)
        assertNull(report.description)
        assertFalse(report.isAnonymous)
    }

    @Test
    fun `save incident report draft rejects too long description`() {
        val repository = FakeIncidentReportRepository()
        val useCase = SaveIncidentReportDraftUseCase(repository, locationPrivacyService)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(
                    SaveIncidentReportDraftInput(
                        type = IncidentType.OTHER,
                        severity = SeverityLevel.LOW,
                        location = AuraLocation(latitude = -12.046374, longitude = -77.042793),
                        locationPrecision = LocationPrecision.APPROXIMATE,
                        description = "x".repeat(CreateIncidentReportUseCase.MAX_DESCRIPTION_LENGTH + 1),
                        isAnonymous = true,
                    ),
                )
            }
        }
        assertNull(repository.createdReport)
    }

    @Test
    fun `add incident evidence delegates valid input to repository`() = runBlocking {
        val repository = FakeIncidentEvidenceRepository()
        val useCase = AddIncidentEvidenceUseCase(repository)

        val evidence = useCase(
            AddIncidentEvidenceInput(
                reportId = "report-1",
                type = EvidenceType.PHOTO,
                sourceUri = "content://evidence/photo.jpg",
            ),
        )

        assertEquals("report-1", repository.reportId)
        assertEquals(EvidenceType.PHOTO, repository.type)
        assertEquals("content://evidence/photo.jpg", repository.sourceUri)
        assertEquals(repository.evidence, evidence)
    }

    @Test
    fun `add incident evidence rejects blank report id`() {
        val repository = FakeIncidentEvidenceRepository()
        val useCase = AddIncidentEvidenceUseCase(repository)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(
                    AddIncidentEvidenceInput(
                        reportId = " ",
                        type = EvidenceType.PHOTO,
                        sourceUri = "content://evidence/photo.jpg",
                    ),
                )
            }
        }
        assertNull(repository.reportId)
    }

    @Test
    fun `add incident evidence rejects blank source uri`() {
        val repository = FakeIncidentEvidenceRepository()
        val useCase = AddIncidentEvidenceUseCase(repository)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(
                    AddIncidentEvidenceInput(
                        reportId = "report-1",
                        type = EvidenceType.VIDEO,
                        sourceUri = " ",
                    ),
                )
            }
        }
        assertNull(repository.sourceUri)
    }

    @Test
    fun `delete local evidence delegates valid id to repository`() = runBlocking {
        val repository = FakeIncidentEvidenceRepository()
        val useCase = DeleteLocalEvidenceUseCase(repository)

        useCase("evidence-1")

        assertEquals("evidence-1", repository.deletedEvidenceId)
    }

    @Test
    fun `delete local evidence rejects blank id`() {
        val repository = FakeIncidentEvidenceRepository()
        val useCase = DeleteLocalEvidenceUseCase(repository)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(" ") }
        }
        assertNull(repository.deletedEvidenceId)
    }
}

private class FakeIncidentReportRepository : IncidentReportRepository {
    var createdReport: IncidentReport? = null
    var queueForSync = false

    override suspend fun createLocalReport(report: IncidentReport, queueForSync: Boolean) {
        createdReport = report
        this.queueForSync = queueForSync
    }
}

private class FakeIncidentEvidenceRepository : IncidentEvidenceRepository {
    val evidence = IncidentEvidence(
        id = "evidence-1",
        reportId = "report-1",
        type = EvidenceType.PHOTO,
        localUri = "content://local/photo.jpg",
        remoteUrl = null,
        sha256Hash = "hash",
        visibility = EvidenceVisibility.PRIVATE,
        createdAtMillis = 100L,
    )

    var reportId: String? = null
    var type: EvidenceType? = null
    var sourceUri: String? = null
    var deletedEvidenceId: String? = null

    override fun observeEvidenceForReport(reportId: String): Flow<List<IncidentEvidence>> =
        flowOf(emptyList())

    override suspend fun addEvidence(
        reportId: String,
        type: EvidenceType,
        sourceUri: String,
    ): IncidentEvidence {
        this.reportId = reportId
        this.type = type
        this.sourceUri = sourceUri
        return evidence
    }

    override suspend fun deleteLocalEvidence(evidenceId: String) {
        deletedEvidenceId = evidenceId
    }
}
