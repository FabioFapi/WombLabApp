package com.rix.womblab.presentation.auth.login

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.auth.LoginUseCase
import com.rix.womblab.domain.usecase.auth.LogoutUseCase
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.domain.model.User
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isRegistrationComplete: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true)

            getCurrentUserUseCase().collect { firebaseUser ->
                if (firebaseUser != null) {
                    val user = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        isEmailVerified = firebaseUser.isEmailVerified
                    )

                    val isRegistrationCompleted = try {
                        authRepository.isRegistrationCompleted(firebaseUser.uid)
                    } catch (e: Exception) {
                        false
                    }

                    _loginState.value = LoginState(
                        isLoading = false,
                        user = user,
                        isLoggedIn = true,
                        isRegistrationComplete = isRegistrationCompleted,
                        error = null
                    )
                } else {
                    _loginState.value = LoginState(
                        isLoading = false,
                        user = null,
                        isLoggedIn = false,
                        isRegistrationComplete = false,
                        error = null
                    )
                }
            }
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInClient().signInIntent
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)

            try {
                val account = authRepository.getSignedInAccountFromIntent(data)
                if (account != null) {
                    when (val result = loginUseCase(account)) {
                        is Resource.Success -> {
                            val user = result.data!!

                            val isRegistrationCompleted = try {
                                authRepository.isRegistrationCompleted(user.id)
                            } catch (e: Exception) {
                                false
                            }

                            _loginState.value = LoginState(
                                isLoading = false,
                                user = user,
                                isLoggedIn = true,
                                isRegistrationComplete = isRegistrationCompleted,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _loginState.value = LoginState(
                                isLoading = false,
                                user = null,
                                isLoggedIn = false,
                                isRegistrationComplete = false,
                                error = result.message ?: "Errore durante il login con Google"
                            )
                        }
                        is Resource.Loading -> {
                        }
                    }
                } else {
                    _loginState.value = LoginState(
                        isLoading = false,
                        user = null,
                        isLoggedIn = false,
                        isRegistrationComplete = false,
                        error = "Login Google annullato o fallito"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState(
                    isLoading = false,
                    user = null,
                    isLoggedIn = false,
                    isRegistrationComplete = false,
                    error = "Errore durante il login: ${e.message}"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true)

            when (val result = logoutUseCase()) {
                is Resource.Success -> {
                    _loginState.value = LoginState(
                        isLoading = false,
                        user = null,
                        isLoggedIn = false,
                        isRegistrationComplete = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Errore durante il logout"
                    )
                }
                is Resource.Loading -> {
                }
            }
        }
    }

    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
    }
}