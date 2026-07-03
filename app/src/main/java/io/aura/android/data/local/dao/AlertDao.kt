package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.AlertEntity
import io.aura.android.domain.model.AlertStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY reportedAtMillis DESC")
    fun observeAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :alertId")
    fun observeAlert(alertId: String): Flow<AlertEntity?>

    @Query("SELECT * FROM alerts WHERE id = :alertId")
    suspend fun getAlert(alertId: String): AlertEntity?

    @Query("SELECT COUNT(*) FROM alerts")
    suspend fun countAlerts(): Int

    @Query("UPDATE alerts SET status = :status WHERE id = :alertId")
    suspend fun updateStatus(alertId: String, status: AlertStatus)

    @Upsert
    suspend fun upsertAll(alerts: List<AlertEntity>)
}
