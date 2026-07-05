package io.aura.android.feature.report

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.aura.android.core.ui.components.AuraPrimaryButton
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.usecase.CreateIncidentReportUseCase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

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
    modifier: Modifier = Modifier,
    viewModel: ReportIncidentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLocationRationale by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.loadLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
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
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Tipo", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    incidentTypes.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedType == type.first,
                            onClick = { viewModel.onTypeSelected(type.first) },
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
                            onClick = { viewModel.onSeveritySelected(option.first) },
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
                                onClick = { viewModel.onLocationPrecisionSelected(option.first) },
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
                        onClick = {
                            if (context.hasLocationPermission()) {
                                viewModel.loadLocation()
                            } else {
                                showLocationRationale = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AuraPrimaryButton(
                        text = if (uiState.isLocationConfirmed) {
                            "Ubicacion confirmada"
                        } else {
                            "Confirmar ubicacion"
                        },
                        enabled = uiState.location != null,
                        onClick = viewModel::confirmLocation,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text("Descripción opcional") },
                supportingText = {
                    Text("${uiState.description.length}/${CreateIncidentReportUseCase.MAX_DESCRIPTION_LENGTH}")
                },
            )

            EvidenceAttachmentCard(
                attachments = uiState.evidenceAttachments,
                onAddAttachment = viewModel::onEvidencePlaceholderAdded,
                onRemoveAttachment = viewModel::onEvidencePlaceholderRemoved,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Modo anónimo", style = MaterialTheme.typography.titleMedium)
                Switch(checked = uiState.isAnonymous, onCheckedChange = viewModel::onAnonymousChanged)
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            uiState.savedReportId?.let {
                Text(
                    text = uiState.savedReportMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            OutlinedButton(
                onClick = viewModel::saveDraft,
                enabled = uiState.canSaveDraft,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSavingDraft) "Guardando borrador..." else "Guardar borrador")
            }

            AuraPrimaryButton(
                text = if (uiState.isSubmitting) "Guardando..." else "Guardar como pendiente",
                enabled = uiState.canSubmit,
                onClick = viewModel::submit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvidenceAttachmentCard(
    attachments: List<EvidenceAttachmentDraft>,
    onAddAttachment: (EvidenceType) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(text = "Evidencia", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Privada por defecto. Se adjuntara al reporte cuando el guardado local este listo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EvidenceActionChip(
                    label = "Foto",
                    icon = Icons.Outlined.PhotoCamera,
                    onClick = { onAddAttachment(EvidenceType.PHOTO) },
                )
                EvidenceActionChip(
                    label = "Video",
                    icon = Icons.Outlined.Videocam,
                    onClick = { onAddAttachment(EvidenceType.VIDEO) },
                )
                EvidenceActionChip(
                    label = "Audio",
                    icon = Icons.Outlined.Mic,
                    onClick = { onAddAttachment(EvidenceType.AUDIO) },
                )
            }

            if (attachments.isEmpty()) {
                Text(
                    text = "Sin evidencia preparada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    attachments.forEach { attachment ->
                        EvidenceAttachmentRow(
                            attachment = attachment,
                            onRemove = { onRemoveAttachment(attachment.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null)
        },
    )
}

@Composable
private fun EvidenceAttachmentRow(
    attachment: EvidenceAttachmentDraft,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = attachment.type.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = attachment.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Pendiente de archivo local",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Quitar evidencia",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun EvidenceType.icon(): ImageVector = when (this) {
    EvidenceType.PHOTO -> Icons.Outlined.PhotoCamera
    EvidenceType.VIDEO -> Icons.Outlined.Videocam
    EvidenceType.AUDIO -> Icons.Outlined.Mic
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
