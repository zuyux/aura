package io.aura.android.feature.alerts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel

@Composable
internal fun SeverityLevel.alertColor(): Color = when (this) {
    SeverityLevel.LOW -> MaterialTheme.colorScheme.tertiary
    SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.primary
    SeverityLevel.HIGH -> MaterialTheme.colorScheme.error
}

internal fun IncidentType.label(): String = when (this) {
    IncidentType.THEFT -> "Robo"
    IncidentType.ATTEMPTED_THEFT -> "Intento de robo"
    IncidentType.SUSPICIOUS_PERSON -> "Persona sospechosa"
    IncidentType.VIOLENCE -> "Violencia"
    IncidentType.HARASSMENT -> "Acoso"
    IncidentType.ACCIDENT -> "Accidente"
    IncidentType.DANGEROUS_AREA -> "Zona peligrosa"
    IncidentType.OTHER -> "Otro incidente"
}

internal fun IncidentType.icon(): ImageVector = when (this) {
    IncidentType.THEFT,
    IncidentType.ATTEMPTED_THEFT,
    IncidentType.SUSPICIOUS_PERSON,
    IncidentType.VIOLENCE,
    IncidentType.HARASSMENT -> Icons.Outlined.Report
    IncidentType.ACCIDENT -> Icons.Outlined.ErrorOutline
    IncidentType.DANGEROUS_AREA -> Icons.Outlined.Flag
    IncidentType.OTHER -> Icons.Outlined.Shield
}

internal fun AlertStatus.label(): String = when (this) {
    AlertStatus.UNVERIFIED -> "No verificado"
    AlertStatus.COMMUNITY_CONFIRMED -> "Comunidad"
    AlertStatus.AUTHORITY_CONFIRMED -> "Autoridad"
    AlertStatus.RESOLVED -> "Resuelto"
    AlertStatus.DISMISSED -> "Descartado"
}

internal fun SeverityLevel.label(): String = when (this) {
    SeverityLevel.LOW -> "Baja"
    SeverityLevel.MEDIUM -> "Media"
    SeverityLevel.HIGH -> "Alta"
}

internal fun LocationPrecision.label(): String = when (this) {
    LocationPrecision.EXACT -> "Exacta"
    LocationPrecision.APPROXIMATE -> "Aproximada"
    LocationPrecision.DISTRICT_ONLY -> "Distrito"
}

internal fun Alert.distanceLabel(): String = when (val distance = distanceMeters) {
    null -> "Distancia no disponible"
    in 0..999 -> "${distance} m"
    else -> "%.1f km".format(distance / 1000.0)
}

internal fun Alert.relativeTimeLabel(): String {
    val elapsedMillis = (System.currentTimeMillis() - reportedAtMillis).coerceAtLeast(0L)
    val elapsedMinutes = elapsedMillis / 60_000L
    return when {
        elapsedMinutes < 1 -> "Ahora"
        elapsedMinutes < 60 -> "Hace ${elapsedMinutes} min"
        elapsedMinutes < 24 * 60 -> "Hace ${elapsedMinutes / 60} h"
        else -> "Hace ${elapsedMinutes / (24 * 60)} d"
    }
}
