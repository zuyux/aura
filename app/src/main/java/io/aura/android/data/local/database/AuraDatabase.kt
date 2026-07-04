package io.aura.android.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.aura.android.data.local.dao.AlertDao
import io.aura.android.data.local.dao.DeviceIdentityDao
import io.aura.android.data.local.dao.GuardianContactDao
import io.aura.android.data.local.dao.IncidentEvidenceDao
import io.aura.android.data.local.dao.IncidentReportDao
import io.aura.android.data.local.dao.ReportVerificationDao
import io.aura.android.data.local.dao.SafetySessionDao
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.dao.UserProfileDao
import io.aura.android.data.local.entity.AlertEntity
import io.aura.android.data.local.entity.DeviceIdentityEntity
import io.aura.android.data.local.entity.GuardianContactEntity
import io.aura.android.data.local.entity.IncidentEvidenceEntity
import io.aura.android.data.local.entity.IncidentReportEntity
import io.aura.android.data.local.entity.ReportVerificationEntity
import io.aura.android.data.local.entity.SafetySessionEntity
import io.aura.android.data.local.entity.SafetySessionUpdateEntity
import io.aura.android.data.local.entity.SyncQueueEntity
import io.aura.android.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        DeviceIdentityEntity::class,
        IncidentReportEntity::class,
        IncidentEvidenceEntity::class,
        AlertEntity::class,
        ReportVerificationEntity::class,
        GuardianContactEntity::class,
        SafetySessionEntity::class,
        SafetySessionUpdateEntity::class,
        SyncQueueEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(AuraTypeConverters::class)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun deviceIdentityDao(): DeviceIdentityDao
    abstract fun incidentReportDao(): IncidentReportDao
    abstract fun incidentEvidenceDao(): IncidentEvidenceDao
    abstract fun alertDao(): AlertDao
    abstract fun reportVerificationDao(): ReportVerificationDao
    abstract fun guardianContactDao(): GuardianContactDao
    abstract fun safetySessionDao(): SafetySessionDao
    abstract fun syncQueueDao(): SyncQueueDao
}
