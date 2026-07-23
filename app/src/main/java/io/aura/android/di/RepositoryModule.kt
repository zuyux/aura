package io.aura.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.aura.android.data.location.AndroidLastKnownLocationStore
import io.aura.android.data.location.AndroidLocationPermissionManager
import io.aura.android.data.location.AndroidLocationProvider
import io.aura.android.data.repository.OfflineFirstAlertRepository
import io.aura.android.data.repository.DataStoreProfileSettingsRepository
import io.aura.android.data.repository.OfflineFirstDeviceIdentityRepository
import io.aura.android.data.repository.OfflineFirstIncidentEvidenceRepository
import io.aura.android.data.repository.OfflineFirstGuardianRepository
import io.aura.android.data.repository.OfflineFirstIncidentReportRepository
import io.aura.android.data.remote.IncidentRemoteDataSource
import io.aura.android.data.remote.SupabaseIncidentRemoteDataSource
import io.aura.android.data.repository.OfflineFirstUserProfileRepository
import io.aura.android.data.security.AndroidKeystoreLocalKeyStore
import io.aura.android.domain.location.LastKnownLocationStore
import io.aura.android.domain.location.LocationPermissionManager
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.repository.AlertRepository
import io.aura.android.domain.repository.DeviceIdentityRepository
import io.aura.android.domain.repository.GuardianRepository
import io.aura.android.domain.repository.IncidentEvidenceRepository
import io.aura.android.domain.repository.IncidentReportRepository
import io.aura.android.domain.repository.ProfileSettingsRepository
import io.aura.android.domain.repository.UserProfileRepository
import io.aura.android.domain.security.LocalKeyStore

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindIncidentRemoteDataSource(
        source: SupabaseIncidentRemoteDataSource,
    ): IncidentRemoteDataSource

    @Binds
    abstract fun bindAlertRepository(
        repository: OfflineFirstAlertRepository,
    ): AlertRepository

    @Binds
    abstract fun bindIncidentReportRepository(
        repository: OfflineFirstIncidentReportRepository,
    ): IncidentReportRepository

    @Binds
    abstract fun bindIncidentEvidenceRepository(
        repository: OfflineFirstIncidentEvidenceRepository,
    ): IncidentEvidenceRepository

    @Binds
    abstract fun bindGuardianRepository(
        repository: OfflineFirstGuardianRepository,
    ): GuardianRepository

    @Binds
    abstract fun bindUserProfileRepository(
        repository: OfflineFirstUserProfileRepository,
    ): UserProfileRepository

    @Binds
    abstract fun bindDeviceIdentityRepository(
        repository: OfflineFirstDeviceIdentityRepository,
    ): DeviceIdentityRepository

    @Binds
    abstract fun bindProfileSettingsRepository(
        repository: DataStoreProfileSettingsRepository,
    ): ProfileSettingsRepository

    @Binds
    abstract fun bindLocalKeyStore(
        keyStore: AndroidKeystoreLocalKeyStore,
    ): LocalKeyStore

    @Binds
    abstract fun bindLocationProvider(
        provider: AndroidLocationProvider,
    ): LocationProvider

    @Binds
    abstract fun bindLocationPermissionManager(
        manager: AndroidLocationPermissionManager,
    ): LocationPermissionManager

    @Binds
    abstract fun bindLastKnownLocationStore(
        store: AndroidLastKnownLocationStore,
    ): LastKnownLocationStore
}
