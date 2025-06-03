package com.aranthalion.presupuesto.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aranthalion.presupuesto.util.AppLogger
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.aranthalion.presupuesto.data.repository.EmailConnectionDetails
import kotlinx.coroutines.launch

private fun getDefaultPortFor(serverType: String, encryptionType: String): String {
    return when (serverType) {
        "IMAP" -> when (encryptionType) {
            "SSL/TLS" -> "993"
            "STARTTLS" -> "143"
            "None" -> "143"
            else -> "993"
        }
        "POP3" -> when (encryptionType) {
            "SSL/TLS" -> "995"
            "STARTTLS" -> "110"
            "None" -> "110"
            else -> "995"
        }
        else -> "993"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Usar el isLoading del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val errorViewModel by viewModel.error.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Estados para los campos del formulario
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var serverTypeState by remember { mutableStateOf("IMAP") }
    var encryptionTypeState by remember { mutableStateOf("SSL/TLS") }
    var serverAddress by remember { mutableStateOf("mail.cock.li") }
    var serverPort by remember { mutableStateOf(getDefaultPortFor(serverTypeState, encryptionTypeState)) }
    
    // Cargar preferencias al iniciar la pantalla
    LaunchedEffect(Unit) {
        AppLogger.d("LoginScreen: LaunchedEffect(Unit) - Cargando preferencias.")
        coroutineScope.launch {
            viewModel.loadLastConnectionDetails()?.let {
                AppLogger.i("LoginScreen: Preferencias cargadas: $it")
                emailInput = it.email
                serverAddress = it.serverAddress
                serverPort = it.serverPort
                serverTypeState = it.serverType
                encryptionTypeState = it.encryptionType
                // La contraseña no se guarda/carga por seguridad
            } ?: run {
                AppLogger.d("LoginScreen: No hay preferencias guardadas, usando valores por defecto cock.li")
                // Si no hay nada guardado, asegurar valores por defecto de cock.li
                emailInput = "usuario@cock.li"
                serverAddress = "mail.cock.li"
                serverTypeState = "IMAP"
                encryptionTypeState = "SSL/TLS"
                serverPort = getDefaultPortFor(serverTypeState, encryptionTypeState)
            }
        }
    }

    // Actualizar puerto cuando serverType o encryptionType cambien
    LaunchedEffect(serverTypeState, encryptionTypeState) {
        serverPort = getDefaultPortFor(serverTypeState, encryptionTypeState)
        AppLogger.d("LoginScreen: serverType o encryptionType cambiado. Nuevo puerto: $serverPort para $serverTypeState y $encryptionTypeState")
    }

    // Navegar al éxito del login
    LaunchedEffect(userEmail) {
        if (userEmail != null) {
            AppLogger.i("LoginScreen: Usuario autenticado ($userEmail), navegando a onLoginSuccess.")
            onLoginSuccess()
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configuración de Correo", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Correo Electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(imageVector  = image, description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Tipo:", modifier = Modifier.weight(1f))
                serverTypes.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (type == serverTypeState),
                            onClick = { serverTypeState = type },
                            enabled = !isLoading
                        )
                        Text(text = type)
                    }
                }
            }

            OutlinedTextField(
                value = serverAddress,
                onValueChange = { serverAddress = it },
                label = { Text("Dirección del Servidor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it }, 
                label = { Text("Puerto del Servidor") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            var expandedEncryption by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedEncryption,
                onExpandedChange = { if (!isLoading) expandedEncryption = !expandedEncryption },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = encryptionTypeState,
                    onValueChange = {},
                    label = { Text("Tipo de Cifrado") },
                    readOnly = true,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEncryption) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(
                    expanded = expandedEncryption,
                    onDismissRequest = { expandedEncryption = false }
                ) {
                    encryptionTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                encryptionTypeState = selectionOption
                                expandedEncryption = false
                            },
                            enabled = !isLoading
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        AppLogger.i("LoginScreen: Botón 'Conectar y Contar Correos' presionado.")
                        val detailsToSave = EmailConnectionDetails(
                            email = emailInput,
                            serverAddress = serverAddress,
                            serverPort = serverPort,
                            serverType = serverTypeState,
                            encryptionType = encryptionTypeState
                        )
                        coroutineScope.launch {
                            viewModel.saveLastConnectionDetails(detailsToSave)
                            AppLogger.d("LoginScreen: Detalles guardados: $detailsToSave")
                            viewModel.loginWithEmailProvider(
                                email = emailInput,
                                password = passwordInput, // La contraseña no se guarda, se toma del campo actual
                                serverType = serverTypeState,
                                serverAddress = serverAddress,
                                port = serverPort.toIntOrNull() ?: getDefaultPortFor(serverTypeState, encryptionTypeState).toInt(),
                                encryption = encryptionTypeState
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = emailInput.isNotBlank() && passwordInput.isNotBlank() && serverAddress.isNotBlank() && serverPort.isNotBlank() && !isLoading
                ) {
                    Text("Conectar y Contar Correos")
                }
            }

            errorViewModel?.let {
                 Text(
                    text = "Error: $errorMsg",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
} 