package io.aura.android.data.evidence

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject

class EvidenceFileHasher @Inject constructor() {
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { "%02x".format(it) }
    }
}
