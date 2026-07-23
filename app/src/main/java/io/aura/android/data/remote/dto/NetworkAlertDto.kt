package io.aura.android.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkAlertDto(
    val id: String,
    @SerialName("report_id")
    val reportId: String? = null,
    val type: String,
    val severity: String,
    val status: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: NetworkLocationDto? = null,
    @SerialName("location_precision")
    val locationPrecision: String? = null,
    val summary: String? = null,
    val description: String? = null,
    @SerialName("distance_meters")
    val distanceMeters: Int? = null,
    @SerialName("occurred_at_millis")
    val reportedAtMillis: Long? = null,
    val reportedAt: Long? = null,
    val occurredAt: Long? = null,
)

@Serializable
data class NetworkLocationDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    val precision: String? = null,
)
