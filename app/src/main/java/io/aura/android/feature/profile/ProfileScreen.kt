package io.aura.android.feature.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.aura.android.core.ui.components.AuraSectionHeader
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.model.ThemeMode

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    anonymousModeDefault: Boolean,
    onAnonymousModeDefaultChanged: (Boolean) -> Unit,
    offlineModeEnabled: Boolean,
    onOfflineModeChanged: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    guardianInviteNotificationsEnabled: Boolean,
    onGuardianInviteNotificationsChanged: (Boolean) -> Unit,
    sosAlertNotificationsEnabled: Boolean,
    onSosAlertNotificationsChanged: (Boolean) -> Unit,
    privacyDisclaimerAccepted: Boolean,
    appVersion: String,
    themeMode: ThemeMode = ThemeMode.DARK,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AuraSectionHeader(
                title = "Perfil",
                subtitle = "Datos verificados localmente para personalizar AURA.",
            )
            ProfileInfoRow(
                label = "Nombre",
                value = profile?.displayName.orEmpty().ifBlank { "Sin nombre" },
                icon = Icons.Outlined.Person,
            )
            ProfileInfoRow(
                label = "Teléfono",
                value = profile?.phoneNumber.orEmpty().ifBlank { "Sin teléfono" },
                icon = Icons.Outlined.Phone,
            )
            AuraSectionHeader(
                title = "Configuracion",
                subtitle = "Preferencias locales para los reportes nuevos.",
            )
            ThemeModeSelector(
                selectedMode = themeMode,
                onModeSelected = onThemeModeChanged,
            )
            ProfileSwitchRow(
                label = "Modo anónimo por defecto",
                value = "Los nuevos reportes ocultaran tu identidad inicialmente.",
                icon = Icons.Outlined.VisibilityOff,
                checked = anonymousModeDefault,
                onCheckedChange = onAnonymousModeDefaultChanged,
            )
            ProfileSwitchRow(
                label = "Modo offline",
                value = "Pausa consultas remotas y conserva cambios en el dispositivo.",
                icon = Icons.Outlined.CloudOff,
                checked = offlineModeEnabled,
                onCheckedChange = onOfflineModeChanged,
            )
            AuraSectionHeader(
                title = "Notificaciones",
                subtitle = "Elige que avisos aparecen en Inicio.",
            )
            ProfileSwitchRow(
                label = "Notificaciones",
                value = "Muestra avisos y solicitudes pendientes dentro de AURA.",
                icon = Icons.Outlined.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsEnabledChanged,
            )
            ProfileSwitchRow(
                label = "Invitaciones Guardián",
                value = "Avisa cuando alguien te agregue a su Red Guardián.",
                icon = Icons.Outlined.PersonAdd,
                checked = notificationsEnabled && guardianInviteNotificationsEnabled,
                enabled = notificationsEnabled,
                onCheckedChange = onGuardianInviteNotificationsChanged,
            )
            ProfileSwitchRow(
                label = "Alertas SOS",
                value = "Avisa cuando un contacto de confianza active SOS.",
                icon = Icons.Outlined.AddAlert,
                checked = notificationsEnabled && sosAlertNotificationsEnabled,
                enabled = notificationsEnabled,
                onCheckedChange = onSosAlertNotificationsChanged,
            )
            AuraSectionHeader(
                title = "Acerca de",
                subtitle = "Información de la aplicación y documentos públicos.",
            )
            ProfileInfoRow(
                label = "Aviso de privacidad",
                value = if (privacyDisclaimerAccepted) "Aceptado" else "Pendiente",
                icon = Icons.Outlined.Security,
            )
            ProfileInfoRow(
                label = "Version",
                value = appVersion,
                icon = Icons.Outlined.Info,
            )
            ProfileInfoRow(
                label = "Politica de privacidad",
                value = "Enlace pendiente para el MVP 0.1",
                icon = Icons.Outlined.VisibilityOff,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Tema de la aplicación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    ThemeMode.DARK -> "Oscuro"
                                    ThemeMode.LIGHT -> "Claro"
                                    ThemeMode.SYSTEM -> "Sistema"
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ProfileSwitchRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
    }
}
