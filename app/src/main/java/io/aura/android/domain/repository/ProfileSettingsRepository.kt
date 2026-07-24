package io.aura.android.domain.repository

import io.aura.android.domain.model.ProfileSettings
import io.aura.android.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ProfileSettingsRepository {
    fun observeSettings(): Flow<ProfileSettings>
    suspend fun setPrivacyDisclaimerAccepted(accepted: Boolean)
    suspend fun setAnonymousModeDefault(enabled: Boolean)
    suspend fun setOfflineModeEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setGuardianInviteNotificationsEnabled(enabled: Boolean)
    suspend fun setSosAlertNotificationsEnabled(enabled: Boolean)
    suspend fun setThemeMode(themeMode: ThemeMode)
}
