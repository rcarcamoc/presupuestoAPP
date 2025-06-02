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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aranthalion.presupuesto.util.AnalyticsLogger
import com.aranthalion.presupuesto.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
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
                    errorMessage = "No se pudo obtener la cuenta de Google. Por favor, intente nuevamente."
                    AppLogger.e("Google Sign-In error: account null")
                    AnalyticsLogger.logLoginEvent(false, "google")
                }
            } catch (e: ApiException) {
                errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Inicio de sesión cancelado por el usuario."
                    GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Ya hay un inicio de sesión en progreso."
                    GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Error en el inicio de sesión. Por favor, verifique su conexión a internet."
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Error de red. Por favor, verifique su conexión a internet."
                    GoogleSignInStatusCodes.INVALID_ACCOUNT -> "Cuenta inválida. Por favor, seleccione otra cuenta."
                    GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Se requiere iniciar sesión nuevamente."
                    else -> "Error de inicio de sesión con Google (${e.statusCode}). Por favor, intente nuevamente."
                }
                AppLogger.e("Google Sign-In error", e)
                AnalyticsLogger.logLoginEvent(false, "google")
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            errorMessage = "Inicio de sesión cancelado por el usuario."
            AppLogger.i("Google Sign-In cancelled by user")
        } else {
            errorMessage = "Error inesperado durante el inicio de sesión. Por favor, intente nuevamente."
            AppLogger.e("Google Sign-In unexpected result code: ${result.resultCode}")
            AnalyticsLogger.logLoginEvent(false, "google")
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
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
} 