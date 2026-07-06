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
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.SeverityLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showNearbyAlert(alert: Alert) {
        if (!context.canPostNotifications()) return

        val title = "Alerta cercana: ${alert.type.label()}"
        val distanceText = alert.distanceMeters?.let { distance ->
            if (distance < 1_000) "${distance} m" else "%.1f km".format(distance / 1_000.0)
        }
        val severityText = "Severidad ${alert.severity.label().lowercase()}"
        val body = listOfNotNull(distanceText, severityText, alert.summary)
            .joinToString(separator = ". ")
        val notification = NotificationCompat.Builder(context, NEARBY_ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(context.openAppPendingIntent())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NEARBY_ALERT_NOTIFICATION_ID, notification)
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

private fun IncidentType.label(): String = when (this) {
    IncidentType.THEFT -> "Robo"
    IncidentType.ATTEMPTED_THEFT -> "Intento de robo"
    IncidentType.SUSPICIOUS_PERSON -> "Persona sospechosa"
    IncidentType.VIOLENCE -> "Violencia"
    IncidentType.HARASSMENT -> "Acoso"
    IncidentType.ACCIDENT -> "Accidente"
    IncidentType.DANGEROUS_AREA -> "Zona peligrosa"
    IncidentType.OTHER -> "Incidente"
}

private fun SeverityLevel.label(): String = when (this) {
    SeverityLevel.LOW -> "Baja"
    SeverityLevel.MEDIUM -> "Media"
    SeverityLevel.HIGH -> "Alta"
}

private const val NEARBY_ALERT_NOTIFICATION_ID = 2003
