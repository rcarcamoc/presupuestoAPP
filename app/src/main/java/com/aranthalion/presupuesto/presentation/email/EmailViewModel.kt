package com.aranthalion.presupuesto.presentation.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.mail.*
import javax.mail.internet.InternetAddress // Necesario para algunas operaciones
import java.util.Properties

// TODO: Implementar almacenamiento seguro de credenciales con Android Keystore

data class EmailProviderPreset(
    val name: String,
    val imapHost: String,
    val imapPort: String,
    val securityType: String // "SSL/TLS" o "STARTTLS" (aunque para imaps suele ser SSL/TLS directo)
)

@HiltViewModel
class EmailViewModel @Inject constructor() : ViewModel() {

    private val _emailAddress = MutableStateFlow("")
    val emailAddress: StateFlow<String> = _emailAddress

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _imapHost = MutableStateFlow("")
    val imapHost: StateFlow<String> = _imapHost

    private val _imapPort = MutableStateFlow("")
    val imapPort: StateFlow<String> = _imapPort

    // Podríamos añadir un StateFlow para el tipo de seguridad si queremos que sea configurable
    // private val _securityType = MutableStateFlow("SSL/TLS")
    // val securityType: StateFlow<String> = _securityType

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus

    private val _emailCount = MutableStateFlow<Int?>(null)
    val emailCount: StateFlow<Int?> = _emailCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val providerPresets = listOf(
        EmailProviderPreset(name = "Gmail", imapHost = "imap.gmail.com", imapPort = "993", securityType = "SSL/TLS"),
        EmailProviderPreset(name = "Outlook/Hotmail", imapHost = "outlook.office365.com", imapPort = "993", securityType = "SSL/TLS"),
        EmailProviderPreset(name = "Cock.li", imapHost = "mail.cock.li", imapPort = "993", securityType = "SSL/TLS"),
        EmailProviderPreset(name = "Otro (Manual)", imapHost = "", imapPort = "", securityType = "SSL/TLS")
    )

    private val _selectedPreset = MutableStateFlow(providerPresets.first { it.name == "Gmail" })
    val selectedPreset: StateFlow<EmailProviderPreset> = _selectedPreset.asStateFlow()

    init {
        // Inicializar con el preset de Gmail por defecto
        applyPreset(providerPresets.first { it.name == "Gmail" })
    }

    fun onEmailChanged(email: String) { _emailAddress.value = email }
    fun onPasswordChanged(pass: String) { _password.value = pass }
    fun onImapHostChanged(host: String) { _imapHost.value = host }
    fun onImapPortChanged(port: String) { _imapPort.value = port }

    fun onPresetSelected(preset: EmailProviderPreset) {
        _selectedPreset.value = preset
        applyPreset(preset)
    }

    private fun applyPreset(preset: EmailProviderPreset) {
        if (preset.name != "Otro (Manual)") {
            _imapHost.value = preset.imapHost
            _imapPort.value = preset.imapPort
            // Aquí también se podría setear _securityType.value si lo implementamos
        } else {
            // Opcional: limpiar campos si se selecciona "Otro"
            // _imapHost.value = ""
            // _imapPort.value = ""
        }
    }

    fun testConnection() {
        connectAndGetEmailCount(isTest = true)
    }

    fun saveConnectionAndGetEmailCount() { // Renombrado para claridad
        connectAndGetEmailCount(isTest = false)
    }

    private fun connectAndGetEmailCount(isTest: Boolean) {
        if (_emailAddress.value.isBlank() || _password.value.isBlank() || _imapHost.value.isBlank() || _imapPort.value.isBlank()) {
            _connectionStatus.value = "Por favor, completa todos los campos obligatorios."
            return
        }
        _isLoading.value = true
        _connectionStatus.value = "Conectando..."
        _emailCount.value = null

        viewModelScope.launch {
            try {
                val props = Properties().apply {
                    // Asumiendo IMAPS (SSL/TLS) por ahora, ya que es lo más común y seguro
                    // Para STARTTLS se usaría "imap" y otras propiedades como "mail.imap.starttls.enable"
                    put("mail.store.protocol", "imaps")
                    put("mail.imaps.host", _imapHost.value)
                    put("mail.imaps.port", _imapPort.value)
                    put("mail.imaps.ssl.enable", "true")
                    put("mail.imaps.auth", "true")
                    put("mail.imaps.timeout", "10000") // 10 segundos timeout conexión
                    put("mail.imaps.connectiontimeout", "10000")
                }

                val session = Session.getInstance(props, null)
                val store = withContext(Dispatchers.IO) {
                    session.getStore("imaps")
                }
                
                AppLogger.d("Intentando conectar a ${_imapHost.value}:${_imapPort.value} con usuario ${_emailAddress.value}")
                withContext(Dispatchers.IO) {
                    store.connect(_emailAddress.value, _password.value) // Host y puerto ya están en props para imaps
                }
                AppLogger.d("Conexión IMAP establecida")

                if (isTest) {
                    _connectionStatus.value = "Conexión exitosa (Prueba)"
                    AppLogger.i("Prueba de conexión IMAP exitosa.")
                } else {
                    val inbox = withContext(Dispatchers.IO) { store.getFolder("INBOX") }
                    withContext(Dispatchers.IO) { inbox.open(Folder.READ_ONLY) }
                    
                    val count = withContext(Dispatchers.IO) { inbox.messageCount }
                    _emailCount.value = count
                    _connectionStatus.value = "Conectado. Correos en INBOX: $count"
                    AppLogger.i("Correos en INBOX: $count")
                    withContext(Dispatchers.IO) { inbox.close(false) }
                }
                withContext(Dispatchers.IO) { store.close() }
            } catch (e: AuthenticationFailedException) {
                AppLogger.e("Error de autenticación JavaMail", e)
                _connectionStatus.value = "Error de autenticación: Verifica usuario/contraseña."
            } catch (e: MessagingException) {
                AppLogger.e("Error de JavaMail", e)
                _connectionStatus.value = "Error al conectar: ${e.localizedMessage ?: e.message}"
            } catch (e: Exception) {
                AppLogger.e("Error inesperado en conexión email", e)
                _connectionStatus.value = "Error inesperado: ${e.localizedMessage ?: e.message}"
            }
            _isLoading.value = false
        }
    }
} 