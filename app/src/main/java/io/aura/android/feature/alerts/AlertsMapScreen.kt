package io.aura.android.feature.alerts

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.aura.android.domain.model.Alert
import kotlin.math.max

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
        AlertMapCanvas(alerts = alerts)

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
private fun AlertMapCanvas(alerts: List<Alert>) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val zoneColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val pinColors = alerts.map { it.severity.alertColor() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.35f)
            .background(surfaceVariant, MaterialTheme.shapes.medium),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val horizontalInset = size.width * 0.10f
            val verticalInset = size.height * 0.12f

            repeat(4) { step ->
                val x = horizontalInset + (size.width - horizontalInset * 2) * (step + 1) / 5f
                drawLine(
                    color = gridColor,
                    start = Offset(x, verticalInset),
                    end = Offset(x, size.height - verticalInset),
                    strokeWidth = 1.dp.toPx(),
                )
                val y = verticalInset + (size.height - verticalInset * 2) * (step + 1) / 5f
                drawLine(
                    color = gridColor,
                    start = Offset(horizontalInset, y),
                    end = Offset(size.width - horizontalInset, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            drawCircle(
                color = zoneColor,
                radius = max(size.minDimension * 0.34f, 1f),
                center = center,
            )
            drawCircle(
                color = gridColor,
                radius = max(size.minDimension * 0.34f, 1f),
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )

            val bounds = alerts.coordinateBounds()
            alerts.forEachIndexed { index, alert ->
                val offset = alert.location.toMapOffset(
                    bounds = bounds,
                    width = size.width,
                    height = size.height,
                    horizontalInset = horizontalInset,
                    verticalInset = verticalInset,
                )
                val color = pinColors[index]
                drawCircle(color = color.copy(alpha = 0.18f), radius = 18.dp.toPx(), center = offset)
                drawCircle(color = color, radius = 7.dp.toPx(), center = offset)
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = offset,
                    style = Stroke(width = 2.dp.toPx()),
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

private data class CoordinateBounds(
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double,
)

private fun List<Alert>.coordinateBounds(): CoordinateBounds {
    if (isEmpty()) {
        return CoordinateBounds(0.0, 1.0, 0.0, 1.0)
    }

    val latitudes = map { it.location.latitude }
    val longitudes = map { it.location.longitude }
    val minLatitude = latitudes.minOrNull() ?: 0.0
    val maxLatitude = latitudes.maxOrNull() ?: minLatitude + 1.0
    val minLongitude = longitudes.minOrNull() ?: 0.0
    val maxLongitude = longitudes.maxOrNull() ?: minLongitude + 1.0

    return CoordinateBounds(
        minLatitude = minLatitude,
        maxLatitude = if (minLatitude == maxLatitude) maxLatitude + 0.01 else maxLatitude,
        minLongitude = minLongitude,
        maxLongitude = if (minLongitude == maxLongitude) maxLongitude + 0.01 else maxLongitude,
    )
}

private fun io.aura.android.domain.model.AuraLocation.toMapOffset(
    bounds: CoordinateBounds,
    width: Float,
    height: Float,
    horizontalInset: Float,
    verticalInset: Float,
): Offset {
    val longitudeRange = bounds.maxLongitude - bounds.minLongitude
    val latitudeRange = bounds.maxLatitude - bounds.minLatitude
    val xRatio = ((longitude - bounds.minLongitude) / longitudeRange).toFloat().coerceIn(0f, 1f)
    val yRatio = (1f - ((latitude - bounds.minLatitude) / latitudeRange).toFloat()).coerceIn(0f, 1f)
    return Offset(
        x = horizontalInset + (width - horizontalInset * 2) * xRatio,
        y = verticalInset + (height - verticalInset * 2) * yRatio,
    )
}
