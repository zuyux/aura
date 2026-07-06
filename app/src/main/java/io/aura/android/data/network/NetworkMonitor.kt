package io.aura.android.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.repository.ProfileSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileSettingsRepository: ProfileSettingsRepository,
) {
    fun isOnline(): Boolean {
        val offlineModeEnabled = runBlocking {
            profileSettingsRepository.observeSettings().first().offlineModeEnabled
        }
        if (offlineModeEnabled) return false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
