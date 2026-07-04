package io.aura.android.data.repository

import io.aura.android.data.local.dao.GuardianContactDao
import io.aura.android.data.local.dao.SafetySessionDao
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.repository.GuardianRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstGuardianRepository @Inject constructor(
    private val guardianContactDao: GuardianContactDao,
    private val safetySessionDao: SafetySessionDao,
) : GuardianRepository {
    override fun observeContacts(): Flow<List<GuardianContact>> =
        guardianContactDao.observeContacts().map { contacts -> contacts.map { it.toDomain() } }

    override fun observeSessions(): Flow<List<SafetySession>> =
        safetySessionDao.observeSessions().map { sessions -> sessions.map { it.toDomain() } }

    override fun observeUpdates(sessionId: String): Flow<List<SafetySessionUpdate>> =
        safetySessionDao.observeUpdates(sessionId).map { updates -> updates.map { it.toDomain() } }

    override suspend fun saveContact(contact: GuardianContact) {
        guardianContactDao.upsert(contact.toEntity())
    }

    override suspend fun saveSession(session: SafetySession) {
        safetySessionDao.upsert(session.toEntity())
    }

    override suspend fun saveUpdate(update: SafetySessionUpdate) {
        safetySessionDao.upsertUpdate(update.toEntity())
    }
}
