package io.aura.android.data.remote

import io.aura.android.data.remote.dto.CreateReportRequestDto
import io.aura.android.data.remote.dto.NetworkAlertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

interface IncidentRemoteDataSource {
    suspend fun createReport(report: CreateReportRequestDto)
    suspend fun getNearbyCommunityReports(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        limit: Int = 100,
    ): List<NetworkAlertDto>
}

class SupabaseIncidentRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient,
) : IncidentRemoteDataSource {
    override suspend fun createReport(report: CreateReportRequestDto) {
        supabase.from("incident_reports").insert(report)
    }

    override suspend fun getNearbyCommunityReports(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        limit: Int,
    ): List<NetworkAlertDto> =
        supabase.postgrest.rpc(
            function = "nearby_incident_reports",
            parameters = NearbyIncidentReportsParameters(
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                limit = limit,
            ),
        ).decodeList()
}

@Serializable
private data class NearbyIncidentReportsParameters(
    @SerialName("p_latitude")
    val latitude: Double,
    @SerialName("p_longitude")
    val longitude: Double,
    @SerialName("p_radius_meters")
    val radiusMeters: Int,
    @SerialName("p_limit")
    val limit: Int,
)
