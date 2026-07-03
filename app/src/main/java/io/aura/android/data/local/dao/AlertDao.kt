package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY reportedAtMillis DESC")
    fun observeAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts")
    suspend fun countAlerts(): Int

    @Upsert
    suspend fun upsertAll(alerts: List<AlertEntity>)
}
