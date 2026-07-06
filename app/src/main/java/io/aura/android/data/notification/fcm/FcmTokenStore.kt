package io.aura.android.data.notification.fcm

import kotlinx.coroutines.flow.Flow

interface FcmTokenStore {
    fun observeToken(): Flow<String?>
    suspend fun saveToken(token: String)
}
