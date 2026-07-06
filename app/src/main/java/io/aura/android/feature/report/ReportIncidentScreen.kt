package io.aura.android.feature.report

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.aura.android.core.ui.components.AuraPrimaryButton
import io.aura.android.data.location.AndroidLocationPermissionManager
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.usecase.CreateIncidentReportUseCase

private val incidentTypes = listOf(
    IncidentType.THEFT to "Robo",
    IncidentType.ATTEMPTED_THEFT to "Intento de robo",
    IncidentType.SUSPICIOUS_PERSON to "Persona sospechosa",
    IncidentType.VIOLENCE to "Violencia",
    IncidentType.HARASSMENT to "Acoso",
    IncidentType.ACCIDENT to "Accidente",
    IncidentType.DANGEROUS_AREA to "Zona peligrosa",
    IncidentType.OTHER to "Otro",
)

private val severityOptions = listOf(
    SeverityLevel.LOW to "Baja",
    SeverityLevel.MEDIUM to "Media",
    SeverityLevel.HIGH to "Alta",
)

private val locationPrecisionOptions = listOf(
    LocationPrecision.APPROXIMATE to "Zona",
    LocationPrecision.DISTRICT_ONLY to "Distrito",
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentScreen(
    onAddEvidenceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportIncidentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationPermissionManager = remember(context) {
        AndroidLocationPermissionManager(context.applicationContext)
    }
    var showLocationRationale by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.loadLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (locationPermissionManager.hasLocationPermission()) {
            viewModel.loadLocation()
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = { Text("Permitir ubicacion") },
            text = {
                Text(
                    "AURA usa tu ubicacion solo para este reporte y guarda una version aproximada o por distrito.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationRationale = false
                        locationPermissionLauncher.launch(locationPermissionManager.locationPermissions)
                    },
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("Ahora no")
                }
            },
        )
    }

    ReportIncidentContent(
        uiState = uiState,
        onTypeSelected = viewModel::onTypeSelected,
        onSeveritySelected = viewModel::onSeveritySelected,
        onLocationPrecisionSelected = viewModel::onLocationPrecisionSelected,
        onUseGpsClick = {
            if (locationPermissionManager.hasLocationPermission()) {
                viewModel.loadLocation()
            } else {
                showLocationRationale = true
            }
        },
        onConfirmLocation = viewModel::confirmLocation,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onAnonymousChanged = viewModel::onAnonymousChanged,
        onSaveDraft = viewModel::saveDraft,
        onSubmit = viewModel::submit,
        onAddEvidenceClick = onAddEvidenceClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ReportIncidentContent(
    uiState: ReportIncidentUiState,
    onTypeSelected: (IncidentType) -> Unit,
    onSeveritySelected: (SeverityLevel) -> Unit,
    onLocationPrecisionSelected: (LocationPrecision) -> Unit,
    onUseGpsClick: () -> Unit,
    onConfirmLocation: () -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAnonymousChanged: (Boolean) -> Unit,
    onSaveDraft: () -> Unit,
    onSubmit: () -> Unit,
    onAddEvidenceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(text = "Reportar incidente", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "El reporte empieza como no verificado y se guarda localmente antes de sincronizar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.pendingSyncCount > 0) {
                Text(
                    text = "${uiState.pendingSyncCount} pendiente(s) de sincronizacion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Tipo", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    incidentTypes.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedType == type.first,
                            onClick = { onTypeSelected(type.first) },
                            label = { Text(type.second) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Gravedad", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    severityOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = uiState.severity == option.first,
                            onClick = { onSeveritySelected(option.first) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = severityOptions.size,
                            ),
                        ) {
                            Text(option.second)
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Ubicación", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = uiState.locationStatus,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        locationPrecisionOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = uiState.locationPrecision == option.first,
                                onClick = { onLocationPrecisionSelected(option.first) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = locationPrecisionOptions.size,
                                ),
                            ) {
                                Text(option.second)
                            }
                        }
                    }
                    AuraPrimaryButton(
                        text = if (uiState.isLoadingLocation) {
                            "Buscando GPS..."
                        } else {
                            "Usar GPS actual"
                        },
                        enabled = !uiState.isLoadingLocation,
                        onClick = onUseGpsClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AuraPrimaryButton(
                        text = if (uiState.isLocationConfirmed) {
                            "Ubicacion confirmada"
                        } else {
                            "Confirmar ubicacion"
                        },
                        enabled = uiState.location != null,
                        onClick = onConfirmLocation,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text("Descripción opcional") },
                supportingText = {
                    Text("${uiState.description.length}/${CreateIncidentReportUseCase.MAX_DESCRIPTION_LENGTH}")
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Modo anónimo", style = MaterialTheme.typography.titleMedium)
                Switch(checked = uiState.isAnonymous, onCheckedChange = onAnonymousChanged)
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            uiState.savedReportId?.let { reportId ->
                Text(
                    text = uiState.savedReportMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (uiState.canAddEvidence) {
                    OutlinedButton(
                        onClick = { onAddEvidenceClick(reportId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Agregar evidencia privada")
                    }
                }
            }

            OutlinedButton(
                onClick = onSaveDraft,
                enabled = uiState.canSaveDraft,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSavingDraft) "Guardando borrador..." else "Guardar borrador")
            }

            AuraPrimaryButton(
                text = if (uiState.isSubmitting) "Guardando..." else "Guardar como pendiente",
                enabled = uiState.canSubmit,
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
