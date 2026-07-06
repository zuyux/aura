package io.aura.android.domain.repository

import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionUpdate
import kotlinx.coroutines.flow.Flow

interface GuardianRepository {
    fun observeContacts(): Flow<List<GuardianContact>>
    fun observeNotifications(): Flow<List<GuardianNotification>>
    fun observeSessions(): Flow<List<SafetySession>>
    fun observeUpdates(sessionId: String): Flow<List<SafetySessionUpdate>>
    suspend fun saveContact(contact: GuardianContact)
    suspend fun removeContact(contactId: String)
    suspend fun saveNotification(notification: GuardianNotification)
    suspend fun markNotificationRead(notificationId: String)
    suspend fun acceptGuardianInvite(notificationId: String)
    suspend fun declineGuardianInvite(notificationId: String)
    suspend fun saveSession(session: SafetySession)
    suspend fun saveUpdate(update: SafetySessionUpdate)
}
