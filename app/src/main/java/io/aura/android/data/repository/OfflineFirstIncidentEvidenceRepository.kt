package io.aura.android.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.data.evidence.EvidenceFileHasher
import io.aura.android.data.local.dao.IncidentEvidenceDao
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.database.AuraDatabase
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.data.sync.SyncEntityTypes
import io.aura.android.data.sync.SyncScheduler
import io.aura.android.domain.model.EvidencePrivacyDefaults
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.IncidentEvidence
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.repository.IncidentEvidenceRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OfflineFirstIncidentEvidenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AuraDatabase,
    private val incidentEvidenceDao: IncidentEvidenceDao,
    private val syncQueueDao: SyncQueueDao,
    private val syncScheduler: SyncScheduler,
    private val evidenceFileHasher: EvidenceFileHasher,
) : IncidentEvidenceRepository {
    override fun observeEvidenceForReport(reportId: String): Flow<List<IncidentEvidence>> =
        incidentEvidenceDao.observeForReport(reportId).map { evidence ->
            evidence.map { it.toDomain() }
        }

    override suspend fun addEvidence(
        reportId: String,
        type: EvidenceType,
        sourceUri: String,
    ): IncidentEvidence = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val evidenceId = UUID.randomUUID().toString()
        val destination = createEvidenceFile(reportId, evidenceId, type, sourceUri.toUri())

        writeEvidenceFile(sourceUri.toUri(), type, destination)
        val evidence = IncidentEvidence(
            id = evidenceId,
            reportId = reportId,
            type = type,
            localUri = destination.toUri().toString(),
            remoteUrl = null,
            sha256Hash = evidenceFileHasher.sha256(destination),
            visibility = EvidencePrivacyDefaults.DEFAULT_VISIBILITY,
            createdAtMillis = now,
        )

        database.withTransaction {
            incidentEvidenceDao.upsert(evidence.toEntity())
            syncQueueDao.upsert(
                    SyncQueueEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = SyncEntityTypes.INCIDENT_EVIDENCE,
                        entityId = evidence.id,
                        operation = SyncOperation.UPLOAD,
                        priority = SyncPriority.LOW,
                    status = SyncStatus.PENDING,
                    attempts = 0,
                    lastError = null,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                ),
            )
        }

        syncScheduler.scheduleAll()
        evidence
    }

    override suspend fun deleteLocalEvidence(evidenceId: String) = withContext(Dispatchers.IO) {
        val evidence = incidentEvidenceDao.getEvidence(evidenceId) ?: return@withContext
        deleteLocalFile(evidence.localUri.toUri())

        database.withTransaction {
            incidentEvidenceDao.deleteById(evidenceId)
            syncQueueDao.deleteForEntity(
                entityType = SyncEntityTypes.INCIDENT_EVIDENCE,
                entityId = evidenceId,
            )
        }
    }

    private fun deleteLocalFile(uri: Uri) {
        if (uri.scheme != "file") return
        uri.path?.let { path -> File(path).delete() }
    }

    private fun createEvidenceFile(
        reportId: String,
        evidenceId: String,
        type: EvidenceType,
        sourceUri: Uri,
    ): File {
        val directory = File(context.filesDir, "incident_evidence/$reportId").apply { mkdirs() }
        val extension = when (type) {
            EvidenceType.PHOTO -> imageExtension(sourceUri)
            EvidenceType.VIDEO -> contentExtension(sourceUri) ?: "mp4"
            EvidenceType.AUDIO -> contentExtension(sourceUri) ?: "m4a"
        }
        return File(directory, "$evidenceId.$extension")
    }

    private fun writeEvidenceFile(sourceUri: Uri, type: EvidenceType, destination: File) {
        if (type == EvidenceType.PHOTO) {
            stripExifAndWriteImage(sourceUri, destination)
        } else {
            context.contentResolver.openInputStream(sourceUri).use { input ->
                requireNotNull(input) { "No se pudo abrir el archivo seleccionado." }
                destination.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun stripExifAndWriteImage(sourceUri: Uri, destination: File) {
        val bitmap = context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "No se pudo abrir la imagen seleccionada." }
            BitmapFactory.decodeStream(input)
        }
        requireNotNull(bitmap) { "No se pudo leer la imagen seleccionada." }

        val orientedBitmap = bitmap.applyExifOrientation(sourceUri)
        destination.outputStream().use { output ->
            val format = if (destination.extension.equals("png", ignoreCase = true)) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            orientedBitmap.compress(format, IMAGE_QUALITY, output)
        }
        if (orientedBitmap !== bitmap) {
            orientedBitmap.recycle()
        }
        bitmap.recycle()
    }

    private fun Bitmap.applyExifOrientation(sourceUri: Uri): Bitmap {
        val orientation = context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "No se pudo abrir la imagen seleccionada." }
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> preScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    preScale(-1f, 1f)
                    postRotate(90f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    preScale(-1f, 1f)
                    postRotate(270f)
                }
            }
        }
        if (matrix.isIdentity) return this
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun imageExtension(sourceUri: Uri): String =
        when (contentExtension(sourceUri)) {
            "png" -> "png"
            else -> "jpg"
        }

    private fun contentExtension(sourceUri: Uri): String? {
        val mimeType = context.contentResolver.getType(sourceUri) ?: return null
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
}
private const val IMAGE_QUALITY = 92
