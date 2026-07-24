package io.aura.android.data.repository

import androidx.room.withTransaction
import io.aura.android.data.local.dao.GuardianContactDao
import io.aura.android.data.local.dao.GuardianNotificationDao
import io.aura.android.data.local.dao.SafetySessionDao
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.dao.UserProfileDao
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.data.sync.SyncEntityTypes
import io.aura.android.data.sync.SyncScheduler
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.repository.GuardianRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstGuardianRepository @Inject constructor(
    private val database: AuraDatabase,
    private val guardianContactDao: GuardianContactDao,
    private val guardianNotificationDao: GuardianNotificationDao,
    private val safetySessionDao: SafetySessionDao,
    private val syncQueueDao: SyncQueueDao,
    private val userProfileDao: UserProfileDao,
    private val syncScheduler: SyncScheduler,
) : GuardianRepository {
    override fun observeContacts(): Flow<List<GuardianContact>> =
        guardianContactDao.observeContacts().map { contacts -> contacts.map { it.toDomain() } }

    override fun observeNotifications(): Flow<List<GuardianNotification>> =
        guardianNotificationDao.observeNotifications().map { notifications -> notifications.map { it.toDomain() } }

    override fun observeSessions(): Flow<List<SafetySession>> =
        safetySessionDao.observeSessions().map { sessions -> sessions.map { it.toDomain() } }

    override fun observeUpdates(sessionId: String): Flow<List<SafetySessionUpdate>> =
        safetySessionDao.observeUpdates(sessionId).map { updates -> updates.map { it.toDomain() } }

    override suspend fun saveContact(contact: GuardianContact) {
        val now = System.currentTimeMillis()
        val profile = userProfileDao.getProfile()
        database.withTransaction {
            guardianContactDao.upsert(contact.toEntity())
            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = "${SyncEntityTypes.GUARDIAN_INVITE_NOTIFICATION}:${contact.id}",
                    entityType = SyncEntityTypes.GUARDIAN_INVITE_NOTIFICATION,
                    entityId = contact.id,
                    operation = SyncOperation.CREATE,
                    priority = SyncPriority.HIGH,
                    status = SyncStatus.PENDING,
                    attempts = 0,
                    lastError = null,
                    createdAtMillis = contact.createdAtMillis,
                    updatedAtMillis = now,
                ),
            )

            if (profile?.phoneNumber.normalizedPhoneNumber() == contact.phoneNumber.normalizedPhoneNumber()) {
                guardianNotificationDao.upsert(
                    GuardianNotification(
                        id = "guardian_invite:${contact.id}",
                        type = GuardianNotificationType.GUARDIAN_INVITE,
                        status = GuardianNotificationStatus.UNREAD,
                        senderName = profile?.displayName?.takeIf { it.isNotBlank() } ?: "Red Guardián",
                        senderPhoneNumber = profile?.phoneNumber,
                        senderPhotoUri = contact.photoUri,
                        message = "${profile?.displayName?.takeIf { it.isNotBlank() } ?: "Alguien"} te agregó a su Red Guardián. Acepta para formar parte.",
                        sessionId = null,
                        latitude = null,
                        longitude = null,
                        createdAtMillis = now,
                        readAtMillis = null,
                        respondedAtMillis = null,
                    ).toEntity(),
                )
            }
        }
        syncScheduler.scheduleAll()
    }

    override suspend fun removeContact(contactId: String) {
        guardianContactDao.deleteContact(contactId)
    }

    override suspend fun saveNotification(notification: GuardianNotification) {
        guardianNotificationDao.upsert(notification.toEntity())
    }

    override suspend fun markNotificationRead(notificationId: String) {
        guardianNotificationDao.markRead(
            id = notificationId,
            status = GuardianNotificationStatus.READ,
            now = System.currentTimeMillis(),
        )
    }

    override suspend fun acceptGuardianInvite(notificationId: String) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val notification = guardianNotificationDao.getNotification(notificationId) ?: return@withTransaction
            if (notification.type != GuardianNotificationType.GUARDIAN_INVITE) return@withTransaction
            if (notification.status == GuardianNotificationStatus.ACCEPTED) return@withTransaction

            val contacts = guardianContactDao.getContacts()
            val contactId = notification.senderPhoneNumber
                ?.takeIf { it.isNotBlank() }
                ?.let { phoneNumber -> "guardian:${phoneNumber.filter { it.isDigit() }}" }
                ?: "guardian:${notification.id}"
            val existing = contacts.firstOrNull { it.id == contactId || it.phoneNumber == notification.senderPhoneNumber }
            guardianContactDao.upsert(
                GuardianContact(
                    id = existing?.id ?: contactId,
                    displayName = notification.senderName,
                    phoneNumber = notification.senderPhoneNumber.orEmpty(),
                    photoUri = notification.senderPhotoUri,
                    isPrimary = existing?.isPrimary ?: contacts.isEmpty(),
                    createdAtMillis = existing?.createdAtMillis ?: now,
                ).toEntity(),
            )
            guardianNotificationDao.updateStatus(
                id = notificationId,
                status = GuardianNotificationStatus.ACCEPTED,
                now = now,
                respondedAtMillis = now,
            )
            queueInviteResponse(notificationId = notificationId, now = now)
        }
        syncScheduler.scheduleAll()
    }

    override suspend fun declineGuardianInvite(notificationId: String) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            guardianNotificationDao.updateStatus(
                id = notificationId,
                status = GuardianNotificationStatus.DECLINED,
                now = now,
                respondedAtMillis = now,
            )
            queueInviteResponse(notificationId = notificationId, now = now)
        }
        syncScheduler.scheduleAll()
    }

    override suspend fun saveSession(session: SafetySession) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            safetySessionDao.upsert(session.toEntity())
            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = "${SyncEntityTypes.SAFETY_SESSION}:${session.id}",
                    entityType = SyncEntityTypes.SAFETY_SESSION,
                    entityId = session.id,
                    operation = SyncOperation.UPDATE,
                    priority = if (session.status == SafetySessionStatus.SOS_TRIGGERED) {
                        SyncPriority.CRITICAL
                    } else {
                        SyncPriority.NORMAL
                    },
                    status = SyncStatus.PENDING,
                    attempts = 0,
                    lastError = null,
                    createdAtMillis = session.startedAtMillis,
                    updatedAtMillis = now,
                ),
            )
        }
        syncScheduler.scheduleAll()
    }

    override suspend fun saveUpdate(update: SafetySessionUpdate) {
        val isSosUpdate = update.note?.contains("SOS", ignoreCase = true) == true
        database.withTransaction {
            safetySessionDao.upsertUpdate(update.toEntity())
            syncQueueDao.upsert(
                SyncQueueEntity(
                    id = "${SyncEntityTypes.SAFETY_SESSION_UPDATE}:${update.id}",
                    entityType = SyncEntityTypes.SAFETY_SESSION_UPDATE,
                    entityId = update.id,
                    operation = SyncOperation.CREATE,
                    priority = if (isSosUpdate) {
                        SyncPriority.CRITICAL
                    } else {
                        SyncPriority.NORMAL
                    },
                    status = SyncStatus.PENDING,
                    attempts = 0,
                    lastError = null,
                    createdAtMillis = update.createdAtMillis,
                    updatedAtMillis = update.createdAtMillis,
                ),
            )
            if (isSosUpdate) {
                syncQueueDao.upsert(
                    SyncQueueEntity(
                        id = "${SyncEntityTypes.GUARDIAN_SOS_NOTIFICATION}:${update.id}",
                        entityType = SyncEntityTypes.GUARDIAN_SOS_NOTIFICATION,
                        entityId = update.id,
                        operation = SyncOperation.CREATE,
                        priority = SyncPriority.CRITICAL,
                        status = SyncStatus.PENDING,
                        attempts = 0,
                        lastError = null,
                        createdAtMillis = update.createdAtMillis,
                        updatedAtMillis = update.createdAtMillis,
                    ),
                )
            }
        }
        syncScheduler.scheduleAll()
    }

    private suspend fun queueInviteResponse(notificationId: String, now: Long) {
        syncQueueDao.upsert(
            SyncQueueEntity(
                id = "${SyncEntityTypes.GUARDIAN_INVITE_RESPONSE}:$notificationId",
                entityType = SyncEntityTypes.GUARDIAN_INVITE_RESPONSE,
                entityId = notificationId,
                operation = SyncOperation.UPDATE,
                priority = SyncPriority.HIGH,
                status = SyncStatus.PENDING,
                attempts = 0,
                lastError = null,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
    }
}

private fun String?.normalizedPhoneNumber(): String =
    orEmpty().filter { it.isDigit() }.takeLast(10)
