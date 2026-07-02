package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_identities")
data class DeviceIdentityEntity(
    @PrimaryKey val id: String,
    val publicKey: String?,
    val createdAtMillis: Long,
)
