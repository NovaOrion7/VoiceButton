package com.novaorion.volumecontrol

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
        
        // Modern API ile dil değiştir (Play Store için)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val locale = Locale(languageCode)
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
                return // Modern API kullanıldı, restart gerekmez
            } catch (e: Exception) {
                // Modern API çalışmazsa eski yönteme devam et
            }
        }
        
        // Manual olarak configuration'ı güncelle (fallback)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Activity'yi recreate et
        activity.recreate()
    }
    
    fun changeLanguageInstantly(activity: Activity, languageCode: String, onComplete: () -> Unit = {}) {
        // Dili kaydet
        setLanguage(activity, languageCode)
        
        // Modern API ile dil değiştir (Play Store için)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val locale = Locale(languageCode)
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
                
                // Callback'i çağır ve return et
                onComplete()
                return // Modern API kullanıldı, restart gerekmez
            } catch (e: Exception) {
                // Modern API çalışmazsa recreation gerekiyor
            }
        }
        
        // Fallback: Manual olarak configuration'ı güncelle
        forceUpdateLanguage(activity, languageCode)
        
        // Callback'i çağır (state güncellemesi için)
        onComplete()
        
        // Activity'yi recreate et (eski Android sürümleri için)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            activity.recreate()
        }
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
    
    fun initializeAppLanguage(context: Context) {
        val languageCode = getLanguage(context)
        
        // Modern API'yi dene (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val locale = Locale(languageCode)
                val localeList = LocaleListCompat.create(locale)
                AppCompatDelegate.setApplicationLocales(localeList)
                return
            } catch (e: Exception) {
                // Modern API çalışmazsa eski yönteme devam et
            }
        }
        
        // Fallback olarak manual güncelleme
        forceUpdateLanguage(context, languageCode)
    }
}
