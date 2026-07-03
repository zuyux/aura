package io.aura.android.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.aura.android.core.ui.components.AuraEmptyState
import io.aura.android.core.ui.components.AuraLoadingState
import io.aura.android.core.ui.components.AuraOfflineBanner
import io.aura.android.core.ui.components.AuraSectionHeader
import io.aura.android.core.ui.components.StatusBadge
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AlertStatus
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.SeverityLevel

@Composable
fun AlertsListScreen(
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraSectionHeader(
                title = "Alertas cercanas",
                subtitle = "Incidentes recientes guardados localmente para revisar rapido.",
            )
            AuraOfflineBanner(
                title = "Modo local-first",
                message = "Las alertas se muestran desde el dispositivo y podran sincronizarse despues.",
            )

            when {
                uiState.isLoading -> AuraLoadingState(message = "Cargando alertas...")
                uiState.alerts.isEmpty() -> AuraEmptyState(
                    title = "No hay alertas cercanas",
                    body = "Cuando se registren incidentes en tu zona apareceran aqui.",
                    icon = Icons.Outlined.Shield,
                )
                else -> uiState.alerts.forEach { alert ->
                    AlertListItem(alert = alert)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlertListItem(alert: Alert, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = alert.type.icon(),
                    contentDescription = null,
                    tint = alert.severity.color(),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = alert.type.label(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = alert.summary.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusBadge(text = alert.status.label())
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(alert.distanceLabel()) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(alert.relativeTimeLabel()) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(alert.severity.label()) },
                )
            }
        }
    }
}

@Composable
private fun SeverityLevel.color() = when (this) {
    SeverityLevel.LOW -> MaterialTheme.colorScheme.tertiary
    SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.primary
    SeverityLevel.HIGH -> MaterialTheme.colorScheme.error
}

private fun IncidentType.label(): String = when (this) {
    IncidentType.THEFT -> "Robo"
    IncidentType.ATTEMPTED_THEFT -> "Intento de robo"
    IncidentType.SUSPICIOUS_PERSON -> "Persona sospechosa"
    IncidentType.VIOLENCE -> "Violencia"
    IncidentType.HARASSMENT -> "Acoso"
    IncidentType.ACCIDENT -> "Accidente"
    IncidentType.DANGEROUS_AREA -> "Zona peligrosa"
    IncidentType.OTHER -> "Otro incidente"
}

private fun IncidentType.icon(): ImageVector = when (this) {
    IncidentType.THEFT,
    IncidentType.ATTEMPTED_THEFT,
    IncidentType.SUSPICIOUS_PERSON,
    IncidentType.VIOLENCE,
    IncidentType.HARASSMENT -> Icons.Outlined.Report
    IncidentType.ACCIDENT -> Icons.Outlined.ErrorOutline
    IncidentType.DANGEROUS_AREA -> Icons.Outlined.Flag
    IncidentType.OTHER -> Icons.Outlined.Shield
}

private fun AlertStatus.label(): String = when (this) {
    AlertStatus.UNVERIFIED -> "No verificado"
    AlertStatus.COMMUNITY_CONFIRMED -> "Comunidad"
    AlertStatus.AUTHORITY_CONFIRMED -> "Autoridad"
    AlertStatus.RESOLVED -> "Resuelto"
    AlertStatus.DISMISSED -> "Descartado"
}

private fun SeverityLevel.label(): String = when (this) {
    SeverityLevel.LOW -> "Baja"
    SeverityLevel.MEDIUM -> "Media"
    SeverityLevel.HIGH -> "Alta"
}

private fun Alert.distanceLabel(): String = when (val distance = distanceMeters) {
    null -> "Distancia no disponible"
    in 0..999 -> "${distance} m"
    else -> "${distance / 1000.0} km"
}

private fun Alert.relativeTimeLabel(): String {
    val elapsedMillis = (System.currentTimeMillis() - reportedAtMillis).coerceAtLeast(0L)
    val elapsedMinutes = elapsedMillis / 60_000L
    return when {
        elapsedMinutes < 1 -> "Ahora"
        elapsedMinutes < 60 -> "Hace ${elapsedMinutes} min"
        elapsedMinutes < 24 * 60 -> "Hace ${elapsedMinutes / 60} h"
        else -> "Hace ${elapsedMinutes / (24 * 60)} d"
    }
}
