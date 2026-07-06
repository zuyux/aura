package io.aura.android.data.location

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.aura.android.domain.location.LastKnownLocationStore
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.LocationPrecision
import javax.inject.Inject
import kotlinx.coroutines.flow.first

private val Context.locationDataStore by preferencesDataStore(
    name = DATA_STORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, LEGACY_PREFERENCES_NAME))
    },
)

class AndroidLastKnownLocationStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : LastKnownLocationStore {
    override suspend fun getLastKnownLocation(): AuraLocation? {
        val preferences = context.locationDataStore.data.first()
        val latitude = preferences[KEY_LATITUDE] ?: return null
        val longitude = preferences[KEY_LONGITUDE] ?: return null

        val precision = runCatching {
            LocationPrecision.valueOf(preferences[KEY_PRECISION] ?: LocationPrecision.APPROXIMATE.name)
        }.getOrDefault(LocationPrecision.APPROXIMATE)

        return AuraLocation(
            latitude = Double.fromBits(latitude),
            longitude = Double.fromBits(longitude),
            precision = precision,
        )
    }

    override suspend fun saveLastKnownLocation(location: AuraLocation) {
        context.locationDataStore.edit { preferences ->
            preferences[KEY_LATITUDE] = location.latitude.toBits()
            preferences[KEY_LONGITUDE] = location.longitude.toBits()
            preferences[KEY_PRECISION] = location.precision.name
        }
    }

    private companion object {
        val KEY_LATITUDE = longPreferencesKey("last_latitude")
        val KEY_LONGITUDE = longPreferencesKey("last_longitude")
        val KEY_PRECISION = stringPreferencesKey("last_precision")
    }
}

private const val DATA_STORE_NAME = "aura_location"
private const val LEGACY_PREFERENCES_NAME = "aura_location"
