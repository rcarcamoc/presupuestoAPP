package com.aranthalion.presupuesto.presentation.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.mail.*
import javax.mail.internet.InternetAddress // Necesario para algunas operaciones
import java.util.Properties

// TODO: Implementar almacenamiento seguro de credenciales con Android Keystore

@HiltViewModel
class EmailViewModel @Inject constructor() : ViewModel() {

    private val _emailAddress = MutableStateFlow("")
    val emailAddress: StateFlow<String> = _emailAddress

    private val _password = MutableStateFlow("") // Considerar CharArray o similar para la contraseña
    val password: StateFlow<String> = _password

    private val _imapHost = MutableStateFlow("imap.gmail.com") // Default para Gmail
    val imapHost: StateFlow<String> = _imapHost

    private val _imapPort = MutableStateFlow("993") // Default para IMAPS
    val imapPort: StateFlow<String> = _imapPort

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus

    private val _emailCount = MutableStateFlow<Int?>(null)
    val emailCount: StateFlow<Int?> = _emailCount

    fun onEmailChanged(email: String) {
        _emailAddress.value = email
    }

    fun onPasswordChanged(pass: String) {
        _password.value = pass
    }

    fun onImapHostChanged(host: String) {
        _imapHost.value = host
    }

    fun onImapPortChanged(port: String) {
        _imapPort.value = port
    }

    fun connectAndGetEmailCount() {
        if (_emailAddress.value.isBlank() || _password.value.isBlank() || _imapHost.value.isBlank() || _imapPort.value.isBlank()) {
            _connectionStatus.value = "Por favor, completa todos los campos."
            return
        }

        viewModelScope.launch {
            _connectionStatus.value = "Conectando..."
            _emailCount.value = null
            try {
                val props = Properties().apply {
                    put("mail.store.protocol", "imaps")
                    put("mail.imaps.host", _imapHost.value)
                    put("mail.imaps.port", _imapPort.value)
                    put("mail.imaps.ssl.enable", "true") // For IMAPS
                    // Podríamos necesitar más propiedades para timeouts, etc.
                }

                val session = Session.getInstance(props, null) // No authenticator needed for now, will pass in connect
                val store = session.getStore("imaps")
                
                AppLogger.d("Intentando conectar a ${_imapHost.value}:${_imapPort.value} con usuario ${_emailAddress.value}")
                store.connect(_imapHost.value, _emailAddress.value, _password.value)
                AppLogger.d("Conexión IMAP establecida")

                val inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)
                
                val count = inbox.messageCount
                _emailCount.value = count
                _connectionStatus.value = "Conectado. Correos en INBOX: $count"
                AppLogger.i("Correos en INBOX: $count")

                inbox.close(false)
                store.close()
            } catch (e: MessagingException) {
                AppLogger.e("Error de JavaMail", e)
                _connectionStatus.value = "Error al conectar: ${e.message}"
            } catch (e: Exception) {
                AppLogger.e("Error inesperado en conexión email", e)
                _connectionStatus.value = "Error inesperado: ${e.message}"
            }
        }
    }
} 