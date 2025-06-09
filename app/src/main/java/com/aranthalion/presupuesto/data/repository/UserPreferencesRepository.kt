package com.aranthalion.presupuesto.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.aranthalion.presupuesto.util.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class EmailConnectionDetails(
    val email: String,
    val serverAddress: String,
    val serverPort: String,
    val serverType: String, // IMAP o POP3
    val encryptionType: String, // SSL/TLS, STARTTLS, None
    val password: String? = null
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_EMAIL = "email"
        private const val KEY_SERVER_ADDRESS = "server_address"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_SERVER_TYPE = "server_type"
        private const val KEY_ENCRYPTION_TYPE = "encryption_type"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_PASSWORD = "remember_password"
    }

    suspend fun saveEmailConnectionDetails(details: EmailConnectionDetails, rememberPassword: Boolean = false) {
        withContext(Dispatchers.IO) {
            AppLogger.d("UserPreferencesRepository: Guardando detalles de conexión para ${details.email}")
            try {
                sharedPreferences.edit()
                    .putString(KEY_EMAIL, details.email)
                    .putString(KEY_SERVER_ADDRESS, details.serverAddress)
                    .putString(KEY_SERVER_PORT, details.serverPort)
                    .putString(KEY_SERVER_TYPE, details.serverType)
                    .putString(KEY_ENCRYPTION_TYPE, details.encryptionType)
                    .putBoolean(KEY_REMEMBER_PASSWORD, rememberPassword)
                    .apply()
                
                // Solo guardar la contraseña si rememberPassword es true
                if (rememberPassword) {
                    sharedPreferences.edit()
                        .putString(KEY_PASSWORD, details.password)
                        .apply()
                } else {
                    // Si no se debe recordar, eliminar la contraseña guardada
                    sharedPreferences.edit()
                        .remove(KEY_PASSWORD)
                        .apply()
                }
                
                AppLogger.i("UserPreferencesRepository: Detalles de conexión guardados exitosamente.")
            } catch (e: Exception) {
                AppLogger.e("UserPreferencesRepository: Error al guardar detalles de conexión", e)
            }
        }
    }

    suspend fun getEmailConnectionDetails(): EmailConnectionDetails? {
        return withContext(Dispatchers.IO) {
            AppLogger.d("UserPreferencesRepository: Obteniendo detalles de conexión.")
            try {
                val email = sharedPreferences.getString(KEY_EMAIL, null)
                val serverAddress = sharedPreferences.getString(KEY_SERVER_ADDRESS, null)
                val serverPort = sharedPreferences.getString(KEY_SERVER_PORT, null)
                val serverType = sharedPreferences.getString(KEY_SERVER_TYPE, null)
                val encryptionType = sharedPreferences.getString(KEY_ENCRYPTION_TYPE, null)
                val rememberPassword = sharedPreferences.getBoolean(KEY_REMEMBER_PASSWORD, false)
                val password = if (rememberPassword) sharedPreferences.getString(KEY_PASSWORD, null) else null

                if (email != null && serverAddress != null && serverPort != null && 
                    serverType != null && encryptionType != null) {
                    AppLogger.i("UserPreferencesRepository: Detalles de conexión encontrados para $email.")
                    EmailConnectionDetails(
                        email = email,
                        serverAddress = serverAddress,
                        serverPort = serverPort,
                        serverType = serverType,
                        encryptionType = encryptionType,
                        password = password
                    )
                } else {
                    AppLogger.d("UserPreferencesRepository: No se encontraron detalles de conexión guardados.")
                    null
                }
            } catch (e: Exception) {
                AppLogger.e("UserPreferencesRepository: Error al obtener detalles de conexión", e)
                null
            }
        }
    }

    suspend fun clearEmailConnectionDetails() {
        withContext(Dispatchers.IO) {
            AppLogger.d("UserPreferencesRepository: Limpiando detalles de conexión.")
            try {
                sharedPreferences.edit()
                    .remove(KEY_EMAIL)
                    .remove(KEY_SERVER_ADDRESS)
                    .remove(KEY_SERVER_PORT)
                    .remove(KEY_SERVER_TYPE)
                    .remove(KEY_ENCRYPTION_TYPE)
                    .remove(KEY_PASSWORD)
                    .remove(KEY_REMEMBER_PASSWORD)
                    .apply()
                AppLogger.i("UserPreferencesRepository: Detalles de conexión eliminados.")
            } catch (e: Exception) {
                AppLogger.e("UserPreferencesRepository: Error al limpiar detalles de conexión", e)
            }
        }
    }
} 