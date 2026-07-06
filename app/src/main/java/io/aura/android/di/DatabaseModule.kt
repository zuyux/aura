package io.aura.android.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.aura.android.data.local.dao.AlertDao
import io.aura.android.data.local.dao.DeviceIdentityDao
import io.aura.android.data.local.dao.GuardianContactDao
import io.aura.android.data.local.dao.GuardianNotificationDao
import io.aura.android.data.local.dao.IncidentEvidenceDao
import io.aura.android.data.local.dao.IncidentReportDao
import io.aura.android.data.local.dao.ReportVerificationDao
import io.aura.android.data.local.dao.SafetySessionDao
import io.aura.android.data.local.dao.SyncQueueDao
import io.aura.android.data.local.dao.UserProfileDao
import io.aura.android.data.local.database.AuraDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAuraDatabase(@ApplicationContext context: Context): AuraDatabase =
        Room.databaseBuilder(
            context = context,
            klass = AuraDatabase::class.java,
            name = "aura.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

    @Provides fun provideUserProfileDao(database: AuraDatabase): UserProfileDao = database.userProfileDao()
    @Provides fun provideDeviceIdentityDao(database: AuraDatabase): DeviceIdentityDao = database.deviceIdentityDao()
    @Provides fun provideIncidentReportDao(database: AuraDatabase): IncidentReportDao = database.incidentReportDao()
    @Provides fun provideIncidentEvidenceDao(database: AuraDatabase): IncidentEvidenceDao = database.incidentEvidenceDao()
    @Provides fun provideAlertDao(database: AuraDatabase): AlertDao = database.alertDao()
    @Provides fun provideReportVerificationDao(database: AuraDatabase): ReportVerificationDao = database.reportVerificationDao()
    @Provides fun provideGuardianContactDao(database: AuraDatabase): GuardianContactDao = database.guardianContactDao()
    @Provides fun provideGuardianNotificationDao(database: AuraDatabase): GuardianNotificationDao = database.guardianNotificationDao()
    @Provides fun provideSafetySessionDao(database: AuraDatabase): SafetySessionDao = database.safetySessionDao()
    @Provides fun provideSyncQueueDao(database: AuraDatabase): SyncQueueDao = database.syncQueueDao()
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE guardian_contacts ADD COLUMN photoUri TEXT")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE incident_evidence ADD COLUMN sha256Hash TEXT NOT NULL DEFAULT ''")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS guardian_notifications (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                status TEXT NOT NULL,
                senderName TEXT NOT NULL,
                senderPhoneNumber TEXT,
                senderPhotoUri TEXT,
                message TEXT NOT NULL,
                sessionId TEXT,
                latitude REAL,
                longitude REAL,
                createdAtMillis INTEGER NOT NULL,
                readAtMillis INTEGER,
                respondedAtMillis INTEGER
            )
            """.trimIndent(),
        )
    }
}
