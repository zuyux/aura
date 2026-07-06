package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query(
        """
        SELECT * FROM sync_queue
        WHERE status = :status
        ORDER BY
            CASE priority
                WHEN 'CRITICAL' THEN 3
                WHEN 'HIGH' THEN 2
                WHEN 'NORMAL' THEN 1
                ELSE 0
            END DESC,
            createdAtMillis ASC
        """,
    )
    fun observeByStatus(status: SyncStatus = SyncStatus.PENDING): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status IN (:statuses)")
    fun observeCountByStatuses(statuses: List<SyncStatus>): Flow<Int>

    @Query(
        """
        SELECT * FROM sync_queue
        WHERE entityType IN (:entityTypes)
        AND status IN (:statuses)
        ORDER BY
            CASE priority
                WHEN 'CRITICAL' THEN 3
                WHEN 'HIGH' THEN 2
                WHEN 'NORMAL' THEN 1
                ELSE 0
            END DESC,
            createdAtMillis ASC
        LIMIT :limit
        """,
    )
    suspend fun pendingItemsForEntityTypes(
        entityTypes: List<String>,
        statuses: List<SyncStatus> = listOf(SyncStatus.PENDING, SyncStatus.FAILED),
        limit: Int,
    ): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = :status, attempts = attempts + 1, lastError = NULL, updatedAtMillis = :now WHERE id = :id")
    suspend fun markRunning(id: String, status: SyncStatus = SyncStatus.RUNNING, now: Long)

    @Query("UPDATE sync_queue SET status = :status, lastError = NULL, updatedAtMillis = :now WHERE id = :id")
    suspend fun markSucceeded(id: String, status: SyncStatus = SyncStatus.SUCCEEDED, now: Long)

    @Query("UPDATE sync_queue SET status = :status, lastError = :error, updatedAtMillis = :now WHERE id = :id")
    suspend fun markFailed(id: String, error: String, status: SyncStatus = SyncStatus.FAILED, now: Long)

    @Query("UPDATE sync_queue SET status = :toStatus, updatedAtMillis = :now WHERE status = :fromStatus")
    suspend fun resetRunning(fromStatus: SyncStatus = SyncStatus.RUNNING, toStatus: SyncStatus = SyncStatus.PENDING, now: Long)

    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteForEntity(entityType: String, entityId: String)

    @Upsert
    suspend fun upsert(item: SyncQueueEntity)
}
