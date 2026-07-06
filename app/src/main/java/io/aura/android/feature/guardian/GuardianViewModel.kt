package io.aura.android.feature.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.data.guardian.GuardianSosNotifier
import io.aura.android.data.guardian.SmsFallbackResult
import io.aura.android.data.guardian.guardianSosMessage
import io.aura.android.domain.location.LocationProvider
import io.aura.android.domain.model.AuraLocation
import io.aura.android.domain.model.GuardianContact
import io.aura.android.domain.model.SafetySession
import io.aura.android.domain.model.SafetySessionStatus
import io.aura.android.domain.model.SafetySessionUpdate
import io.aura.android.domain.repository.GuardianRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class GuardianViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository,
    private val locationProvider: LocationProvider,
    private val guardianSosNotifier: GuardianSosNotifier,
) : ViewModel() {
    private val draftState = MutableStateFlow(GuardianDraftState())
    private var locationTrackingJob: Job? = null
    private var locationTrackingSessionId: String? = null

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

    init {
        viewModelScope.launch {
            guardianRepository.observeSessions()
                .map { sessions -> sessions.firstOrNull { it.isLiveSession } }
                .distinctUntilChangedBy { session -> session?.id }
                .collect { session ->
                    if (session == null) {
                        stopLocationTracking()
                    } else {
                        startLocationTracking(session.id)
                    }
                }
        }
    }

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
                    GuardianDraftState(message = "Contacto guardado. Invitacion enviada para aprobacion.")
                }
            }.onFailure { error ->
                draftState.update { it.copy(errorMessage = error.message ?: "No se pudo guardar el contacto.") }
            }
        }
    }

    fun removeContact(contact: GuardianContact) {
        viewModelScope.launch {
            runCatching {
                guardianRepository.removeContact(contact.id)
            }.onSuccess {
                draftState.update {
                    it.copy(
                        message = "${contact.displayName} fue eliminado de Red Guardian.",
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                draftState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo eliminar el contacto.", message = null)
                }
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
        viewModelScope.launch {
            draftState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            val state = uiState.value
            val now = System.currentTimeMillis()
            val location = runCatching { locationProvider.getCurrentLocation() }.getOrNull()
            val activeSession = state.activeSession
            val session = if (activeSession == null) {
                SafetySession(
                    id = UUID.randomUUID().toString(),
                    status = SafetySessionStatus.SOS_TRIGGERED,
                    startedAtMillis = now,
                    endedAtMillis = null,
                    lastLocation = location,
                )
            } else {
                activeSession.copy(
                    status = SafetySessionStatus.SOS_TRIGGERED,
                    lastLocation = location ?: activeSession.lastLocation,
                )
            }
            val sosMessage = guardianSosMessage(session.lastLocation)

            runCatching {
                guardianRepository.saveSession(session)
                guardianRepository.saveUpdate(
                    SafetySessionUpdate(
                        id = UUID.randomUUID().toString(),
                        sessionId = session.id,
                        location = session.lastLocation,
                        note = SOS_UPDATE_NOTE,
                        createdAtMillis = now,
                    ),
                )
                guardianSosNotifier.sendSmsFallback(state.contacts, sosMessage)
            }.onSuccess { smsResult ->
                draftState.update {
                    it.copy(
                        isBusy = false,
                        message = sosSuccessMessage(
                            locationAvailable = session.lastLocation != null,
                            smsResult = smsResult,
                        ),
                    )
                }
            }.onFailure { error ->
                draftState.update {
                    it.copy(isBusy = false, errorMessage = error.message ?: "No se pudo activar SOS.")
                }
            }
        }
    }

    fun endSession() {
        val session = uiState.value.activeSession ?: return
        viewModelScope.launch {
            val endedSession = session.copy(
                status = SafetySessionStatus.ENDED_SAFE,
                endedAtMillis = System.currentTimeMillis(),
            )
            val didEndSession = saveSessionUpdate(
                session = endedSession,
                location = session.lastLocation,
                note = "Sesion finalizada",
                message = "Sesion finalizada como segura.",
            )
            if (didEndSession) stopLocationTracking()
        }
    }

    private suspend fun saveSessionUpdate(
        session: SafetySession,
        location: AuraLocation?,
        note: String,
        message: String,
    ): Boolean {
        draftState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
        val result = runCatching {
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
        }
        result.onSuccess {
            draftState.update { it.copy(isBusy = false, message = message) }
        }.onFailure { error ->
            draftState.update {
                it.copy(isBusy = false, errorMessage = error.message ?: "No se pudo actualizar la sesion.")
            }
        }
        return result.isSuccess
    }

    private fun startLocationTracking(sessionId: String) {
        if (locationTrackingJob?.isActive == true && locationTrackingSessionId == sessionId) return

        stopLocationTracking()
        locationTrackingSessionId = sessionId

        locationTrackingJob = viewModelScope.launch {
            delay(ACTIVE_SESSION_LOCATION_INTERVAL_MILLIS)
            while (isActive) {
                val session = liveSession(sessionId) ?: break
                val location = runCatching { locationProvider.getCurrentLocation() }.getOrNull()
                if (location != null) {
                    runCatching {
                        guardianRepository.saveSession(session.copy(lastLocation = location))
                        guardianRepository.saveUpdate(
                            SafetySessionUpdate(
                                id = UUID.randomUUID().toString(),
                                sessionId = session.id,
                                location = location,
                                note = "Ubicacion actualizada",
                                createdAtMillis = System.currentTimeMillis(),
                            ),
                        )
                    }
                }
                delay(ACTIVE_SESSION_LOCATION_INTERVAL_MILLIS)
            }
        }
    }

    private fun stopLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
        locationTrackingSessionId = null
    }

    private suspend fun liveSession(sessionId: String): SafetySession? =
        guardianRepository.observeSessions()
            .map { sessions -> sessions.firstOrNull { it.id == sessionId && it.isLiveSession } }
            .first()
}

private fun sosSuccessMessage(
    locationAvailable: Boolean,
    smsResult: SmsFallbackResult,
): String {
    val locationText = if (locationAvailable) {
        "GPS actual guardado"
    } else {
        "GPS no disponible todavia"
    }
    val smsText = when (smsResult) {
        is SmsFallbackResult.Sent -> "SMS enviado a ${smsResult.contactCount} contacto(s)"
        SmsFallbackResult.NoContacts -> "sin contactos SMS"
        SmsFallbackResult.PermissionMissing -> "SMS pendiente por permiso"
    }
    return "SOS activado. $locationText; notificacion de Red Guardian en cola; $smsText."
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

private val SafetySession.isLiveSession: Boolean
    get() = status == SafetySessionStatus.ACTIVE || status == SafetySessionStatus.SOS_TRIGGERED

private const val ACTIVE_SESSION_LOCATION_INTERVAL_MILLIS = 60_000L
private const val SOS_UPDATE_NOTE = "SOS activado"
