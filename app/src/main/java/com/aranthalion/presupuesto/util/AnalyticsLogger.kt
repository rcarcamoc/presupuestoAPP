package com.aranthalion.presupuesto.util

// import com.google.firebase.analytics.FirebaseAnalytics // Eliminado
// import com.google.firebase.analytics.ktx.logEvent // Eliminado
// import com.google.firebase.crashlytics.FirebaseCrashlytics // Eliminado
import timber.log.Timber

object AnalyticsLogger {
    // private var firebaseAnalytics: FirebaseAnalytics? = null // Eliminado

    fun init(/* analytics: FirebaseAnalytics */) { // Parámetro eliminado
        // firebaseAnalytics = analytics // Eliminado
        Timber.i("AnalyticsLogger: init() llamado. Funcionalidad de Firebase eliminada.")
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        Timber.i("EVENT: $eventName - $params")
        
        // firebaseAnalytics?.logEvent(eventName) { // Bloque eliminado
        //     params.forEach { (key, value) ->
        //         when (value) {
        //             is String -> param(key, value)
        //             is Long -> param(key, value)
        //             is Double -> param(key, value)
        //             is Boolean -> param(key, value.toString())
        //             else -> param(key, value.toString())
        //         }
        //     }
        // }
    }

    fun logLogin(method: String, success: Boolean) {
        logEvent("login_attempt", mapOf(
            "method" to method,
            "success" to success
        ))
    }

    fun logScreenView(screenName: String) {
        logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    fun logError(errorType: String, errorMessage: String, throwable: Throwable? = null) {
        logEvent("app_error", mapOf(
            "error_type" to errorType,
            "error_message" to errorMessage
        ))
        
        // FirebaseCrashlytics.getInstance().apply { // Bloque eliminado
        //     setCustomKey("error_type", errorType)
        //     log(errorMessage)
        //     throwable?.let { recordException(it) }
        // }
        
        throwable?.let { Timber.e(it, "ERROR_REPORT: Type: $errorType, Message: $errorMessage") } ?: Timber.e("ERROR_REPORT: Type: $errorType, Message: $errorMessage")
    }

    fun logDeviceInfo() {
        val deviceInfo = mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "android_version" to android.os.Build.VERSION.RELEASE,
            "sdk_version" to android.os.Build.VERSION.SDK_INT.toString()
        )
        
        logEvent("device_info", deviceInfo)
        
        // deviceInfo.forEach { (key, value) -> // Bloque eliminado
        //     FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        // }
    }
} 