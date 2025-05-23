package com.rix.womblab.domain.usecase.auth

import com.rix.womblab.domain.model.User
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.presentation.auth.register.UserProfile
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(user: User, profile: UserProfile): Resource<User> {
        return try {

            val result = authRepository.updateUserProfile(user, profile)

            when (result) {
                is Resource.Success -> {
                    result
                }
                is Resource.Error -> {
                    result
                }
                is Resource.Loading -> {
                    result
                }
            }
        } catch (e: Exception) {
            Resource.Error("Errore durante l'aggiornamento del profilo: ${e.message}")
        }
    }
}