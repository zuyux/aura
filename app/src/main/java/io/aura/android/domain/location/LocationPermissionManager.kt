package io.aura.android.domain.location

interface LocationPermissionManager {
    val locationPermissions: Array<String>

    fun hasLocationPermission(): Boolean
    fun hasPreciseLocationPermission(): Boolean
}
