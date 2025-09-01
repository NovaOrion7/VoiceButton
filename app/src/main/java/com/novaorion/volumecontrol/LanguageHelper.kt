package com.novaorion.volumecontrol

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.preference.PreferenceManager
import java.util.Locale

object LanguageHelper {
    private const val LANGUAGE_KEY = "selected_language"
    
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }
    
    fun getLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(LANGUAGE_KEY, "tr") ?: "tr"
    }
    
    fun applyLanguage(context: Context): Context {
        val languageCode = getLanguage(context)
        return updateContextLocale(context, languageCode)
    }
    
    fun updateBaseContextLanguage(context: Context): Context {
        val languageCode = getLanguage(context)
        return updateContextLocale(context, languageCode)
    }
    
    private fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
            return context
        }
    }
    
    fun changeLanguageAndRestart(activity: Activity, languageCode: String) {
        // Dili kaydet
        setLanguage(activity, languageCode)
        
        // Configuration'ı güncelle
        forceUpdateLanguage(activity, languageCode)
        
        // Activity'yi recreate et
        activity.recreate()
    }
    
    fun changeLanguageInstantly(activity: Activity, languageCode: String, onComplete: () -> Unit = {}) {
        // Dili kaydet
        setLanguage(activity, languageCode)
        
        // Güvenilir metod: Manual olarak configuration'ı güncelle
        forceUpdateLanguage(activity, languageCode)
        
        // Service'leri yeniden başlat (bildirimler için)
        restartActiveServices(activity)
        
        // Callback'i çağır (state güncellemesi için)
        onComplete()
        
        // Activity'yi recreate et
        activity.recreate()
    }
    
    fun forceUpdateLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources: Resources = context.resources
        val config: Configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    private fun restartActiveServices(context: Context) {
        try {
            // VolumeControlService yeniden başlat
            val volumeServiceIntent = Intent(context, VolumeControlService::class.java)
            context.stopService(volumeServiceIntent)
            
            // Kısa bekleme sonrası yeniden başlat
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(volumeServiceIntent)
                    } else {
                        context.startService(volumeServiceIntent)
                    }
                } catch (e: Exception) {
                    // Service başlatılamadıysa görmezden gel
                }
            }, 100)
            
            // FloatingButtonService yeniden başlat (eğer aktifse)
            if (FloatingButtonService.isFloatingActive()) {
                val floatingServiceIntent = Intent(context, FloatingButtonService::class.java)
                context.stopService(floatingServiceIntent)
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        floatingServiceIntent.action = FloatingButtonService.ACTION_START_FLOATING
                        context.startService(floatingServiceIntent)
                    } catch (e: Exception) {
                        // Service başlatılamadıysa görmezden gel
                    }
                }, 150)
            }
        } catch (e: Exception) {
            // Service yeniden başlatma hatası - görmezden gel
        }
    }
}