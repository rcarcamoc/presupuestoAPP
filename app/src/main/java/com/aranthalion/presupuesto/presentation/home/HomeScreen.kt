package com.aranthalion.presupuesto.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aranthalion.presupuesto.R
import com.aranthalion.presupuesto.presentation.auth.AuthViewModel
import com.aranthalion.presupuesto.util.AppLogger

@Composable
fun HomeScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit // Callback para navegar al login si no hay sesión
) {
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val unreadEmailCount by viewModel.unreadEmailCount.collectAsState()
    val error by viewModel.error.collectAsState()

    // Si el email del usuario es nulo después de la composición inicial y no hay error,
    // podría significar que el usuario no está logueado y deberíamos volver a la pantalla de login.
    LaunchedEffect(userEmail, error) {
        AppLogger.d("HomeScreen: userEmail: $userEmail, error: $error")
        if (userEmail == null && error == null) {
             // Considerar si hay un estado de "cargando" o si esto es un signo de que no hay sesión.
             // Por ahora, si no hay email y no hay error, asumimos que se debe volver a login.
             // Esto puede necesitar ajuste si hay un estado de carga inicial.
            AppLogger.d("HomeScreen: No user email and no error, navigating to login.")
            // onNavigateToLogin() // Descomentar si se quiere este comportamiento
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido: ${userName ?: "Usuario"}",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Correo: ${userEmail ?: "No disponible"}",
                style = MaterialTheme.typography.bodyLarge
            )

            unreadEmailCount?.let {
                val message = if (it > 0) {
                    stringResource(id = R.string.unread_emails_message, it)
                } else {
                    stringResource(id = R.string.no_unread_emails_message)
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            error?.let {
                Text(
                    text = stringResource(id = R.string.error_connecting_server, it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Button(
                onClick = {
                    AppLogger.d("Botón de cerrar sesión presionado")
                    viewModel.signOut()
                    // Después de signOut, userEmail será null, y el LaunchedEffect de arriba
                    // (si está activo y configurado) podría redirigir a Login.
                    // O podrías llamar a onNavigateToLogin() directamente si es preferible.
                    onNavigateToLogin() // Forzar navegación a login al cerrar sesión
                }
            ) {
                Text("Cerrar sesión")
            }
        }
    }
} 