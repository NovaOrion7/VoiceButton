package com.example.voicebutton

import android.app.Application
import android.content.Context

class VolumeControlApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Uygulama başlarken dil ayarlarını zorla uygula
        val languageCode = LanguageHelper.getLanguage(this)
        LanguageHelper.forceUpdateLanguage(this, languageCode)
    }
    
    override fun attachBaseContext(base: Context?) {
        val context = base?.let { LanguageHelper.updateBaseContextLanguage(it) } ?: base
        super.attachBaseContext(context)
    }
}
