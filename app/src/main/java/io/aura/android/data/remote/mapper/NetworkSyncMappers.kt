package io.aura.android.data.remote.mapper

import io.aura.android.data.remote.dto.CreateReportRequestDto
import io.aura.android.data.remote.dto.CreateVerificationRequestDto
import io.aura.android.data.remote.dto.GuardianInviteNotificationRequestDto
import io.aura.android.data.remote.dto.GuardianInviteResponseRequestDto
import io.aura.android.data.remote.dto.GuardianNotificationDto
import io.aura.android.data.remote.dto.GuardianSosContactDto
import io.aura.android.data.remote.dto.GuardianSosNotificationRequestDto
import io.aura.android.data.remote.dto.NetworkIncidentReportDto
import io.aura.android.data.remote.dto.SyncSafetySessionRequestDto
import io.aura.android.data.remote.dto.SyncSafetySessionUpdateRequestDto
import io.aura.android.data.remote.dto.UpdateSafetySessionRequestDto
import io.aura.android.data.remote.dto.UploadEvidenceRequestDto
import io.aura.android.domain.model.AuraLocation
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
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.model.UserProfile

fun IncidentReport.toCreateReportRequestDto() = CreateReportRequestDto(
    clientId = id,
    type = type.name,
    severity = severity.name,
    description = description,
    latitude = location.latitude,
    longitude = location.longitude,
    locationPrecision = location.precision.name,
    occurredAtMillis = createdAtMillis,
    visibility = visibility.name,
    anonymous = isAnonymous,
)

fun IncidentEvidence.toUploadEvidenceRequestDto() = UploadEvidenceRequestDto(
    clientId = id,
    type = type.name,
    localUri = localUri,
    sha256Hash = sha256Hash,
    visibility = visibility.name,
    createdAtMillis = createdAtMillis,
)

fun ReportVerification.toCreateVerificationRequestDto() = CreateVerificationRequestDto(
    clientId = id,
    action = action.name,
    deviceId = deviceId,
    createdAtMillis = createdAtMillis,
)

fun SafetySession.toSyncSafetySessionRequestDto() = SyncSafetySessionRequestDto(
    clientId = id,
    status = status.name,
    latitude = lastLocation?.latitude,
    longitude = lastLocation?.longitude,
    locationPrecision = lastLocation?.precision?.name,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis,
)

fun SafetySession.toUpdateSafetySessionRequestDto() = UpdateSafetySessionRequestDto(
    status = status.name,
    latitude = lastLocation?.latitude,
    longitude = lastLocation?.longitude,
    locationPrecision = lastLocation?.precision?.name,
    endedAtMillis = endedAtMillis,
)

fun SafetySessionUpdate.toSyncSafetySessionUpdateRequestDto() = SyncSafetySessionUpdateRequestDto(
    clientId = id,
    sessionId = sessionId,
    latitude = location?.latitude,
    longitude = location?.longitude,
    locationPrecision = location?.precision?.name,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun NetworkIncidentReportDto.toDomain() = IncidentReport(
    id = id,
    type = type.enumValueOrDefault(IncidentType.OTHER),
    severity = severity.enumValueOrDefault(SeverityLevel.MEDIUM),
    status = status.enumValueOrDefault(ReportStatus.SUBMITTED),
    location = AuraLocation(
        latitude = latitude ?: location?.latitude ?: location?.lat ?: 0.0,
        longitude = longitude ?: location?.longitude ?: location?.lng ?: 0.0,
        precision = (locationPrecision ?: location?.precision).toLocationPrecision(),
    ),
    description = description,
    visibility = visibility.enumValueOrDefault(ReportVisibility.COMMUNITY),
    isAnonymous = anonymous ?: true,
    createdAtMillis = createdAtMillis ?: occurredAtMillis ?: reportedAtMillis ?: System.currentTimeMillis(),
    updatedAtMillis = updatedAtMillis ?: createdAtMillis ?: occurredAtMillis ?: reportedAtMillis ?: System.currentTimeMillis(),
)

fun GuardianContact.toGuardianInviteNotificationRequestDto(
    inviter: UserProfile?,
    message: String = defaultGuardianInviteMessage(inviter),
) = GuardianInviteNotificationRequestDto(
    contactId = id,
    inviteePhoneNumber = phoneNumber,
    inviterName = inviter?.displayName,
    inviterPhoneNumber = inviter?.phoneNumber,
    message = message,
    createdAtMillis = createdAtMillis,
)

fun GuardianNotification.toGuardianInviteResponseRequestDto(
    responderPhoneNumber: String?,
    respondedAtMillis: Long = this.respondedAtMillis ?: System.currentTimeMillis(),
) = GuardianInviteResponseRequestDto(
    notificationId = id,
    status = status.name,
    responderPhoneNumber = responderPhoneNumber,
    respondedAtMillis = respondedAtMillis,
)

fun SafetySessionUpdate.toGuardianSosNotificationRequestDto(
    contacts: List<GuardianContact>,
    message: String = note ?: DEFAULT_SOS_MESSAGE,
) = GuardianSosNotificationRequestDto(
    sessionId = sessionId,
    updateId = id,
    contacts = contacts.map { contact ->
        GuardianSosContactDto(
            displayName = contact.displayName,
            phoneNumber = contact.phoneNumber,
        )
    },
    message = message,
    latitude = location?.latitude,
    longitude = location?.longitude,
    locationPrecision = location?.precision?.name,
    createdAtMillis = createdAtMillis,
)

fun GuardianNotificationDto.toDomain(
    existing: GuardianNotification? = null,
) = GuardianNotification(
    id = id,
    type = GuardianNotificationType.valueOf(type),
    status = existing?.terminalStatus() ?: existing?.status ?: GuardianNotificationStatus.UNREAD,
    senderName = senderName,
    senderPhoneNumber = senderPhoneNumber,
    senderPhotoUri = senderPhotoUri,
    message = message,
    sessionId = sessionId,
    latitude = latitude,
    longitude = longitude,
    createdAtMillis = createdAtMillis,
    readAtMillis = existing?.readAtMillis,
    respondedAtMillis = existing?.respondedAtMillis,
)

private fun GuardianNotification.terminalStatus(): GuardianNotificationStatus? =
    status.takeIf { it == GuardianNotificationStatus.ACCEPTED || it == GuardianNotificationStatus.DECLINED }

private fun defaultGuardianInviteMessage(inviter: UserProfile?): String {
    val displayName = inviter?.displayName?.takeIf { it.isNotBlank() } ?: "Alguien"
    return "$displayName te agrego a su Red Guardian. Acepta para formar parte."
}

private inline fun <reified T : Enum<T>> String?.enumValueOrDefault(default: T): T =
    this?.trim()
        ?.uppercase()
        ?.replace('-', '_')
        ?.let { value -> runCatching { enumValueOf<T>(value) }.getOrNull() }
        ?: default

private fun String?.toLocationPrecision(): LocationPrecision =
    when (normalized()) {
        "EXACT" -> LocationPrecision.EXACT
        "DISTRICT", "DISTRICT_ONLY", "ZONE" -> LocationPrecision.DISTRICT_ONLY
        else -> LocationPrecision.APPROXIMATE
    }

private fun String?.normalized(): String =
    this?.trim()
        ?.uppercase()
        ?.replace('-', '_')
        .orEmpty()

private const val DEFAULT_SOS_MESSAGE = "SOS activado"
