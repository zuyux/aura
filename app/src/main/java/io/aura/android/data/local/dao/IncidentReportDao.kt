package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.IncidentReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentReportDao {
    @Query("SELECT * FROM incident_reports ORDER BY createdAtMillis DESC")
    fun observeReports(): Flow<List<IncidentReportEntity>>

    @Query("SELECT * FROM incident_reports WHERE id = :id")
    fun observeReport(id: String): Flow<IncidentReportEntity?>

    @Upsert
    suspend fun upsert(report: IncidentReportEntity)
}
