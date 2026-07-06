package io.aura.android.data.repository

import io.aura.android.data.local.dao.DeviceIdentityDao
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.domain.model.DeviceIdentity
import io.aura.android.domain.repository.DeviceIdentityRepository
import io.aura.android.domain.security.LocalKeyStore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineFirstDeviceIdentityRepository @Inject constructor(
    private val deviceIdentityDao: DeviceIdentityDao,
    private val localKeyStore: LocalKeyStore,
) : DeviceIdentityRepository {
    override fun observeIdentity(): Flow<DeviceIdentity?> =
        deviceIdentityDao.observeIdentity().map { identity -> identity?.toDomain() }

    override suspend fun getOrCreateIdentity(): DeviceIdentity {
        val storedIdentity = deviceIdentityDao.getIdentity()?.toDomain()
        val publicKey = localKeyStore.getOrCreateDevicePublicKey()

        if (storedIdentity?.publicKey == publicKey) {
            return storedIdentity
        }

        val identity = DeviceIdentity(
            id = storedIdentity?.id ?: UUID.randomUUID().toString(),
            publicKey = publicKey,
            createdAtMillis = storedIdentity?.createdAtMillis ?: System.currentTimeMillis(),
        )
        deviceIdentityDao.upsert(identity.toEntity())
        return identity
    }

    override suspend fun signWithDeviceKey(payload: ByteArray): ByteArray =
        localKeyStore.signWithDeviceKey(payload)
}
