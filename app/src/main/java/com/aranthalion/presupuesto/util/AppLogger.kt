package com.aranthalion.presupuesto.util

import android.util.Log
import com.aranthalion.presupuesto.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object AppLogger {
    private const val TAG = "PresupuestoApp"
    
    fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
    
    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }
    
    fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }
    
    fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        Timber.e(throwable, message)
    }
    
    private class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("priority", priority)
                tag?.let { setCustomKey("tag", it) }
                log(message)
                
                if (t != null && (priority == Log.ERROR || priority == Log.WARN)) {
                    recordException(t)
                }
            }
        }
    }
} 