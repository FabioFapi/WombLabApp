package com.rix.womblab.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rix.womblab.domain.model.User
import com.rix.womblab.domain.repository.AuthRepository
import com.rix.womblab.presentation.auth.register.UserProfile
import com.rix.womblab.utils.PreferencesUtils
import com.rix.womblab.utils.Resource as WombLabResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val preferencesUtils: PreferencesUtils
) : AuthRepository {

    private fun createGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("327632805229-8jmrm0qn9mgvqganr31p1blih1cdpp9t.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun signInWithGoogle(account: GoogleSignInAccount): WombLabResource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    isEmailVerified = firebaseUser.isEmailVerified
                )

                preferencesUtils.clearForcedLogout()
                preferencesUtils.setUserId(firebaseUser.uid)

                WombLabResource.Success(user)
            } else {
                WombLabResource.Error("Login fallito")
            }
        } catch (e: Exception) {
            WombLabResource.Error(e.message ?: "Errore sconosciuto")
        }
    }

    override fun getGoogleSignInClient(): GoogleSignInClient = createGoogleSignInClient()

    override suspend fun getSignedInAccountFromIntent(data: android.content.Intent?): GoogleSignInAccount? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signOut(): WombLabResource<Unit> {
        return try {
            firebaseAuth.signOut()
            createGoogleSignInClient().signOut().await()
            preferencesUtils.clearAll()

            try {
                context.cacheDir.deleteRecursively()
            } catch (e: Exception) {

            }

            WombLabResource.Success(Unit)
        } catch (e: Exception) {
            WombLabResource.Error(e.message ?: "Errore durante il logout")
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun updateUserProfile(user: User, profile: UserProfile): WombLabResource<User> {
        return try {
            preferencesUtils.setUserProfile(profile)
            preferencesUtils.setRegistrationCompleted(true)

            val updatedUser = user.copy(
                displayName = "${profile.firstName} ${profile.lastName}"
            )

            WombLabResource.Success(updatedUser)
        } catch (e: Exception) {
            WombLabResource.Error(e.message ?: "Errore durante l'aggiornamento del profilo")
        }
    }

    override suspend fun getUserProfile(userId: String): WombLabResource<UserProfile?> {
        return try {
            val profile = preferencesUtils.getUserProfile()
            WombLabResource.Success(profile)
        } catch (e: Exception) {
            WombLabResource.Error(e.message ?: "Errore durante il recupero del profilo")
        }
    }

    override suspend fun isRegistrationCompleted(userId: String): Boolean {
        return preferencesUtils.isRegistrationCompleted()
    }

    override suspend fun setRegistrationCompleted(userId: String): WombLabResource<Unit> {
        return try {
            preferencesUtils.setRegistrationCompleted(true)
            WombLabResource.Success(Unit)
        } catch (e: Exception) {
            WombLabResource.Error(e.message ?: "Errore durante l'aggiornamento dello stato di registrazione")
        }
    }
}