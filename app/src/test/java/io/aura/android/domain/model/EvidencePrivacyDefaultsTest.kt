package io.aura.android.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EvidencePrivacyDefaultsTest {
    @Test
    fun `evidence defaults to private visibility`() {
        assertEquals(EvidenceVisibility.PRIVATE, EvidencePrivacyDefaults.DEFAULT_VISIBILITY)
    }
}
