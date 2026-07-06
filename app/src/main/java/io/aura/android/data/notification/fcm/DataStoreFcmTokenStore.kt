package io.aura.android.data.notification.fcm

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.fcmTokenDataStore by preferencesDataStore(name = "fcm_token")

@Singleton
class DataStoreFcmTokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : FcmTokenStore {
    override fun observeToken(): Flow<String?> =
        context.fcmTokenDataStore.data.map { preferences -> preferences[FCM_TOKEN] }

    override suspend fun saveToken(token: String) {
        context.fcmTokenDataStore.edit { preferences ->
            preferences[FCM_TOKEN] = token
        }
    }
}

private val FCM_TOKEN = stringPreferencesKey("fcm_token")
