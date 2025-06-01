package com.aranthalion.presupuesto.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aranthalion.presupuesto.presentation.auth.AuthViewModel
import com.aranthalion.presupuesto.util.AppLogger

@Composable
fun HomeScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val userEmail by viewModel.userEmail.collectAsState()
    
    LaunchedEffect(Unit) {
        AppLogger.d("HomeScreen launched")
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido: ${userEmail ?: "Usuario"}",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(
                onClick = {
                    AppLogger.d("Botón de cerrar sesión presionado")
                    viewModel.signOut()
                }
            ) {
                Text("Cerrar sesión")
            }
        }
    }
} 