package com.aranthalion.presupuesto.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aranthalion.presupuesto.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val error by viewModel.error.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    LaunchedEffect(Unit) {
        AppLogger.d("LoginScreen launched")
        
        if (viewModel.isUserSignedIn()) {
            AppLogger.d("Usuario ya está autenticado")
            onLoginSuccess()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        AppLogger.d("Resultado de actividad recibido: ${result.resultCode}")
        
        if (result.resultCode == Activity.RESULT_OK) {
            isLoading = true
            showError = false
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.handleSignInResult(account)
                onLoginSuccess()
            } catch (e: ApiException) {
                AppLogger.e("Error en inicio de sesión", e)
                showError = true
                errorMessage = "Error al iniciar sesión: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            AppLogger.w("Inicio de sesión cancelado por el usuario")
            showError = true
            errorMessage = "Inicio de sesión cancelado"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        AppLogger.d("Botón de inicio de sesión presionado")
                        showError = false
                        launcher.launch(viewModel.getSignInClient().signInIntent)
                    }
                ) {
                    Text("Iniciar sesión con Google")
                }
            }

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 