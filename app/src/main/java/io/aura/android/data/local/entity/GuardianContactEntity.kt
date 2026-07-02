package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guardian_contacts")
data class GuardianContactEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val phoneNumber: String,
    val isPrimary: Boolean,
    val createdAtMillis: Long,
)
