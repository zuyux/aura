package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.GuardianNotificationEntity
import io.aura.android.domain.model.GuardianNotificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianNotificationDao {
    @Query("SELECT * FROM guardian_notifications ORDER BY createdAtMillis DESC")
    fun observeNotifications(): Flow<List<GuardianNotificationEntity>>

    @Query("SELECT * FROM guardian_notifications WHERE id = :id")
    suspend fun getNotification(id: String): GuardianNotificationEntity?

    @Query("UPDATE guardian_notifications SET status = :status, readAtMillis = COALESCE(readAtMillis, :now), respondedAtMillis = :respondedAtMillis WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: GuardianNotificationStatus,
        now: Long,
        respondedAtMillis: Long?,
    )

    @Query("UPDATE guardian_notifications SET status = :status, readAtMillis = COALESCE(readAtMillis, :now) WHERE id = :id AND status = :unread")
    suspend fun markRead(
        id: String,
        status: GuardianNotificationStatus,
        unread: GuardianNotificationStatus = GuardianNotificationStatus.UNREAD,
        now: Long,
    )

    @Upsert
    suspend fun upsert(notification: GuardianNotificationEntity)

    @Upsert
    suspend fun upsertAll(notifications: List<GuardianNotificationEntity>)
}
