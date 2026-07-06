package io.aura.android.domain.model

data class GuardianNotification(
    val id: String,
    val type: GuardianNotificationType,
    val status: GuardianNotificationStatus,
    val senderName: String,
    val senderPhoneNumber: String?,
    val senderPhotoUri: String?,
    val message: String,
    val sessionId: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAtMillis: Long,
    val readAtMillis: Long?,
    val respondedAtMillis: Long?,
)

enum class GuardianNotificationType {
    SOS_ALERT,
    GUARDIAN_INVITE,
}

enum class GuardianNotificationStatus {
    UNREAD,
    READ,
    ACCEPTED,
    DECLINED,
}
