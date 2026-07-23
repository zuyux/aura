package io.aura.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateReportRequestDto(
    @kotlinx.serialization.SerialName("id")
    val clientId: String,
    val type: String,
    val severity: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    @kotlinx.serialization.SerialName("location_precision")
    val locationPrecision: String,
    @kotlinx.serialization.SerialName("occurred_at_millis")
    val occurredAtMillis: Long,
    val visibility: String,
    @kotlinx.serialization.SerialName("is_anonymous")
    val anonymous: Boolean,
)

@Serializable
data class CreateReportResponseDto(
    val id: String? = null,
    val syncedAtMillis: Long? = null,
)

@Serializable
data class NetworkIncidentReportDto(
    val id: String,
    val type: String,
    val severity: String,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: NetworkLocationDto? = null,
    val locationPrecision: String? = null,
    val description: String? = null,
    val visibility: String? = null,
    val anonymous: Boolean? = null,
    val createdAtMillis: Long? = null,
    val updatedAtMillis: Long? = null,
    val occurredAtMillis: Long? = null,
    val reportedAtMillis: Long? = null,
)

@Serializable
data class UploadEvidenceRequestDto(
    val clientId: String,
    val type: String,
    val localUri: String,
    val sha256Hash: String,
    val visibility: String,
    val createdAtMillis: Long,
)

@Serializable
data class UploadEvidenceResponseDto(
    val remoteUrl: String,
    val syncedAtMillis: Long? = null,
)

@Serializable
data class CreateVerificationRequestDto(
    val clientId: String,
    val action: String,
    val deviceId: String? = null,
    val createdAtMillis: Long,
)

@Serializable
data class SyncSafetySessionRequestDto(
    val clientId: String,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationPrecision: String? = null,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
)

@Serializable
data class UpdateSafetySessionRequestDto(
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationPrecision: String? = null,
    val endedAtMillis: Long? = null,
)

@Serializable
data class SyncSafetySessionUpdateRequestDto(
    val clientId: String,
    val sessionId: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationPrecision: String? = null,
    val note: String?,
    val createdAtMillis: Long,
)

@Serializable
data class GuardianSosNotificationRequestDto(
    val sessionId: String,
    val updateId: String,
    val contacts: List<GuardianSosContactDto>,
    val message: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationPrecision: String? = null,
    val createdAtMillis: Long,
)

@Serializable
data class GuardianInviteNotificationRequestDto(
    val contactId: String,
    val inviteePhoneNumber: String,
    val inviterName: String?,
    val inviterPhoneNumber: String?,
    val message: String,
    val createdAtMillis: Long,
)

@Serializable
data class GuardianNotificationDto(
    val id: String,
    val type: String,
    val senderName: String,
    val senderPhoneNumber: String? = null,
    val senderPhotoUri: String? = null,
    val message: String,
    val sessionId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAtMillis: Long,
)

@Serializable
data class GuardianInviteResponseRequestDto(
    val notificationId: String,
    val status: String,
    val responderPhoneNumber: String?,
    val respondedAtMillis: Long,
)

@Serializable
data class GuardianSosContactDto(
    val displayName: String,
    val phoneNumber: String,
)
