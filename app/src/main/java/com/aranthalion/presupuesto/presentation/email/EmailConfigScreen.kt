package com.aranthalion.presupuesto.presentation.email

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Configurar Acceso IMAP/POP3", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = emailAddress,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Dirección de Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = imapHost,
            onValueChange = { viewModel.onImapHostChanged(it) },
            label = { Text("Servidor IMAP (ej: imap.gmail.com)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = imapPort,
            onValueChange = { viewModel.onImapPortChanged(it) },
            label = { Text("Puerto IMAP (ej: 993)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.connectAndGetEmailCount() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Conectar y Contar Correos")
        }

        connectionStatus?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }

        emailCount?.let {
            Text("Total de correos en INBOX: $it", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Advertencia: Ingresar tus credenciales de email directamente es riesgoso. " +
            "Esta es una función de prueba. Las credenciales se manejarán en memoria y no se guardarán de forma persistente en esta versión de prueba.",
            style = MaterialTheme.typography.bodySmall
        )
    }
} 