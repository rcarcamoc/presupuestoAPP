package com.aranthalion.presupuesto.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.data.repository.UserPreferencesRepository
import com.aranthalion.presupuesto.util.AppLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.inject.Inject
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.NoSuchProviderException
import javax.mail.Session
import javax.mail.Store

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _unreadEmailCount = MutableStateFlow<Int?>(null)
    val unreadEmailCount: StateFlow<Int?> = _unreadEmailCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        AppLogger.d("AuthViewModel: init block.")
        FirebaseCrashlytics.getInstance().setCustomKey("last_init_time", System.currentTimeMillis())
    }
    
    fun isUserSignedIn(): Boolean {
        AppLogger.d("AuthViewModel: isUserSignedIn() llamado.")
        return _userEmail.value != null 
    }
    
    fun signOut() {
        AppLogger.i("AuthViewModel: signOut() llamado.")
        viewModelScope.launch {
            _userEmail.value = null
            _userName.value = null
            _error.value = null
            _unreadEmailCount.value = null
            _isLoading.value = false
            FirebaseCrashlytics.getInstance().log("Usuario cerró sesión: ${_userEmail.value}")
        }
    }

    fun loginWithEmailProvider(email: String, password: String, serverType: String, serverAddress: String, port: Int, encryption: String) {
        AppLogger.i("AuthViewModel: loginWithEmailProvider llamado para $email, Servidor: $serverType $serverAddress:$port, Cifrado: $encryption")
        
        // Registrar información para Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("email_domain", email.substringAfter("@"))
            setCustomKey("server_type", serverType)
            setCustomKey("server_address", serverAddress)
            setCustomKey("server_port", port)
            setCustomKey("encryption_type", encryption)
            log("Iniciando conexión de correo")
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _unreadEmailCount.value = null
            try {
                val count = connectAndCountUnreadEmails(email, password, serverType, serverAddress, port, encryption)
                _userEmail.value = email
                _userName.value = email.substringBefore('@')
                _unreadEmailCount.value = count
                AppLogger.i("AuthViewModel: Conexión exitosa. Usuario: $email, Correos no leídos: $count")
                FirebaseCrashlytics.getInstance().log("Conexión exitosa - Correos no leídos: $count")
            } catch (e: MessagingException) {
                AppLogger.e("AuthViewModel: Error de mensajería", e)
                _error.value = "Error de correo: ${e.message}"
                FirebaseCrashlytics.getInstance().apply {
                    recordException(e)
                    setCustomKey("error_type", "messaging")
                    setCustomKey("error_message", e.message ?: "Unknown")
                }
            } catch (e: IllegalArgumentException) {
                AppLogger.e("AuthViewModel: Argumento ilegal", e)
                _error.value = "Configuración inválida: ${e.message}"
                FirebaseCrashlytics.getInstance().apply {
                    recordException(e)
                    setCustomKey("error_type", "configuration")
                    setCustomKey("error_message", e.message ?: "Unknown")
                }
            } catch (e: Exception) {
                AppLogger.e("AuthViewModel: Error desconocido en loginWithEmailProvider", e)
                _error.value = "Error inesperado: ${e.message}"
                FirebaseCrashlytics.getInstance().apply {
                    recordException(e)
                    setCustomKey("error_type", "unknown")
                    setCustomKey("error_message", e.message ?: "Unknown")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun connectAndCountUnreadEmails(email: String, pass: String, serverType: String, host: String, port: Int, encryption: String): Int {
        return withContext(Dispatchers.IO) {
            val props = Properties()
            val protocol = serverType.lowercase()

            // Configuración común
            props["mail.$protocol.host"] = host
            props["mail.$protocol.port"] = port.toString()
            props["mail.$protocol.auth"] = "true"
            props["mail.$protocol.connectiontimeout"] = "60000"
            props["mail.$protocol.timeout"] = "60000"

            // Configuración de cifrado
            when (encryption) {
                "SSL/TLS" -> {
                    props["mail.$protocol.ssl.enable"] = "true"
                    if (protocol == "pop3") {
                        props["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                        props["mail.pop3.socketFactory.fallback"] = "false"
                        props["mail.pop3.socketFactory.port"] = port.toString()
                    } else if (protocol == "imap") {
                        props["mail.imap.ssl.trust"] = "*"
                    }
                }
                "STARTTLS" -> {
                    props["mail.$protocol.starttls.enable"] = "true"
                    if (protocol == "imap") {
                        props["mail.imap.starttls.required"] = "true"
                    }
                }
                "None" -> {
                    AppLogger.w("Conexión sin cifrado seleccionada para $protocol")
                    FirebaseCrashlytics.getInstance().log("Advertencia: Conexión sin cifrado para $protocol")
                }
                else -> {
                    val error = "Tipo de cifrado no soportado: $encryption"
                    AppLogger.e(error)
                    FirebaseCrashlytics.getInstance().log(error)
                    throw IllegalArgumentException(error)
                }
            }
            
            if (protocol == "imap") {
                props["mail.store.protocol"] = if(encryption == "SSL/TLS") "imaps" else "imap"
            } else if (protocol == "pop3") {
                props["mail.store.protocol"] = if(encryption == "SSL/TLS") "pop3s" else "pop3"
            }

            AppLogger.d("Propiedades de JavaMail: $props")
            FirebaseCrashlytics.getInstance().log("Configuración de conexión: $props")

            val session = Session.getInstance(props, null)
            var store: Store? = null
            var inbox: Folder? = null
            
            try {
                store = session.getStore(props["mail.store.protocol"] as String? ?: protocol)
                AppLogger.d("Intentando conectar a $host:$port como $email con protocolo ${store.urlName}")
                FirebaseCrashlytics.getInstance().log("Iniciando conexión al servidor")
                
                try {
                    store.connect(email, pass)
                    AppLogger.i("Conectado exitosamente al servidor ${props["mail.store.protocol"]}")
                    FirebaseCrashlytics.getInstance().log("Conexión al servidor exitosa")
                } catch (e: MessagingException) {
                    val errorMessage = when {
                        e.message?.contains("AuthenticationFailedException") == true -> 
                            "Credenciales incorrectas. Por favor, verifica tu correo y contraseña."
                        e.message?.contains("ConnectException") == true -> 
                            "No se pudo conectar al servidor. Verifica la dirección y el puerto."
                        e.message?.contains("SSLHandshakeException") == true -> 
                            "Error de SSL/TLS. Verifica la configuración de cifrado."
                        else -> "Error al conectar: ${e.message}"
                    }
                    FirebaseCrashlytics.getInstance().apply {
                        recordException(e)
                        setCustomKey("connection_error", errorMessage)
                    }
                    throw MessagingException(errorMessage, e)
                }

                try {
                    inbox = store.getFolder("INBOX")
                    AppLogger.d("Intentando abrir buzón INBOX")
                    FirebaseCrashlytics.getInstance().log("Abriendo buzón INBOX")
                    
                    // Verificar si el buzón existe
                    if (!inbox.exists()) {
                        val error = "El buzón INBOX no existe en la cuenta"
                        AppLogger.e(error)
                        FirebaseCrashlytics.getInstance().log(error)
                        throw MessagingException(error)
                    }

                    // Obtener información del buzón antes de abrirlo
                    val totalMessages = inbox.messageCount
                    AppLogger.d("Total de mensajes en el buzón: $totalMessages")
                    FirebaseCrashlytics.getInstance().log("Total de mensajes en el buzón: $totalMessages")

                    // Abrir el buzón
                    inbox.open(Folder.READ_ONLY)
                    AppLogger.d("Buzón INBOX abierto exitosamente")
                    
                    // Obtener el conteo de mensajes no leídos
                    val unreadCount = inbox.unreadMessageCount
                    AppLogger.i("Número de mensajes no leídos: $unreadCount")
                    FirebaseCrashlytics.getInstance().log("Correos no leídos encontrados: $unreadCount")

                    // Verificar si el conteo es válido
                    if (unreadCount < 0) {
                        val error = "Conteo de mensajes no leídos inválido: $unreadCount"
                        AppLogger.e(error)
                        FirebaseCrashlytics.getInstance().log(error)
                        throw MessagingException(error)
                    }

                    // Obtener información adicional para diagnóstico
                    val flags = inbox.permanentFlags
                    AppLogger.d("Flags disponibles en el buzón: $flags")
                    FirebaseCrashlytics.getInstance().log("Flags disponibles en el buzón: $flags")

                    // Verificar si el buzón soporta la bandera de no leído
                    if (!flags.contains(javax.mail.Flags.Flag.SEEN)) {
                        val warning = "El buzón no soporta la bandera de mensajes leídos/no leídos"
                        AppLogger.w(warning)
                        FirebaseCrashlytics.getInstance().log(warning)
                    }

                    return@withContext unreadCount
                } catch (e: MessagingException) {
                    val errorMessage = when {
                        e.message?.contains("FolderNotFoundException") == true -> 
                            "No se encontró el buzón de entrada. Verifica que la cuenta tenga acceso al correo."
                        e.message?.contains("AuthenticationFailedException") == true ->
                            "Error de autenticación al acceder al buzón. Verifica las credenciales."
                        else -> "Error al acceder al buzón: ${e.message}"
                    }
                    AppLogger.e("Error al acceder al buzón: $errorMessage", e)
                    FirebaseCrashlytics.getInstance().apply {
                        recordException(e)
                        setCustomKey("inbox_error", errorMessage)
                        log("Error detallado al acceder al buzón: ${e.message}")
                    }
                    throw MessagingException(errorMessage, e)
                }
            } catch (e: NoSuchProviderException) {
                val error = "Proveedor no encontrado para $protocol o ${props["mail.store.protocol"]}"
                AppLogger.e(error, e)
                FirebaseCrashlytics.getInstance().apply {
                    recordException(e)
                    setCustomKey("provider_error", error)
                }
                throw MessagingException(error, e)
            } catch (e: MessagingException) {
                AppLogger.e("Error de mensajería al conectar o leer el buzón", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                throw e
            } catch (e: Exception) {
                val error = "Error inesperado durante la conexión al correo: ${e.message}"
                AppLogger.e(error, e)
                FirebaseCrashlytics.getInstance().apply {
                    recordException(e)
                    setCustomKey("unexpected_error", error)
                }
                throw RuntimeException(error, e)
            } finally {
                try {
                    inbox?.close(false)
                    store?.close()
                    AppLogger.d("Conexiones de correo cerradas.")
                    FirebaseCrashlytics.getInstance().log("Conexiones de correo cerradas correctamente")
                } catch (e: MessagingException) {
                    AppLogger.e("Error al cerrar conexiones de correo", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    // Funciones para UserPreferencesRepository
    suspend fun saveLastConnectionDetails(details: com.aranthalion.presupuesto.data.repository.EmailConnectionDetails, rememberPassword: Boolean = false) {
        userPreferencesRepository.saveEmailConnectionDetails(details, rememberPassword)
    }

    suspend fun loadLastConnectionDetails(): com.aranthalion.presupuesto.data.repository.EmailConnectionDetails? {
        return userPreferencesRepository.getEmailConnectionDetails()
    }

    suspend fun clearLastConnectionDetails() {
        userPreferencesRepository.clearEmailConnectionDetails()
    }
} 