package io.aura.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.EvidenceVisibility

@Entity(
    tableName = "incident_evidence",
    indices = [Index(value = ["reportId"])],
)
data class IncidentEvidenceEntity(
    @PrimaryKey val id: String,
    val reportId: String,
    val type: EvidenceType,
    val localUri: String,
    val remoteUrl: String?,
    val visibility: EvidenceVisibility,
    val createdAtMillis: Long,
)
