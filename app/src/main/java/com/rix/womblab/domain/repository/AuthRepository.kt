package com.rix.womblab.domain.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import com.rix.womblab.domain.model.User
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
}