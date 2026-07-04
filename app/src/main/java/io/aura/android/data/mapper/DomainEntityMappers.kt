package io.aura.android.data.mapper

import io.aura.android.data.local.entity.AlertEntity
import io.aura.android.data.local.entity.DeviceIdentityEntity
import io.aura.android.data.local.entity.GuardianContactEntity
import io.aura.android.data.local.entity.IncidentEvidenceEntity
import io.aura.android.data.local.entity.IncidentReportEntity
import io.aura.android.data.local.entity.ReportVerificationEntity
import io.aura.android.data.local.entity.SafetySessionEntity
import io.aura.android.data.local.entity.SafetySessionUpdateEntity
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.local.entity.UserProfileEntity
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.DeviceIdentity
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.IncidentEvidence
import io.aura.android.domain.model.IncidentReport
import io.aura.android.domain.model.ReportVerification
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.model.SyncQueueItem
import io.aura.android.domain.model.UserProfile

fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    displayName = displayName,
    phoneNumber = phoneNumber,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id,
    displayName = displayName,
    phoneNumber = phoneNumber,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun DeviceIdentityEntity.toDomain() = DeviceIdentity(
    id = id,
    publicKey = publicKey,
    createdAtMillis = createdAtMillis,
)

fun DeviceIdentity.toEntity() = DeviceIdentityEntity(
    id = id,
    publicKey = publicKey,
    createdAtMillis = createdAtMillis,
)

fun IncidentReportEntity.toDomain() = IncidentReport(
    id = id,
    type = type,
    severity = severity,
    status = status,
    location = AuraLocation(latitude, longitude, locationPrecision),
    description = description,
    visibility = visibility,
    isAnonymous = isAnonymous,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun IncidentReport.toEntity() = IncidentReportEntity(
    id = id,
    type = type,
    severity = severity,
    status = status,
    latitude = location.latitude,
    longitude = location.longitude,
    locationPrecision = location.precision,
    description = description,
    visibility = visibility,
    isAnonymous = isAnonymous,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun IncidentEvidenceEntity.toDomain() = IncidentEvidence(
    id = id,
    reportId = reportId,
    type = type,
    localUri = localUri,
    remoteUrl = remoteUrl,
    visibility = visibility,
    createdAtMillis = createdAtMillis,
)

fun IncidentEvidence.toEntity() = IncidentEvidenceEntity(
    id = id,
    reportId = reportId,
    type = type,
    localUri = localUri,
    remoteUrl = remoteUrl,
    visibility = visibility,
    createdAtMillis = createdAtMillis,
)

fun AlertEntity.toDomain() = Alert(
    id = id,
    reportId = reportId,
    type = type,
    severity = severity,
    status = status,
    location = AuraLocation(latitude, longitude, locationPrecision),
    summary = summary,
    distanceMeters = distanceMeters,
    reportedAtMillis = reportedAtMillis,
)

fun Alert.toEntity() = AlertEntity(
    id = id,
    reportId = reportId,
    type = type,
    severity = severity,
    status = status,
    latitude = location.latitude,
    longitude = location.longitude,
    locationPrecision = location.precision,
    summary = summary,
    distanceMeters = distanceMeters,
    reportedAtMillis = reportedAtMillis,
)

fun ReportVerificationEntity.toDomain() = ReportVerification(
    id = id,
    reportId = reportId,
    action = action,
    deviceId = deviceId,
    createdAtMillis = createdAtMillis,
)

fun ReportVerification.toEntity() = ReportVerificationEntity(
    id = id,
    reportId = reportId,
    action = action,
    deviceId = deviceId,
    createdAtMillis = createdAtMillis,
)

fun GuardianContactEntity.toDomain() = GuardianContact(
    id = id,
    displayName = displayName,
    phoneNumber = phoneNumber,
    photoUri = photoUri,
    isPrimary = isPrimary,
    createdAtMillis = createdAtMillis,
)

fun GuardianContact.toEntity() = GuardianContactEntity(
    id = id,
    displayName = displayName,
    phoneNumber = phoneNumber,
    photoUri = photoUri,
    isPrimary = isPrimary,
    createdAtMillis = createdAtMillis,
)

fun SafetySessionEntity.toDomain() = SafetySession(
    id = id,
    status = status,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis,
    lastLocation = lastLocationPrecision?.let { precision ->
        AuraLocation(
            latitude = checkNotNull(lastLatitude),
            longitude = checkNotNull(lastLongitude),
            precision = precision,
        )
    },
)

fun SafetySession.toEntity() = SafetySessionEntity(
    id = id,
    status = status,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis,
    lastLatitude = lastLocation?.latitude,
    lastLongitude = lastLocation?.longitude,
    lastLocationPrecision = lastLocation?.precision,
)

fun SafetySessionUpdateEntity.toDomain() = SafetySessionUpdate(
    id = id,
    sessionId = sessionId,
    location = locationPrecision?.let { precision ->
        AuraLocation(
            latitude = checkNotNull(latitude),
            longitude = checkNotNull(longitude),
            precision = precision,
        )
    },
    note = note,
    createdAtMillis = createdAtMillis,
)

fun SafetySessionUpdate.toEntity() = SafetySessionUpdateEntity(
    id = id,
    sessionId = sessionId,
    latitude = location?.latitude,
    longitude = location?.longitude,
    locationPrecision = location?.precision,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun SyncQueueEntity.toDomain() = SyncQueueItem(
    id = id,
    entityType = entityType,
    entityId = entityId,
    operation = operation,
    priority = priority,
    status = status,
    attempts = attempts,
    lastError = lastError,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

fun SyncQueueItem.toEntity() = SyncQueueEntity(
    id = id,
    entityType = entityType,
    entityId = entityId,
    operation = operation,
    priority = priority,
    status = status,
    attempts = attempts,
    lastError = lastError,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)
