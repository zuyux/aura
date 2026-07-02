package io.aura.android.domain.model

data class GuardianContact(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val isPrimary: Boolean,
    val createdAtMillis: Long,
)
