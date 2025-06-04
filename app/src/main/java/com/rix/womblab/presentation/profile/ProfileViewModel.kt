package com.rix.womblab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.User
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.auth.LogoutUseCase
import com.rix.womblab.domain.usecase.favorites.GetFavoritesUseCase
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.presentation.auth.register.UserProfile
import com.rix.womblab.utils.PreferencesUtils
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val userProfile: UserProfile? = null,
    val favoriteEventsCount: Int = 0,
    val error: String? = null,
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    val showLogoutDialog: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val authRepository: AuthRepository,
    private val preferencesUtils: PreferencesUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                getCurrentUserUseCase().collect { firebaseUser ->
                    if (firebaseUser != null) {
                        val user = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: "",
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            isEmailVerified = firebaseUser.isEmailVerified
                        )

                        val userProfile = preferencesUtils.getUserProfile()

                        loadFavoriteEventsCount(firebaseUser.uid)

                        _uiState.value = _uiState.value.copy(
                            user = user,
                            userProfile = userProfile,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            user = null,
                            userProfile = null,
                            isLoading = false,
                            error = "Utente non autenticato"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Errore nel caricamento del profilo: ${e.message}"
                )
            }
        }
    }

    private fun loadFavoriteEventsCount(userId: String) {
        viewModelScope.launch {
            getFavoritesUseCase(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val count = resource.data?.size ?: 0
                        _uiState.value = _uiState.value.copy(
                            favoriteEventsCount = count
                        )
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun hideLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun confirmLogout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoggingOut = true,
                error = null,
                showLogoutDialog = false
            )

            when (val result = logoutUseCase()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        logoutSuccess = true
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        error = result.message ?: "Errore durante il logout"
                    )
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearLogoutSuccess() {
        _uiState.value = _uiState.value.copy(logoutSuccess = false)
    }

    fun onProfileUpdated() {
        viewModelScope.launch {
            try {
                val updatedProfile = preferencesUtils.getUserProfile()
                _uiState.value = _uiState.value.copy(
                    userProfile = updatedProfile
                )
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Errore aggiornamento profilo", e)
            }
        }
    }
}