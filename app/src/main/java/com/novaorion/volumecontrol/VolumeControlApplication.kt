package com.novaorion.volumecontrol

import android.app.Application
import android.content.Context

class VolumeControlApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Modern API ile dil ayarlarını başlat
        LanguageHelper.initializeAppLanguage(this)
    }
    
    override fun attachBaseContext(base: Context?) {
        val context = base?.let { LanguageHelper.updateBaseContextLanguage(it) } ?: base
        super.attachBaseContext(context)
    }
}
