package com.novaorion.volumecontrol

import android.app.Application
import android.content.Context

class VolumeControlApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Klasik metod ile dil ayarlarını başlat
        val languageCode = LanguageHelper.getLanguage(this)
        LanguageHelper.forceUpdateLanguage(this, languageCode)
    }
    
    override fun attachBaseContext(base: Context?) {
        val context = base?.let { LanguageHelper.updateBaseContextLanguage(it) } ?: base
        super.attachBaseContext(context)
    }
}
