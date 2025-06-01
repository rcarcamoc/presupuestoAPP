package com.aranthalion.presupuesto.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        // Verificar si hay una sesión activa al iniciar
        val account = GoogleSignIn.getLastSignedInAccount(context)
        account?.let {
            _userEmail.value = it.email
            _userName.value = it.displayName
            AppLogger.i("Sesión existente encontrada: ${it.email}")
        }
    }
    
    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
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
            } catch (e: Exception) {
                AppLogger.e("Error al procesar el inicio de sesión", e)
                _error.value = "Error al procesar el inicio de sesión: ${e.message}"
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                getSignInClient().signOut()
                _userEmail.value = null
                _userName.value = null
                AppLogger.i("Usuario cerrado sesión")
            } catch (e: Exception) {
                AppLogger.e("Error al cerrar sesión", e)
                _error.value = "Error al cerrar sesión: ${e.message}"
            }
        }
    }
} 