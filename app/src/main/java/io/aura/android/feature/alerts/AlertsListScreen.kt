package io.aura.android.feature.alerts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import io.aura.android.core.ui.components.AuraEmptyState
import io.aura.android.core.ui.components.AuraLoadingState
import io.aura.android.core.ui.components.AuraOfflineBanner
import io.aura.android.core.ui.components.AuraSectionHeader
import io.aura.android.core.ui.components.StatusBadge
import io.aura.android.domain.model.Alert

@Composable
fun AlertsListScreen(
    onAlertClick: (String) -> Unit,
    initialDisplayMode: AlertsDisplayMode = AlertsDisplayMode.MAP,
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var displayMode by remember(initialDisplayMode) { mutableStateOf(initialDisplayMode) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { granted -> granted }) {
            viewModel.refreshNearbyAlerts()
        }
    }

    LaunchedEffect(Unit) {
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (displayMode == AlertsDisplayMode.MAP) {
            Box(modifier = Modifier.fillMaxSize()) {
                AlertsMapScreen(
                    alerts = uiState.alerts,
                    currentLocation = uiState.currentLocation,
                    onAlertClick = onAlertClick,
                )
                AlertsModeSelector(
                    selectedMode = displayMode,
                    onModeSelected = { displayMode = it },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
                AlertFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::selectFilter,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                )
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraSectionHeader(
                title = "Alertas cercanas",
                subtitle = "Incidentes recientes guardados localmente para revisar rápido.",
            )
            AuraOfflineBanner(
                title = "Modo local-first",
                message = "Las alertas se muestran desde el dispositivo y podrán sincronizarse después.",
            )

            AlertsModeSelector(
                selectedMode = displayMode,
                onModeSelected = { displayMode = it },
            )
            AlertFilterRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = viewModel::selectFilter,
            )

            when {
                uiState.isLoading -> AuraLoadingState(message = "Cargando alertas...")
                uiState.alerts.isEmpty() -> AuraEmptyState(
                    title = if (uiState.totalAlerts == 0) {
                        "No hay alertas cercanas"
                    } else {
                        "No hay alertas de ${uiState.selectedFilter.label.lowercase()}"
                    },
                    body = if (uiState.totalAlerts == 0) {
                        "Cuando se registren incidentes en tu zona aparecerán aquí."
                    } else {
                        "Prueba con otro filtro para revisar el resto de alertas guardadas."
                    },
                    icon = Icons.Outlined.Shield,
                )
                else -> {
                    uiState.alerts.forEach { alert ->
                        AlertListItem(
                            alert = alert,
                            onClick = { onAlertClick(alert.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlertsModeSelector(
    selectedMode: AlertsDisplayMode,
    onModeSelected: (AlertsDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AlertsDisplayMode.entries.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text(mode.label) },
                )
            }
        }
    }
}

@Composable
private fun AlertFilterRow(
    selectedFilter: AlertFilter,
    onFilterSelected: (AlertFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AlertFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

enum class AlertsDisplayMode(val label: String) {
    MAP("Mapa"),
    LIST("Lista"),
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
