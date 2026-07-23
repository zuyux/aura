package io.aura.android.data.remote.api

import io.aura.android.data.remote.dto.CreateVerificationRequestDto
import io.aura.android.data.remote.dto.GuardianInviteNotificationRequestDto
import io.aura.android.data.remote.dto.GuardianInviteResponseRequestDto
import io.aura.android.data.remote.dto.GuardianNotificationDto
import io.aura.android.data.remote.dto.GuardianSosNotificationRequestDto
import io.aura.android.data.remote.dto.NetworkIncidentReportDto
import io.aura.android.data.remote.dto.SyncSafetySessionRequestDto
import io.aura.android.data.remote.dto.SyncSafetySessionUpdateRequestDto
import io.aura.android.data.remote.dto.UpdateSafetySessionRequestDto
import io.aura.android.data.remote.dto.UploadEvidenceRequestDto
import io.aura.android.data.remote.dto.UploadEvidenceResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SyncApi {
    @GET("reports/nearby")
    suspend fun getNearbyReports(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusMeters: Int,
    ): List<NetworkIncidentReportDto>

    @GET("reports/{reportId}")
    suspend fun getReport(@Path("reportId") reportId: String): NetworkIncidentReportDto

    @POST("reports/{reportId}/evidence")
    suspend fun uploadEvidence(
        @Path("reportId") reportId: String,
        @Body request: UploadEvidenceRequestDto,
    ): UploadEvidenceResponseDto

    @POST("reports/{reportId}/verifications")
    suspend fun createVerification(
        @Path("reportId") reportId: String,
        @Body request: CreateVerificationRequestDto,
    )

    @POST("safety-sessions")
    suspend fun syncSafetySession(@Body request: SyncSafetySessionRequestDto)

    @PATCH("safety-sessions/{sessionId}")
    suspend fun updateSafetySession(
        @Path("sessionId") sessionId: String,
        @Body request: UpdateSafetySessionRequestDto,
    )

    @POST("safety-sessions/{sessionId}/updates")
    suspend fun syncSafetySessionUpdate(
        @Path("sessionId") sessionId: String,
        @Body request: SyncSafetySessionUpdateRequestDto,
    )

    @POST("safety-sessions/{sessionId}/sos-notifications")
    suspend fun notifyGuardianContacts(
        @Path("sessionId") sessionId: String,
        @Body request: GuardianSosNotificationRequestDto,
    )

    @POST("guardian-invitations")
    suspend fun notifyGuardianInvite(@Body request: GuardianInviteNotificationRequestDto)

    @GET("guardian-notifications")
    suspend fun getGuardianNotifications(
        @Query("phoneNumber") phoneNumber: String,
    ): List<GuardianNotificationDto>

    @POST("guardian-notifications/{notificationId}/response")
    suspend fun respondToGuardianInvite(
        @Path("notificationId") notificationId: String,
        @Body request: GuardianInviteResponseRequestDto,
    )
}
