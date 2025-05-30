package com.rix.womblab.domain.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import com.rix.womblab.domain.model.User
import com.rix.womblab.presentation.auth.register.UserProfile
import com.rix.womblab.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    fun getCurrentUser(): FirebaseUser?
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Resource<User>
    fun getGoogleSignInClient(): GoogleSignInClient
    suspend fun getSignedInAccountFromIntent(data: Intent?): GoogleSignInAccount?
    suspend fun signOut(): Resource<Unit>
    fun isUserLoggedIn(): Boolean
    suspend fun updateUserProfile(user: User, profile: UserProfile): Resource<User>
    suspend fun getUserProfile(userId: String): Resource<UserProfile?>
    suspend fun isRegistrationCompleted(userId: String): Boolean
    suspend fun setRegistrationCompleted(userId: String): Resource<Unit>
    suspend fun signInWithEmail(email: String, password: String): Resource<User>
    suspend fun signUpWithEmail(firstName: String, lastName: String, email: String, password: String): Resource<User>
}