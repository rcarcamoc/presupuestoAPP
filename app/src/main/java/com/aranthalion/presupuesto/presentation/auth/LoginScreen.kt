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

// --- Definiciones de Preajustes ---
data class ServerPreset(
    val displayName: String,
    val serverAddress: String,
    val serverPort: String,
    val serverType: String, // IMAP o POP3
    val encryptionType: String // SSL/TLS, STARTTLS, None
)

val serverPresets = listOf(
    ServerPreset("Gmail (IMAP)", "imap.gmail.com", "993", "IMAP", "SSL/TLS"),
    ServerPreset("Gmail (POP3)", "pop.gmail.com", "995", "POP3", "SSL/TLS"),
    ServerPreset("Cock.li (IMAP - SSL/TLS)", "mail.cock.li", "993", "IMAP", "SSL/TLS"),
    ServerPreset("Cock.li (POP3 - SSL/TLS)", "mail.cock.li", "995", "POP3", "SSL/TLS"),
    ServerPreset("Cock.li (IMAP - STARTTLS)", "mail.cock.li", "143", "IMAP", "STARTTLS"),
    ServerPreset("Cock.li (POP3 - STARTTLS)", "mail.cock.li", "110", "POP3", "STARTTLS"),
    // Podríamos añadir una opción para "Manual" o "Personalizado" si quisiéramos limpiar los campos o tener un estado explícito
)
// --- Fin Definiciones de Preajustes ---

private fun getDefaultPortFor(serverType: String, encryptionType: String): String {
    // Esta función sigue siendo útil si el usuario cambia manualmente el tipo/cifrado después de un preajuste o sin usar uno.
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
    
    // Estado para el desplegable de preajustes
    var presetDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPresetDisplayName by remember { mutableStateOf("Configuración Rápida...") } // Placeholder

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
                // Actualizar el display name del preset si coincide con alguno (opcional, más complejo)
                 val matchedPreset = serverPresets.firstOrNull { p ->
                    p.serverAddress == it.serverAddress && p.serverPort == it.serverPort && 
                    p.serverType == it.serverType && p.encryptionType == it.encryptionType
                }
                selectedPresetDisplayName = matchedPreset?.displayName ?: "Configuración Personalizada"

            } ?: run {
                AppLogger.d("LoginScreen: No hay preferencias guardadas, usando valores por defecto cock.li IMAP SSL/TLS")
                val defaultPreset = serverPresets.first { it.displayName == "Cock.li (IMAP - SSL/TLS)" }
                emailInput = "usuario@cock.li" // O dejar vacío: ""
                serverAddress = defaultPreset.serverAddress
                serverPort = defaultPreset.serverPort
                serverTypeState = defaultPreset.serverType
                encryptionTypeState = defaultPreset.encryptionType
                selectedPresetDisplayName = defaultPreset.displayName
            }
        }
    }

    // Actualizar puerto cuando serverType o encryptionType cambien
    LaunchedEffect(serverTypeState, encryptionTypeState) {
        // Solo actualiza el puerto si no se acaba de seleccionar un preajuste que ya traía el puerto.
        // Esto es para que el cambio manual de tipo/cifrado actualice el puerto.
        if (serverPresets.none { it.displayName == selectedPresetDisplayName && 
            it.serverType == serverTypeState && it.encryptionType == encryptionTypeState && 
            it.serverPort == serverPort }) {
            serverPort = getDefaultPortFor(serverTypeState, encryptionTypeState)
        }
        AppLogger.d("LoginScreen: serverType o encryptionType cambiado. Puerto: $serverPort para $serverTypeState y $encryptionTypeState")
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
            verticalArrangement = Arrangement.spacedBy(8.dp), // Reducido espacio para más campos
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configuración de Correo", style = MaterialTheme.typography.headlineSmall)

            // Desplegable de Preajustes
            ExposedDropdownMenuBox(
                expanded = presetDropdownExpanded,
                onExpandedChange = { if (!isLoading) presetDropdownExpanded = !presetDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedPresetDisplayName,
                    onValueChange = {}, // No editable directamente
                    label = { Text("Configuración Rápida") },
                    readOnly = true,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(
                    expanded = presetDropdownExpanded,
                    onDismissRequest = { presetDropdownExpanded = false }
                ) {
                    serverPresets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset.displayName) },
                            onClick = {
                                serverAddress = preset.serverAddress
                                serverPort = preset.serverPort
                                serverTypeState = preset.serverType
                                encryptionTypeState = preset.encryptionType
                                selectedPresetDisplayName = preset.displayName
                                presetDropdownExpanded = false
                                // Opcional: Sugerir dominio de email si es un proveedor conocido y el email está vacío
                                if (emailInput.isEmpty() || !emailInput.contains("@")) {
                                    if (preset.displayName.startsWith("Gmail")) emailInput = "@gmail.com"
                                    else if (preset.displayName.startsWith("Cock.li")) emailInput = "@cock.li"
                                }
                            },
                            enabled = !isLoading
                        )
                    }
                }
            }

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it 
                                // Si el usuario edita el email, podría ya no ser un preajuste exacto
                                if(selectedPresetDisplayName != "Configuración Personalizada") {
                                     selectedPresetDisplayName = "Configuración Personalizada"
                                }
                },
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
            
            Text("Configuración Manual (Avanzado)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Tipo Servidor:", modifier = Modifier.weight(1f))
                val serverTypes = listOf("IMAP", "POP3") // Definir localmente o pasar como param
                serverTypes.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (type == serverTypeState),
                            onClick = { serverTypeState = type; selectedPresetDisplayName = "Configuración Personalizada" },
                            enabled = !isLoading
                        )
                        Text(text = type)
                    }
                }
            }

            OutlinedTextField(
                value = serverAddress,
                onValueChange = { serverAddress = it; selectedPresetDisplayName = "Configuración Personalizada" },
                label = { Text("Dirección del Servidor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it; selectedPresetDisplayName = "Configuración Personalizada" }, 
                label = { Text("Puerto del Servidor") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            var expandedEncryption by remember { mutableStateOf(false) }
            val encryptionTypes = listOf("SSL/TLS", "STARTTLS", "None") // Definir localmente o pasar como param
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
                                selectedPresetDisplayName = "Configuración Personalizada"
                                expandedEncryption = false
                            },
                            enabled = !isLoading
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                                password = passwordInput, 
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
                    text = "Error: $errorViewModel",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
} 