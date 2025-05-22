package com.rix.womblab.domain.usecase.auth

import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return authRepository.signOut()
    }
}