package io.aura.android.feature.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.aura.android.core.ui.components.AuraPrimaryButton

private val incidentTypes = listOf(
    "Robo",
    "Intento de robo",
    "Persona sospechosa",
    "Violencia",
    "Acoso",
    "Accidente",
    "Zona peligrosa",
    "Otro",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportIncidentScreen(modifier: Modifier = Modifier) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var severity by remember { mutableFloatStateOf(2f) }
    var description by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(true) }

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
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Gravedad: ${severity.toInt()}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = severity,
                    onValueChange = { severity = it },
                    valueRange = 1f..3f,
                    steps = 1,
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text("Descripción opcional") },
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Modo anónimo", style = MaterialTheme.typography.titleMedium)
                Switch(checked = anonymous, onCheckedChange = { anonymous = it })
            }

            AuraPrimaryButton(
                text = "Guardar como pendiente",
                enabled = selectedType != null,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
