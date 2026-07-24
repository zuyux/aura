package io.aura.android.feature.alerts

import android.util.Log
import io.aura.android.domain.model.AuraLocation
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class NearbySafetyPlace(
    val id: Long,
    val name: String,
    val type: SafetyPlaceType,
    val latitude: Double,
    val longitude: Double,
)

enum class SafetyPlaceType(val label: String) {
    POLICE("Policía"),
    HOSPITAL("Hospital o clínica"),
    FIRE_STATION("Bomberos"),
    AMBULANCE_STATION("Ambulancia"),
    PHARMACY("Farmacia"),
}

object NearbySafetyPlaces {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(
        location: AuraLocation,
        radiusMeters: Int = SAFETY_PLACE_RADIUS_METERS,
    ): List<NearbySafetyPlace> = withContext(Dispatchers.IO) {
        val query = """
            [out:json][timeout:20];
            (
              nwr["amenity"~"^(police|hospital|clinic|fire_station|pharmacy)$"](around:$radiusMeters,${location.latitude},${location.longitude});
              nwr["emergency"="ambulance_station"](around:$radiusMeters,${location.latitude},${location.longitude});
            );
            out center tags;
        """.trimIndent()

        OVERPASS_ENDPOINTS.firstNotNullOfOrNull { endpoint ->
            runCatching {
                request(endpoint, query)
            }.onFailure { error ->
                Log.w(LOG_TAG, "Falló el servidor $endpoint", error)
            }.getOrNull()
        }.orEmpty()
    }

    private fun request(endpoint: String, query: String): List<NearbySafetyPlace> {
        val body = "data=${URLEncoder.encode(query, Charsets.UTF_8.name())}"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 25_000
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", "AURA-Android/0.1")
        }
        connection.outputStream.use { output ->
            output.write(body.toByteArray())
        }
        return try {
            check(connection.responseCode in 200..299) {
                "Overpass respondió ${connection.responseCode}"
            }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<OverpassResponse>(response)
                .elements
                .mapNotNull(OverpassElement::toSafetyPlace)
                .distinctBy { it.type to it.name }
                .take(MAX_SAFETY_PLACES)
        } finally {
            connection.disconnect()
        }
    }
}

@Serializable
private data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList(),
)

@Serializable
private data class OverpassElement(
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String> = emptyMap(),
) {
    fun toSafetyPlace(): NearbySafetyPlace? {
        val latitude = lat ?: center?.lat ?: return null
        val longitude = lon ?: center?.lon ?: return null
        val type = when {
            tags["emergency"] == "ambulance_station" -> SafetyPlaceType.AMBULANCE_STATION
            tags["amenity"] == "police" -> SafetyPlaceType.POLICE
            tags["amenity"] in setOf("hospital", "clinic") -> SafetyPlaceType.HOSPITAL
            tags["amenity"] == "fire_station" -> SafetyPlaceType.FIRE_STATION
            tags["amenity"] == "pharmacy" -> SafetyPlaceType.PHARMACY
            else -> return null
        }
        return NearbySafetyPlace(
            id = id,
            name = tags["name"] ?: type.label,
            type = type,
            latitude = latitude,
            longitude = longitude,
        )
    }
}

@Serializable
private data class OverpassCenter(
    val lat: Double,
    val lon: Double,
)

private val OVERPASS_ENDPOINTS = listOf(
    "https://overpass.kumi.systems/api/interpreter",
    "https://overpass-api.de/api/interpreter",
)
private const val SAFETY_PLACE_RADIUS_METERS = 2_000
private const val MAX_SAFETY_PLACES = 40
private const val LOG_TAG = "NearbySafetyPlaces"
