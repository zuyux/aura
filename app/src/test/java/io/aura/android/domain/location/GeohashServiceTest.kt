package io.aura.android.domain.location

import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import org.junit.Assert.assertEquals
import org.junit.Test

class GeohashServiceTest {
    private val service = GeohashService()

    @Test
    fun `encodes known coordinate`() {
        val location = AuraLocation(latitude = 42.6, longitude = -5.6)

        val result = service.encode(location, length = 5)

        assertEquals("ezs42", result)
    }

    @Test
    fun `uses shorter geohash for less precise locations`() {
        val location = AuraLocation(latitude = -12.046374, longitude = -77.042793)

        assertEquals(8, service.encodeForPrecision(location, LocationPrecision.EXACT).length)
        assertEquals(6, service.encodeForPrecision(location, LocationPrecision.APPROXIMATE).length)
        assertEquals(5, service.encodeForPrecision(location, LocationPrecision.DISTRICT_ONLY).length)
    }
}
