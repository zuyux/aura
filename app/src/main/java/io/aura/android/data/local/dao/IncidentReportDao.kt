package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.IncidentReportEntity
import io.aura.android.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentReportDao {
    @Query("SELECT * FROM incident_reports ORDER BY createdAtMillis DESC")
    fun observeReports(): Flow<List<IncidentReportEntity>>

    @Query("SELECT * FROM incident_reports WHERE id = :id")
    fun observeReport(id: String): Flow<IncidentReportEntity?>

    @Query("SELECT * FROM incident_reports WHERE id = :id")
    suspend fun getReport(id: String): IncidentReportEntity?

    @Query("SELECT * FROM incident_reports WHERE status = :status ORDER BY createdAtMillis DESC")
    suspend fun getReportsByStatus(status: ReportStatus): List<IncidentReportEntity>

    @Query("UPDATE incident_reports SET status = :status, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateStatus(id: String, status: ReportStatus, updatedAtMillis: Long)

    @Upsert
    suspend fun upsert(report: IncidentReportEntity)
}
