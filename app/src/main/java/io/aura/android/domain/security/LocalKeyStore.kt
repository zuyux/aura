package io.aura.android.domain.security

interface LocalKeyStore {
    fun getOrCreateDevicePublicKey(): String
    fun signWithDeviceKey(payload: ByteArray): ByteArray
}
