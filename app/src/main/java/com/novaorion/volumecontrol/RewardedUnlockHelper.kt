package com.novaorion.volumecontrol

import android.content.Context
import android.content.SharedPreferences

object RewardedUnlockHelper {
    private const val PREFS_NAME = "rewarded_unlocks"
    private const val KEY_AUTUMN_THEME_ADS_WATCHED = "autumn_theme_ads_watched"
    private const val KEY_AUTUMN_THEME_UNLOCKED = "autumn_theme_unlocked"
    private const val REQUIRED_ADS_FOR_AUTUMN = 3
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Sonbahar teması için izlenen reklam sayısını al
    fun getAutumnThemeAdsWatched(context: Context): Int {
        return getPrefs(context).getInt(KEY_AUTUMN_THEME_ADS_WATCHED, 0)
    }
    
    // Sonbahar teması için reklam izlendiğinde çağırılır
    fun incrementAutumnThemeAds(context: Context): Int {
        val prefs = getPrefs(context)
        val currentCount = prefs.getInt(KEY_AUTUMN_THEME_ADS_WATCHED, 0)
        val newCount = (currentCount + 1).coerceAtMost(REQUIRED_ADS_FOR_AUTUMN)
        
        prefs.edit().putInt(KEY_AUTUMN_THEME_ADS_WATCHED, newCount).apply()
        
        // Eğer gerekli sayıya ulaştıysa unlock et
        if (newCount >= REQUIRED_ADS_FOR_AUTUMN) {
            unlockAutumnTheme(context)
        }
        
        return newCount
    }
    
    // Sonbahar teması unlock edildi mi kontrol et
    fun isAutumnThemeUnlocked(context: Context): Boolean {
        val prefs = getPrefs(context)
        // Eğer gerekli sayıda reklam izlendiyse otomatik unlock
        val adsWatched = prefs.getInt(KEY_AUTUMN_THEME_ADS_WATCHED, 0)
        val isUnlocked = prefs.getBoolean(KEY_AUTUMN_THEME_UNLOCKED, false)
        
        return isUnlocked || adsWatched >= REQUIRED_ADS_FOR_AUTUMN
    }
    
    // Sonbahar temasını unlock et
    fun unlockAutumnTheme(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_AUTUMN_THEME_UNLOCKED, true).apply()
    }
    
    // Sonbahar teması için kalan reklam sayısı
    fun getRemainingAdsForAutumn(context: Context): Int {
        if (isAutumnThemeUnlocked(context)) return 0
        val watched = getAutumnThemeAdsWatched(context)
        return (REQUIRED_ADS_FOR_AUTUMN - watched).coerceAtLeast(0)
    }
    
    // Gerekli reklam sayısını al
    fun getRequiredAdsForAutumn(): Int = REQUIRED_ADS_FOR_AUTUMN
    
    // Test için - tüm unlockları sıfırla
    fun resetUnlocks(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    // Debug bilgileri
    fun getDebugInfo(context: Context): String {
        val adsWatched = getAutumnThemeAdsWatched(context)
        val isUnlocked = isAutumnThemeUnlocked(context)
        val remaining = getRemainingAdsForAutumn(context)
        
        return """
            Sonbahar Teması:
            - İzlenen reklam: $adsWatched/$REQUIRED_ADS_FOR_AUTUMN
            - Unlock durumu: $isUnlocked
            - Kalan reklam: $remaining
        """.trimIndent()
    }
}
