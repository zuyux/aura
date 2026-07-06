package io.aura.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.model.ProfileSettings
import io.aura.android.domain.repository.ProfileSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileSettingsDataStore by preferencesDataStore(name = "profile_settings")

class DataStoreProfileSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ProfileSettingsRepository {
    override fun observeSettings(): Flow<ProfileSettings> =
        context.profileSettingsDataStore.data.map { preferences ->
            ProfileSettings(
                privacyDisclaimerAccepted = preferences[PRIVACY_DISCLAIMER_ACCEPTED] ?: false,
                anonymousModeDefault = preferences[ANONYMOUS_MODE_DEFAULT] ?: true,
                offlineModeEnabled = preferences[OFFLINE_MODE_ENABLED] ?: false,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                guardianInviteNotificationsEnabled = preferences[GUARDIAN_INVITE_NOTIFICATIONS_ENABLED] ?: true,
                sosAlertNotificationsEnabled = preferences[SOS_ALERT_NOTIFICATIONS_ENABLED] ?: true,
            )
        }

    override suspend fun setPrivacyDisclaimerAccepted(accepted: Boolean) {
        context.profileSettingsDataStore.edit { preferences ->
            preferences[PRIVACY_DISCLAIMER_ACCEPTED] = accepted
        }
    }

    override suspend fun setAnonymousModeDefault(enabled: Boolean) {
        context.profileSettingsDataStore.edit { preferences ->
            preferences[ANONYMOUS_MODE_DEFAULT] = enabled
        }
    }

    override suspend fun setOfflineModeEnabled(enabled: Boolean) {
        context.profileSettingsDataStore.edit { preferences ->
            preferences[OFFLINE_MODE_ENABLED] = enabled
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.profileSettingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setGuardianInviteNotificationsEnabled(enabled: Boolean) {
        context.profileSettingsDataStore.edit { preferences ->
            preferences[GUARDIAN_INVITE_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setSosAlertNotificationsEnabled(enabled: Boolean) {
        context.profileSettingsDataStore.edit { preferences ->
            preferences[SOS_ALERT_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    private companion object {
        val PRIVACY_DISCLAIMER_ACCEPTED = booleanPreferencesKey("privacy_disclaimer_accepted")
        val ANONYMOUS_MODE_DEFAULT = booleanPreferencesKey("anonymous_mode_default")
        val OFFLINE_MODE_ENABLED = booleanPreferencesKey("offline_mode_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val GUARDIAN_INVITE_NOTIFICATIONS_ENABLED = booleanPreferencesKey("guardian_invite_notifications_enabled")
        val SOS_ALERT_NOTIFICATIONS_ENABLED = booleanPreferencesKey("sos_alert_notifications_enabled")
    }
}
