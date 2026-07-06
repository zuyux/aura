package io.aura.android.domain.location

import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationPrivacyServiceTest {
    private val service = LocationPrivacyService()

    @Test
    fun `approximate precision rounds to three decimals`() {
        val location = AuraLocation(latitude = -12.046374, longitude = -77.042793)

        val result = service.applyPrecision(location, LocationPrecision.APPROXIMATE)

        assertEquals(-12.046, result.latitude, 0.0)
        assertEquals(-77.043, result.longitude, 0.0)
        assertEquals(LocationPrecision.APPROXIMATE, result.precision)
    }

    @Test
    fun `district precision rounds to two decimals`() {
        val location = AuraLocation(latitude = -12.046374, longitude = -77.042793)

        val result = service.applyPrecision(location, LocationPrecision.DISTRICT_ONLY)

        assertEquals(-12.05, result.latitude, 0.0)
        assertEquals(-77.04, result.longitude, 0.0)
        assertEquals(LocationPrecision.DISTRICT_ONLY, result.precision)
    }

    @Test
    fun `exact precision keeps coordinates`() {
        val location = AuraLocation(latitude = -12.046374, longitude = -77.042793)

        val result = service.applyPrecision(location, LocationPrecision.EXACT)

        assertEquals(location.copy(precision = LocationPrecision.EXACT), result)
    }

    @Test
    fun `public precision downgrades exact location to approximate`() {
        val location = AuraLocation(latitude = -12.046374, longitude = -77.042793, precision = LocationPrecision.EXACT)

        val result = service.applyPublicPrecision(location, LocationPrecision.EXACT)

        assertEquals(AuraLocation(latitude = -12.046, longitude = -77.043, precision = LocationPrecision.APPROXIMATE), result)
    }
}
