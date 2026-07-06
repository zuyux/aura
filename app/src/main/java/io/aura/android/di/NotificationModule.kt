package io.aura.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.aura.android.data.notification.fcm.DataStoreFcmTokenStore
import io.aura.android.data.notification.fcm.FcmTokenStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindFcmTokenStore(store: DataStoreFcmTokenStore): FcmTokenStore
}
