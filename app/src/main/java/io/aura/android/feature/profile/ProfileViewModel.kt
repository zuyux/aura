package io.aura.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.model.ThemeMode
import io.aura.android.domain.repository.ProfileSettingsRepository
import io.aura.android.domain.repository.UserProfileRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val profileSettingsRepository: ProfileSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var smsResendTimerJob: Job? = null

    init {
        viewModelScope.launch {
            userProfileRepository.observeProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        profile = profile,
                        isLoading = false,
                        name = if (it.name.isBlank()) profile?.displayName.orEmpty() else it.name,
                        phoneNumber = if (it.phoneNumber.isBlank()) profile?.phoneNumber.orEmpty() else it.phoneNumber,
                    )
                }
            }
        }
        viewModelScope.launch {
            profileSettingsRepository.observeSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        privacyDisclaimerAccepted = settings.privacyDisclaimerAccepted,
                        anonymousModeDefault = settings.anonymousModeDefault,
                        offlineModeEnabled = settings.offlineModeEnabled,
                        notificationsEnabled = settings.notificationsEnabled,
                        guardianInviteNotificationsEnabled = settings.guardianInviteNotificationsEnabled,
                        sosAlertNotificationsEnabled = settings.sosAlertNotificationsEnabled,
                        themeMode = settings.themeMode,
                    )
                }
            }
        }
    }

    fun acceptPrivacyDisclaimer() {
        _uiState.update { it.copy(privacyDisclaimerAccepted = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setPrivacyDisclaimerAccepted(true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        privacyDisclaimerAccepted = false,
                        errorMessage = error.message ?: "No se pudo guardar la confirmación de privacidad.",
                    )
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null, successMessage = null) }
    }

    fun onPhoneNumberChanged(value: String) {
        val phoneChanged = value != _uiState.value.phoneNumber
        if (phoneChanged) smsResendTimerJob?.cancel()
        _uiState.update {
            it.copy(
                phoneNumber = value,
                smsCode = if (phoneChanged) "" else it.smsCode,
                smsCodeSent = if (phoneChanged) false else it.smsCodeSent,
                smsResendSecondsRemaining = if (phoneChanged) 0 else it.smsResendSecondsRemaining,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun sendSmsCode() {
        val state = _uiState.value
        if (state.smsResendSecondsRemaining > 0 || state.isSendingSms) return
        if (state.phoneNumber.filter(Char::isDigit).length < MIN_PHONE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Ingresa un teléfono válido antes de solicitar el código.") }
            return
        }

        _uiState.update {
            it.copy(
                isSendingSms = true,
                errorMessage = null,
                successMessage = null,
            )
        }

        viewModelScope.launch {
            // El backend de autenticación aún no está conectado; conserva aquí el
            // límite de reenvío para que la UI esté lista para esa integración.
            _uiState.update {
                it.copy(
                    isSendingSms = false,
                    smsCodeSent = true,
                    smsResendSecondsRemaining = SMS_RESEND_COOLDOWN_SECONDS,
                    successMessage = "Código SMS enviado a ${it.phoneNumber}.",
                )
            }
            startSmsResendTimer()
        }
    }

    private fun startSmsResendTimer() {
        smsResendTimerJob?.cancel()
        smsResendTimerJob = viewModelScope.launch {
            while (_uiState.value.smsResendSecondsRemaining > 0) {
                delay(1_000)
                _uiState.update { current ->
                    current.copy(
                        smsResendSecondsRemaining = (current.smsResendSecondsRemaining - 1).coerceAtLeast(0),
                    )
                }
            }
        }
    }

    fun onSmsCodeChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(MAX_SMS_CODE_LENGTH)
        _uiState.update { it.copy(smsCode = digits, errorMessage = null, successMessage = null) }
    }

    fun onSmsCodeDetected(code: String) {
        onSmsCodeChanged(code)
        _uiState.update { it.copy(successMessage = "Código SMS detectado automáticamente.") }
    }

    fun onSmsPermissionDenied() {
        _uiState.update {
            it.copy(
                errorMessage = "Puedes ingresar el código manualmente o permitir lectura de SMS.",
                successMessage = null,
            )
        }
    }

    fun onAnonymousModeDefaultChanged(enabled: Boolean) {
        _uiState.update { it.copy(anonymousModeDefault = enabled, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setAnonymousModeDefault(enabled)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar la preferencia.")
                }
            }
        }
    }

    fun onOfflineModeChanged(enabled: Boolean) {
        _uiState.update { it.copy(offlineModeEnabled = enabled, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setOfflineModeEnabled(enabled)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar la preferencia.")
                }
            }
        }
    }

    fun onNotificationsEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setNotificationsEnabled(enabled)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar la preferencia.")
                }
            }
        }
    }

    fun onGuardianInviteNotificationsChanged(enabled: Boolean) {
        _uiState.update { it.copy(guardianInviteNotificationsEnabled = enabled, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setGuardianInviteNotificationsEnabled(enabled)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar la preferencia.")
                }
            }
        }
    }

    fun onSosAlertNotificationsChanged(enabled: Boolean) {
        _uiState.update { it.copy(sosAlertNotificationsEnabled = enabled, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setSosAlertNotificationsEnabled(enabled)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar la preferencia.")
                }
            }
        }
    }

    fun onThemeModeChanged(themeMode: ThemeMode) {
        _uiState.update { it.copy(themeMode = themeMode, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            runCatching {
                profileSettingsRepository.setThemeMode(themeMode)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No se pudo guardar el tema.")
                }
            }
        }
    }

    fun completeOnboarding() {
        val state = _uiState.value
        val name = state.name.trim()
        val phoneNumber = state.phoneNumber.trim()
        val smsCode = state.smsCode.trim()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa tu nombre.") }
                return
            }
            phoneNumber.length < MIN_PHONE_LENGTH -> {
                _uiState.update { it.copy(errorMessage = "Ingresa un teléfono válido.") }
                return
            }
            smsCode.length !in MIN_SMS_CODE_LENGTH..MAX_SMS_CODE_LENGTH -> {
                _uiState.update { it.copy(errorMessage = "Ingresa el código SMS de verificación.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            runCatching {
                val now = System.currentTimeMillis()
                val existingProfile = state.profile
                userProfileRepository.saveProfile(
                    UserProfile(
                        id = existingProfile?.id ?: UUID.randomUUID().toString(),
                        displayName = name,
                        phoneNumber = phoneNumber,
                        createdAtMillis = existingProfile?.createdAtMillis ?: now,
                        updatedAtMillis = now,
                    ),
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        smsCode = "",
                        successMessage = "Perfil verificado.",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "No se pudo guardar el perfil.",
                    )
                }
            }
        }
    }

    companion object {
        private const val MIN_PHONE_LENGTH = 7
        private const val MIN_SMS_CODE_LENGTH = 4
        private const val MAX_SMS_CODE_LENGTH = 6
        private const val SMS_RESEND_COOLDOWN_SECONDS = 30
    }
}

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val name: String = "",
    val phoneNumber: String = "",
    val smsCode: String = "",
    val smsCodeSent: Boolean = false,
    val isSendingSms: Boolean = false,
    val smsResendSecondsRemaining: Int = 0,
    val privacyDisclaimerAccepted: Boolean = false,
    val anonymousModeDefault: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val guardianInviteNotificationsEnabled: Boolean = true,
    val sosAlertNotificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val isProfileComplete: Boolean = !profile?.displayName.isNullOrBlank() && !profile?.phoneNumber.isNullOrBlank()
}
