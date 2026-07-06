package io.aura.android.data.remote.mapper

import io.aura.android.data.remote.dto.NetworkAlertDto
import io.aura.android.domain.location.LocationPrivacyService
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel

fun NetworkAlertDto.toDomain(): Alert {
    val resolvedLatitude = latitude ?: location?.latitude ?: location?.lat ?: 0.0
    val resolvedLongitude = longitude ?: location?.longitude ?: location?.lng ?: 0.0
    val precision = locationPrecision ?: location?.precision
    val publicLocation = LocationPrivacyService().applyPublicPrecision(
        location = AuraLocation(
            latitude = resolvedLatitude,
            longitude = resolvedLongitude,
        ),
        precision = precision.toLocationPrecision(),
    )

    return Alert(
        id = id,
        reportId = reportId,
        type = type.toIncidentType(),
        severity = severity.toSeverityLevel(),
        status = status.toAlertStatus(),
        location = publicLocation,
        summary = summary ?: description,
        distanceMeters = distanceMeters,
        reportedAtMillis = (reportedAtMillis ?: reportedAt ?: occurredAt).toMillisTimestamp(),
    )
}

private fun String.toIncidentType(): IncidentType = normalized().let { value ->
    when (value) {
        "THEFT", "ROBBERY", "ROBO" -> IncidentType.THEFT
        "ATTEMPTED_THEFT", "THEFT_ATTEMPT", "INTENTO_ROBO" -> IncidentType.ATTEMPTED_THEFT
        "SUSPICIOUS_PERSON", "SUSPICIOUS_ACTIVITY", "SUSPICIOUS" -> IncidentType.SUSPICIOUS_PERSON
        "VIOLENCE", "ASSAULT", "AGGRESSION" -> IncidentType.VIOLENCE
        "HARASSMENT", "ACOSO" -> IncidentType.HARASSMENT
        "ACCIDENT", "TRAFFIC_ACCIDENT" -> IncidentType.ACCIDENT
        "DANGEROUS_AREA", "DANGER_ZONE", "ZONE" -> IncidentType.DANGEROUS_AREA
        else -> IncidentType.OTHER
    }
}

private fun String.toSeverityLevel(): SeverityLevel = normalized().let { value ->
    when (value) {
        "LOW", "BAJA" -> SeverityLevel.LOW
        "HIGH", "ALTA", "CRITICAL" -> SeverityLevel.HIGH
        else -> SeverityLevel.MEDIUM
    }
}

private fun String?.toAlertStatus(): AlertStatus = normalized().let { value ->
    when (value) {
        "COMMUNITY_CONFIRMED", "AUTHORITY_CONFIRMED", "CONFIRMED", "VERIFIED" -> AlertStatus.COMMUNITY_CONFIRMED
        "RESOLVED" -> AlertStatus.RESOLVED
        "DISMISSED", "FALSE", "REJECTED" -> AlertStatus.DISMISSED
        else -> AlertStatus.UNVERIFIED
    }
}

private fun String?.toLocationPrecision(): LocationPrecision = normalized().let { value ->
    when (value) {
        "EXACT" -> LocationPrecision.EXACT
        "DISTRICT_ONLY", "DISTRICT", "ZONE" -> LocationPrecision.DISTRICT_ONLY
        else -> LocationPrecision.APPROXIMATE
    }
}

private fun Long?.toMillisTimestamp(): Long {
    val timestamp = this ?: return System.currentTimeMillis()
    return if (timestamp < EPOCH_MILLIS_THRESHOLD) timestamp * 1_000 else timestamp
}

private fun String?.normalized(): String = this
    ?.trim()
    ?.uppercase()
    ?.replace('-', '_')
    .orEmpty()

private const val EPOCH_MILLIS_THRESHOLD = 10_000_000_000L
