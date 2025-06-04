package com.rix.womblab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.auth.UpdateUserProfileUseCase
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.presentation.auth.register.UserProfile
import com.rix.womblab.utils.PreferencesUtils
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val authRepository: AuthRepository,
    private val preferencesUtils: PreferencesUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userProfile = preferencesUtils.getUserProfile()
                _uiState.value = _uiState.value.copy(
                    userProfile = userProfile,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore nel caricamento del profilo: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updateProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Utente non autenticato",
                        isLoading = false
                    )
                    return@launch
                }

                val user = com.rix.womblab.domain.model.User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = "${updatedProfile.firstName} ${updatedProfile.lastName}",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    isEmailVerified = firebaseUser.isEmailVerified
                )

                when (val result = updateUserProfileUseCase(user, updatedProfile)) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            userProfile = updatedProfile,
                            isLoading = false,
                            updateSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Errore nell'aggiornamento del profilo",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore nell'aggiornamento del profilo: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun clearUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}