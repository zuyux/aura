package io.aura.android.domain.model

data class AuraLocation(
    val latitude: Double,
    val longitude: Double,
    val precision: LocationPrecision = LocationPrecision.APPROXIMATE,
)
