package io.aura.android.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class ReportSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork() = SyncWorkerProcessor(syncEntryPoint()).syncReports()
}

class EvidenceUploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork() = SyncWorkerProcessor(syncEntryPoint()).uploadEvidence()
}

class AlertFetchWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork() = SyncWorkerProcessor(syncEntryPoint()).fetchAlerts()
}

class GuardianNotificationReceiveWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork() = SyncWorkerProcessor(syncEntryPoint()).receiveGuardianNotifications()
}

class SafetySessionWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork() = SyncWorkerProcessor(syncEntryPoint()).syncSafetySessions()
}

private fun CoroutineWorker.syncEntryPoint(): SyncWorkerEntryPoint =
    EntryPoints.get(applicationContext, SyncWorkerEntryPoint::class.java)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint : SyncWorkerDependencies
