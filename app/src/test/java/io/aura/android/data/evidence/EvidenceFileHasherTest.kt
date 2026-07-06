package io.aura.android.data.evidence

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EvidenceFileHasherTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `sha256 returns lowercase hash for evidence file contents`() {
        val file = File(temporaryFolder.root, "evidence.txt")
        file.writeText("aura evidence")

        val hash = EvidenceFileHasher().sha256(file)

        assertEquals("efd8a62a27e68b91a100d22c3aa6fa0a05b77b20f7f1701e2c52c54f39f1d0ef", hash)
    }
}
