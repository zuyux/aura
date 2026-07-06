package io.aura.android.data.sync

import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import java.net.SocketTimeoutException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncQueueManagerTest {
    private val dao = FakeSyncQueueDao()
    private val manager = SyncQueueManager(dao)

    @Test
    fun `observe pending count includes retryable and running statuses`() = runBlocking {
        dao.pendingCount = 4

        val count = manager.observePendingCount().first()

        assertEquals(4, count)
        assertEquals(listOf(SyncStatus.PENDING, SyncStatus.FAILED, SyncStatus.RUNNING), dao.lastObservedStatuses)
    }

    @Test
    fun `next items requests pending work for entity types using default batch size`() = runBlocking {
        val item = syncQueueItem(id = "sync-1", entityType = "incident_report")
        dao.items = listOf(item)

        val result = manager.nextItems(entityTypes = listOf("incident_report"))

        assertEquals(listOf(item), result)
        assertEquals(listOf("incident_report"), dao.lastRequestedEntityTypes)
        assertEquals(SyncQueueManager.DEFAULT_BATCH_SIZE, dao.lastRequestedLimit)
        assertEquals(listOf(SyncStatus.PENDING, SyncStatus.FAILED), dao.lastRequestedStatuses)
    }

    @Test
    fun `mark running increments attempts and clears previous error`() = runBlocking {
        dao.upsert(syncQueueItem(id = "sync-1", attempts = 2, status = SyncStatus.FAILED, lastError = "timeout"))

        manager.markRunning("sync-1")

        val item = dao.item("sync-1")
        assertEquals(SyncStatus.RUNNING, item.status)
        assertEquals(3, item.attempts)
        assertNull(item.lastError)
        assertTrue(item.updatedAtMillis > 0)
    }

    @Test
    fun `mark succeeded clears error without changing attempts`() = runBlocking {
        dao.upsert(syncQueueItem(id = "sync-1", attempts = 2, status = SyncStatus.RUNNING, lastError = "timeout"))

        manager.markSucceeded("sync-1")

        val item = dao.item("sync-1")
        assertEquals(SyncStatus.SUCCEEDED, item.status)
        assertEquals(2, item.attempts)
        assertNull(item.lastError)
        assertTrue(item.updatedAtMillis > 0)
    }

    @Test
    fun `mark failed stores normalized timeout message`() = runBlocking {
        dao.upsert(syncQueueItem(id = "sync-1", status = SyncStatus.RUNNING))

        manager.markFailed("sync-1", SocketTimeoutException("connect timed out"))

        val item = dao.item("sync-1")
        assertEquals(SyncStatus.FAILED, item.status)
        assertEquals("Network request timed out", item.lastError)
        assertTrue(item.updatedAtMillis > 0)
    }

    @Test
    fun `mark failed stores normalized unknown error message`() = runBlocking {
        dao.upsert(syncQueueItem(id = "sync-1", status = SyncStatus.RUNNING))

        manager.markFailed("sync-1", NullPointerException())

        val item = dao.item("sync-1")
        assertEquals(SyncStatus.FAILED, item.status)
        assertEquals("Network request failed", item.lastError)
    }

    @Test
    fun `reset interrupted work moves only running items back to pending`() = runBlocking {
        dao.upsert(syncQueueItem(id = "running", status = SyncStatus.RUNNING))
        dao.upsert(syncQueueItem(id = "failed", status = SyncStatus.FAILED))

        manager.resetInterruptedWork()

        assertEquals(SyncStatus.PENDING, dao.item("running").status)
        assertEquals(SyncStatus.FAILED, dao.item("failed").status)
    }

    private fun syncQueueItem(
        id: String,
        entityType: String = "incident_report",
        entityId: String = "report-1",
        operation: SyncOperation = SyncOperation.CREATE,
        priority: SyncPriority = SyncPriority.NORMAL,
        status: SyncStatus = SyncStatus.PENDING,
        attempts: Int = 0,
        lastError: String? = null,
        createdAtMillis: Long = 1L,
        updatedAtMillis: Long = 1L,
    ) = SyncQueueEntity(
        id = id,
        entityType = entityType,
        entityId = entityId,
        operation = operation,
        priority = priority,
        status = status,
        attempts = attempts,
        lastError = lastError,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

private class FakeSyncQueueDao : SyncQueueDao {
    private val storedItems = linkedMapOf<String, SyncQueueEntity>()

    var pendingCount = 0
    var items = emptyList<SyncQueueEntity>()
    var lastObservedStatuses: List<SyncStatus>? = null
    var lastRequestedEntityTypes: List<String>? = null
    var lastRequestedStatuses: List<SyncStatus>? = null
    var lastRequestedLimit: Int? = null

    override fun observeByStatus(status: SyncStatus): Flow<List<SyncQueueEntity>> =
        flowOf(storedItems.values.filter { it.status == status })

    override fun observeCountByStatuses(statuses: List<SyncStatus>): Flow<Int> {
        lastObservedStatuses = statuses
        return flowOf(pendingCount)
    }

    override suspend fun pendingItemsForEntityTypes(
        entityTypes: List<String>,
        statuses: List<SyncStatus>,
        limit: Int,
    ): List<SyncQueueEntity> {
        lastRequestedEntityTypes = entityTypes
        lastRequestedStatuses = statuses
        lastRequestedLimit = limit
        return items
    }

    override suspend fun markRunning(id: String, status: SyncStatus, now: Long) {
        update(id) {
            it.copy(status = status, attempts = it.attempts + 1, lastError = null, updatedAtMillis = now)
        }
    }

    override suspend fun markSucceeded(id: String, status: SyncStatus, now: Long) {
        update(id) {
            it.copy(status = status, lastError = null, updatedAtMillis = now)
        }
    }

    override suspend fun markFailed(id: String, error: String, status: SyncStatus, now: Long) {
        update(id) {
            it.copy(status = status, lastError = error, updatedAtMillis = now)
        }
    }

    override suspend fun resetRunning(fromStatus: SyncStatus, toStatus: SyncStatus, now: Long) {
        storedItems.replaceAll { _, item ->
            if (item.status == fromStatus) item.copy(status = toStatus, updatedAtMillis = now) else item
        }
    }

    override suspend fun deleteForEntity(entityType: String, entityId: String) {
        storedItems.entries.removeIf { (_, item) ->
            item.entityType == entityType && item.entityId == entityId
        }
    }

    override suspend fun upsert(item: SyncQueueEntity) {
        storedItems[item.id] = item
    }

    fun item(id: String): SyncQueueEntity =
        checkNotNull(storedItems[id]) { "Expected sync item $id to exist" }

    private fun update(id: String, transform: (SyncQueueEntity) -> SyncQueueEntity) {
        storedItems[id] = transform(item(id))
    }
}
