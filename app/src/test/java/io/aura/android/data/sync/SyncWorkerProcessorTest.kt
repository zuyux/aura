package io.aura.android.data.sync

import androidx.work.ListenableWorker.Result
import io.aura.android.data.local.dao.GuardianContactDao
import io.aura.android.data.local.dao.GuardianNotificationDao
import io.aura.android.data.local.dao.IncidentEvidenceDao
import io.aura.android.data.local.dao.IncidentReportDao
import io.aura.android.data.local.dao.ReportVerificationDao
import io.aura.android.data.local.dao.SafetySessionDao
import io.aura.android.data.local.dao.UserProfileDao
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.GuardianNotificationEntity
import io.aura.android.data.local.entity.IncidentEvidenceEntity
import io.aura.android.data.local.entity.IncidentReportEntity
import io.aura.android.data.local.entity.SafetySessionEntity
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.local.entity.UserProfileEntity
import io.aura.android.data.network.NetworkMonitor
import io.aura.android.data.remote.api.SyncApi
import io.aura.android.data.remote.dto.CreateReportResponseDto
import io.aura.android.data.remote.dto.GuardianNotificationDto
import io.aura.android.data.remote.dto.UploadEvidenceResponseDto
import io.aura.android.domain.location.LastKnownLocationStore
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.EvidenceVisibility
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.model.ReportVisibility
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.repository.AlertRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncWorkerProcessorTest {
    private val networkMonitor = mockk<NetworkMonitor>()
    private val syncQueueManager = mockk<SyncQueueManager>(relaxUnitFun = true)
    private val syncApi = mockk<SyncApi>(relaxUnitFun = true)
    private val incidentReportDao = mockk<IncidentReportDao>(relaxUnitFun = true)
    private val incidentEvidenceDao = mockk<IncidentEvidenceDao>(relaxUnitFun = true)
    private val alertRepository = mockk<AlertRepository>(relaxUnitFun = true)
    private val lastKnownLocationStore = mockk<LastKnownLocationStore>()
    private val guardianNotificationDao = mockk<GuardianNotificationDao>(relaxUnitFun = true)
    private val safetySessionDao = mockk<SafetySessionDao>()
    private val userProfileDao = mockk<UserProfileDao>()
    private val dependencies = FakeSyncWorkerDependencies(
        alertRepository = alertRepository,
        guardianNotificationDao = guardianNotificationDao,
        incidentEvidenceDao = incidentEvidenceDao,
        incidentReportDao = incidentReportDao,
        lastKnownLocationStore = lastKnownLocationStore,
        networkMonitor = networkMonitor,
        safetySessionDao = safetySessionDao,
        syncApi = syncApi,
        syncQueueManager = syncQueueManager,
        userProfileDao = userProfileDao,
    )
    private val processor = SyncWorkerProcessor(dependencies) { block -> block() }

    @Test
    fun `report sync retries when offline without touching queue`() = runBlocking {
        every { networkMonitor.isOnline() } returns false

        val result = processor.syncReports()

        assertEquals(Result.retry(), result)
        coVerify(exactly = 0) { syncQueueManager.resetInterruptedWork() }
    }

    @Test
    fun `report sync submits report and marks queue item succeeded`() = runBlocking {
        every { networkMonitor.isOnline() } returns true
        coEvery { syncQueueManager.nextItems(listOf(SyncEntityTypes.INCIDENT_REPORT, SyncEntityTypes.REPORT_VERIFICATION)) } returns
            listOf(syncItem(id = "sync-report", entityType = SyncEntityTypes.INCIDENT_REPORT, entityId = "report-1"))
        coEvery { incidentReportDao.getReport("report-1") } returns incidentReport("report-1")
        coEvery { syncApi.createReport(any()) } returns CreateReportResponseDto()

        val result = processor.syncReports()

        assertEquals(Result.success(), result)
        coVerify { syncQueueManager.resetInterruptedWork() }
        coVerify { syncQueueManager.markRunning("sync-report") }
        coVerify { incidentReportDao.updateStatus("report-1", ReportStatus.SUBMITTED, any()) }
        coVerify { syncQueueManager.markSucceeded("sync-report") }
    }

    @Test
    fun `evidence upload stores remote url and marks queue item succeeded`() = runBlocking {
        every { networkMonitor.isOnline() } returns true
        coEvery { syncQueueManager.nextItems(listOf(SyncEntityTypes.INCIDENT_EVIDENCE)) } returns
            listOf(syncItem(id = "sync-evidence", entityType = SyncEntityTypes.INCIDENT_EVIDENCE, entityId = "evidence-1"))
        coEvery { incidentEvidenceDao.getEvidence("evidence-1") } returns evidence("evidence-1")
        coEvery { syncApi.uploadEvidence(any(), any()) } returns UploadEvidenceResponseDto(remoteUrl = "https://cdn.example/evidence.jpg")

        val result = processor.uploadEvidence()

        assertEquals(Result.success(), result)
        coVerify { incidentEvidenceDao.updateRemoteUrl("evidence-1", "https://cdn.example/evidence.jpg") }
        coVerify { syncQueueManager.markSucceeded("sync-evidence") }
    }

    @Test
    fun `alert fetch refreshes nearby alerts with last known location`() = runBlocking {
        val location = AuraLocation(
            latitude = -12.1,
            longitude = -77.0,
            precision = LocationPrecision.APPROXIMATE,
        )
        every { networkMonitor.isOnline() } returns true
        coEvery { lastKnownLocationStore.getLastKnownLocation() } returns location

        val result = processor.fetchAlerts()

        assertEquals(Result.success(), result)
        coVerify { alertRepository.refreshNearbyAlerts(location = location, radiusMeters = 1_500) }
    }

    @Test
    fun `guardian notification receive stores valid notifications and preserves terminal status`() = runBlocking {
        val notificationsSlot = slot<List<GuardianNotificationEntity>>()
        every { networkMonitor.isOnline() } returns true
        coEvery { userProfileDao.getProfile() } returns userProfile(phoneNumber = "+51999999999")
        coEvery { syncApi.getGuardianNotifications("+51999999999") } returns listOf(
            GuardianNotificationDto(
                id = "invite",
                type = GuardianNotificationType.GUARDIAN_INVITE.name,
                senderName = "Luz",
                message = "Invitacion",
                createdAtMillis = 10,
            ),
            GuardianNotificationDto(
                id = "bad",
                type = "NOT_A_TYPE",
                senderName = "Luz",
                message = "Ignored",
                createdAtMillis = 20,
            ),
        )
        coEvery { guardianNotificationDao.getNotification("invite") } returns guardianNotification(
            id = "invite",
            status = GuardianNotificationStatus.ACCEPTED,
            respondedAtMillis = 15,
        )
        coEvery { guardianNotificationDao.getNotification("bad") } returns null
        coEvery { guardianNotificationDao.upsertAll(capture(notificationsSlot)) } returns Unit

        val result = processor.receiveGuardianNotifications()

        assertEquals(Result.success(), result)
        assertEquals(listOf("invite"), notificationsSlot.captured.map { it.id })
        assertEquals(GuardianNotificationStatus.ACCEPTED, notificationsSlot.captured.single().status)
        assertEquals(15L, notificationsSlot.captured.single().respondedAtMillis)
    }

    @Test
    fun `safety session sync sends create and ended update`() = runBlocking {
        every { networkMonitor.isOnline() } returns true
        coEvery {
            syncQueueManager.nextItems(
                listOf(
                    SyncEntityTypes.SAFETY_SESSION,
                    SyncEntityTypes.SAFETY_SESSION_UPDATE,
                    SyncEntityTypes.GUARDIAN_INVITE_NOTIFICATION,
                    SyncEntityTypes.GUARDIAN_INVITE_RESPONSE,
                    SyncEntityTypes.GUARDIAN_SOS_NOTIFICATION,
                ),
            )
        } returns listOf(syncItem(id = "sync-session", entityType = SyncEntityTypes.SAFETY_SESSION, entityId = "session-1"))
        coEvery { safetySessionDao.getSession("session-1") } returns safetySession("session-1")

        val result = processor.syncSafetySessions()

        assertEquals(Result.success(), result)
        coVerify { syncApi.syncSafetySession(match { it.clientId == "session-1" && it.status == SafetySessionStatus.ENDED_SAFE.name }) }
        coVerify { syncApi.updateSafetySession("session-1", match { it.status == SafetySessionStatus.ENDED_SAFE.name }) }
        coVerify { syncQueueManager.markSucceeded("sync-session") }
    }

    private fun syncItem(
        id: String,
        entityType: String,
        entityId: String,
    ) = SyncQueueEntity(
        id = id,
        entityType = entityType,
        entityId = entityId,
        operation = SyncOperation.CREATE,
        priority = SyncPriority.NORMAL,
        status = SyncStatus.PENDING,
        attempts = 0,
        lastError = null,
        createdAtMillis = 1,
        updatedAtMillis = 1,
    )

    private fun incidentReport(id: String) = IncidentReportEntity(
        id = id,
        type = IncidentType.THEFT,
        severity = SeverityLevel.HIGH,
        status = ReportStatus.PENDING_SYNC,
        latitude = -12.1,
        longitude = -77.0,
        locationPrecision = LocationPrecision.APPROXIMATE,
        description = "Reported incident",
        visibility = ReportVisibility.COMMUNITY,
        isAnonymous = true,
        createdAtMillis = 1,
        updatedAtMillis = 1,
    )

    private fun evidence(id: String) = IncidentEvidenceEntity(
        id = id,
        reportId = "report-1",
        type = EvidenceType.PHOTO,
        localUri = "content://evidence",
        remoteUrl = null,
        sha256Hash = "hash",
        visibility = EvidenceVisibility.PRIVATE,
        createdAtMillis = 1,
    )

    private fun userProfile(phoneNumber: String) = UserProfileEntity(
        id = "profile",
        displayName = "Valeria",
        phoneNumber = phoneNumber,
        createdAtMillis = 1,
        updatedAtMillis = 1,
    )

    private fun guardianNotification(
        id: String,
        status: GuardianNotificationStatus,
        respondedAtMillis: Long?,
    ) = GuardianNotificationEntity(
        id = id,
        type = GuardianNotificationType.GUARDIAN_INVITE,
        status = status,
        senderName = "Luz",
        senderPhoneNumber = null,
        senderPhotoUri = null,
        message = "Invitacion",
        sessionId = null,
        latitude = null,
        longitude = null,
        createdAtMillis = 1,
        readAtMillis = null,
        respondedAtMillis = respondedAtMillis,
    )

    private fun safetySession(id: String) = SafetySessionEntity(
        id = id,
        status = SafetySessionStatus.ENDED_SAFE,
        startedAtMillis = 1,
        endedAtMillis = 10,
        lastLatitude = -12.1,
        lastLongitude = -77.0,
        lastLocationPrecision = LocationPrecision.EXACT,
    )
}

