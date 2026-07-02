package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.aura.android.domain.model.LocationPrecision

@Entity(
    tableName = "safety_session_updates",
    indices = [Index(value = ["sessionId"])],
)
data class SafetySessionUpdateEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationPrecision: LocationPrecision?,
    val note: String?,
    val createdAtMillis: Long,
)
