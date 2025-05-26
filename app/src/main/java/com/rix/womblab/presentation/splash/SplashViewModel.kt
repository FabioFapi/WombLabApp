package com.rix.womblab.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.utils.PreferencesUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashNavigationTarget {
    Login,
    Registration,
    Main
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val navigationTarget: SplashNavigationTarget? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesUtils: PreferencesUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun checkAuthState() {
        viewModelScope.launch {
            // Resetta eventuali flag di logout forzato rimaste
            if (preferencesUtils.isForcedLogout()) {
                preferencesUtils.clearForcedLogout()
            }

            val currentUser = authRepository.getCurrentUser()

            val navigationTarget = when {
                currentUser == null -> {
                    SplashNavigationTarget.Login
                }
                !authRepository.isRegistrationCompleted(currentUser.uid) -> {
                    SplashNavigationTarget.Registration
                }
                else -> {
                    SplashNavigationTarget.Main
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                navigationTarget = navigationTarget
            )
        }
    }
}