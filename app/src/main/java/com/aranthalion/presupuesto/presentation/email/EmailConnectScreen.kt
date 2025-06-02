package com.aranthalion.presupuesto.presentation.email

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailConnectScreen(viewModel: EmailConnectViewModel = viewModel()) {
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val server by viewModel.server
    val port by viewModel.port
    val username by viewModel.username
    val password by viewModel.password
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val emailProviders = viewModel.emailProviders
    val connectionTestResult by viewModel.connectionTestResult.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    var showProviderDialog by remember { mutableStateOf(false) }
    var showTestResultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(connectionTestResult) {
        connectionTestResult?.let { 
            showTestResultDialog = true 
        }
    }
    
    LaunchedEffect(saveStatus) {
        // Podrías mostrar un Snackbar o Toast aquí para el estado de guardado
        // Por ahora, simplemente lo limpiaremos después de un tiempo para permitir re-guardar
        if (saveStatus != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSaveStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Conectar Correo Electrónico") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Configurar conexión de correo", style = MaterialTheme.typography.titleMedium)

            // Selector de Proveedor
            OutlinedButton(
                onClick = {
                    Log.d("EmailConnectScreen", "Selector de Proveedor - onClick")
                    showProviderDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedProvider?.name ?: "Seleccionar Proveedor (Opcional)")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Proveedor")
            }

            if (showProviderDialog) {
                AlertDialog(
                    onDismissRequest = {
                        Log.d("EmailConnectScreen", "AlertDialog - onDismissRequest")
                        showProviderDialog = false
                    },
                    title = { Text("Seleccionar Proveedor") },
                    text = {
                        LazyColumn {
                            items(items = emailProviders) { provider ->
                                TextButton(onClick = {
                                    Log.d("EmailConnectScreen", "Proveedor seleccionado del diálogo: ${provider.name}")
                                    viewModel.onProviderSelected(provider)
                                    showProviderDialog = false
                                }) {
                                    Text(provider.name)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            Log.d("EmailConnectScreen", "AlertDialog - Cancelar clickeado")
                            showProviderDialog = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Selector de Protocolo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Protocolo: ")
                RadioButton(selected = selectedProtocol == "IMAP", onClick = {
                    Log.d("EmailConnectScreen", "RadioButton IMAP - onClick")
                    viewModel.onProtocolSelected("IMAP")
                })
                Text("IMAP")
                Spacer(Modifier.width(8.dp))
                RadioButton(selected = selectedProtocol == "POP3", onClick = {
                    Log.d("EmailConnectScreen", "RadioButton POP3 - onClick")
                    viewModel.onProtocolSelected("POP3")
                })
                Text("POP3")
            }

            OutlinedTextField(
                value = server,
                onValueChange = viewModel::onServerChanged,
                label = { Text("Servidor") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = port,
                onValueChange = viewModel::onPortChanged,
                label = { Text("Puerto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Usuario (correo electrónico)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { viewModel.testConnection() }) {
                    Text("Probar Conexión")
                }
                Button(onClick = { viewModel.saveConnection() }) {
                    Text("Guardar")
                }
            }
            
            saveStatus?.let {
                Text(it, color = if (it.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showTestResultDialog && connectionTestResult != null) {
        val result = connectionTestResult!!
        Dialog(onDismissRequest = {
            showTestResultDialog = false
            viewModel.clearTestResult()
        }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (result.success) "Prueba Exitosa" else "Prueba Fallida", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (result.success) {
                        Text("Servidor: ${result.serverName}")
                        Text("Correos pendientes: ${result.pendingEmails}")
                    } else {
                        Text("Error: ${result.errorMessage}")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        showTestResultDialog = false
                        viewModel.clearTestResult()
                    }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
} 