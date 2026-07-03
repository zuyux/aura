package io.aura.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.aura.android.core.ui.components.AuraEmptyState
import io.aura.android.core.ui.components.AuraSectionHeader
import io.aura.android.core.ui.theme.AuraDarkBlue
import io.aura.android.core.ui.theme.AuraRed

@Composable
fun HomeScreen(
    onReportClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onGuardianClick: () -> Unit,
    modifier: Modifier = Modifier,
    username: String = "Valeria",
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "AURA",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Box {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp)
                            .size(9.dp)
                            .background(AuraRed, CircleShape),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Hola, $username",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Estamos aqui para cuidarnos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            EmergencySosCard(
                onClick = onGuardianClick,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionRow(
                    title = "Reportar incidente",
                    body = "Informa lo que esta sucediendo",
                    icon = Icons.Outlined.EditNote,
                    onClick = onReportClick,
                )
                HomeActionRow(
                    title = "Ver alertas cercanas",
                    body = "Revisa incidentes y alertas en tu area",
                    icon = Icons.Outlined.Map,
                    onClick = onAlertsClick,
                )
                HomeActionRow(
                    title = "Red Guardián",
                    body = "Comparte tu ubicacion y viaja mas segura",
                    icon = Icons.Outlined.Groups,
                    onClick = onGuardianClick,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EmergencySosCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF1744), Color(0xFFD50032)),
                ),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Emergencia",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Toca aqui para pedir ayuda",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HomeActionRow(
    title: String,
    body: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AuraDarkBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
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
fun ProfilePlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Perfil",
        body = "Datos personales, preferencias de privacidad y contactos de confianza.",
        modifier = modifier,
    )
}

@Composable
private fun PlaceholderScreen(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AuraSectionHeader(
                title = title,
                subtitle = body,
            )
            AuraEmptyState(
                title = "Todavia no hay datos",
                body = "Esta vista queda preparada para conectarse al flujo del MVP.",
            )
        }
    }
}
