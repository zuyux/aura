package io.aura.android.feature.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.aura.android.core.ui.components.AuraEmptyState
import io.aura.android.core.ui.components.AuraSectionHeader
import io.aura.android.core.ui.theme.AuraDarkBlue
import io.aura.android.core.ui.theme.AuraGreen
import io.aura.android.core.ui.theme.AuraRed
import io.aura.android.core.ui.theme.AuraShapes
import io.aura.android.data.location.AndroidLocationPermissionManager
import io.aura.android.domain.model.GuardianNotification
import io.aura.android.domain.model.GuardianNotificationStatus
import io.aura.android.domain.model.GuardianNotificationType
import io.aura.android.feature.guardian.GuardianViewModel

@Composable
fun HomeScreen(
    onReportClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onGuardianClick: () -> Unit,
    modifier: Modifier = Modifier,
    username: String = "Valeria",
    guardianViewModel: GuardianViewModel = hiltViewModel(),
    notificationsViewModel: HomeNotificationsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()
    var showNotifications by remember { mutableStateOf(false) }
    val locationPermissionManager = remember(context) {
        AndroidLocationPermissionManager(context.applicationContext)
    }
    val sosPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        guardianViewModel.triggerSos()
        onGuardianClick()
    }

    notificationsUiState.pendingInvite?.let { invite ->
        GuardianInviteDialog(
            notification = invite,
            onAccept = { notificationsViewModel.acceptInvite(invite.id) },
            onDecline = { notificationsViewModel.declineInvite(invite.id) },
        )
    }

    if (showNotifications) {
        GuardianNotificationsDialog(
            uiState = notificationsUiState,
            onDismiss = { showNotifications = false },
            onMarkRead = notificationsViewModel::markRead,
            onAcceptInvite = notificationsViewModel::acceptInvite,
            onDeclineInvite = notificationsViewModel::declineInvite,
            onOpenGuardian = {
                showNotifications = false
                onGuardianClick()
            },
        )
    }

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
                    text = "A U R A",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                NotificationBell(
                    unreadCount = notificationsUiState.unreadCount,
                    onClick = { showNotifications = true },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Hola, $username",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Estamos aquí para cuidarnos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            EmergencySosCard(
                onClick = {
                    if (context.hasSosPermissions(locationPermissionManager)) {
                        guardianViewModel.triggerSos()
                        onGuardianClick()
                    } else {
                        sosPermissionLauncher.launch(
                            locationPermissionManager.locationPermissions +
                                Manifest.permission.SEND_SMS +
                                notificationPermission(),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                    body = "Comparte tu ubicación y viaja más segura",
                    icon = Icons.Outlined.Groups,
                    onClick = onGuardianClick,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notificaciones",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(18.dp)
                    .background(AuraRed, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = unreadCount.coerceAtMost(9).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun GuardianInviteDialog(
    notification: GuardianNotification,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(
                imageVector = Icons.Outlined.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text("Invitación a Red Guardián") },
        text = {
            Text("${notification.senderName} te agregó a su Red Guardián. Acepta para formar parte y guardar este contacto de confianza.")
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                Text(text = "Aceptar", modifier = Modifier.padding(start = 8.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text("Rechazar")
            }
        },
    )
}

@Composable
private fun GuardianNotificationsDialog(
    uiState: HomeNotificationsUiState,
    onDismiss: () -> Unit,
    onMarkRead: (String) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    onOpenGuardian: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notificaciones") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.notifications.isEmpty()) {
                    AuraEmptyState(
                        title = "Sin notificaciones",
                        body = "Tus alertas de SOS e invitaciones de Red Guardián aparecerán aquí.",
                        icon = Icons.Outlined.Notifications,
                    )
                } else {
                    uiState.notifications.forEachIndexed { index, notification ->
                        GuardianNotificationRow(
                            notification = notification,
                            onMarkRead = { onMarkRead(notification.id) },
                            onAcceptInvite = { onAcceptInvite(notification.id) },
                            onDeclineInvite = { onDeclineInvite(notification.id) },
                        )
                        if (index < uiState.notifications.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenGuardian) {
                Text("Red Guardián")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
    )
}

@Composable
private fun GuardianNotificationRow(
    notification: GuardianNotification,
    onMarkRead: () -> Unit,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (notification.type == GuardianNotificationType.SOS_ALERT) AuraRed else AuraDarkBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (notification.type == GuardianNotificationType.SOS_ALERT) {
                    Icons.Outlined.Shield
                } else {
                    Icons.Outlined.PersonAdd
                },
                contentDescription = null,
                tint = Color.White,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = notificationTitle(notification),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            notification.locationText()?.let { location ->
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            NotificationActions(
                notification = notification,
                onMarkRead = onMarkRead,
                onAcceptInvite = onAcceptInvite,
                onDeclineInvite = onDeclineInvite,
            )
        }
    }
}

@Composable
private fun NotificationActions(
    notification: GuardianNotification,
    onMarkRead: () -> Unit,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
) {
    when {
        notification.isActionable -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAcceptInvite) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                Text(text = "Aceptar", modifier = Modifier.padding(start = 6.dp))
            }
            OutlinedButton(onClick = onDeclineInvite) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                Text(text = "Rechazar", modifier = Modifier.padding(start = 6.dp))
            }
        }
        notification.status == GuardianNotificationStatus.UNREAD -> TextButton(onClick = onMarkRead) {
            Text("Marcar leida")
        }
        notification.status == GuardianNotificationStatus.ACCEPTED -> Text(
            text = "Aceptada",
            style = MaterialTheme.typography.labelMedium,
            color = AuraGreen,
            fontWeight = FontWeight.SemiBold,
        )
        notification.status == GuardianNotificationStatus.DECLINED -> Text(
            text = "Rechazada",
            style = MaterialTheme.typography.labelMedium,
            color = AuraRed,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun notificationTitle(notification: GuardianNotification): String =
    when (notification.type) {
        GuardianNotificationType.SOS_ALERT -> "SOS de ${notification.senderName}"
        GuardianNotificationType.GUARDIAN_INVITE -> "${notification.senderName} te agregó"
    }

private fun GuardianNotification.locationText(): String? {
    val latitude = latitude ?: return null
    val longitude = longitude ?: return null
    return "Ubicación: %.5f, %.5f".format(java.util.Locale.US, latitude, longitude)
}

private fun Context.hasSosPermissions(locationPermissionManager: AndroidLocationPermissionManager): Boolean {
    val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
    return locationPermissionManager.hasLocationPermission() &&
        sms == PackageManager.PERMISSION_GRANTED &&
        hasNotificationPermission()
}

private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun notificationPermission(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

@Composable
private fun EmergencySosCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .shadow(10.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF4B4D), AuraRed),
                    ),
                )
                .clickable(onClick = onClick)
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                    text = "Toca aquí para pedir ayuda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
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
        shape = AuraShapes.card,
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
                    .clip(AuraShapes.control)
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
