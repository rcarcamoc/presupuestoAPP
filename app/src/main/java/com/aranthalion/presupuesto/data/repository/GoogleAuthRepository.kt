package com.aranthalion.presupuesto.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes
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
            .requestScopes(
                Scope(Scopes.EMAIL),
                Scope(Scopes.PROFILE),
                Scope(GmailScopes.GMAIL_READONLY)
            )
            .build()
        
        GoogleSignIn.getClient(context, gso).also {
            Log.d(TAG, "GoogleSignInClient inicializado con todos los scopes necesarios")
        }
    }

    fun getSignInClient(): GoogleSignInClient = googleSignInClient.also {
        Log.d(TAG, "Obteniendo GoogleSignInClient")
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context).also { account ->
            Log.d(TAG, "Última cuenta con sesión iniciada: ${account?.email}")
        }

    fun isUserSignedIn(): Boolean {
        val account = getLastSignedInAccount()
        return account?.let { acc ->
            Log.d(TAG, "Verificando sesión para: ${acc.email}")
            val hasRequiredScopes = GoogleSignIn.hasPermissions(
                acc,
                Scope(Scopes.EMAIL),
                Scope(Scopes.PROFILE),
                Scope(GmailScopes.GMAIL_READONLY)
            )
            val hasTokens = acc.idToken != null && acc.serverAuthCode != null
            
            if (!hasRequiredScopes) {
                Log.w(TAG, "Usuario no tiene todos los permisos necesarios")
                false
            } else if (!hasTokens) {
                Log.w(TAG, "Usuario no tiene los tokens necesarios")
                false
            } else {
                Log.d(TAG, "Usuario tiene sesión válida con todos los permisos")
                true
            }
        } ?: false
    }

    suspend fun signOut() {
        try {
            Log.d(TAG, "Cerrando sesión")
            googleSignInClient.revokeAccess().await()
            googleSignInClient.signOut().await()
            Log.d(TAG, "Sesión cerrada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            throw Exception("Error al cerrar sesión: ${e.message}")
        }
    }

    suspend fun handleSignInResult(account: GoogleSignInAccount) {
        try {
            Log.d(TAG, "Procesando resultado de inicio de sesión para: ${account.email}")
            
            // Verificar tokens
            if (account.idToken == null || account.serverAuthCode == null) {
                Log.e(TAG, "Faltan tokens necesarios")
                throw Exception("No se obtuvieron los tokens necesarios. Por favor, intente nuevamente.")
            }

            // Verificar permisos
            if (!GoogleSignIn.hasPermissions(
                account,
                Scope(Scopes.EMAIL),
                Scope(Scopes.PROFILE),
                Scope(GmailScopes.GMAIL_READONLY)
            )) {
                Log.e(TAG, "Faltan permisos necesarios")
                googleSignInClient.signOut().await()
                throw Exception("No se obtuvieron todos los permisos necesarios. Por favor, intente nuevamente.")
            }

            Log.d(TAG, "Inicio de sesión exitoso con todos los permisos y tokens")
        } catch (e: ApiException) {
            Log.e(TAG, "Error de API de Google al procesar inicio de sesión", e)
            throw Exception("Error al procesar la autenticación: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar inicio de sesión", e)
            throw e
        }
    }
} 