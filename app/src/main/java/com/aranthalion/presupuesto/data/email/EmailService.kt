package com.aranthalion.presupuesto.data.email

import android.util.Log
import com.aranthalion.presupuesto.util.AppLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.search.FlagTerm
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailService @Inject constructor() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    suspend fun connectAndCountUnreadEmails(
        email: String,
        password: String,
        serverType: String,
        serverAddress: String,
        port: Int,
        encryption: String
    ): Result<Int> {
        crashlytics.log("Iniciando conexión IMAP para $email en $serverAddress:$port")
        crashlytics.setCustomKey("server_type", serverType)
        crashlytics.setCustomKey("encryption_type", encryption)
        
        return try {
            val props = Properties().apply {
                put("mail.imaps.host", serverAddress)
                put("mail.imaps.port", port.toString())
                put("mail.imaps.ssl.enable", "true")
                put("mail.imaps.auth.mechanisms", "XOAUTH2")
                put("mail.imaps.auth.xoauth2.disable", "false")
                put("mail.debug", "true")
            }

            crashlytics.log("Configuración IMAP: ${props.toString()}")

            val session = Session.getInstance(props, null)
            val store = session.getStore("imaps")
            
            crashlytics.log("Intentando conectar al servidor IMAP")
            store.connect(serverAddress, port, email, password)
            
            val inbox = store.getFolder("INBOX")
            if (!inbox.exists()) {
                crashlytics.recordException(Exception("Buzón INBOX no existe"))
                return Result.failure(Exception("El buzón INBOX no existe"))
            }

            crashlytics.log("Buzón INBOX encontrado, verificando acceso")
            if (!inbox.isOpen) {
                inbox.open(Folder.READ_ONLY)
            }

            val totalMessages = inbox.messageCount
            crashlytics.log("Total de mensajes en el buzón: $totalMessages")
            crashlytics.setCustomKey("total_messages", totalMessages)

            val flags = inbox.permanentFlags
            crashlytics.log("Flags disponibles en el buzón: ${flags.joinToString()}")
            
            if (!flags.contains(Flags.Flag.SEEN)) {
                crashlytics.recordException(Exception("El buzón no soporta la bandera de mensajes leídos"))
                return Result.failure(Exception("El servidor no soporta la bandera de mensajes leídos"))
            }

            val unreadMessages = inbox.getUnreadMessageCount()
            crashlytics.log("Mensajes no leídos encontrados: $unreadMessages")
            crashlytics.setCustomKey("unread_messages", unreadMessages)

            inbox.close(false)
            store.close()

            Result.success(unreadMessages)
        } catch (e: AuthenticationFailedException) {
            crashlytics.recordException(e)
            crashlytics.log("Error de autenticación: ${e.message}")
            Result.failure(Exception("Error de autenticación: ${e.message}"))
        } catch (e: MessagingException) {
            crashlytics.recordException(e)
            crashlytics.log("Error de conexión IMAP: ${e.message}")
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            crashlytics.recordException(e)
            crashlytics.log("Error inesperado: ${e.message}")
            Result.failure(e)
        }
    }

    private fun getDefaultPortFor(serverType: String, encryption: String): Int {
        return when {
            serverType.equals("IMAP", ignoreCase = true) && encryption.equals("SSL/TLS", ignoreCase = true) -> 993
            serverType.equals("IMAP", ignoreCase = true) && encryption.equals("STARTTLS", ignoreCase = true) -> 143
            else -> 993
        }
    }
} 