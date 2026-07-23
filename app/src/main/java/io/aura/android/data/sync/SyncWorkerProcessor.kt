package io.aura.android.data.sync

import androidx.room.withTransaction
import androidx.work.ListenableWorker.Result
import io.aura.android.data.local.dao.GuardianContactDao
import io.aura.android.data.local.dao.GuardianNotificationDao
import io.aura.android.data.local.dao.IncidentEvidenceDao
import io.aura.android.data.local.dao.IncidentReportDao
import io.aura.android.data.local.dao.ReportVerificationDao
import io.aura.android.data.local.dao.SafetySessionDao
import io.aura.android.data.local.dao.UserProfileDao
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.mapper.toEntity
import io.aura.android.data.network.NetworkMonitor
import io.aura.android.data.network.mapNetworkErrors
import io.aura.android.data.remote.api.SyncApi
import io.aura.android.data.remote.dto.CreateReportRequestDto
import io.aura.android.data.remote.dto.CreateVerificationRequestDto
import io.aura.android.data.remote.dto.GuardianInviteNotificationRequestDto
import io.aura.android.data.remote.dto.GuardianInviteResponseRequestDto
import io.aura.android.data.remote.dto.GuardianSosContactDto
import io.aura.android.data.remote.dto.GuardianSosNotificationRequestDto
import io.aura.android.data.remote.dto.SyncSafetySessionRequestDto
import io.aura.android.data.remote.dto.SyncSafetySessionUpdateRequestDto
import io.aura.android.data.remote.dto.UpdateSafetySessionRequestDto
import io.aura.android.data.remote.dto.UploadEvidenceRequestDto
import io.aura.android.data.remote.IncidentRemoteDataSource
import io.aura.android.domain.location.LastKnownLocationStore
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.repository.AlertRepository

