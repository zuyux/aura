package io.aura.android.data.remote.mapper

import io.aura.android.data.remote.dto.NetworkAlertDto
import io.aura.android.domain.model.LocationPrecision
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkAlertMappersTest {
    @Test
    fun `network alert exact location maps to approximate public location`() {
        val dto = NetworkAlertDto(
            id = "alert-1",
            reportId = "report-1",
            type = "theft",
            severity = "high",
            latitude = -12.046374,
            longitude = -77.042793,
            locationPrecision = "exact",
            summary = "Reported nearby",
            reportedAtMillis = 100L,
        )

        val alert = dto.toDomain()

        assertEquals(LocationPrecision.APPROXIMATE, alert.location.precision)
        assertEquals(-12.046, alert.location.latitude, 0.0)
        assertEquals(-77.043, alert.location.longitude, 0.0)
    }
}
