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
        ).addMigrations(MIGRATION_1_2).build()

    @Provides fun provideUserProfileDao(database: AuraDatabase): UserProfileDao = database.userProfileDao()
    @Provides fun provideDeviceIdentityDao(database: AuraDatabase): DeviceIdentityDao = database.deviceIdentityDao()
    @Provides fun provideIncidentReportDao(database: AuraDatabase): IncidentReportDao = database.incidentReportDao()
    @Provides fun provideIncidentEvidenceDao(database: AuraDatabase): IncidentEvidenceDao = database.incidentEvidenceDao()
    @Provides fun provideAlertDao(database: AuraDatabase): AlertDao = database.alertDao()
    @Provides fun provideReportVerificationDao(database: AuraDatabase): ReportVerificationDao = database.reportVerificationDao()
    @Provides fun provideGuardianContactDao(database: AuraDatabase): GuardianContactDao = database.guardianContactDao()
    @Provides fun provideSafetySessionDao(database: AuraDatabase): SafetySessionDao = database.safetySessionDao()
    @Provides fun provideSyncQueueDao(database: AuraDatabase): SyncQueueDao = database.syncQueueDao()
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE guardian_contacts ADD COLUMN photoUri TEXT")
    }
}
