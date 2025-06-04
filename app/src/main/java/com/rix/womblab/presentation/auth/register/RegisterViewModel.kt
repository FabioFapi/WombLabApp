package com.rix.womblab.presentation.auth.register

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.User
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.auth.LoginUseCase
import com.rix.womblab.domain.usecase.auth.UpdateUserProfileUseCase
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val isRegistrationComplete: Boolean = false,
    val registrationMethod: String? = null,
    val needsProfileCompletion: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
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
                    } catch (_: Exception) {
                        false
                    }

                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        needsProfileCompletion = !isRegistrationCompleted
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentUser = null,
                        needsProfileCompletion = false
                    )
                }
            }
        }
    }

    fun signUpWithEmail(firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                registrationMethod = "email"
            )

            try {
                when (val result = authRepository.signUpWithEmail(firstName, lastName, email, password)) {
                    is Resource.Success -> {
                        val user = result.data!!

                        val userProfile = UserProfile(
                            firstName = firstName,
                            lastName = lastName,
                            profession = "Professionista Sanitario",
                            specialization = null,
                            workplace = null,
                            city = null,
                            phone = null,
                            wantsNewsletter = true,
                            wantsNotifications = true
                        )

                        when (val profileResult = updateUserProfileUseCase(user, userProfile)) {
                            is Resource.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    currentUser = user,
                                    isRegistrationComplete = true,
                                    registrationMethod = null
                                )
                            }
                            is Resource.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = profileResult.message ?: "Errore nel salvataggio del profilo",
                                    registrationMethod = null
                                )
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message ?: "Errore durante la registrazione",
                            registrationMethod = null
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Errore durante la registrazione: ${e.message}",
                    registrationMethod = null
                )
            }
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInClient().signInIntent
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                registrationMethod = "google"
            )

            try {
                val account = authRepository.getSignedInAccountFromIntent(data)
                if (account != null) {
                    when (val result = loginUseCase(account)) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentUser = result.data,
                                isRegistrationComplete = true,
                                registrationMethod = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message ?: "Errore durante la registrazione con Google",
                                registrationMethod = null
                            )
                        }
                        is Resource.Loading -> {
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Registrazione Google annullata o fallita",
                        registrationMethod = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Errore durante la registrazione: ${e.message}",
                    registrationMethod = null
                )
            }
        }
    }

}