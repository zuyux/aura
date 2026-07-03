package io.aura.android.domain.repository

import io.aura.android.domain.model.Alert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeNearbyAlerts(): Flow<List<Alert>>

    suspend fun seedDemoAlertsIfEmpty()
}
