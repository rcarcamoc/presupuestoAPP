package com.aranthalion.presupuesto.presentation.auth

import androidx.lifecycle.ViewModel
import com.aranthalion.presupuesto.data.repository.GoogleAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository
) : ViewModel() {
    
    fun getSignInClient(): GoogleSignInClient = authRepository.getSignInClient()
    
    fun isUserSignedIn(): Boolean = authRepository.isUserSignedIn()
    
    suspend fun signOut() = authRepository.signOut()
} 