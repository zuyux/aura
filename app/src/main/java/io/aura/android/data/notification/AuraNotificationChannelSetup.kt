package io.aura.android.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuraNotificationChannelSetup @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        context.notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    GUARDIAN_SESSION_CHANNEL_ID,
                    "Red Guardian",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Estado de sesiones activas de Red Guardian"
                    setShowBadge(false)
                },
                NotificationChannel(
                    PENDING_SYNC_CHANNEL_ID,
                    "Sincronizacion",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Estado de datos pendientes de sincronizar"
                    setShowBadge(false)
                },
                NotificationChannel(
                    NEARBY_ALERT_CHANNEL_ID,
                    "Alertas cercanas",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Avisos de incidentes reportados cerca de ti"
                },
            ),
        )
    }
}

private val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

const val GUARDIAN_SESSION_CHANNEL_ID = "guardian_session"
const val PENDING_SYNC_CHANNEL_ID = "pending_sync"
const val NEARBY_ALERT_CHANNEL_ID = "nearby_alerts"
