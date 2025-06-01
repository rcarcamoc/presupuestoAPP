package com.aranthalion.presupuesto.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

object AnalyticsLogger {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun init(analytics: FirebaseAnalytics) {
        // Este método ya no se usa en la nueva implementación
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        Timber.i("EVENT: $eventName - $params")
        
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Boolean -> param(key, value.toString())
                    else -> param(key, value.toString())
                }
            }
        }
    }

    fun logLogin(method: String, success: Boolean) {
        logEvent("login_attempt", mapOf(
            "method" to method,
            "success" to success
        ))
    }

    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    fun logError(errorType: String, errorMessage: String, throwable: Throwable? = null) {
        logEvent("app_error", mapOf(
            "error_type" to errorType,
            "error_message" to errorMessage
        ))
        
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", errorType)
            log(errorMessage)
            throwable?.let { recordException(it) }
        }
        
        throwable?.let { Timber.e(it) }
    }

    fun logDeviceInfo() {
        val deviceInfo = mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "android_version" to android.os.Build.VERSION.RELEASE,
            "sdk_version" to android.os.Build.VERSION.SDK_INT.toString()
        )
        
        logEvent("device_info", deviceInfo)
        
        deviceInfo.forEach { (key, value) ->
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    fun logLoginEvent(success: Boolean, method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.SUCCESS, success.toString())
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
} 