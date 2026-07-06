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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingSyncNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showPendingSync(pendingCount: Int) {
        if (pendingCount <= 0 || !context.canPostNotifications()) return

        val text = if (pendingCount == 1) {
            "Hay 1 elemento esperando conexion para sincronizarse."
        } else {
            "Hay $pendingCount elementos esperando conexion para sincronizarse."
        }
        val notification = NotificationCompat.Builder(context, PENDING_SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Sincronizacion pendiente")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(context.openAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setNumber(pendingCount)
            .build()

        NotificationManagerCompat.from(context).notify(PENDING_SYNC_NOTIFICATION_ID, notification)
    }

    fun cancelPendingSync() {
        NotificationManagerCompat.from(context).cancel(PENDING_SYNC_NOTIFICATION_ID)
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

private const val PENDING_SYNC_NOTIFICATION_ID = 2002
