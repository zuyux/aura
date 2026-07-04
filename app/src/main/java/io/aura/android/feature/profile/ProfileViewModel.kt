package io.aura.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.repository.UserProfileRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null, successMessage = null) }
    }

    fun onPhoneNumberChanged(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null, successMessage = null) }
    }

    fun onSmsCodeChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(MAX_SMS_CODE_LENGTH)
        _uiState.update { it.copy(smsCode = digits, errorMessage = null, successMessage = null) }
    }

    fun onSmsCodeDetected(code: String) {
        onSmsCodeChanged(code)
        _uiState.update { it.copy(successMessage = "Codigo SMS detectado automaticamente.") }
    }

    fun onSmsPermissionDenied() {
        _uiState.update {
            it.copy(
                errorMessage = "Puedes ingresar el codigo manualmente o permitir lectura de SMS.",
                successMessage = null,
            )
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
                _uiState.update { it.copy(errorMessage = "Ingresa un telefono valido.") }
                return
            }
            smsCode.length !in MIN_SMS_CODE_LENGTH..MAX_SMS_CODE_LENGTH -> {
                _uiState.update { it.copy(errorMessage = "Ingresa el codigo SMS de verificacion.") }
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
    }
}

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val name: String = "",
    val phoneNumber: String = "",
    val smsCode: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val isProfileComplete: Boolean = !profile?.displayName.isNullOrBlank() && !profile?.phoneNumber.isNullOrBlank()
}
