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

@Composable
fun AlertsListScreen(
    onAlertClick: (String) -> Unit,
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
                    AlertListItem(
                        alert = alert,
                        onClick = { onAlertClick(alert.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlertListItem(
    alert: Alert,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
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
                    tint = alert.severity.alertColor(),
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
