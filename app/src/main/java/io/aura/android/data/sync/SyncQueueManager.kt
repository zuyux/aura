package io.aura.android.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.network.toNetworkError
import io.aura.android.domain.model.SyncStatus
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SyncQueueManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
) {
    fun observePendingCount(): Flow<Int> =
        syncQueueDao.observeCountByStatuses(listOf(SyncStatus.PENDING, SyncStatus.FAILED, SyncStatus.RUNNING))

    suspend fun nextItems(entityTypes: List<String>, limit: Int = DEFAULT_BATCH_SIZE): List<SyncQueueEntity> =
        syncQueueDao.pendingItemsForEntityTypes(entityTypes = entityTypes, limit = limit)

    suspend fun markRunning(itemId: String) {
        syncQueueDao.markRunning(id = itemId, now = System.currentTimeMillis())
    }

    suspend fun markSucceeded(itemId: String) {
        syncQueueDao.markSucceeded(id = itemId, now = System.currentTimeMillis())
    }

    suspend fun markFailed(itemId: String, error: Throwable) {
        val networkError = error.toNetworkError()
        syncQueueDao.markFailed(
            id = itemId,
            error = networkError.message?.take(MAX_ERROR_LENGTH) ?: networkError::class.java.simpleName,
            now = System.currentTimeMillis(),
        )
    }

    suspend fun resetInterruptedWork() {
        syncQueueDao.resetRunning(now = System.currentTimeMillis())
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 10
        private const val MAX_ERROR_LENGTH = 240
    }
}

class SyncScheduler @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun scheduleAll() {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            REPORT_SYNC_WORK,
            ExistingWorkPolicy.KEEP,
            syncRequest<ReportSyncWorker>(),
        )
        workManager.enqueueUniqueWork(
            EVIDENCE_UPLOAD_WORK,
            ExistingWorkPolicy.KEEP,
            syncRequest<EvidenceUploadWorker>(),
        )
        workManager.enqueueUniqueWork(
            ALERT_FETCH_WORK,
            ExistingWorkPolicy.KEEP,
            syncRequest<AlertFetchWorker>(),
        )
        workManager.enqueueUniqueWork(
            SAFETY_SESSION_WORK,
            ExistingWorkPolicy.KEEP,
            syncRequest<SafetySessionWorker>(),
        )
        workManager.enqueueUniqueWork(
            GUARDIAN_NOTIFICATION_RECEIVE_WORK,
            ExistingWorkPolicy.REPLACE,
            syncRequest<GuardianNotificationReceiveWorker>(),
        )
        workManager.enqueueUniquePeriodicWork(
            GUARDIAN_NOTIFICATION_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest<GuardianNotificationReceiveWorker>(),
        )
    }
}

private inline fun <reified T : androidx.work.ListenableWorker> syncRequest(): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<T>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            SYNC_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS,
        )
        .setInputData(workDataOf())
        .build()

private inline fun <reified T : androidx.work.ListenableWorker> periodicSyncRequest(): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<T>(GUARDIAN_NOTIFICATION_POLL_INTERVAL_MINUTES, TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            SYNC_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS,
        )
        .setInputData(workDataOf())
        .build()

const val REPORT_SYNC_WORK = "aura_report_sync"
const val EVIDENCE_UPLOAD_WORK = "aura_evidence_upload"
const val ALERT_FETCH_WORK = "aura_alert_fetch"
const val SAFETY_SESSION_WORK = "aura_safety_session_sync"
const val GUARDIAN_NOTIFICATION_RECEIVE_WORK = "aura_guardian_notification_receive"
const val GUARDIAN_NOTIFICATION_PERIODIC_WORK = "aura_guardian_notification_periodic"

private const val SYNC_BACKOFF_MILLIS = 30_000L
private const val GUARDIAN_NOTIFICATION_POLL_INTERVAL_MINUTES = 15L
