package com.example.voicebutton

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
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
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    fun changeLanguageAndRestart(activity: Activity, languageCode: String) {
        // Dili kaydet
        setLanguage(activity, languageCode)
        
        // Task'ı temizle ve aktiviteyi yeniden başlat
        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        activity.startActivity(intent)
        activity.finish()
    }
}
