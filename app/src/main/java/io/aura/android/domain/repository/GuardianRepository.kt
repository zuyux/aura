package io.aura.android.domain.repository

import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionUpdate
import kotlinx.coroutines.flow.Flow

interface GuardianRepository {
    fun observeContacts(): Flow<List<GuardianContact>>
    fun observeSessions(): Flow<List<SafetySession>>
    fun observeUpdates(sessionId: String): Flow<List<SafetySessionUpdate>>
    suspend fun saveContact(contact: GuardianContact)
    suspend fun saveSession(session: SafetySession)
    suspend fun saveUpdate(update: SafetySessionUpdate)
}
