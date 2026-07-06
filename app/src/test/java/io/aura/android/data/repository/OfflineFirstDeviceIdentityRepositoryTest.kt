package io.aura.android.data.repository

import io.aura.android.data.local.dao.DeviceIdentityDao
import io.aura.android.data.local.entity.DeviceIdentityEntity
import io.aura.android.domain.security.LocalKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineFirstDeviceIdentityRepositoryTest {
    @Test
    fun `getOrCreateIdentity stores only public key metadata`() = runBlocking {
        val dao = FakeDeviceIdentityDao()
        val keyStore = FakeLocalKeyStore(publicKey = "public-key")
        val repository = OfflineFirstDeviceIdentityRepository(dao, keyStore)

        val identity = repository.getOrCreateIdentity()

        assertEquals("public-key", identity.publicKey)
        assertEquals("public-key", dao.identity.value?.publicKey)
        assertNull(dao.identity.value?.publicKey?.takeIf { it == keyStore.privateKeyMaterial })
        assertNotEquals(keyStore.privateKeyMaterial, dao.identity.value?.publicKey)
    }

    @Test
    fun `getOrCreateIdentity preserves existing identity id`() = runBlocking {
        val dao = FakeDeviceIdentityDao(
            DeviceIdentityEntity(
                id = "device-1",
                publicKey = "old-public-key",
                createdAtMillis = 100L,
            ),
        )
        val repository = OfflineFirstDeviceIdentityRepository(
            deviceIdentityDao = dao,
            localKeyStore = FakeLocalKeyStore(publicKey = "new-public-key"),
        )

        val identity = repository.getOrCreateIdentity()

        assertEquals("device-1", identity.id)
        assertEquals(100L, identity.createdAtMillis)
        assertEquals("new-public-key", identity.publicKey)
    }

    @Test
    fun `signWithDeviceKey delegates signing to local key store`() = runBlocking {
        val repository = OfflineFirstDeviceIdentityRepository(
            deviceIdentityDao = FakeDeviceIdentityDao(),
            localKeyStore = FakeLocalKeyStore(publicKey = "public-key"),
        )

        assertArrayEquals(byteArrayOf(4, 5, 6), repository.signWithDeviceKey(byteArrayOf(1, 2, 3)))
    }

    private class FakeDeviceIdentityDao(
        initialIdentity: DeviceIdentityEntity? = null,
    ) : DeviceIdentityDao {
        val identity = MutableStateFlow(initialIdentity)

        override fun observeIdentity(): Flow<DeviceIdentityEntity?> = identity

        override suspend fun getIdentity(): DeviceIdentityEntity? = identity.value

        override suspend fun upsert(identity: DeviceIdentityEntity) {
            this.identity.value = identity
        }
    }

    private class FakeLocalKeyStore(
        private val publicKey: String,
    ) : LocalKeyStore {
        val privateKeyMaterial = "private-key"

        override fun getOrCreateDevicePublicKey(): String = publicKey

        override fun signWithDeviceKey(payload: ByteArray): ByteArray = byteArrayOf(4, 5, 6)
    }
}
