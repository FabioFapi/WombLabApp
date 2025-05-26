package com.rix.womblab.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

enum class SplashState {
    LOADING,
    READY,
    ERROR
}

data class SplashUiState(
    val state: SplashState = SplashState.LOADING,
    val errorMessage: String? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        initializeSplash()
    }

    private fun initializeSplash() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(state = SplashState.LOADING)

                delay(2000)

                _uiState.value = _uiState.value.copy(
                    state = SplashState.READY,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    state = SplashState.ERROR,
                    errorMessage = e.message ?: "Errore sconosciuto"
                )
            }
        }
    }

    fun retry() {
        initializeSplash()
    }
}