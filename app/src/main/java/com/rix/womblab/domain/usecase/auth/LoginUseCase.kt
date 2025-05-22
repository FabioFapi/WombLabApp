package com.rix.womblab.domain.usecase.auth

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.rix.womblab.domain.model.User
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.utils.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(account: GoogleSignInAccount): Resource<User> {
        return authRepository.signInWithGoogle(account)
    }
}