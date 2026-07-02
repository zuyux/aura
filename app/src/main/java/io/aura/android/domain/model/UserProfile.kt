package io.aura.android.domain.model

data class UserProfile(
    val id: String,
    val displayName: String?,
    val phoneNumber: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
