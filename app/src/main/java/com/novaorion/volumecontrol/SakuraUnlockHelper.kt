package com.novaorion.volumecontrol

import android.content.Context
import android.content.SharedPreferences

object SakuraUnlockHelper {
    private const val PREFS_NAME = "sakura_unlocks"
    private const val KEY_SAKURA_THEME_ADS_WATCHED = "sakura_theme_ads_watched"
    private const val KEY_SAKURA_THEME_UNLOCKED = "sakura_theme_unlocked"
    private const val REQUIRED_ADS_FOR_SAKURA = 3
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Sakura teması için izlenen reklam sayısını al
    fun getSakuraThemeAdsWatched(context: Context): Int {
        return getPrefs(context).getInt(KEY_SAKURA_THEME_ADS_WATCHED, 0)
    }
    
    // Sakura teması için reklam izlendiğinde çağırılır
    fun incrementSakuraThemeAds(context: Context): Int {
        val prefs = getPrefs(context)
        val currentCount = prefs.getInt(KEY_SAKURA_THEME_ADS_WATCHED, 0)
        val newCount = (currentCount + 1).coerceAtMost(REQUIRED_ADS_FOR_SAKURA)
        
        prefs.edit().putInt(KEY_SAKURA_THEME_ADS_WATCHED, newCount).apply()
        
        // Eğer gerekli sayıya ulaştıysa unlock et
        if (newCount >= REQUIRED_ADS_FOR_SAKURA) {
            unlockSakuraTheme(context)
        }
        
        return newCount
    }
    
    // Sakura teması unlock edildi mi kontrol et
    fun isSakuraThemeUnlocked(context: Context): Boolean {
        val prefs = getPrefs(context)
        // Eğer gerekli sayıda reklam izlendiyse otomatik unlock
        val adsWatched = prefs.getInt(KEY_SAKURA_THEME_ADS_WATCHED, 0)
        val isUnlocked = prefs.getBoolean(KEY_SAKURA_THEME_UNLOCKED, false)
        
        return isUnlocked || adsWatched >= REQUIRED_ADS_FOR_SAKURA
    }
    
    // Sakura temasını unlock et
    fun unlockSakuraTheme(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_SAKURA_THEME_UNLOCKED, true).apply()
    }
    
    // Sakura teması için kalan reklam sayısı
    fun getRemainingAdsForSakura(context: Context): Int {
        if (isSakuraThemeUnlocked(context)) return 0
        val watched = getSakuraThemeAdsWatched(context)
        return (REQUIRED_ADS_FOR_SAKURA - watched).coerceAtLeast(0)
    }
    
    // Gerekli reklam sayısını al
    fun getRequiredAdsForSakura(): Int = REQUIRED_ADS_FOR_SAKURA
    
    // Test için - tüm unlockları sıfırla
    fun resetUnlocks(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    // Debug bilgileri
    fun getDebugInfo(context: Context): String {
        val adsWatched = getSakuraThemeAdsWatched(context)
        val isUnlocked = isSakuraThemeUnlocked(context)
        val remaining = getRemainingAdsForSakura(context)
        
        return """
            Sakura Teması:
            - İzlenen reklam: $adsWatched/$REQUIRED_ADS_FOR_SAKURA
            - Unlock durumu: $isUnlocked
            - Kalan reklam: $remaining
        """.trimIndent()
    }
}