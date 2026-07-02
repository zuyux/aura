package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.SafetySessionEntity
import io.aura.android.data.local.entity.SafetySessionUpdateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetySessionDao {
    @Query("SELECT * FROM safety_sessions ORDER BY startedAtMillis DESC")
    fun observeSessions(): Flow<List<SafetySessionEntity>>

    @Query("SELECT * FROM safety_session_updates WHERE sessionId = :sessionId ORDER BY createdAtMillis DESC")
    fun observeUpdates(sessionId: String): Flow<List<SafetySessionUpdateEntity>>

    @Upsert
    suspend fun upsert(session: SafetySessionEntity)

    @Upsert
    suspend fun upsertUpdate(update: SafetySessionUpdateEntity)
}
