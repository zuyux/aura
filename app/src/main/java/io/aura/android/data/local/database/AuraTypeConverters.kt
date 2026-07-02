package io.aura.android.data.local.database

import androidx.room.TypeConverter
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.EvidenceVisibility
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.ReportStatus
import io.aura.android.domain.model.ReportVisibility
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.model.SyncOperation
import io.aura.android.domain.model.SyncPriority
import io.aura.android.domain.model.SyncStatus
import io.aura.android.domain.model.VerificationAction

class AuraTypeConverters {
    @TypeConverter fun incidentType(value: IncidentType): String = value.name
    @TypeConverter fun incidentType(value: String): IncidentType = IncidentType.valueOf(value)

    @TypeConverter fun severityLevel(value: SeverityLevel): String = value.name
    @TypeConverter fun severityLevel(value: String): SeverityLevel = SeverityLevel.valueOf(value)

    @TypeConverter fun reportStatus(value: ReportStatus): String = value.name
    @TypeConverter fun reportStatus(value: String): ReportStatus = ReportStatus.valueOf(value)

    @TypeConverter fun locationPrecision(value: LocationPrecision): String = value.name
    @TypeConverter fun locationPrecision(value: String): LocationPrecision = LocationPrecision.valueOf(value)

    @TypeConverter fun reportVisibility(value: ReportVisibility): String = value.name
    @TypeConverter fun reportVisibility(value: String): ReportVisibility = ReportVisibility.valueOf(value)

    @TypeConverter fun evidenceType(value: EvidenceType): String = value.name
    @TypeConverter fun evidenceType(value: String): EvidenceType = EvidenceType.valueOf(value)

    @TypeConverter fun evidenceVisibility(value: EvidenceVisibility): String = value.name
    @TypeConverter fun evidenceVisibility(value: String): EvidenceVisibility = EvidenceVisibility.valueOf(value)

    @TypeConverter fun verificationAction(value: VerificationAction): String = value.name
    @TypeConverter fun verificationAction(value: String): VerificationAction = VerificationAction.valueOf(value)

    @TypeConverter fun alertStatus(value: AlertStatus): String = value.name
    @TypeConverter fun alertStatus(value: String): AlertStatus = AlertStatus.valueOf(value)

    @TypeConverter fun safetySessionStatus(value: SafetySessionStatus): String = value.name
    @TypeConverter fun safetySessionStatus(value: String): SafetySessionStatus = SafetySessionStatus.valueOf(value)

    @TypeConverter fun syncOperation(value: SyncOperation): String = value.name
    @TypeConverter fun syncOperation(value: String): SyncOperation = SyncOperation.valueOf(value)

    @TypeConverter fun syncPriority(value: SyncPriority): String = value.name
    @TypeConverter fun syncPriority(value: String): SyncPriority = SyncPriority.valueOf(value)

    @TypeConverter fun syncStatus(value: SyncStatus): String = value.name
    @TypeConverter fun syncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
