package io.aura.android.data.remote.api

import io.aura.android.data.remote.dto.NetworkAlertDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AlertApi {
    @GET("alerts/nearby")
    suspend fun getNearbyAlerts(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusMeters: Int,
    ): List<NetworkAlertDto>
}
