package com.aranthalion.presupuesto

import android.app.Application
import com.aranthalion.presupuesto.util.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PresupuestoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init()
        AppLogger.i("Aplicación iniciada")
    }
} 