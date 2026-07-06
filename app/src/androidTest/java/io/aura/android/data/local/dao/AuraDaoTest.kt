package io.aura.android.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.AlertEntity
import io.aura.android.data.local.entity.DeviceIdentityEntity
import io.aura.android.data.local.entity.GuardianContactEntity
import io.aura.android.data.local.entity.GuardianNotificationEntity
import io.aura.android.data.local.entity.IncidentEvidenceEntity
import io.aura.android.data.local.entity.IncidentReportEntity
import io.aura.android.data.local.entity.ReportVerificationEntity
import io.aura.android.data.local.entity.SafetySessionEntity
import io.aura.android.data.local.entity.SafetySessionUpdateEntity
import io.aura.android.data.local.entity.UserProfileEntity
import io.aura.android.domain.model.AlertStatus
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
import io.aura.android.domain.model.VerificationAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuraDaoTest {
    private lateinit var database: AuraDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun alertDao_ordersAlertsAndUpdatesStatus() = runBlocking {
        val dao = database.alertDao()
        dao.upsertAll(
            listOf(
                alert(id = "old", reportedAtMillis = 10, status = AlertStatus.UNVERIFIED),
                alert(id = "new", reportedAtMillis = 20, status = AlertStatus.UNVERIFIED),
            ),
        )

        assertEquals(listOf("new", "old"), dao.observeAlerts().first().map { it.id })
        assertEquals(2, dao.countAlerts())

        dao.updateStatus("old", AlertStatus.RESOLVED)

        assertEquals(AlertStatus.RESOLVED, dao.getAlert("old")?.status)
    }

    @Test
    fun guardianContactDao_ordersPrimaryThenNameAndDeletes() = runBlocking {
        val dao = database.guardianContactDao()
        dao.upsert(guardianContact(id = "zoe", displayName = "Zoe", isPrimary = false))
        dao.upsert(guardianContact(id = "ana", displayName = "Ana", isPrimary = false))
        dao.upsert(guardianContact(id = "primary", displayName = "Luz", isPrimary = true))

        assertEquals(listOf("primary", "ana", "zoe"), dao.getContacts().map { it.id })

        dao.deleteContact("ana")

        assertNull(dao.getContact("ana"))
        assertEquals(listOf("primary", "zoe"), dao.observeContacts().first().map { it.id })
    }

    @Test
    fun guardianNotificationDao_marksUnreadAndPreservesExistingReadTime() = runBlocking {
        val dao = database.guardianNotificationDao()
        dao.upsert(guardianNotification(id = "unread", status = GuardianNotificationStatus.UNREAD, createdAtMillis = 10))
        dao.upsert(
            guardianNotification(
                id = "read",
                status = GuardianNotificationStatus.READ,
                createdAtMillis = 20,
                readAtMillis = 5,
            ),
        )

        assertEquals(listOf("read", "unread"), dao.observeNotifications().first().map { it.id })

        dao.markRead(id = "unread", status = GuardianNotificationStatus.READ, now = 30)
        dao.markRead(id = "read", status = GuardianNotificationStatus.READ, now = 40)
        dao.updateStatus(
            id = "read",
            status = GuardianNotificationStatus.ACCEPTED,
            now = 50,
            respondedAtMillis = 60,
        )

        assertEquals(30L, dao.getNotification("unread")?.readAtMillis)
        assertEquals(5L, dao.getNotification("read")?.readAtMillis)
        assertEquals(60L, dao.getNotification("read")?.respondedAtMillis)
        assertEquals(GuardianNotificationStatus.ACCEPTED, dao.getNotification("read")?.status)
    }

    @Test
    fun incidentReportAndEvidenceDaos_updateAndOrderRelatedRows() = runBlocking {
        val reportDao = database.incidentReportDao()
        val evidenceDao = database.incidentEvidenceDao()
        reportDao.upsert(incidentReport(id = "report", status = ReportStatus.PENDING_SYNC))
        evidenceDao.upsert(evidence(id = "old", reportId = "report", createdAtMillis = 10))
        evidenceDao.upsert(evidence(id = "new", reportId = "report", createdAtMillis = 20))

        reportDao.updateStatus("report", ReportStatus.SUBMITTED, updatedAtMillis = 99)
        evidenceDao.updateRemoteUrl("old", "https://cdn.example/old.jpg")
        evidenceDao.deleteById("new")

        val report = reportDao.getReport("report")
        assertEquals(ReportStatus.SUBMITTED, report?.status)
        assertEquals(99L, report?.updatedAtMillis)
        assertEquals("https://cdn.example/old.jpg", evidenceDao.getEvidence("old")?.remoteUrl)
        assertEquals(listOf("old"), evidenceDao.observeForReport("report").first().map { it.id })
    }

    @Test
    fun reportVerificationDao_ordersByCreatedAtDescending() = runBlocking {
        val dao = database.reportVerificationDao()
        dao.upsert(reportVerification(id = "old", reportId = "report", createdAtMillis = 10))
        dao.upsert(reportVerification(id = "new", reportId = "report", createdAtMillis = 20))
        dao.upsert(reportVerification(id = "other", reportId = "other", createdAtMillis = 30))

        assertEquals(listOf("new", "old"), dao.observeForReport("report").first().map { it.id })
        assertEquals(VerificationAction.ALSO_SEEN, dao.getVerification("old")?.action)
    }

    @Test
    fun safetySessionDao_ordersSessionsAndUpdatesBySession() = runBlocking {
        val dao = database.safetySessionDao()
        dao.upsert(safetySession(id = "old", startedAtMillis = 10))
        dao.upsert(safetySession(id = "new", startedAtMillis = 20))
        dao.upsertUpdate(safetyUpdate(id = "update-old", sessionId = "new", createdAtMillis = 30))
        dao.upsertUpdate(safetyUpdate(id = "update-new", sessionId = "new", createdAtMillis = 40))
        dao.upsertUpdate(safetyUpdate(id = "other", sessionId = "old", createdAtMillis = 50))

        assertEquals(listOf("new", "old"), dao.observeSessions().first().map { it.id })
        assertEquals(listOf("update-new", "update-old"), dao.observeUpdates("new").first().map { it.id })
        assertEquals("update-old", dao.getUpdate("update-old")?.id)
    }

    @Test
    fun userProfileAndDeviceIdentityDaos_upsertSingleRows() = runBlocking {
        database.userProfileDao().upsert(userProfile(id = "profile", displayName = "Valeria"))
        database.deviceIdentityDao().upsert(deviceIdentity(id = "device", publicKey = "public-key"))

        assertEquals("Valeria", database.userProfileDao().getProfile()?.displayName)
        assertEquals("profile", database.userProfileDao().observeProfile().first()?.id)
        assertEquals("public-key", database.deviceIdentityDao().getIdentity()?.publicKey)
        assertEquals("device", database.deviceIdentityDao().observeIdentity().first()?.id)
    }

    private fun alert(
        id: String,
        status: AlertStatus = AlertStatus.UNVERIFIED,
        reportedAtMillis: Long = 1,
    ) = AlertEntity(
        id = id,
        reportId = null,
        type = IncidentType.THEFT,
        severity = SeverityLevel.HIGH,
        status = status,
        latitude = -12.1,
        longitude = -77.0,
        locationPrecision = LocationPrecision.APPROXIMATE,
        summary = "Reported nearby",
        distanceMeters = 500,
        reportedAtMillis = reportedAtMillis,
    )

    private fun guardianContact(
        id: String,
        displayName: String,
        isPrimary: Boolean,
    ) = GuardianContactEntity(
        id = id,
        displayName = displayName,
        phoneNumber = "+51999999999",
        photoUri = null,
        isPrimary = isPrimary,
        createdAtMillis = 1,
    )

    private fun guardianNotification(
        id: String,
        status: GuardianNotificationStatus,
        createdAtMillis: Long,
        readAtMillis: Long? = null,
    ) = GuardianNotificationEntity(
        id = id,
        type = GuardianNotificationType.GUARDIAN_INVITE,
        status = status,
        senderName = "Luz",
        senderPhoneNumber = "+51999999999",
        senderPhotoUri = null,
        message = "Invitacion",
        sessionId = null,
        latitude = null,
        longitude = null,
        createdAtMillis = createdAtMillis,
        readAtMillis = readAtMillis,
        respondedAtMillis = null,
    )

    private fun incidentReport(
        id: String,
        status: ReportStatus,
    ) = IncidentReportEntity(
        id = id,
        type = IncidentType.HARASSMENT,
        severity = SeverityLevel.MEDIUM,
        status = status,
        latitude = -12.1,
        longitude = -77.0,
        locationPrecision = LocationPrecision.APPROXIMATE,
        description = "Description",
        visibility = ReportVisibility.COMMUNITY,
        isAnonymous = true,
        createdAtMillis = 1,
        updatedAtMillis = 1,
    )

    private fun evidence(
        id: String,
        reportId: String,
        createdAtMillis: Long,
    ) = IncidentEvidenceEntity(
        id = id,
        reportId = reportId,
        type = EvidenceType.PHOTO,
        localUri = "content://$id",
        remoteUrl = null,
        sha256Hash = "hash-$id",
        visibility = EvidenceVisibility.PRIVATE,
        createdAtMillis = createdAtMillis,
    )

    private fun reportVerification(
        id: String,
        reportId: String,
        createdAtMillis: Long,
    ) = ReportVerificationEntity(
        id = id,
        reportId = reportId,
        action = VerificationAction.ALSO_SEEN,
        deviceId = null,
        createdAtMillis = createdAtMillis,
    )

    private fun safetySession(
        id: String,
        startedAtMillis: Long,
    ) = SafetySessionEntity(
        id = id,
        status = SafetySessionStatus.ACTIVE,
        startedAtMillis = startedAtMillis,
        endedAtMillis = null,
        lastLatitude = -12.1,
        lastLongitude = -77.0,
        lastLocationPrecision = LocationPrecision.EXACT,
    )

    private fun safetyUpdate(
        id: String,
        sessionId: String,
        createdAtMillis: Long,
    ) = SafetySessionUpdateEntity(
        id = id,
        sessionId = sessionId,
        latitude = -12.1,
        longitude = -77.0,
        locationPrecision = LocationPrecision.EXACT,
        note = "Update",
        createdAtMillis = createdAtMillis,
    )

    private fun userProfile(
        id: String,
        displayName: String,
    ) = UserProfileEntity(
        id = id,
        displayName = displayName,
        phoneNumber = "+51999999999",
        createdAtMillis = 1,
        updatedAtMillis = 1,
    )

    private fun deviceIdentity(
        id: String,
        publicKey: String,
    ) = DeviceIdentityEntity(
        id = id,
        publicKey = publicKey,
        createdAtMillis = 1,
    )
}
