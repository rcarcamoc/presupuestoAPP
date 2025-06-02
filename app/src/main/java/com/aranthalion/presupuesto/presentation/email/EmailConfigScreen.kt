package com.aranthalion.presupuesto.presentation.email

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailConfigScreen(
    viewModel: EmailViewModel = hiltViewModel()
) {
    val emailAddress by viewModel.emailAddress.collectAsState()
    val password by viewModel.password.collectAsState()
    val imapHost by viewModel.imapHost.collectAsState()
    val imapPort by viewModel.imapPort.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val emailCount by viewModel.emailCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val providerPresets = viewModel.providerPresets
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    var presetsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Configurar Acceso IMAP", style = MaterialTheme.typography.headlineSmall)

        // Selector de Preajustes
        Box {
            OutlinedTextField(
                value = selectedPreset.name,
                onValueChange = { /* No se cambia directamente */ },
                label = { Text("Proveedor de Email") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Abrir preajustes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { presetsExpanded = true }
            )
            DropdownMenu(
                expanded = presetsExpanded,
                onDismissRequest = { presetsExpanded = false },
                modifier = Modifier.fillMaxWidth(0.8f) // Ajustar ancho según necesidad
            ) {
                providerPresets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.name) },
                        onClick = {
                            viewModel.onPresetSelected(preset)
                            presetsExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = emailAddress,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Dirección de Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = imapHost,
            onValueChange = { viewModel.onImapHostChanged(it) },
            label = { Text("Servidor IMAP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && selectedPreset.name == "Otro (Manual)"
        )

        OutlinedTextField(
            value = imapPort,
            onValueChange = { viewModel.onImapPortChanged(it) },
            label = { Text("Puerto IMAP") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && selectedPreset.name == "Otro (Manual)"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Probar Conexión")
            }
            Button(
                onClick = { viewModel.saveConnectionAndGetEmailCount() }, // Renombrado para claridad
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Guardar y Contar")
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
        }

        connectionStatus?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }

        emailCount?.let {
            Text("Total de correos en INBOX: $it", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Advertencia: Ingresar tus credenciales de email directamente es riesgoso. " +
            "Esta función es para propósitos de prueba. Las credenciales se manejan en memoria y no se guardarán de forma persistente en esta versión de prueba.",
            style = MaterialTheme.typography.bodySmall
        )
    }
} 