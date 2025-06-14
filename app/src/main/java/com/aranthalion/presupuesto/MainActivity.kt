package com.aranthalion.presupuesto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aranthalion.presupuesto.presentation.auth.AuthViewModel
import com.aranthalion.presupuesto.presentation.auth.LoginScreen
import com.aranthalion.presupuesto.presentation.home.HomeScreen
import com.aranthalion.presupuesto.ui.theme.PresupuestoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PresupuestoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        // Botón de prueba de crash
                        Button(
                            onClick = { 
                                throw RuntimeException("Test Crash") // Forzar un crash
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Test Crash")
                        }
                        
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // val authViewModel: AuthViewModel = hiltViewModel() // Todavía necesario si HomeScreen lo usa indirectamente o para otras cosas
    // val userEmail by authViewModel.userEmail.collectAsState() // Eliminado para resolver la advertencia
    
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
} 