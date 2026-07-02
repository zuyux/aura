package io.aura.android.domain.location

import io.aura.android.domain.model.AuraLocation

interface LocationProvider {
    suspend fun getCurrentLocation(): AuraLocation?
}
