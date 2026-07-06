package io.aura.android.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.location.LastKnownLocationStore
import io.aura.android.domain.location.LocationPermissionManager
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: LocationPermissionManager,
    private val lastKnownLocationStore: LastKnownLocationStore,
) : LocationProvider {
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    override suspend fun getCurrentLocation(): AuraLocation? {
        if (!permissionManager.hasLocationPermission()) return null

        val provider = bestAvailableProvider() ?: return fallbackLocation()
        val liveLocation = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            requestSingleLocation(provider)
        }

        return liveLocation?.toAuraLocation()?.also { location ->
            lastKnownLocationStore.saveLastKnownLocation(location)
        } ?: fallbackLocation()
    }

    private fun bestAvailableProvider(): String? {
        val manager = locationManager ?: return null
        return when {
            permissionManager.hasPreciseLocationPermission() &&
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): AuraLocation? {
        val manager = locationManager ?: return null
        val providers = buildList {
            if (permissionManager.hasPreciseLocationPermission()) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }
        return providers
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { location -> location.time }
            ?.toAuraLocation()
    }

    private suspend fun fallbackLocation(): AuraLocation? {
        val systemLocation = lastKnownLocation()
        if (systemLocation != null) {
            lastKnownLocationStore.saveLastKnownLocation(systemLocation)
            return systemLocation
        }
        return lastKnownLocationStore.getLastKnownLocation()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val manager = locationManager
            if (manager == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }

                @Deprecated("Deprecated by Android framework")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) = Unit
            }

            continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            runCatching {
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }.onFailure {
                manager.removeUpdates(listener)
                if (continuation.isActive) continuation.resume(null)
            }
        }

    private fun Location.toAuraLocation(): AuraLocation =
        AuraLocation(
            latitude = latitude,
            longitude = longitude,
            precision = LocationPrecision.EXACT,
        )
}

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
