package io.aura.android.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncQueueDaoTest {
    private lateinit var database: AuraDatabase
    private lateinit var dao: SyncQueueDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.syncQueueDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun pendingItemsForEntityTypes_ordersByPriorityThenCreationTime() = runBlocking {
        dao.upsert(item(id = "normal-old", priority = SyncPriority.NORMAL, createdAtMillis = 10))
        dao.upsert(item(id = "critical-new", priority = SyncPriority.CRITICAL, createdAtMillis = 30))
        dao.upsert(item(id = "critical-old", priority = SyncPriority.CRITICAL, createdAtMillis = 20))
        dao.upsert(item(id = "ignored", entityType = "alert", priority = SyncPriority.CRITICAL))
        dao.upsert(item(id = "running", priority = SyncPriority.CRITICAL, status = SyncStatus.RUNNING))

        val result = dao.pendingItemsForEntityTypes(
            entityTypes = listOf("incident_report"),
            limit = 10,
        )

        assertEquals(listOf("critical-old", "critical-new", "normal-old"), result.map { it.id })
    }

    @Test
    fun markRunning_incrementsAttemptsAndClearsLastError() = runBlocking {
        dao.upsert(item(id = "report-1", attempts = 2, lastError = "timeout"))

        dao.markRunning(id = "report-1", now = 50)

        val result = dao.pendingItemsForEntityTypes(
            entityTypes = listOf("incident_report"),
            statuses = listOf(SyncStatus.RUNNING),
            limit = 1,
        ).single()
        assertEquals(SyncStatus.RUNNING, result.status)
        assertEquals(3, result.attempts)
        assertEquals(null, result.lastError)
        assertEquals(50, result.updatedAtMillis)
    }

    @Test
    fun resetRunning_movesInterruptedWorkBackToPending() = runBlocking {
        dao.upsert(item(id = "running", status = SyncStatus.RUNNING))
        dao.upsert(item(id = "failed", status = SyncStatus.FAILED))

        dao.resetRunning(now = 75)

        val pending = dao.pendingItemsForEntityTypes(
            entityTypes = listOf("incident_report"),
            statuses = listOf(SyncStatus.PENDING),
            limit = 10,
        )
        val failed = dao.pendingItemsForEntityTypes(
            entityTypes = listOf("incident_report"),
            statuses = listOf(SyncStatus.FAILED),
            limit = 10,
        )

        assertEquals(listOf("running"), pending.map { it.id })
        assertEquals(75, pending.single().updatedAtMillis)
        assertEquals(listOf("failed"), failed.map { it.id })
    }

    private fun item(
        id: String,
        entityType: String = "incident_report",
        entityId: String = "$id-entity",
        operation: SyncOperation = SyncOperation.CREATE,
        priority: SyncPriority = SyncPriority.NORMAL,
        status: SyncStatus = SyncStatus.PENDING,
        attempts: Int = 0,
        lastError: String? = null,
        createdAtMillis: Long = 1,
        updatedAtMillis: Long = createdAtMillis,
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
