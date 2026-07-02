package io.aura.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.aura.android.data.location.AndroidLocationProvider
import io.aura.android.data.repository.OfflineFirstIncidentReportRepository
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.repository.IncidentReportRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindIncidentReportRepository(
        repository: OfflineFirstIncidentReportRepository,
    ): IncidentReportRepository

    @Binds
    abstract fun bindLocationProvider(
        provider: AndroidLocationProvider,
    ): LocationProvider
}
