package com.rix.womblab.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rix.womblab.domain.model.User
import com.rix.womblab.domain.model.UserPreferences
import com.rix.womblab.domain.usecase.auth.GetCurrentUserUseCase
import com.rix.womblab.domain.usecase.auth.UpdateUserProfileUseCase
import com.rix.womblab.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val firstName: String = "",
    val lastName: String = "",
    val profession: String = "",
    val specialization: String = "",
    val workplace: String = "",
    val city: String = "",
    val phone: String = "",
    val wantsNewsletter: Boolean = true,
    val wantsNotifications: Boolean = true,
    val error: String? = null,
    val isRegistrationComplete: Boolean = false
) {
    val isFormValid: Boolean
        get() = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                profession.isNotBlank()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        setupCurrentUser()
    }

    private fun setupCurrentUser() {
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

                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        // Pre-populate fields if available
                        firstName = extractFirstName(user.displayName),
                        lastName = extractLastName(user.displayName)
                    )
                }
            }
        }
    }

    private fun extractFirstName(displayName: String): String {
        return displayName.split(" ").firstOrNull() ?: ""
    }

    private fun extractLastName(displayName: String): String {
        val parts = displayName.split(" ")
        return if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
    }

    fun onFirstNameChange(firstName: String) {
        _uiState.value = _uiState.value.copy(firstName = firstName)
    }

    fun onLastNameChange(lastName: String) {
        _uiState.value = _uiState.value.copy(lastName = lastName)
    }

    fun onProfessionChange(profession: String) {
        _uiState.value = _uiState.value.copy(profession = profession)
    }

    fun onSpecializationChange(specialization: String) {
        _uiState.value = _uiState.value.copy(specialization = specialization)
    }

    fun onWorkplaceChange(workplace: String) {
        _uiState.value = _uiState.value.copy(workplace = workplace)
    }

    fun onCityChange(city: String) {
        _uiState.value = _uiState.value.copy(city = city)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun onNewsletterChange(wantsNewsletter: Boolean) {
        _uiState.value = _uiState.value.copy(wantsNewsletter = wantsNewsletter)
    }

    fun onNotificationsChange(wantsNotifications: Boolean) {
        _uiState.value = _uiState.value.copy(wantsNotifications = wantsNotifications)
    }

    fun completeRegistration() {
        val currentState = _uiState.value

        if (!currentState.isFormValid) {
            _uiState.value = currentState.copy(
                error = "Compila tutti i campi obbligatori"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            val updatedUser = currentState.currentUser?.copy(
                displayName = "${currentState.firstName} ${currentState.lastName}",
                preferences = UserPreferences(
                    notificationsEnabled = currentState.wantsNotifications,
                    emailNotifications = currentState.wantsNewsletter,
                    pushNotifications = currentState.wantsNotifications,
                    favoriteCategories = if (currentState.profession.isNotBlank()) {
                        listOf(currentState.profession)
                    } else {
                        emptyList()
                    }
                )
            )

            if (updatedUser != null) {
                when (val result = updateUserProfileUseCase(updatedUser, currentState.toUserProfile())) {
                    is Resource.Success -> {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isRegistrationComplete = true,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = result.message ?: "Errore durante la registrazione"
                        )
                    }
                    is Resource.Loading -> {
                        // Already handled above
                    }
                }
            } else {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Errore: utente non trovato"
                )
            }
        }
    }

    private fun RegisterUiState.toUserProfile(): UserProfile {
        return UserProfile(
            firstName = firstName,
            lastName = lastName,
            profession = profession,
            specialization = specialization.takeIf { it.isNotBlank() },
            workplace = workplace.takeIf { it.isNotBlank() },
            city = city.takeIf { it.isNotBlank() },
            phone = phone.takeIf { it.isNotBlank() },
            wantsNewsletter = wantsNewsletter,
            wantsNotifications = wantsNotifications
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val profession: String,
    val specialization: String? = null,
    val workplace: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val wantsNewsletter: Boolean = true,
    val wantsNotifications: Boolean = true
)