private class FakeSyncWorkerDependencies(
    private val alertRepository: AlertRepository = mockk(),
    private val database: AuraDatabase = mockk(),
    private val guardianContactDao: GuardianContactDao = mockk(),
    private val guardianNotificationDao: GuardianNotificationDao = mockk(),
    private val incidentEvidenceDao: IncidentEvidenceDao = mockk(),
    private val incidentReportDao: IncidentReportDao = mockk(),
    private val lastKnownLocationStore: LastKnownLocationStore = mockk(),
    private val networkMonitor: NetworkMonitor = mockk(),
    private val reportVerificationDao: ReportVerificationDao = mockk(),
    private val safetySessionDao: SafetySessionDao = mockk(),
    private val syncApi: SyncApi = mockk(),
    private val syncQueueManager: SyncQueueManager = mockk(),
    private val userProfileDao: UserProfileDao = mockk(),
) : SyncWorkerDependencies {
    override fun alertRepository() = alertRepository
    override fun database() = database
    override fun guardianContactDao() = guardianContactDao
    override fun guardianNotificationDao() = guardianNotificationDao
    override fun incidentEvidenceDao() = incidentEvidenceDao
    override fun incidentReportDao() = incidentReportDao
    override fun lastKnownLocationStore() = lastKnownLocationStore
    override fun networkMonitor() = networkMonitor
    override fun reportVerificationDao() = reportVerificationDao
    override fun safetySessionDao() = safetySessionDao
    override fun syncApi() = syncApi
    override fun syncQueueManager() = syncQueueManager
    override fun userProfileDao() = userProfileDao
}
