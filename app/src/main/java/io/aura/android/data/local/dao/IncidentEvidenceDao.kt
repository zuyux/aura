package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.IncidentEvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentEvidenceDao {
    @Query("SELECT * FROM incident_evidence WHERE reportId = :reportId ORDER BY createdAtMillis DESC")
    fun observeForReport(reportId: String): Flow<List<IncidentEvidenceEntity>>

    @Query("SELECT * FROM incident_evidence WHERE id = :id")
    suspend fun getEvidence(id: String): IncidentEvidenceEntity?

    @Query("UPDATE incident_evidence SET remoteUrl = :remoteUrl WHERE id = :id")
    suspend fun updateRemoteUrl(id: String, remoteUrl: String)

    @Query("DELETE FROM incident_evidence WHERE id = :id")
    suspend fun deleteById(id: String)

    @Upsert
    suspend fun upsert(evidence: IncidentEvidenceEntity)
}
