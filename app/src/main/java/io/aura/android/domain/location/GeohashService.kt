package io.aura.android.domain.location

import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import javax.inject.Inject

class GeohashService @Inject constructor() {
    fun encode(location: AuraLocation, length: Int = DEFAULT_GEOHASH_LENGTH): String {
        require(length in 1..MAX_GEOHASH_LENGTH) { "Geohash length must be between 1 and $MAX_GEOHASH_LENGTH." }

        val latitudeRange = doubleArrayOf(MIN_LATITUDE, MAX_LATITUDE)
        val longitudeRange = doubleArrayOf(MIN_LONGITUDE, MAX_LONGITUDE)
        val geohash = StringBuilder(length)
        var bits = 0
        var bit = 0
        var isLongitude = true

        while (geohash.length < length) {
            val range = if (isLongitude) longitudeRange else latitudeRange
            val value = if (isLongitude) location.longitude else location.latitude
            val midpoint = (range[0] + range[1]) / 2

            if (value >= midpoint) {
                bits = bits or BITS[bit]
                range[0] = midpoint
            } else {
                range[1] = midpoint
            }

            isLongitude = !isLongitude
            if (bit < BITS.lastIndex) {
                bit += 1
            } else {
                geohash.append(BASE32[bits])
                bits = 0
                bit = 0
            }
        }

        return geohash.toString()
    }

    fun lengthForPrecision(precision: LocationPrecision): Int =
        when (precision) {
            LocationPrecision.EXACT -> 8
            LocationPrecision.APPROXIMATE -> 6
            LocationPrecision.DISTRICT_ONLY -> 5
        }

    fun encodeForPrecision(location: AuraLocation, precision: LocationPrecision): String =
        encode(location = location, length = lengthForPrecision(precision))
}

private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
private val BITS = intArrayOf(16, 8, 4, 2, 1)
private const val DEFAULT_GEOHASH_LENGTH = 7
private const val MAX_GEOHASH_LENGTH = 12
private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0
