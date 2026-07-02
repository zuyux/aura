package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus

@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["status", "priority"]), Index(value = ["entityType", "entityId"])],
)
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val operation: SyncOperation,
    val priority: SyncPriority,
    val status: SyncStatus,
    val attempts: Int,
    val lastError: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
