package io.aura.android.feature.evidence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.model.EvidenceType
import io.aura.android.domain.model.IncidentEvidence
import io.aura.android.domain.repository.IncidentEvidenceRepository
import io.aura.android.domain.usecase.AddIncidentEvidenceInput
import io.aura.android.domain.usecase.AddIncidentEvidenceUseCase
import io.aura.android.domain.usecase.DeleteLocalEvidenceUseCase
import io.aura.android.navigation.AuraRoute
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddEvidenceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    incidentEvidenceRepository: IncidentEvidenceRepository,
    private val addIncidentEvidence: AddIncidentEvidenceUseCase,
    private val deleteLocalEvidence: DeleteLocalEvidenceUseCase,
) : ViewModel() {
    private val reportId: String = checkNotNull(savedStateHandle[AuraRoute.REPORT_ID_ARG])

    val evidence: StateFlow<List<IncidentEvidence>> =
        incidentEvidenceRepository.observeEvidenceForReport(reportId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _uiState = MutableStateFlow(AddEvidenceUiState(reportId = reportId))
    val uiState: StateFlow<AddEvidenceUiState> = _uiState.asStateFlow()

    fun onEvidencePicked(type: EvidenceType, sourceUri: String?) {
        if (sourceUri == null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedType = type,
                    isSaving = true,
                    errorMessage = null,
                    savedMessage = null,
                )
            }
            runCatching {
                addIncidentEvidence(
                    AddIncidentEvidenceInput(
                        reportId = reportId,
                        type = type,
                        sourceUri = sourceUri,
                    ),
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        selectedType = null,
                        isSaving = false,
                        savedMessage = "Evidencia privada guardada y en cola para subir.",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        selectedType = null,
                        isSaving = false,
                        errorMessage = error.message ?: "No se pudo guardar la evidencia.",
                    )
                }
            }
        }
    }

    fun onDeleteEvidenceClick(evidenceId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    deletingEvidenceId = evidenceId,
                    errorMessage = null,
                    savedMessage = null,
                )
            }
            runCatching {
                deleteLocalEvidence(evidenceId)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        deletingEvidenceId = null,
                        savedMessage = "Evidencia local eliminada.",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        deletingEvidenceId = null,
                        errorMessage = error.message ?: "No se pudo eliminar la evidencia local.",
                    )
                }
            }
        }
    }
}

data class AddEvidenceUiState(
    val reportId: String,
    val selectedType: EvidenceType? = null,
    val isSaving: Boolean = false,
    val deletingEvidenceId: String? = null,
    val errorMessage: String? = null,
    val savedMessage: String? = null,
)
