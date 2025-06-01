package com.aranthalion.presupuesto.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.R
import com.aranthalion.presupuesto.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId
    
    private val _gmailAccessToken = MutableStateFlow<String?>(null)
    val gmailAccessToken: StateFlow<String?> = _gmailAccessToken
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        // Verificar si hay una sesión activa al iniciar
        val account = GoogleSignIn.getLastSignedInAccount(context)
        account?.let {
            _userEmail.value = it.email
            _userName.value = it.displayName
            _userId.value = it.id
            AppLogger.i("Sesión existente encontrada: ${it.email}")
            
            // Verificar si tenemos acceso a Gmail
            if (it.grantedScopes.contains(Scope(GmailScopes.GMAIL_READONLY))) {
                AppLogger.i("Acceso a Gmail ya concedido")
                _gmailAccessToken.value = "Acceso concedido"
            }
        }
    }
    
    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestId()
            .requestScopes(Scope(GmailScopes.GMAIL_READONLY))
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    fun isUserSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null
    }
    
    fun handleSignInResult(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                AppLogger.i("Usuario autenticado: ${account.email}")
                _userEmail.value = account.email
                _userName.value = account.displayName
                _userId.value = account.id
                
                // Verificar acceso a Gmail
                if (account.grantedScopes.contains(Scope(GmailScopes.GMAIL_READONLY))) {
                    AppLogger.i("Acceso a Gmail concedido")
                    _gmailAccessToken.value = "Acceso concedido"
                } else {
                    AppLogger.w("Acceso a Gmail no concedido")
                    _gmailAccessToken.value = "Acceso no concedido"
                }
                
                // Obtener el token ID
                account.idToken?.let { token ->
                    AppLogger.d("Token ID obtenido: ${token.take(10)}...")
                }
            } catch (e: Exception) {
                AppLogger.e("Error al procesar el inicio de sesión", e)
                _error.value = "Error al procesar el inicio de sesión: ${e.message}"
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                // Primero revocar el acceso
                getSignInClient().revokeAccess().addOnCompleteListener {
                    // Luego cerrar sesión
                    getSignInClient().signOut()
                    _userEmail.value = null
                    _userName.value = null
                    _userId.value = null
                    _gmailAccessToken.value = null
                    AppLogger.i("Usuario cerrado sesión y acceso revocado")
                }
            } catch (e: Exception) {
                AppLogger.e("Error al cerrar sesión", e)
                _error.value = "Error al cerrar sesión: ${e.message}"
            }
        }
    }
} 