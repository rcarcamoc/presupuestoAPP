package com.aranthalion.presupuesto.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aranthalion.presupuesto.util.AnalyticsLogger
import com.aranthalion.presupuesto.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val userEmail by viewModel.userEmail.collectAsState()
    
    LaunchedEffect(userEmail) {
        if (userEmail != null) {
            AnalyticsLogger.logLoginEvent(true, "google")
            onNavigateToHome()
        }
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    viewModel.handleSignInResult(account)
                    AnalyticsLogger.logLoginEvent(true, "google")
                } else {
                    errorMessage = "No se pudo obtener la cuenta de Google."
                    AnalyticsLogger.logLoginEvent(false, "google")
                }
            } catch (e: ApiException) {
                errorMessage = "Error de inicio de sesión con Google: ${e.statusCode}"
                AppLogger.e("Google Sign-In error", e)
                AnalyticsLogger.logLoginEvent(false, "google")
            }
        } else {
            errorMessage = "Inicio de sesión con Google cancelado o fallido."
            if (result.resultCode != Activity.RESULT_CANCELED) {
                AnalyticsLogger.logLoginEvent(false, "google")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    val signInClient = viewModel.getSignInClient()
                    launcher.launch(signInClient.signInIntent)
                }
            ) {
                Text("Iniciar sesión con Google")
            }
        }
        
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
} 