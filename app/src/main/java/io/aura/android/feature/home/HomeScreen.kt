package io.aura.android.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.aura.android.R
import io.aura.android.core.ui.components.AuraAlertCard
import io.aura.android.core.ui.components.AuraCategoryCard
import io.aura.android.core.ui.components.AuraEmergencyButton

@Composable
fun HomeScreen(
    onReportClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onGuardianClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_aura_logo),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                )
                Column {
                    Text(text = "AURA", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Seguridad ciudadana privada y comunitaria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AuraEmergencyButton(
                text = "Activar SOS",
                onClick = onGuardianClick,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AuraCategoryCard(
                    title = "Reportar incidente",
                    icon = Icons.Outlined.AddAlert,
                    onClick = onReportClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                AuraCategoryCard(
                    title = "Alertas cercanas",
                    icon = Icons.Outlined.Map,
                    onClick = onAlertsClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                AuraCategoryCard(
                    title = "Red Guardián",
                    icon = Icons.Outlined.Shield,
                    onClick = onGuardianClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AuraAlertCard(
                title = "Sin conexión",
                metadata = "Tus reportes se guardarán en el dispositivo y se sincronizarán después.",
                status = "Local-first",
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AlertsPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Alertas cercanas",
        body = "Mapa y lista de incidentes próximos para el MVP 0.1.",
        modifier = modifier,
    )
}

@Composable
fun GuardianPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Red Guardián",
        body = "Sesiones privadas de seguridad, contactos de confianza y SOS.",
        modifier = modifier,
    )
}

@Composable
private fun PlaceholderScreen(title: String, body: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
