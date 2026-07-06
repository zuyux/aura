package io.aura.android.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.MainActivity
import io.aura.android.R
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuardianSessionNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showActiveSession(session: SafetySession) {
        if (!context.canPostNotifications()) return

        val title = when (session.status) {
            SafetySessionStatus.SOS_TRIGGERED -> "SOS activo"
            else -> "Red Guardian activa"
        }
        val text = when (session.status) {
            SafetySessionStatus.SOS_TRIGGERED -> "Tus contactos de confianza pueden recibir tu estado."
            else -> "Tu sesion de seguridad sigue activa."
        }
        val notification = NotificationCompat.Builder(context, GUARDIAN_SESSION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(context.openAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(GUARDIAN_SESSION_NOTIFICATION_ID, notification)
    }

    fun cancelActiveSession() {
        NotificationManagerCompat.from(context).cancel(GUARDIAN_SESSION_NOTIFICATION_ID)
    }
}

private fun Context.openAppPendingIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun Context.canPostNotifications(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private const val GUARDIAN_SESSION_NOTIFICATION_ID = 2001
