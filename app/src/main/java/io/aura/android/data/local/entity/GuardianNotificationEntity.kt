package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType

@Entity(tableName = "guardian_notifications")
data class GuardianNotificationEntity(
    @PrimaryKey val id: String,
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
