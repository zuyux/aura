package io.aura.android.domain.location

import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

class LocationPrivacyService @Inject constructor() {
    fun applyPrecision(location: AuraLocation, precision: LocationPrecision): AuraLocation =
        when (precision) {
            LocationPrecision.EXACT -> location.copy(precision = LocationPrecision.EXACT)
            LocationPrecision.APPROXIMATE -> location.roundedTo(decimals = 3, precision = precision)
            LocationPrecision.DISTRICT_ONLY -> location.roundedTo(decimals = 2, precision = precision)
        }

    private fun AuraLocation.roundedTo(decimals: Int, precision: LocationPrecision): AuraLocation {
        val factor = 10.0.pow(decimals)
        return copy(
            latitude = (latitude * factor).roundToLong() / factor,
            longitude = (longitude * factor).roundToLong() / factor,
            precision = precision,
        )
    }
}
