package io.aura.android.feature.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.model.Alert
import io.aura.android.domain.model.VerificationAction
import io.aura.android.domain.repository.AlertRepository
import io.aura.android.navigation.AuraRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val alertRepository: AlertRepository,
) : ViewModel() {
    private val alertId: String = checkNotNull(savedStateHandle[AuraRoute.ALERT_ID_ARG])
    private val actionMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AlertDetailUiState> = combine(
        alertRepository.observeAlert(alertId),
        actionMessage,
    ) { alert, message ->
        AlertDetailUiState(
            isLoading = false,
            alert = alert,
            actionMessage = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlertDetailUiState(isLoading = true),
    )

    fun recordAction(action: VerificationAction) {
        viewModelScope.launch {
            alertRepository.recordVerification(alertId, action)
            actionMessage.value = when (action) {
                VerificationAction.ALSO_SEEN -> "Confirmacion guardada localmente."
                VerificationAction.SEEMS_FALSE -> "Reporte marcado para revision."
                VerificationAction.RESOLVED -> "Alerta marcada como resuelta."
                VerificationAction.HIDE_ALERT -> "Alerta ocultada localmente."
            }
        }
    }

    fun clearActionMessage() {
        actionMessage.value = null
    }
}

data class AlertDetailUiState(
    val isLoading: Boolean = false,
    val alert: Alert? = null,
    val actionMessage: String? = null,
)
