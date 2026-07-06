package io.aura.android.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyDisclaimerScreen(
    onAcceptClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Privacidad en AURA",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "AURA reduce la exposicion de datos sensibles, pero no reemplaza a los servicios de emergencia.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PrivacyDisclaimerItem(
                icon = Icons.Outlined.GpsFixed,
                title = "Ubicacion publica aproximada",
                body = "Los reportes comunitarios se publican por zona o distrito. La ubicacion exacta se reserva para flujos privados como Red Guardian o SOS.",
            )
            PrivacyDisclaimerItem(
                icon = Icons.Outlined.Lock,
                title = "Evidencia privada",
                body = "Fotos, videos y audio se guardan como evidencia privada por defecto. Las fotos se reescriben sin metadatos y cada archivo recibe hash SHA-256.",
            )
            PrivacyDisclaimerItem(
                icon = Icons.Outlined.UploadFile,
                title = "Sincronizacion controlada",
                body = "Cuando hay conexion, los reportes y acciones pendientes pueden sincronizarse con el backend configurado.",
            )
            PrivacyDisclaimerItem(
                icon = Icons.Outlined.CloudOff,
                title = "Modo offline",
                body = "Puedes conservar cambios en el dispositivo y pausar consultas remotas desde Perfil.",
            )
            PrivacyDisclaimerItem(
                icon = Icons.Outlined.Delete,
                title = "Borrado local",
                body = "Puedes eliminar evidencia local adjunta a un reporte antes de que se suba.",
            )

            Button(
                onClick = onAcceptClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Entiendo y continuar")
            }
        }
    }
}

@Composable
private fun PrivacyDisclaimerItem(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
