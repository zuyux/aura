package io.aura.android.feature.alerts

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.SeverityLevel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun AlertsMapScreen(
    alerts: List<Alert>,
    onAlertClick: (String) -> Unit,
    currentLocation: AuraLocation? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapCenter = currentLocation ?: alerts.firstOrNull()?.location ?: DEFAULT_MAP_LOCATION
    val safetyPlaces by produceState(
        initialValue = emptyList(),
        key1 = currentLocation?.latitude,
        key2 = currentLocation?.longitude,
    ) {
        value = NearbySafetyPlaces.fetch(mapCenter)
    }
    var hasCenteredMap by remember { mutableStateOf(false) }
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(mapCenter.latitude, mapCenter.longitude) {
        if (!hasCenteredMap || currentLocation != null) {
            mapView.controller.setZoom(16.0)
            mapView.controller.setCenter(mapCenter.toGeoPoint())
            hasCenteredMap = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.clear()

                currentLocation?.let { location ->
                    map.overlays.add(
                        map.marker(
                            position = location.toGeoPoint(),
                            title = "Tu ubicación actual",
                            snippet = "Ubicación aproximada del dispositivo",
                            color = CURRENT_LOCATION_COLOR,
                        ),
                    )
                }

                safetyPlaces.forEach { place ->
                    map.overlays.add(
                        map.marker(
                            position = GeoPoint(place.latitude, place.longitude),
                            title = place.name,
                            snippet = place.type.label,
                            color = place.type.markerColor(),
                        ),
                    )
                }

                alerts.forEach { alert ->
                    map.overlays.add(
                        map.marker(
                            position = alert.location.toGeoPoint(),
                            title = alert.type.label(),
                            snippet = alert.relativeTimeLabel(),
                            color = alert.severity.markerColor(),
                            onClick = { onAlertClick(alert.id) },
                        ),
                    )
                }
                map.invalidate()
            },
        )

        Text(
            text = if (currentLocation == null) "Zona aproximada" else "Tu zona actual",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun MapView.marker(
    position: GeoPoint,
    title: String,
    snippet: String,
    color: Int,
    onClick: (() -> Unit)? = null,
): Marker = Marker(this).apply {
    this.position = position
    this.title = title
    this.snippet = snippet
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    icon = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)?.let { drawable ->
        DrawableCompat.wrap(drawable.mutate()).also { DrawableCompat.setTint(it, color) }
    }
    if (onClick != null) {
        setOnMarkerClickListener { marker, _ ->
            onClick()
            marker.showInfoWindow()
            true
        }
    }
}

private fun AuraLocation.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

private fun SafetyPlaceType.markerColor(): Int = when (this) {
    SafetyPlaceType.POLICE -> Color.rgb(37, 99, 235)
    SafetyPlaceType.HOSPITAL -> Color.rgb(220, 38, 38)
    SafetyPlaceType.FIRE_STATION -> Color.rgb(234, 88, 12)
    SafetyPlaceType.AMBULANCE_STATION -> Color.rgb(225, 29, 72)
    SafetyPlaceType.PHARMACY -> Color.rgb(22, 163, 74)
}

private fun SeverityLevel.markerColor(): Int = when (this) {
    SeverityLevel.LOW -> Color.rgb(22, 163, 74)
    SeverityLevel.MEDIUM -> Color.rgb(234, 88, 12)
    SeverityLevel.HIGH -> Color.rgb(220, 38, 38)
}

private val DEFAULT_MAP_LOCATION = AuraLocation(latitude = -12.0464, longitude = -77.0428)
private const val CURRENT_LOCATION_COLOR = 0xFF2563EB.toInt()
