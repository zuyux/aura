package io.aura.android.data.notification.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.MainActivity
import io.aura.android.R
import io.aura.android.data.notification.NEARBY_ALERT_CHANNEL_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmMessageHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStore: FcmTokenStore,
) {
    suspend fun handleNewToken(token: String) {
        tokenStore.saveToken(token)
    }

    fun handleMessage(message: RemoteMessage) {
        val notification = message.notification ?: return
        val title = notification.title ?: message.data["title"] ?: "AURA"
        val body = notification.body ?: message.data["body"] ?: return
        if (!context.canPostNotifications()) return

        val localNotification = NotificationCompat.Builder(context, NEARBY_ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(context.openAppPendingIntent())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(FCM_NOTIFICATION_ID, localNotification)
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

private const val FCM_NOTIFICATION_ID = 2004
