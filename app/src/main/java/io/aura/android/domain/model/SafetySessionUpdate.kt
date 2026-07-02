package io.aura.android.domain.model

data class SafetySessionUpdate(
    val id: String,
    val sessionId: String,
    val location: AuraLocation?,
    val note: String?,
    val createdAtMillis: Long,
)
