package com.aranthalion.presupuesto.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoogleAuthRepository"

@Singleton
class GoogleAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestId()
            .requestIdToken(context.getString(com.aranthalion.presupuesto.R.string.server_client_id))
            .requestServerAuthCode(context.getString(com.aranthalion.presupuesto.R.string.server_client_id))
            .requestScopes(Scope(Scopes.EMAIL))
            .requestScopes(Scope(Scopes.PROFILE))
            .build()
        
        GoogleSignIn.getClient(context, gso).also {
            Log.d(TAG, "GoogleSignInClient initialized with all scopes")
        }
    }

    fun getSignInClient(): GoogleSignInClient = googleSignInClient.also {
        Log.d(TAG, "Getting GoogleSignInClient")
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context).also { account ->
            Log.d(TAG, "Last signed in account: ${account?.email}")
        }

    fun isUserSignedIn(): Boolean =
        getLastSignedInAccount()?.let { account ->
            Log.d(TAG, "Checking if user is signed in with email: ${account.email}")
            account.idToken != null && account.serverAuthCode != null
        } ?: false

    suspend fun signOut() {
        try {
            Log.d(TAG, "Signing out")
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            throw Exception("Error al cerrar sesión: ${e.message}")
        }
    }

    suspend fun handleSignInResult(account: GoogleSignInAccount) {
        try {
            Log.d(TAG, "Handling sign in result for account: ${account.email}")
            if (account.idToken == null || account.serverAuthCode == null) {
                Log.e(TAG, "Missing token or auth code")
        googleSignInClient.signOut().await()
                throw Exception("No se obtuvieron los permisos necesarios. Por favor, inténtalo de nuevo.")
            }
            Log.d(TAG, "Sign in successful with token and auth code")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling sign in result", e)
            throw Exception("Error al procesar la autenticación: ${e.message}")
        }
    }
} 