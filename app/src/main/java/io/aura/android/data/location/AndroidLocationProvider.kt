package io.aura.android.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    override suspend fun getCurrentLocation(): AuraLocation? {
        if (!hasLocationPermission()) return null

        val provider = bestAvailableProvider() ?: return lastKnownLocation()
        val liveLocation = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            requestSingleLocation(provider)
        }

        return liveLocation?.toAuraLocation() ?: lastKnownLocation()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    private fun bestAvailableProvider(): String? {
        val manager = locationManager ?: return null
        return when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): AuraLocation? {
        val manager = locationManager ?: return null
        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { provider -> manager.getLastKnownLocation(provider) }
            .maxByOrNull { location -> location.time }
            ?.toAuraLocation()
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
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }

    private fun Location.toAuraLocation(): AuraLocation =
        AuraLocation(
            latitude = latitude,
            longitude = longitude,
            precision = LocationPrecision.EXACT,
        )
}

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
