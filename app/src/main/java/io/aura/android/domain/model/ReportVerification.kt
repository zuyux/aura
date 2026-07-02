package io.aura.android.domain.model

data class ReportVerification(
    val id: String,
    val reportId: String,
    val action: VerificationAction,
    val deviceId: String?,
    val createdAtMillis: Long,
)
