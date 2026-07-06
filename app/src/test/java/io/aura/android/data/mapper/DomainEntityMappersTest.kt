package io.aura.android.data.mapper

import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.DeviceIdentity
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
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncQueueItem
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.model.VerificationAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainEntityMappersTest {
    @Test
    fun `user profile maps domain entity round trip`() {
        val domain = UserProfile(
            id = "user-1",
            displayName = "Aura User",
            phoneNumber = "+51999999999",
            createdAtMillis = 100L,
            updatedAtMillis = 200L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `device identity maps domain entity round trip`() {
        val domain = DeviceIdentity(
            id = "device-1",
            publicKey = "public-key",
            createdAtMillis = 100L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `incident report maps domain entity round trip`() {
        val domain = IncidentReport(
            id = "report-1",
            type = IncidentType.HARASSMENT,
            severity = SeverityLevel.HIGH,
            status = ReportStatus.PENDING_SYNC,
            location = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.APPROXIMATE),
            description = "Near the station",
            visibility = ReportVisibility.COMMUNITY,
            isAnonymous = true,
            createdAtMillis = 100L,
            updatedAtMillis = 200L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `incident evidence maps domain entity round trip`() {
        val domain = IncidentEvidence(
            id = "evidence-1",
            reportId = "report-1",
            type = EvidenceType.PHOTO,
            localUri = "content://evidence/photo.jpg",
            remoteUrl = "https://example.test/photo.jpg",
            sha256Hash = "abc123",
            visibility = EvidenceVisibility.PRIVATE,
            createdAtMillis = 100L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `alert maps domain entity round trip`() {
        val domain = Alert(
            id = "alert-1",
            reportId = "report-1",
            type = IncidentType.THEFT,
            severity = SeverityLevel.MEDIUM,
            status = AlertStatus.COMMUNITY_CONFIRMED,
            location = AuraLocation(latitude = -12.05, longitude = -77.04, precision = LocationPrecision.DISTRICT_ONLY),
            summary = "Reported nearby",
            distanceMeters = 350,
            reportedAtMillis = 100L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `report verification maps domain entity round trip`() {
        val domain = ReportVerification(
            id = "verification-1",
            reportId = "report-1",
            action = VerificationAction.ALSO_SEEN,
            deviceId = "device-1",
            createdAtMillis = 100L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `guardian contact maps domain entity round trip`() {
        val domain = GuardianContact(
            id = "contact-1",
            displayName = "Marisol",
            phoneNumber = "+51988888888",
            photoUri = "content://contacts/photo",
            isPrimary = true,
            createdAtMillis = 100L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `guardian notification maps domain entity round trip`() {
        val domain = GuardianNotification(
            id = "notification-1",
            type = GuardianNotificationType.SOS_ALERT,
            status = GuardianNotificationStatus.UNREAD,
            senderName = "Marisol",
            senderPhoneNumber = "+51988888888",
            senderPhotoUri = "content://contacts/photo",
            message = "Necesito ayuda",
            sessionId = "session-1",
            latitude = -12.046374,
            longitude = -77.042793,
            createdAtMillis = 100L,
            readAtMillis = null,
            respondedAtMillis = null,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `safety session maps domain entity round trip with location`() {
        val domain = SafetySession(
            id = "session-1",
            status = SafetySessionStatus.ACTIVE,
            startedAtMillis = 100L,
            endedAtMillis = null,
            lastLocation = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.EXACT),
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `safety session maps null location without coordinates`() {
        val entity = SafetySession(
            id = "session-1",
            status = SafetySessionStatus.ENDED_SAFE,
            startedAtMillis = 100L,
            endedAtMillis = 200L,
            lastLocation = null,
        ).toEntity()

        assertNull(entity.lastLatitude)
        assertNull(entity.lastLongitude)
        assertNull(entity.lastLocationPrecision)
        assertNull(entity.toDomain().lastLocation)
    }

    @Test
    fun `safety session update maps domain entity round trip with location`() {
        val domain = SafetySessionUpdate(
            id = "update-1",
            sessionId = "session-1",
            location = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.EXACT),
            note = "Moving home",
            createdAtMillis = 100L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `safety session update maps null location without coordinates`() {
        val entity = SafetySessionUpdate(
            id = "update-1",
            sessionId = "session-1",
            location = null,
            note = "No GPS",
            createdAtMillis = 100L,
        ).toEntity()

        assertNull(entity.latitude)
        assertNull(entity.longitude)
        assertNull(entity.locationPrecision)
        assertNull(entity.toDomain().location)
    }

    @Test
    fun `sync queue item maps domain entity round trip`() {
        val domain = SyncQueueItem(
            id = "sync-1",
            entityType = "incident_report",
            entityId = "report-1",
            operation = SyncOperation.CREATE,
            priority = SyncPriority.HIGH,
            status = SyncStatus.FAILED,
            attempts = 2,
            lastError = "timeout",
            createdAtMillis = 100L,
            updatedAtMillis = 200L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }
}
