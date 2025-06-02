package com.aranthalion.presupuesto.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _unreadEmailCount = MutableStateFlow<Int?>(null)
    val unreadEmailCount: StateFlow<Int?> = _unreadEmailCount
    
    init {
        AppLogger.d("AuthViewModel: init block.")
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
            AppLogger.i("AuthViewModel: Usuario cerró sesión.")
        }
    }

    fun loginWithEmailProvider(email: String, password: String, serverType: String, serverAddress: String, port: Int, encryption: String) {
        AppLogger.i("AuthViewModel: loginWithEmailProvider llamado para $email, Servidor: $serverType $serverAddress:$port, Cifrado: $encryption")
        viewModelScope.launch {
            _error.value = null
            _unreadEmailCount.value = null
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

            // Configuración de cifrado
            when (encryption) {
                "SSL/TLS" -> {
                    props["mail.$protocol.ssl.enable"] = "true"
                    if (protocol == "pop3") { // POP3S
                        props["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                        props["mail.pop3.socketFactory.fallback"] = "false"
                        props["mail.pop3.socketFactory.port"] = port.toString()
                    }
                }
                "STARTTLS" -> {
                    props["mail.$protocol.starttls.enable"] = "true"
                     // Para IMAP, starttls es común. Para POP3, puede requerir config adicional o no ser estándar.
                    if (protocol == "imap") {
                         props["mail.imap.starttls.required"] = "true" // Opcional, pero más seguro
                    }
                }
                "None" -> {
                    // No se añade configuración extra de seguridad
                    AppLogger.w("Conexión sin cifrado seleccionada para $protocol")
                }
                else -> {
                    AppLogger.e("Tipo de cifrado no soportado: $encryption")
                    throw IllegalArgumentException("Tipo de cifrado no soportado: $encryption")
                }
            }
            
            // Ajustes específicos para IMAP y POP3
             if (protocol == "imap") {
                props["mail.store.protocol"] = "imaps" // Usar imaps si SSL/TLS está activo
                 if(encryption == "SSL/TLS") props["mail.imap.ssl.trust"] = "*" // Confiar en todos los certificados SSL (considerar la seguridad)
                 else props["mail.store.protocol"] = "imap"

            } else if (protocol == "pop3") {
                 if(encryption == "SSL/TLS") props["mail.store.protocol"] = "pop3s"
                 else props["mail.store.protocol"] = "pop3"
            }


            AppLogger.d("Propiedades de JavaMail: $props")

            val session = Session.getInstance(props, null)
            // session.debug = true // Habilitar para depuración detallada de JavaMail

            var store: Store? = null
            var inbox: Folder? = null
            try {
                store = session.getStore(protocol)
                AppLogger.d("Intentando conectar a $host:$port como $email")
                store.connect(host, port, email, pass)
                AppLogger.i("Conectado exitosamente al servidor $protocol")

                inbox = store.getFolder("INBOX")
                inbox.open(Folder.READ_ONLY)
                
                val unreadCount = inbox.unreadMessageCount
                AppLogger.i("Número de mensajes no leídos: $unreadCount")
                return@withContext unreadCount
            } catch (e: NoSuchProviderException) {
                AppLogger.e("Proveedor no encontrado para $protocol", e)
                throw MessagingException("Proveedor de servicio no encontrado: $protocol", e)
            } catch (e: MessagingException) {
                AppLogger.e("Error de mensajería al conectar o leer el buzón", e)
                throw e // Re-lanzar para ser atrapado por el bloqueador externo
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
} 