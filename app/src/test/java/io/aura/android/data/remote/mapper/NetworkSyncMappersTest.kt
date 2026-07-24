package io.aura.android.data.remote.mapper

import io.aura.android.data.remote.dto.GuardianNotificationDto
import io.aura.android.data.remote.dto.NetworkIncidentReportDto
import io.aura.android.data.remote.dto.NetworkLocationDto
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.EvidenceVisibility
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType
import io.aura.android.domain.model.IncidentEvidence
import io.aura.android.domain.model.IncidentReport
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.model.ReportVerification
import io.aura.android.domain.model.ReportVisibility
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.model.VerificationAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkSyncMappersTest {
    @Test
    fun `incident report maps to create report request dto`() {
        val report = IncidentReport(
            id = "report-1",
            type = IncidentType.THEFT,
            severity = SeverityLevel.HIGH,
            status = ReportStatus.PENDING_SYNC,
            location = AuraLocation(-12.046374, -77.042793, LocationPrecision.APPROXIMATE),
            description = "Near the station",
            visibility = ReportVisibility.COMMUNITY,
            isAnonymous = true,
            createdAtMillis = 100L,
            updatedAtMillis = 200L,
        )

        val dto = report.toCreateReportRequestDto()

        assertEquals("report-1", dto.clientId)
        assertEquals("THEFT", dto.type)
        assertEquals("HIGH", dto.severity)
        assertEquals(-12.046374, dto.latitude, 0.0)
        assertEquals(-77.042793, dto.longitude, 0.0)
        assertEquals("APPROXIMATE", dto.locationPrecision)
        assertEquals("COMMUNITY", dto.visibility)
        assertEquals(true, dto.anonymous)
        assertEquals(100L, dto.occurredAtMillis)
    }

    @Test
    fun `incident evidence maps to upload evidence request dto`() {
        val evidence = IncidentEvidence(
            id = "evidence-1",
            reportId = "report-1",
            type = EvidenceType.PHOTO,
            localUri = "content://evidence/photo.jpg",
            remoteUrl = null,
            sha256Hash = "abc123",
            visibility = EvidenceVisibility.PRIVATE,
            createdAtMillis = 100L,
        )

        val dto = evidence.toUploadEvidenceRequestDto()

        assertEquals("evidence-1", dto.clientId)
        assertEquals("PHOTO", dto.type)
        assertEquals("content://evidence/photo.jpg", dto.localUri)
        assertEquals("abc123", dto.sha256Hash)
        assertEquals("PRIVATE", dto.visibility)
        assertEquals(100L, dto.createdAtMillis)
    }

    @Test
    fun `report verification maps to create verification request dto`() {
        val verification = ReportVerification(
            id = "verification-1",
            reportId = "report-1",
            action = VerificationAction.ALSO_SEEN,
            deviceId = "device-1",
            createdAtMillis = 100L,
        )

        val dto = verification.toCreateVerificationRequestDto()

        assertEquals("verification-1", dto.clientId)
        assertEquals("ALSO_SEEN", dto.action)
        assertEquals("device-1", dto.deviceId)
        assertEquals(100L, dto.createdAtMillis)
    }

    @Test
    fun `safety session maps to sync request dto`() {
        val session = SafetySession(
            id = "session-1",
            status = SafetySessionStatus.ACTIVE,
            startedAtMillis = 100L,
            endedAtMillis = null,
            lastLocation = AuraLocation(-12.046374, -77.042793, LocationPrecision.EXACT),
        )

        val dto = session.toSyncSafetySessionRequestDto()

        assertEquals("session-1", dto.clientId)
        assertEquals("ACTIVE", dto.status)
        assertEquals(-12.046374, dto.latitude ?: 0.0, 0.0)
        assertEquals(-77.042793, dto.longitude ?: 0.0, 0.0)
        assertEquals("EXACT", dto.locationPrecision)
        assertEquals(100L, dto.startedAtMillis)
        assertNull(dto.endedAtMillis)
    }

    @Test
    fun `safety session maps to update request dto`() {
        val session = SafetySession(
            id = "session-1",
            status = SafetySessionStatus.ENDED_SAFE,
            startedAtMillis = 100L,
            endedAtMillis = 300L,
            lastLocation = AuraLocation(-12.046374, -77.042793, LocationPrecision.APPROXIMATE),
        )

        val dto = session.toUpdateSafetySessionRequestDto()

        assertEquals("ENDED_SAFE", dto.status)
        assertEquals(-12.046374, dto.latitude ?: 0.0, 0.0)
        assertEquals(-77.042793, dto.longitude ?: 0.0, 0.0)
        assertEquals("APPROXIMATE", dto.locationPrecision)
        assertEquals(300L, dto.endedAtMillis)
    }

    @Test
    fun `safety session update maps to sync update request dto`() {
        val update = SafetySessionUpdate(
            id = "update-1",
            sessionId = "session-1",
            location = AuraLocation(-12.046374, -77.042793, LocationPrecision.DISTRICT_ONLY),
            note = "Heading home",
            createdAtMillis = 100L,
        )

        val dto = update.toSyncSafetySessionUpdateRequestDto()

        assertEquals("update-1", dto.clientId)
        assertEquals("session-1", dto.sessionId)
        assertEquals(-12.046374, dto.latitude ?: 0.0, 0.0)
        assertEquals(-77.042793, dto.longitude ?: 0.0, 0.0)
        assertEquals("DISTRICT_ONLY", dto.locationPrecision)
        assertEquals("Heading home", dto.note)
        assertEquals(100L, dto.createdAtMillis)
    }

    @Test
    fun `network incident report dto maps to domain with nested location`() {
        val dto = NetworkIncidentReportDto(
            id = "report-1",
            type = "theft",
            severity = "high",
            status = "community-confirmed",
            location = NetworkLocationDto(lat = -12.046374, lng = -77.042793, precision = "district"),
            description = "Near the station",
            visibility = "community",
            anonymous = false,
            createdAtMillis = 100L,
            updatedAtMillis = 200L,
        )

        val domain = dto.toDomain()

        assertEquals("report-1", domain.id)
        assertEquals(IncidentType.THEFT, domain.type)
        assertEquals(SeverityLevel.HIGH, domain.severity)
        assertEquals(ReportStatus.COMMUNITY_CONFIRMED, domain.status)
        assertEquals(LocationPrecision.DISTRICT_ONLY, domain.location.precision)
        assertEquals(-12.046374, domain.location.latitude, 0.0)
        assertEquals(-77.042793, domain.location.longitude, 0.0)
        assertEquals(ReportVisibility.COMMUNITY, domain.visibility)
        assertEquals(false, domain.isAnonymous)
        assertEquals(100L, domain.createdAtMillis)
        assertEquals(200L, domain.updatedAtMillis)
    }

    @Test
    fun `guardian contact maps to invite notification request dto`() {
        val contact = guardianContact()
        val inviter = UserProfile(
            id = "user-1",
            displayName = "Aura User",
            phoneNumber = "+51999999999",
            createdAtMillis = 50L,
            updatedAtMillis = 60L,
        )

        val dto = contact.toGuardianInviteNotificationRequestDto(inviter)

        assertEquals("contact-1", dto.contactId)
        assertEquals("+51988888888", dto.inviteePhoneNumber)
        assertEquals("Aura User", dto.inviterName)
        assertEquals("+51999999999", dto.inviterPhoneNumber)
        assertEquals("Aura User te agregó a su Red Guardián. Acepta para formar parte.", dto.message)
        assertEquals(100L, dto.createdAtMillis)
    }

    @Test
    fun `guardian notification maps to invite response request dto`() {
        val notification = guardianNotification(status = GuardianNotificationStatus.ACCEPTED)

        val dto = notification.toGuardianInviteResponseRequestDto(
            responderPhoneNumber = "+51999999999",
            respondedAtMillis = 300L,
        )

        assertEquals("notification-1", dto.notificationId)
        assertEquals("ACCEPTED", dto.status)
        assertEquals("+51999999999", dto.responderPhoneNumber)
        assertEquals(300L, dto.respondedAtMillis)
    }

    @Test
    fun `safety session update maps to sos notification request dto`() {
        val update = SafetySessionUpdate(
            id = "update-1",
            sessionId = "session-1",
            location = AuraLocation(-12.046374, -77.042793, LocationPrecision.EXACT),
            note = null,
            createdAtMillis = 100L,
        )

        val dto = update.toGuardianSosNotificationRequestDto(listOf(guardianContact()))

        assertEquals("session-1", dto.sessionId)
        assertEquals("update-1", dto.updateId)
        assertEquals("SOS activado", dto.message)
        assertEquals(-12.046374, dto.latitude ?: 0.0, 0.0)
        assertEquals(-77.042793, dto.longitude ?: 0.0, 0.0)
        assertEquals("EXACT", dto.locationPrecision)
        assertEquals(1, dto.contacts.size)
        assertEquals("Marisol", dto.contacts.first().displayName)
        assertEquals("+51988888888", dto.contacts.first().phoneNumber)
    }

    @Test
    fun `guardian notification dto maps to domain preserving terminal status`() {
        val existing = guardianNotification(
            status = GuardianNotificationStatus.DECLINED,
            readAtMillis = 200L,
            respondedAtMillis = 300L,
        )
        val dto = GuardianNotificationDto(
            id = "notification-1",
            type = "GUARDIAN_INVITE",
            senderName = "Marisol",
            senderPhoneNumber = "+51988888888",
            senderPhotoUri = "content://contacts/photo",
            message = "Join my guardian network",
            sessionId = null,
            latitude = null,
            longitude = null,
            createdAtMillis = 100L,
        )

        val domain = dto.toDomain(existing)

        assertEquals(GuardianNotificationType.GUARDIAN_INVITE, domain.type)
        assertEquals(GuardianNotificationStatus.DECLINED, domain.status)
        assertEquals(200L, domain.readAtMillis)
        assertEquals(300L, domain.respondedAtMillis)
    }

    private fun guardianContact() = GuardianContact(
        id = "contact-1",
        displayName = "Marisol",
        phoneNumber = "+51988888888",
        photoUri = null,
        isPrimary = true,
        createdAtMillis = 100L,
    )

    private fun guardianNotification(
        status: GuardianNotificationStatus,
        readAtMillis: Long? = null,
        respondedAtMillis: Long? = null,
    ) = GuardianNotification(
        id = "notification-1",
        type = GuardianNotificationType.GUARDIAN_INVITE,
        status = status,
        senderName = "Marisol",
        senderPhoneNumber = "+51988888888",
        senderPhotoUri = null,
        message = "Join my guardian network",
        sessionId = null,
        latitude = null,
        longitude = null,
        createdAtMillis = 100L,
        readAtMillis = readAtMillis,
        respondedAtMillis = respondedAtMillis,
    )
}
