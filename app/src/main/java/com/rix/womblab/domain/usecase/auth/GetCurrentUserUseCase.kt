package com.rix.womblab.domain.usecase.auth

import com.google.firebase.auth.FirebaseUser
import com.rix.womblab.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<FirebaseUser?> {
        return authRepository.currentUser
    }

    fun getCurrentUserSync(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}