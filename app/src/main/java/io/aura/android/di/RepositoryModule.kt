package io.aura.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.aura.android.data.location.AndroidLocationProvider
import io.aura.android.data.repository.OfflineFirstAlertRepository
import io.aura.android.data.repository.OfflineFirstGuardianRepository
import io.aura.android.data.repository.OfflineFirstIncidentReportRepository
import io.aura.android.data.repository.OfflineFirstUserProfileRepository
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.repository.AlertRepository
import io.aura.android.domain.repository.GuardianRepository
import io.aura.android.domain.repository.IncidentReportRepository
import io.aura.android.domain.repository.UserProfileRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAlertRepository(
        repository: OfflineFirstAlertRepository,
    ): AlertRepository

    @Binds
    abstract fun bindIncidentReportRepository(
        repository: OfflineFirstIncidentReportRepository,
    ): IncidentReportRepository

    @Binds
    abstract fun bindGuardianRepository(
        repository: OfflineFirstGuardianRepository,
    ): GuardianRepository

    @Binds
    abstract fun bindUserProfileRepository(
        repository: OfflineFirstUserProfileRepository,
    ): UserProfileRepository

    @Binds
    abstract fun bindLocationProvider(
        provider: AndroidLocationProvider,
    ): LocationProvider
}
