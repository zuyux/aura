package io.aura.android.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(AlertFilter.ALL)

    val uiState: StateFlow<AlertsUiState> = combine(
        alertRepository.observeNearbyAlerts(),
        selectedFilter,
    ) { alerts, filter ->
        AlertsUiState(
            isLoading = false,
            alerts = alerts.filter { alert -> filter.matches(alert) },
            selectedFilter = filter,
            totalAlerts = alerts.size,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlertsUiState(isLoading = true),
        )

    init {
        viewModelScope.launch {
            refreshNearbyAlerts()
        }
    }

    fun selectFilter(filter: AlertFilter) {
        selectedFilter.value = filter
    }

    fun refreshNearbyAlerts() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation() ?: return@launch
            alertRepository.refreshNearbyAlerts(
                location = location,
                radiusMeters = DEFAULT_ALERT_RADIUS_METERS,
            )
        }
    }
}

data class AlertsUiState(
    val isLoading: Boolean = false,
    val alerts: List<Alert> = emptyList(),
    val selectedFilter: AlertFilter = AlertFilter.ALL,
    val totalAlerts: Int = 0,
)

enum class AlertFilter(
    val label: String,
    private val types: Set<IncidentType> = emptySet(),
) {
    ALL("Todas"),
    THEFT("Robo", setOf(IncidentType.THEFT, IncidentType.ATTEMPTED_THEFT)),
    VIOLENCE("Violencia", setOf(IncidentType.VIOLENCE)),
    HARASSMENT("Acoso", setOf(IncidentType.HARASSMENT)),
    ACCIDENT("Accidente", setOf(IncidentType.ACCIDENT));

    fun matches(alert: Alert): Boolean = this == ALL || alert.type in types
}

private const val DEFAULT_ALERT_RADIUS_METERS = 1_500
