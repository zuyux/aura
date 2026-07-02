package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String?,
    val phoneNumber: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