class SyncWorkerProcessor(
    private val dependencies: SyncWorkerDependencies,
    private val transactionRunner: suspend (suspend () -> Unit) -> Unit = { block ->
        dependencies.database().withTransaction { block() }
    },
) {
    suspend fun syncReports(): Result {
        if (!dependencies.networkMonitor().isOnline()) return Result.retry()

        val queueManager = dependencies.syncQueueManager()
        queueManager.resetInterruptedWork()
        return processQueue(
            queueManager = queueManager,
            entityTypes = listOf(SyncEntityTypes.INCIDENT_REPORT, SyncEntityTypes.REPORT_VERIFICATION),
        ) { item ->
            if (item.entityType == SyncEntityTypes.REPORT_VERIFICATION) {
                syncVerification(item.entityId)
                queueManager.markSucceeded(item.id)
                return@processQueue
            }

            val report = dependencies.incidentReportDao().getReport(item.entityId)
                ?: error("No local report found for ${item.entityId}")
            mapNetworkErrors {
                dependencies.incidentRemoteDataSource().createReport(
                    CreateReportRequestDto(
                    clientId = report.id,
                    type = report.type.name,
                    severity = report.severity.name,
                    description = report.description,
                    latitude = report.latitude,
                    longitude = report.longitude,
                    locationPrecision = report.locationPrecision.name,
                    occurredAtMillis = report.createdAtMillis,
                    visibility = report.visibility.name,
                    anonymous = report.isAnonymous,
                    ),
                )
            }
            transactionRunner {
                dependencies.incidentReportDao().updateStatus(
                    id = report.id,
                    status = ReportStatus.SUBMITTED,
                    updatedAtMillis = System.currentTimeMillis(),
                )
                queueManager.markSucceeded(item.id)
            }
        }
    }

    suspend fun uploadEvidence(): Result {
        if (!dependencies.networkMonitor().isOnline()) return Result.retry()

        val queueManager = dependencies.syncQueueManager()
        queueManager.resetInterruptedWork()
        return processQueue(
            queueManager = queueManager,
            entityTypes = listOf(SyncEntityTypes.INCIDENT_EVIDENCE),
        ) { item ->
            val evidence = dependencies.incidentEvidenceDao().getEvidence(item.entityId)
                ?: error("No local evidence found for ${item.entityId}")
            val response = mapNetworkErrors { dependencies.syncApi().uploadEvidence(
                reportId = evidence.reportId,
                request = UploadEvidenceRequestDto(
                    clientId = evidence.id,
                    type = evidence.type.name,
                    localUri = evidence.localUri,
                    sha256Hash = evidence.sha256Hash,
                    visibility = evidence.visibility.name,
                    createdAtMillis = evidence.createdAtMillis,
                ),
            ) }
            transactionRunner {
                dependencies.incidentEvidenceDao().updateRemoteUrl(evidence.id, response.remoteUrl)
                queueManager.markSucceeded(item.id)
            }
        }
    }

    suspend fun fetchAlerts(): Result {
        if (!dependencies.networkMonitor().isOnline()) return Result.retry()

        val location = dependencies.lastKnownLocationStore().getLastKnownLocation() ?: return Result.success()
        return runCatching {
            dependencies.alertRepository().refreshNearbyAlerts(
                location = location,
                radiusMeters = DEFAULT_ALERT_RADIUS_METERS,
            )
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    suspend fun receiveGuardianNotifications(): Result {
        if (!dependencies.networkMonitor().isOnline()) return Result.retry()

        val profile = dependencies.userProfileDao().getProfile()
        val phoneNumber = profile?.phoneNumber?.takeIf { it.isNotBlank() }
        if (phoneNumber == null) return Result.success()

        return runCatching {
            val remoteNotifications = mapNetworkErrors { dependencies.syncApi().getGuardianNotifications(phoneNumber) }
            val entities = remoteNotifications.mapNotNull { remote ->
                val existing = dependencies.guardianNotificationDao().getNotification(remote.id)
                val existingTerminalStatus = existing?.status?.takeIf {
                    it == GuardianNotificationStatus.ACCEPTED || it == GuardianNotificationStatus.DECLINED
                }
                val type = runCatching { GuardianNotificationType.valueOf(remote.type) }.getOrNull()
                    ?: return@mapNotNull null
                GuardianNotification(
                    id = remote.id,
                    type = type,
                    status = existingTerminalStatus ?: existing?.status ?: GuardianNotificationStatus.UNREAD,
                    senderName = remote.senderName,
                    senderPhoneNumber = remote.senderPhoneNumber,
                    senderPhotoUri = remote.senderPhotoUri,
                    message = remote.message,
                    sessionId = remote.sessionId,
                    latitude = remote.latitude,
                    longitude = remote.longitude,
                    createdAtMillis = remote.createdAtMillis,
                    readAtMillis = existing?.readAtMillis,
                    respondedAtMillis = existing?.respondedAtMillis,
                ).toEntity()
            }
            dependencies.guardianNotificationDao().upsertAll(entities)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    suspend fun syncSafetySessions(): Result {
        if (!dependencies.networkMonitor().isOnline()) return Result.retry()

        val queueManager = dependencies.syncQueueManager()
        queueManager.resetInterruptedWork()
        return processQueue(
            queueManager = queueManager,
            entityTypes = listOf(
                SyncEntityTypes.SAFETY_SESSION,
                SyncEntityTypes.SAFETY_SESSION_UPDATE,
                SyncEntityTypes.GUARDIAN_INVITE_NOTIFICATION,
                SyncEntityTypes.GUARDIAN_INVITE_RESPONSE,
                SyncEntityTypes.GUARDIAN_SOS_NOTIFICATION,
            ),
        ) { item ->
            when (item.entityType) {
                SyncEntityTypes.SAFETY_SESSION -> syncSession(item.entityId)
                SyncEntityTypes.SAFETY_SESSION_UPDATE -> syncSessionUpdate(item.entityId)
                SyncEntityTypes.GUARDIAN_INVITE_NOTIFICATION -> syncGuardianInvite(item.entityId)
                SyncEntityTypes.GUARDIAN_INVITE_RESPONSE -> syncGuardianInviteResponse(item.entityId)
                SyncEntityTypes.GUARDIAN_SOS_NOTIFICATION -> syncSosNotification(item.entityId)
                else -> error("Unsupported safety sync entity ${item.entityType}")
            }
            queueManager.markSucceeded(item.id)
        }
    }

    private suspend fun syncVerification(entityId: String) {
        val verification = dependencies.reportVerificationDao().getVerification(entityId)
            ?: error("No local report verification found for $entityId")
        mapNetworkErrors { dependencies.syncApi().createVerification(
            reportId = verification.reportId,
            request = CreateVerificationRequestDto(
                clientId = verification.id,
                action = verification.action.name,
                deviceId = verification.deviceId,
                createdAtMillis = verification.createdAtMillis,
            ),
        ) }
    }

    private suspend fun syncSession(entityId: String) {
        val session = dependencies.safetySessionDao().getSession(entityId)
            ?: error("No local safety session found for $entityId")
        mapNetworkErrors { dependencies.syncApi().syncSafetySession(
            SyncSafetySessionRequestDto(
                clientId = session.id,
                status = session.status.name,
                latitude = session.lastLatitude,
                longitude = session.lastLongitude,
                locationPrecision = session.lastLocationPrecision?.name,
                startedAtMillis = session.startedAtMillis,
                endedAtMillis = session.endedAtMillis,
            ),
        ) }
        if (session.endedAtMillis != null) {
            mapNetworkErrors { dependencies.syncApi().updateSafetySession(
                sessionId = session.id,
                request = UpdateSafetySessionRequestDto(
                    status = session.status.name,
                    latitude = session.lastLatitude,
                    longitude = session.lastLongitude,
                    locationPrecision = session.lastLocationPrecision?.name,
                    endedAtMillis = session.endedAtMillis,
                ),
            ) }
        }
    }

    private suspend fun syncSessionUpdate(entityId: String) {
        val update = dependencies.safetySessionDao().getUpdate(entityId)
            ?: error("No local safety session update found for $entityId")
        mapNetworkErrors { dependencies.syncApi().syncSafetySessionUpdate(
            sessionId = update.sessionId,
            request = SyncSafetySessionUpdateRequestDto(
                clientId = update.id,
                sessionId = update.sessionId,
                latitude = update.latitude,
                longitude = update.longitude,
                locationPrecision = update.locationPrecision?.name,
                note = update.note,
                createdAtMillis = update.createdAtMillis,
            ),
        ) }
    }

    private suspend fun syncGuardianInvite(entityId: String) {
        val contact = dependencies.guardianContactDao().getContact(entityId)
            ?: error("No local guardian contact found for $entityId")
        val profile = dependencies.userProfileDao().getProfile()
        mapNetworkErrors { dependencies.syncApi().notifyGuardianInvite(
            GuardianInviteNotificationRequestDto(
                contactId = contact.id,
                inviteePhoneNumber = contact.phoneNumber,
                inviterName = profile?.displayName,
                inviterPhoneNumber = profile?.phoneNumber,
                message = "${profile?.displayName?.takeIf { it.isNotBlank() } ?: "Alguien"} te agrego a su Red Guardian. Acepta para formar parte.",
                createdAtMillis = contact.createdAtMillis,
            ),
        ) }
    }

    private suspend fun syncGuardianInviteResponse(entityId: String) {
        val notification = dependencies.guardianNotificationDao().getNotification(entityId)
            ?: error("No local guardian notification found for $entityId")
        if (notification.status != GuardianNotificationStatus.ACCEPTED &&
            notification.status != GuardianNotificationStatus.DECLINED
        ) {
            error("Guardian invite response is not final for $entityId")
        }
        val profile = dependencies.userProfileDao().getProfile()
        mapNetworkErrors { dependencies.syncApi().respondToGuardianInvite(
            notificationId = notification.id,
            request = GuardianInviteResponseRequestDto(
                notificationId = notification.id,
                status = notification.status.name,
                responderPhoneNumber = profile?.phoneNumber,
                respondedAtMillis = notification.respondedAtMillis ?: System.currentTimeMillis(),
            ),
        ) }
    }

    private suspend fun syncSosNotification(entityId: String) {
        val update = dependencies.safetySessionDao().getUpdate(entityId)
            ?: error("No local SOS update found for $entityId")
        val contacts = dependencies.guardianContactDao().getContacts()
        if (contacts.isEmpty()) return

        mapNetworkErrors { dependencies.syncApi().notifyGuardianContacts(
            sessionId = update.sessionId,
            request = GuardianSosNotificationRequestDto(
                sessionId = update.sessionId,
                updateId = update.id,
                contacts = contacts.map { contact ->
                    GuardianSosContactDto(
                        displayName = contact.displayName,
                        phoneNumber = contact.phoneNumber,
                    )
                },
                message = update.note ?: "SOS activado",
                latitude = update.latitude,
                longitude = update.longitude,
                locationPrecision = update.locationPrecision?.name,
                createdAtMillis = update.createdAtMillis,
            ),
        ) }
    }

    private suspend fun processQueue(
        queueManager: SyncQueueManager,
        entityTypes: List<String>,
        block: suspend (io.aura.android.data.local.entity.SyncQueueEntity) -> Unit,
    ): Result {
        var shouldRetry = false
        val items = queueManager.nextItems(entityTypes)

        items.forEach { item ->
            queueManager.markRunning(item.id)
            runCatching { block(item) }
                .onFailure { error ->
                    shouldRetry = true
                    queueManager.markFailed(item.id, error)
                }
        }

        return if (shouldRetry) Result.retry() else Result.success()
    }
}

interface SyncWorkerDependencies {
    fun alertRepository(): AlertRepository
    fun database(): AuraDatabase
    fun guardianContactDao(): GuardianContactDao
    fun guardianNotificationDao(): GuardianNotificationDao
    fun incidentEvidenceDao(): IncidentEvidenceDao
    fun incidentReportDao(): IncidentReportDao
    fun incidentRemoteDataSource(): IncidentRemoteDataSource
    fun lastKnownLocationStore(): LastKnownLocationStore
    fun networkMonitor(): NetworkMonitor
    fun reportVerificationDao(): ReportVerificationDao
    fun safetySessionDao(): SafetySessionDao
    fun syncApi(): SyncApi
    fun syncQueueManager(): SyncQueueManager
    fun userProfileDao(): UserProfileDao
}

private const val DEFAULT_ALERT_RADIUS_METERS = 1_500
