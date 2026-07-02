package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.aura.android.domain.model.VerificationAction

@Entity(
    tableName = "report_verifications",
    indices = [Index(value = ["reportId"])],
)
data class ReportVerificationEntity(
    @PrimaryKey val id: String,
    val reportId: String,
    val action: VerificationAction,
    val deviceId: String?,
    val createdAtMillis: Long,
)
