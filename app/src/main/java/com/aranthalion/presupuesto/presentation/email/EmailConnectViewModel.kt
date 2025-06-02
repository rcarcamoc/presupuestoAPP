package com.aranthalion.presupuesto.presentation.email

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Simulación de datos para la prueba de conexión
data class EmailConnectionTestResult(
    val success: Boolean,
    val serverName: String? = null,
    val pendingEmails: Int? = null,
    val errorMessage: String? = null
)

class EmailConnectViewModel : ViewModel() {

    private val _selectedProtocol = MutableStateFlow("IMAP")
    val selectedProtocol: StateFlow<String> = _selectedProtocol

    private val _server = mutableStateOf("")
    val server: androidx.compose.runtime.State<String> = _server

    private val _port = mutableStateOf("")
    val port: androidx.compose.runtime.State<String> = _port

    private val _username = mutableStateOf("")
    val username: androidx.compose.runtime.State<String> = _username

    private val _password = mutableStateOf("")
    val password: androidx.compose.runtime.State<String> = _password

    private val _selectedProvider = MutableStateFlow<EmailProvider?>(null)
    val selectedProvider: StateFlow<EmailProvider?> = _selectedProvider

    val emailProviders: List<EmailProvider> = popularEmailProviders

    private val _connectionTestResult = MutableStateFlow<EmailConnectionTestResult?>(null)
    val connectionTestResult: StateFlow<EmailConnectionTestResult?> = _connectionTestResult

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    fun onProtocolSelected(protocol: String) {
        _selectedProtocol.value = protocol
        _selectedProvider.value?.let { updateFieldsFromProvider(it, protocol) }
    }

    fun onServerChanged(newServer: String) {
        _server.value = newServer
    }

    fun onPortChanged(newPort: String) {
        _port.value = newPort
    }

    fun onUsernameChanged(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun onProviderSelected(provider: EmailProvider?) {
        _selectedProvider.value = provider
        provider?.let { updateFieldsFromProvider(it, _selectedProtocol.value) }
    }

    private fun updateFieldsFromProvider(provider: EmailProvider, protocol: String) {
        if (protocol == "IMAP") {
            _server.value = provider.imapServer
            _port.value = provider.imapPort.toString()
        } else { // POP3
            _server.value = provider.pop3Server
            _port.value = provider.pop3Port.toString()
        }
        // El nombre de usuario y la contraseña no se autocompletan desde el proveedor general
    }

    fun testConnection() {
        viewModelScope.launch {
            // Simulación de prueba de conexión
            AppLogger.i("Probando conexión: ${_username.value}, Server: ${_server.value}, Port: ${_port.value}, Protocol: ${_selectedProtocol.value}")
            kotlinx.coroutines.delay(2000) // Simular demora de red
            if (_username.value.isNotEmpty() && _password.value.isNotEmpty() && _server.value.isNotEmpty() && _port.value.isNotEmpty()) {
                // Simulación de éxito
                val pending = (0..100).random()
                _connectionTestResult.value = EmailConnectionTestResult(success = true, serverName = _server.value, pendingEmails = pending)
                AppLogger.i("Prueba de conexión exitosa: Server: ${_server.value}, Correos pendientes: $pending")
            } else {
                _connectionTestResult.value = EmailConnectionTestResult(success = false, errorMessage = "Por favor, complete todos los campos.")
                AppLogger.w("Prueba de conexión fallida: campos incompletos")
            }
        }
    }

    fun saveConnection() {
        viewModelScope.launch {
            AppLogger.i("Guardando conexión: ${_username.value}, Server: ${_server.value}")
            if (_username.value.isNotEmpty() && _password.value.isNotEmpty() && _server.value.isNotEmpty() && _port.value.isNotEmpty()) {
                // Aquí iría la lógica para guardar la configuración de forma segura
                // Por ahora, solo mostramos un mensaje.
                kotlinx.coroutines.delay(1000)
                _saveStatus.value = "Conexión guardada exitosamente para ${_username.value}!"
                AppLogger.i("Conexión guardada para ${_username.value}")
            } else {
                _saveStatus.value = "Error: Faltan datos para guardar la conexión."
                AppLogger.w("Error al guardar: faltan datos")
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    fun clearTestResult() {
        _connectionTestResult.value = null
    }
} 