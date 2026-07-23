package io.aura.android.data.remote

import io.aura.android.data.remote.dto.CreateReportRequestDto
import io.aura.android.data.remote.dto.NetworkAlertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

interface IncidentRemoteDataSource {
    suspend fun createReport(report: CreateReportRequestDto)
    suspend fun getCommunityReports(limit: Long = 100): List<NetworkAlertDto>
}

class SupabaseIncidentRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient,
) : IncidentRemoteDataSource {
    override suspend fun createReport(report: CreateReportRequestDto) {
        supabase.from("incident_reports").insert(report)
    }

    override suspend fun getCommunityReports(limit: Long): List<NetworkAlertDto> =
        supabase.from("incident_reports").select {
            filter { eq("visibility", "COMMUNITY") }
            order("occurred_at_millis", Order.DESCENDING)
            limit(limit)
        }.decodeList()
}
