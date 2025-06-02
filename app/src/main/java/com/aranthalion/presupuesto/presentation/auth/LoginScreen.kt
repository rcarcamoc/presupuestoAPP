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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val errorViewModel by viewModel.error.collectAsState()

    val userEmail by viewModel.userEmail.collectAsState()

    var emailInput by remember { mutableStateOf("usuario@cock.li") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var serverType by remember { mutableStateOf("IMAP") }
    var serverAddress by remember { mutableStateOf("mail.cock.li") }
    var serverPort by remember { mutableStateof("993") }
    var encryptionType by remember { mutableStateOf("SSL/TLS") }

    val serverTypes = listOf("IMAP", "POP3")
    val encryptionTypes = listOf("SSL/TLS", "STARTTLS", "None")

    LaunchedEffect(userEmail) {
        AppLogger.d("LoginScreen: userEmail state changed to: $userEmail")
        if (userEmail != null) {
            AppLogger.i("LoginScreen: Usuario autenticado ($userEmail), navegando a onLoginSuccess.")
            onLoginSuccess()
        } else {
            AppLogger.d("LoginScreen: Usuario no está autenticado.")
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Tipo:", modifier = Modifier.weight(1f))
                serverTypes.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (type == serverType),
                            onClick = { 
                                serverType = type
                                if (type == "IMAP") {
                                    serverPort = "993"
                                    serverAddress = "mail.cock.li"
                                } else {
                                    serverPort = "995"
                                    serverAddress = "mail.cock.li"
                                }
                            }
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
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("Puerto del Servidor") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            var expandedEncryption by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = encryptionType,
                    onValueChange = { },
                    label = { Text("Tipo de Cifrado") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEncryption) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(
                    expanded = expandedEncryption,
                    onDismissRequest = { expandedEncryption = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    encryptionTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                encryptionType = selectionOption
                                expandedEncryption = false
                            }
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
                        isLoading = true
                        showError = false
                        viewModel.loginWithEmailProvider(
                            email = emailInput,
                            password = passwordInput,
                            serverType = serverType,
                            serverAddress = serverAddress,
                            port = serverPort.toIntOrNull() ?: if (serverType == "IMAP") 993 else 995,
                            encryption = encryptionType
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = emailInput.isNotBlank() && passwordInput.isNotBlank() && serverAddress.isNotBlank() && serverPort.isNotBlank()
                ) {
                    Text("Conectar y Contar Correos")
                }
            }

            errorViewModel?.let { errorMsg ->
                 Text(
                    text = "Error: $errorMsg",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (showError) {
                // Esta sección podría usarse para errores de validación de UI antes de llamar al ViewModel
                // Por ahora, el error del ViewModel es el principal.
            }
        }
    }
} 