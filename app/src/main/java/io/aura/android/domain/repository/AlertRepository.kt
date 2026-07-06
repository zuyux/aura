package io.aura.android.domain.repository

import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.VerificationAction
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeNearbyAlerts(): Flow<List<Alert>>

    fun observeAlert(alertId: String): Flow<Alert?>

    suspend fun recordVerification(alertId: String, action: VerificationAction)

    suspend fun refreshNearbyAlerts(location: AuraLocation, radiusMeters: Int)

    suspend fun seedDemoAlertsIfEmpty()
}
