package com.aranthalion.presupuesto.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aranthalion.presupuesto.data.repository.UserPreferencesRepository
import com.aranthalion.presupuesto.util.AppLogger
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
        // Cargar detalles de conexión guardados al iniciar (se hará en LoginScreen para pre-rellenar)
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
            _isLoading.value = false // Asegurar que isLoading se resetee
            // Considerar si se deben limpiar las preferencias guardadas aquí o dar una opción al usuario
            // userPreferencesRepository.clearEmailConnectionDetails() 
            AppLogger.i("AuthViewModel: Usuario cerró sesión.")
        }
    }

    fun loginWithEmailProvider(email: String, password: String, serverType: String, serverAddress: String, port: Int, encryption: String) {
        AppLogger.i("AuthViewModel: loginWithEmailProvider llamado para $email, Servidor: $serverType $serverAddress:$port, Cifrado: $encryption")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null // Limpiar error anterior
            _unreadEmailCount.value = null // Limpiar contador anterior
            try {
                val count = connectAndCountUnreadEmails(email, password, serverType, serverAddress, port, encryption)
                _userEmail.value = email
                _userName.value = email.substringBefore('@')
                _unreadEmailCount.value = count
                AppLogger.i("AuthViewModel: Conexión exitosa. Usuario: $email, Correos no leídos: $count")
            } catch (e: MessagingException) {
                AppLogger.e("AuthViewModel: Error de mensajería", e)
                _error.value = "Error de correo: ${e.message}"
            } catch (e: IllegalArgumentException) {
                AppLogger.e("AuthViewModel: Argumento ilegal", e)
                _error.value = "Configuración inválida: ${e.message}"
            }
            catch (e: Exception) {
                AppLogger.e("AuthViewModel: Error desconocido en loginWithEmailProvider", e)
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false // Asegurar que isLoading se apague
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
            // Timeouts (en milisegundos)
            props["mail.$protocol.connectiontimeout"] = "60000" // 1 minuto
            props["mail.$protocol.timeout"] = "60000" // 1 minuto

            // Configuración de cifrado
            when (encryption) {
                "SSL/TLS" -> {
                    props["mail.$protocol.ssl.enable"] = "true"
                    if (protocol == "pop3") { // POP3S
                        props["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                        props["mail.pop3.socketFactory.fallback"] = "false"
                        props["mail.pop3.socketFactory.port"] = port.toString()
                    } else if (protocol == "imap") { // IMAPS
                         props["mail.imap.ssl.trust"] = "*" // Confiar en todos los certificados SSL (considerar la seguridad para producción)
                    }
                }
                "STARTTLS" -> {
                    props["mail.$protocol.starttls.enable"] = "true"
                     if (protocol == "imap") {
                         props["mail.imap.starttls.required"] = "true" // Opcional, pero más seguro
                    }
                }
                "None" -> {
                    AppLogger.w("Conexión sin cifrado seleccionada para $protocol")
                }
                else -> {
                    AppLogger.e("Tipo de cifrado no soportado: $encryption")
                    throw IllegalArgumentException("Tipo de cifrado no soportado: $encryption")
                }
            }
            
            // Ajustes específicos de protocolo (store.protocol)
             if (protocol == "imap") {
                props["mail.store.protocol"] = if(encryption == "SSL/TLS") "imaps" else "imap"
            } else if (protocol == "pop3") {
                 props["mail.store.protocol"] = if(encryption == "SSL/TLS") "pop3s" else "pop3"
            }

            AppLogger.d("Propiedades de JavaMail: $props")

            val session = Session.getInstance(props, null)
            // session.debug = true // Habilitar para depuración detallada de JavaMail

            var store: Store? = null
            var inbox: Folder? = null
            try {
                store = session.getStore(props["mail.store.protocol"] as String? ?: protocol)
                AppLogger.d("Intentando conectar a $host:$port como $email con protocolo ${store.urlName}")
                store.connect(email, pass) // El host y puerto ya están en las propiedades para connect
                AppLogger.i("Conectado exitosamente al servidor ${props["mail.store.protocol"]}")

                inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)
                
                val unreadCount = inbox.unreadMessageCount
                AppLogger.i("Número de mensajes no leídos: $unreadCount")
                return@withContext unreadCount
            } catch (e: NoSuchProviderException) {
                AppLogger.e("Proveedor no encontrado para $protocol o ${props["mail.store.protocol"]}", e)
                throw MessagingException("Proveedor de servicio no encontrado: ${props["mail.store.protocol"]}", e)
            } catch (e: MessagingException) {
                AppLogger.e("Error de mensajería al conectar o leer el buzón", e)
                throw e 
            } catch (e: Exception) {
                AppLogger.e("Error inesperado en connectAndCountUnreadEmails", e)
                throw RuntimeException("Error inesperado durante la conexión al correo.", e)
            } finally {
                try {
                    inbox?.close(false)
                    store?.close()
                    AppLogger.d("Conexiones de correo cerradas.")
                } catch (e: MessagingException) {
                    AppLogger.e("Error al cerrar conexiones de correo", e)
                }
            }
        }
    }

    // Funciones para UserPreferencesRepository
    suspend fun saveLastConnectionDetails(details: com.aranthalion.presupuesto.data.repository.EmailConnectionDetails) {
        userPreferencesRepository.saveEmailConnectionDetails(details)
    }

    suspend fun loadLastConnectionDetails(): com.aranthalion.presupuesto.data.repository.EmailConnectionDetails? {
        return userPreferencesRepository.getEmailConnectionDetails()
    }

    suspend fun clearLastConnectionDetails() {
        userPreferencesRepository.clearEmailConnectionDetails()
    }
} 