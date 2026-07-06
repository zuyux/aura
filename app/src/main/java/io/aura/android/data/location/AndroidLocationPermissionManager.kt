package io.aura.android.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.location.LocationPermissionManager
import javax.inject.Inject

class AndroidLocationPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationPermissionManager {
    override val locationPermissions: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    override fun hasLocationPermission(): Boolean =
        hasPreciseLocationPermission() || hasCoarseLocationPermission()

    override fun hasPreciseLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}
