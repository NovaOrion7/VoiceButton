package com.example.voicebutton

import android.content.Context
import android.content.res.Configuration
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
    
    fun applyLanguage(context: Context) {
        val languageCode = getLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // Modern Android için context güncelleme
        context.createConfigurationContext(config)
    }
    
    fun updateBaseContextLanguage(context: Context): Context {
        val languageCode = getLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
}
