package io.aura.android.domain.model

data class SyncQueueItem(
    val id: String,
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
