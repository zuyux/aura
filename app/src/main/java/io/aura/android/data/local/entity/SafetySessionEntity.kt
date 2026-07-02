package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SafetySessionStatus

@Entity(
    tableName = "safety_sessions",
    indices = [Index(value = ["status"])],
)
data class SafetySessionEntity(
    @PrimaryKey val id: String,
    val status: SafetySessionStatus,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastLocationPrecision: LocationPrecision?,
)
