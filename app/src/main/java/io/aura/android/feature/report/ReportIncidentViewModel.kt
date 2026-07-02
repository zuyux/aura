package io.aura.android.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.IncidentType
import io.aura.android.domain.model.LocationPrecision
import io.aura.android.domain.model.SeverityLevel
import io.aura.android.domain.usecase.CreateIncidentReportInput
import io.aura.android.domain.usecase.CreateIncidentReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportIncidentViewModel @Inject constructor(
    private val createIncidentReport: CreateIncidentReportUseCase,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportIncidentUiState())
    val uiState: StateFlow<ReportIncidentUiState> = _uiState.asStateFlow()

    fun loadLocation() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingLocation = true,
                    isLocationConfirmed = false,
                    locationStatus = "Buscando ubicacion actual",
                    errorMessage = null,
                    savedReportId = null,
                )
            }
            val location = locationProvider.getCurrentLocation()
            _uiState.update {
                it.copy(
                    location = location,
                    isLoadingLocation = false,
                    locationStatus = if (location == null) {
                        "Permite la ubicacion o activa el GPS"
                    } else {
                        "Ubicacion actual detectada"
                    },
                )
            }
        }
    }

    fun onTypeSelected(type: IncidentType) {
        _uiState.update { it.copy(selectedType = type, errorMessage = null, savedReportId = null) }
    }

    fun onSeveritySelected(severity: SeverityLevel) {
        _uiState.update { it.copy(severity = severity, savedReportId = null) }
    }

    fun onDescriptionChanged(description: String) {
        if (description.length <= CreateIncidentReportUseCase.MAX_DESCRIPTION_LENGTH) {
            _uiState.update { it.copy(description = description, errorMessage = null, savedReportId = null) }
        }
    }

    fun onAnonymousChanged(isAnonymous: Boolean) {
        _uiState.update { it.copy(isAnonymous = isAnonymous, savedReportId = null) }
    }

    fun onLocationPrecisionSelected(precision: LocationPrecision) {
        _uiState.update {
            it.copy(
                locationPrecision = precision,
                isLocationConfirmed = false,
                savedReportId = null,
            )
        }
    }

    fun confirmLocation() {
        _uiState.update {
            it.copy(
                isLocationConfirmed = it.location != null,
                errorMessage = if (it.location == null) "No hay ubicacion disponible." else null,
                savedReportId = null,
            )
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                isLoadingLocation = false,
                isLocationConfirmed = false,
                locationStatus = "Permiso de ubicacion no concedido",
                errorMessage = "AURA necesita permiso de ubicacion para usar GPS en este reporte.",
                savedReportId = null,
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val type = state.selectedType
        if (type == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona un tipo de incidente.") }
            return
        }
        val location = state.location
        if (location == null) {
            _uiState.update { it.copy(errorMessage = "No hay ubicacion disponible.") }
            return
        }
        if (!state.isLocationConfirmed) {
            _uiState.update { it.copy(errorMessage = "Confirma la ubicacion aproximada del incidente.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, savedReportId = null) }
            runCatching {
                createIncidentReport(
                    CreateIncidentReportInput(
                        type = type,
                        severity = state.severity,
                        location = location,
                        locationPrecision = state.locationPrecision,
                        isLocationConfirmed = state.isLocationConfirmed,
                        description = state.description,
                        isAnonymous = state.isAnonymous,
                    ),
                )
            }.onSuccess { report ->
                _uiState.update {
                    ReportIncidentUiState(
                        isAnonymous = it.isAnonymous,
                        location = it.location,
                        locationStatus = it.locationStatus,
                        locationPrecision = it.locationPrecision,
                        isLocationConfirmed = it.isLocationConfirmed,
                        savedReportId = report.id,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "No se pudo guardar el reporte.",
                    )
                }
            }
        }
    }
}

data class ReportIncidentUiState(
    val selectedType: IncidentType? = null,
    val severity: SeverityLevel = SeverityLevel.MEDIUM,
    val location: AuraLocation? = null,
    val locationStatus: String = "Usa GPS para detectar la ubicacion actual",
    val locationPrecision: LocationPrecision = LocationPrecision.APPROXIMATE,
    val isLocationConfirmed: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val description: String = "",
    val isAnonymous: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val savedReportId: String? = null,
) {
    val canSubmit: Boolean = selectedType != null && location != null && isLocationConfirmed && !isSubmitting
}
