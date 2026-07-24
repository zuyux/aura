package io.aura.android.feature.evidence

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.IncidentEvidence

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEvidenceScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEvidenceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val evidence by viewModel.evidence.collectAsStateWithLifecycle()
    var pendingType by remember { mutableStateOf<EvidenceType?>(null) }
    var evidencePendingDelete by remember { mutableStateOf<IncidentEvidence?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvidencePicked(pendingType ?: EvidenceType.PHOTO, uri?.toString())
        pendingType = null
    }

    evidencePendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { evidencePendingDelete = null },
            title = { Text("Eliminar evidencia local") },
            text = { Text("Se quitara este adjunto del dispositivo y de la cola de subida.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        evidencePendingDelete = null
                        viewModel.onDeleteEvidenceClick(item.id)
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { evidencePendingDelete = null }) {
                    Text("Cancelar")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar evidencia") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Evidencia privada", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Se guarda localmente, sin EXIF en fotos, con hash SHA-256 y cola de subida.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            EvidencePickerChip(
                                label = "Foto",
                                icon = Icons.Outlined.Image,
                                enabled = !uiState.isSaving,
                                onClick = {
                                    pendingType = EvidenceType.PHOTO
                                    picker.launch("image/*")
                                },
                            )
                            EvidencePickerChip(
                                label = "Video",
                                icon = Icons.Outlined.Videocam,
                                enabled = !uiState.isSaving,
                                onClick = {
                                    pendingType = EvidenceType.VIDEO
                                    picker.launch("video/*")
                                },
                            )
                            EvidencePickerChip(
                                label = "Audio",
                                icon = Icons.Outlined.AudioFile,
                                enabled = !uiState.isSaving,
                                onClick = {
                                    pendingType = EvidenceType.AUDIO
                                    picker.launch("audio/*")
                                },
                            )
                        }

                        if (uiState.isSaving) {
                            Text(
                                text = "Guardando evidencia...",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                uiState.savedMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Adjuntos del reporte", style = MaterialTheme.typography.titleMedium)
                    if (evidence.isEmpty()) {
                        Text(
                            "Aún no hay evidencia adjunta.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        evidence.forEach { item ->
                            EvidenceRow(
                                evidence = item,
                                isDeleting = uiState.deletingEvidenceId == item.id,
                                onDeleteClick = { evidencePendingDelete = item },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidencePickerChip(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
    )
}

@Composable
private fun EvidenceRow(
    evidence: IncidentEvidence,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(evidence.type.displayLabel()) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isDeleting) "Eliminando evidencia local..." else "Privada - subida pendiente",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = evidence.sha256Hash,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        leadingContent = {
            Icon(imageVector = evidence.type.icon(), contentDescription = null)
        },
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar evidencia local",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}

private fun EvidenceType.displayLabel(): String = when (this) {
    EvidenceType.PHOTO -> "Foto"
    EvidenceType.VIDEO -> "Video"
    EvidenceType.AUDIO -> "Audio"
}

private fun EvidenceType.icon(): ImageVector = when (this) {
    EvidenceType.PHOTO -> Icons.Outlined.Image
    EvidenceType.VIDEO -> Icons.Outlined.Videocam
    EvidenceType.AUDIO -> Icons.Outlined.AudioFile
}
