package io.aura.android.domain.location

import io.aura.android.domain.model.AuraLocation

interface LastKnownLocationStore {
    suspend fun getLastKnownLocation(): AuraLocation?
    suspend fun saveLastKnownLocation(location: AuraLocation)
}
