package io.aura.android.domain.repository

import io.aura.android.domain.model.DeviceIdentity
import kotlinx.coroutines.flow.Flow

interface DeviceIdentityRepository {
    fun observeIdentity(): Flow<DeviceIdentity?>
    suspend fun getOrCreateIdentity(): DeviceIdentity
    suspend fun signWithDeviceKey(payload: ByteArray): ByteArray
}
