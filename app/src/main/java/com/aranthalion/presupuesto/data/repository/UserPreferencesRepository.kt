package com.aranthalion.presupuesto.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
    val encryptionType: String // SSL/TLS, STARTTLS, None
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFERENCES_FILE_NAME = "user_email_prefs"
        private const val KEY_EMAIL = "email"
        private const val KEY_SERVER_ADDRESS = "server_address"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_SERVER_TYPE = "server_type"
        private const val KEY_ENCRYPTION_TYPE = "encryption_type"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            PREFERENCES_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveEmailConnectionDetails(details: EmailConnectionDetails) {
        withContext(Dispatchers.IO) {
            AppLogger.d("UserPreferencesRepository: Guardando detalles de conexión para ${details.email}")
            try {
                sharedPreferences.edit()
                    .putString(KEY_EMAIL, details.email)
                    .putString(KEY_SERVER_ADDRESS, details.serverAddress)
                    .putString(KEY_SERVER_PORT, details.serverPort)
                    .putString(KEY_SERVER_TYPE, details.serverType)
                    .putString(KEY_ENCRYPTION_TYPE, details.encryptionType)
                    .apply()
                AppLogger.i("UserPreferencesRepository: Detalles de conexión guardados exitosamente.")
            } catch (e: Exception) {
                AppLogger.e("UserPreferencesRepository: Error al guardar detalles de conexión", e)
                // Considerar re-lanzar o manejar el error de forma más específica si es necesario
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

                if (email != null && serverAddress != null && serverPort != null && serverType != null && encryptionType != null) {
                    AppLogger.i("UserPreferencesRepository: Detalles de conexión encontrados para $email.")
                    EmailConnectionDetails(email, serverAddress, serverPort, serverType, encryptionType)
                } else {
                    AppLogger.d("UserPreferencesRepository: No se encontraron detalles de conexión guardados.")
                    null
                }
            } catch (e: Exception) {
                AppLogger.e("UserPreferencesRepository: Error al obtener detalles de conexión", e)
                null // Retornar null en caso de error para no bloquear la app
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
                    .apply()
                AppLogger.i("UserPreferencesRepository: Detalles de conexión eliminados.")
            } catch (e: Exception) {
                AppLogger.e("UserPreferencesRepository: Error al limpiar detalles de conexión", e)
            }
        }
    }
} 