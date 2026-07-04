package io.aura.android.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.repository.GuardianRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GuardianViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val draftState = MutableStateFlow(GuardianDraftState())

    val uiState: StateFlow<GuardianUiState> = combine(
        guardianRepository.observeContacts(),
        guardianRepository.observeSessions(),
        draftState,
    ) { contacts, sessions, draft ->
        val activeSession = sessions.firstOrNull { it.status == SafetySessionStatus.ACTIVE || it.status == SafetySessionStatus.SOS_TRIGGERED }
        val primaryContact = contacts.firstOrNull { it.isPrimary } ?: contacts.firstOrNull()
        GuardianUiState(
            contacts = contacts,
            primaryContact = primaryContact,
            activeSession = activeSession,
            selectedContactName = draft.selectedContactName,
            selectedPhoneNumber = draft.selectedPhoneNumber,
            selectedPhotoUri = draft.selectedPhotoUri,
            isBusy = draft.isBusy,
            message = draft.message,
            errorMessage = draft.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GuardianUiState(),
    )

    fun onContactPicked(displayName: String, phoneNumber: String, photoUri: String?) {
        draftState.update {
            it.copy(
                selectedContactName = displayName,
                selectedPhoneNumber = phoneNumber,
                selectedPhotoUri = photoUri,
                errorMessage = null,
                message = null,
            )
        }
    }

    fun onContactPickUnavailable() {
        draftState.update {
            it.copy(errorMessage = "No se pudo leer el contacto seleccionado.", message = null)
        }
    }

    fun onContactsPermissionDenied() {
        draftState.update {
            it.copy(errorMessage = "Permite acceso a contactos para elegir una persona de confianza.", message = null)
        }
    }

    fun onContactActionUnavailable() {
        draftState.update {
            it.copy(errorMessage = "Agrega un contacto de confianza para usar esta accion.", message = null)
        }
    }

    fun onExternalActionUnavailable() {
        draftState.update {
            it.copy(errorMessage = "No hay una app disponible para completar esta accion.", message = null)
        }
    }

    fun addContact() {
        val state = draftState.value
        val selectedContactName = state.selectedContactName.trim()
        val phoneNumber = state.selectedPhoneNumber.trim()
        if (selectedContactName.isBlank() || phoneNumber.isBlank()) {
            draftState.update { it.copy(errorMessage = "Elige un contacto del telefono.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                guardianRepository.saveContact(
                    GuardianContact(
                        id = UUID.randomUUID().toString(),
                        displayName = selectedContactName,
                        phoneNumber = phoneNumber,
                        photoUri = state.selectedPhotoUri,
                        isPrimary = uiState.value.contacts.isEmpty(),
                        createdAtMillis = System.currentTimeMillis(),
                    ),
                )
            }.onSuccess {
                draftState.update {
                    GuardianDraftState(message = "Contacto guardado localmente.")
                }
            }.onFailure { error ->
                draftState.update { it.copy(errorMessage = error.message ?: "No se pudo guardar el contacto.") }
            }
        }
    }

    fun startSession() {
        viewModelScope.launch {
            draftState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            val location = runCatching { locationProvider.getCurrentLocation() }.getOrNull()
            runCatching {
                val now = System.currentTimeMillis()
                val session = SafetySession(
                    id = UUID.randomUUID().toString(),
                    status = SafetySessionStatus.ACTIVE,
                    startedAtMillis = now,
                    endedAtMillis = null,
                    lastLocation = location,
                )
                guardianRepository.saveSession(session)
                guardianRepository.saveUpdate(
                    SafetySessionUpdate(
                        id = UUID.randomUUID().toString(),
                        sessionId = session.id,
                        location = location,
                        note = "Sesion iniciada",
                        createdAtMillis = now,
                    ),
                )
            }.onSuccess {
                draftState.update {
                    it.copy(
                        isBusy = false,
                        message = if (location == null) {
                            "Sesion iniciada sin GPS. Puedes compartir ubicacion cuando este disponible."
                        } else {
                            "Sesion iniciada y ubicacion guardada localmente."
                        },
                    )
                }
            }.onFailure { error ->
                draftState.update {
                    it.copy(isBusy = false, errorMessage = error.message ?: "No se pudo iniciar la sesion.")
                }
            }
        }
    }

    fun shareLocation() {
        val session = uiState.value.activeSession ?: run {
            draftState.update { it.copy(errorMessage = "Inicia una sesion antes de compartir ubicacion.") }
            return
        }
        viewModelScope.launch {
            draftState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            val location = runCatching { locationProvider.getCurrentLocation() }.getOrNull()
            if (location == null) {
                draftState.update {
                    it.copy(isBusy = false, errorMessage = "No se pudo obtener la ubicacion actual.")
                }
                return@launch
            }
            saveSessionUpdate(
                session = session.copy(lastLocation = location),
                location = location,
                note = "Ubicacion compartida",
                message = "Ubicacion guardada para compartir con tus contactos.",
            )
        }
    }

    fun markSafe() {
        val session = uiState.value.activeSession ?: return
        viewModelScope.launch {
            saveSessionUpdate(
                session = session,
                location = session.lastLocation,
                note = "Estoy bien",
                message = "Estado Estoy bien guardado.",
            )
        }
    }

    fun triggerSos() {
        val session = uiState.value.activeSession ?: run {
            startSession()
            return
        }
        viewModelScope.launch {
            saveSessionUpdate(
                session = session.copy(status = SafetySessionStatus.SOS_TRIGGERED),
                location = session.lastLocation,
                note = "SOS activado",
                message = "SOS activado y guardado localmente.",
            )
        }
    }

    fun endSession() {
        val session = uiState.value.activeSession ?: return
        viewModelScope.launch {
            val endedSession = session.copy(
                status = SafetySessionStatus.ENDED_SAFE,
                endedAtMillis = System.currentTimeMillis(),
            )
            saveSessionUpdate(
                session = endedSession,
                location = session.lastLocation,
                note = "Sesion finalizada",
                message = "Sesion finalizada como segura.",
            )
        }
    }

    private suspend fun saveSessionUpdate(
        session: SafetySession,
        location: AuraLocation?,
        note: String,
        message: String,
    ) {
        draftState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
        runCatching {
            guardianRepository.saveSession(session)
            guardianRepository.saveUpdate(
                SafetySessionUpdate(
                    id = UUID.randomUUID().toString(),
                    sessionId = session.id,
                    location = location,
                    note = note,
                    createdAtMillis = System.currentTimeMillis(),
                ),
            )
        }.onSuccess {
            draftState.update { it.copy(isBusy = false, message = message) }
        }.onFailure { error ->
            draftState.update {
                it.copy(isBusy = false, errorMessage = error.message ?: "No se pudo actualizar la sesion.")
            }
        }
    }
}

data class GuardianUiState(
    val contacts: List<GuardianContact> = emptyList(),
    val primaryContact: GuardianContact? = null,
    val activeSession: SafetySession? = null,
    val selectedContactName: String = "",
    val selectedPhoneNumber: String = "",
    val selectedPhotoUri: String? = null,
    val isBusy: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
) {
    val hasActiveSession: Boolean = activeSession != null
}

private data class GuardianDraftState(
    val selectedContactName: String = "",
    val selectedPhoneNumber: String = "",
    val selectedPhotoUri: String? = null,
    val isBusy: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)
