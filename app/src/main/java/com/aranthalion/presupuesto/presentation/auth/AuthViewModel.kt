package com.aranthalion.presupuesto.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.data.repository.GoogleAuthRepository
import com.aranthalion.presupuesto.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleAuthRepository: GoogleAuthRepository
) : ViewModel() {
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        checkExistingSession()
    }
    
    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (googleAuthRepository.isUserSignedIn()) {
                    val account = googleAuthRepository.getLastSignedInAccount()
                    account?.let {
                        updateUserInfo(it)
                        AppLogger.i("Sesión existente recuperada: ${it.email}")
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("Error al verificar sesión existente", e)
                _error.value = "Error al verificar la sesión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getSignInClient(): GoogleSignInClient {
        return googleAuthRepository.getSignInClient()
    }
    
    fun handleSignInResult(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                googleAuthRepository.handleSignInResult(account)
                updateUserInfo(account)
                
                AppLogger.i("Usuario autenticado exitosamente: ${account.email}")
            } catch (e: Exception) {
                AppLogger.e("Error en el proceso de inicio de sesión", e)
                _error.value = e.message
                clearUserInfo()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                googleAuthRepository.signOut()
                clearUserInfo()
                AppLogger.i("Usuario cerró sesión exitosamente")
            } catch (e: Exception) {
                AppLogger.e("Error al cerrar sesión", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun updateUserInfo(account: GoogleSignInAccount) {
        _userEmail.value = account.email
        _userName.value = account.displayName
        _userId.value = account.id
    }
    
    private fun clearUserInfo() {
        _userEmail.value = null
        _userName.value = null
        _userId.value = null
    }
} 