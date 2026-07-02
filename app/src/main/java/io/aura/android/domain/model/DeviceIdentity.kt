package io.aura.android.domain.model

data class DeviceIdentity(
    val id: String,
    val publicKey: String?,
    val createdAtMillis: Long,
)
