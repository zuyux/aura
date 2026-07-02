package io.aura.android.domain.model

data class SafetySession(
    val id: String,
    val status: SafetySessionStatus,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val lastLocation: AuraLocation?,
)
