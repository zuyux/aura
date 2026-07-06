package io.aura.android.feature.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.SeverityLevel
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertsMapScreen(
    alerts: List<Alert>,
    onAlertClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AlertGoogleMap(
            alerts = alerts,
            onAlertClick = onAlertClick,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            alerts.forEachIndexed { index, alert ->
                AssistChip(
                    onClick = { onAlertClick(alert.id) },
                    label = { Text("${index + 1}. ${alert.type.label()}") },
                )
            }
        }

        alerts.forEachIndexed { index, alert ->
            MapAlertRow(
                index = index + 1,
                alert = alert,
                onClick = { onAlertClick(alert.id) },
            )
        }
    }
}

@Composable
private fun AlertGoogleMap(
    alerts: List<Alert>,
    onAlertClick: (String) -> Unit,
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val mapShape = MaterialTheme.shapes.medium
    val cameraTarget = remember(alerts) { alerts.cameraTarget() }
    val cameraZoom = remember(alerts) { alerts.cameraZoom() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cameraTarget, cameraZoom)
    }

    LaunchedEffect(cameraTarget, cameraZoom) {
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(cameraTarget, cameraZoom))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.35f)
            .clip(mapShape)
            .background(surfaceVariant),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isBuildingEnabled = false),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
            ),
        ) {
            alerts.forEach { alert ->
                Marker(
                    state = MarkerState(position = alert.location.toLatLng()),
                    title = alert.type.label(),
                    snippet = alert.relativeTimeLabel(),
                    icon = BitmapDescriptorFactory.defaultMarker(alert.severity.markerHue()),
                    onClick = {
                        onAlertClick(alert.id)
                        true
                    },
                )
            }
        }

        Text(
            text = "Zona aproximada",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MapAlertRow(
    index: Int,
    alert: Alert,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(alert.severity.alertColor(), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = alert.type.label(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${alert.distanceLabel()} / ${alert.relativeTimeLabel()} / ${alert.status.label()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun List<Alert>.cameraTarget(): LatLng {
    if (isEmpty()) {
        return LatLng(-12.0464, -77.0428)
    }

    val latitude = sumOf { it.location.latitude } / size
    val longitude = sumOf { it.location.longitude } / size
    return LatLng(latitude, longitude)
}

private fun List<Alert>.cameraZoom(): Float {
    if (size <= 1) {
        return 14f
    }

    val latitudeSpan = (maxOf { it.location.latitude } - minOf { it.location.latitude }).coerceAtLeast(0.01)
    val longitudeSpan = (maxOf { it.location.longitude } - minOf { it.location.longitude }).coerceAtLeast(0.01)
    val maxSpan = max(latitudeSpan, longitudeSpan)
    val zoom = 14.5 - ln(maxSpan * 100.0) / ln(2.0)
    return min(15.5, max(10.0, zoom)).toFloat()
}

private fun AuraLocation.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun SeverityLevel.markerHue(): Float = when (this) {
    SeverityLevel.LOW -> BitmapDescriptorFactory.HUE_GREEN
    SeverityLevel.MEDIUM -> BitmapDescriptorFactory.HUE_ORANGE
    SeverityLevel.HIGH -> BitmapDescriptorFactory.HUE_RED
}
