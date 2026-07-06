package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.DeviceIdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceIdentityDao {
    @Query("SELECT * FROM device_identities LIMIT 1")
    fun observeIdentity(): Flow<DeviceIdentityEntity?>

    @Query("SELECT * FROM device_identities LIMIT 1")
    suspend fun getIdentity(): DeviceIdentityEntity?

    @Upsert
    suspend fun upsert(identity: DeviceIdentityEntity)
}
