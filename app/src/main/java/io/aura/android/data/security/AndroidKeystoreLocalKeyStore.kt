package io.aura.android.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import io.aura.android.domain.security.LocalKeyStore
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidKeystoreLocalKeyStore @Inject constructor() : LocalKeyStore {
    override fun getOrCreateDevicePublicKey(): String {
        val publicKey = getOrCreateDeviceKeyPair().public
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    override fun signWithDeviceKey(payload: ByteArray): ByteArray {
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(getOrCreateDeviceKeyPair().private)
        signature.update(payload)
        return signature.sign()
    }

    @Synchronized
    private fun getOrCreateDeviceKeyPair(): KeyPair {
        val keyStore = loadKeyStore()
        val privateKey = keyStore.getKey(DEVICE_SIGNING_KEY_ALIAS, null) as? PrivateKey
        val certificate = keyStore.getCertificate(DEVICE_SIGNING_KEY_ALIAS)
        if (privateKey != null && certificate != null) {
            return KeyPair(certificate.publicKey, privateKey)
        }

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE_PROVIDER,
        )
        generator.initialize(
            KeyGenParameterSpec.Builder(
                DEVICE_SIGNING_KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build(),
        )
        return generator.generateKeyPair()
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) }

    private companion object {
        const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val DEVICE_SIGNING_KEY_ALIAS = "aura_device_signing_key"
        const val EC_CURVE = "secp256r1"
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    }
}
