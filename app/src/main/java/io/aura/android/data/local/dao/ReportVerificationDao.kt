package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.ReportVerificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportVerificationDao {
    @Query("SELECT * FROM report_verifications WHERE reportId = :reportId ORDER BY createdAtMillis DESC")
    fun observeForReport(reportId: String): Flow<List<ReportVerificationEntity>>

    @Query("SELECT * FROM report_verifications WHERE id = :id")
    suspend fun getVerification(id: String): ReportVerificationEntity?

    @Upsert
    suspend fun upsert(verification: ReportVerificationEntity)
}
