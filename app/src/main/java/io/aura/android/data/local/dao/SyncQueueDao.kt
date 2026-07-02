package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY priority DESC, createdAtMillis ASC")
    fun observeByStatus(status: SyncStatus = SyncStatus.PENDING): Flow<List<SyncQueueEntity>>

    @Upsert
    suspend fun upsert(item: SyncQueueEntity)
}
