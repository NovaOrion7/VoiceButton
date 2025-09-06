package com.novaorion.volumecontrol

import android.content.Context
import android.content.SharedPreferences

object AquariumUnlockHelper {
    private const val PREFS_NAME = "aquarium_unlocks"
    private const val KEY_AQUARIUM_THEME_ADS_WATCHED = "aquarium_theme_ads_watched"
    private const val KEY_AQUARIUM_THEME_UNLOCKED = "aquarium_theme_unlocked"
    private const val REQUIRED_ADS_FOR_AQUARIUM = 3
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Akvaryum teması için izlenen reklam sayısını al
    fun getAquariumThemeAdsWatched(context: Context): Int {
        return getPrefs(context).getInt(KEY_AQUARIUM_THEME_ADS_WATCHED, 0)
    }
    
    // Akvaryum teması için reklam izlendiğinde çağırılır
    fun incrementAquariumThemeAds(context: Context): Int {
        val prefs = getPrefs(context)
        val currentCount = prefs.getInt(KEY_AQUARIUM_THEME_ADS_WATCHED, 0)
        val newCount = (currentCount + 1).coerceAtMost(REQUIRED_ADS_FOR_AQUARIUM)
        
        prefs.edit().putInt(KEY_AQUARIUM_THEME_ADS_WATCHED, newCount).apply()
        
        // Eğer gerekli sayıya ulaştıysa unlock et
        if (newCount >= REQUIRED_ADS_FOR_AQUARIUM) {
            unlockAquariumTheme(context)
        }
        
        return newCount
    }
    
    // Akvaryum teması unlock edildi mi kontrol et
    fun isAquariumThemeUnlocked(context: Context): Boolean {
        val prefs = getPrefs(context)
        // Eğer gerekli sayıda reklam izlendiyse otomatik unlock
        val adsWatched = prefs.getInt(KEY_AQUARIUM_THEME_ADS_WATCHED, 0)
        val isUnlocked = prefs.getBoolean(KEY_AQUARIUM_THEME_UNLOCKED, false)
        
        return isUnlocked || adsWatched >= REQUIRED_ADS_FOR_AQUARIUM
    }
    
    // Akvaryum temasını unlock et
    fun unlockAquariumTheme(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_AQUARIUM_THEME_UNLOCKED, true).apply()
    }
    
    // Akvaryum teması için kalan reklam sayısı
    fun getRemainingAdsForAquarium(context: Context): Int {
        if (isAquariumThemeUnlocked(context)) return 0
        val watched = getAquariumThemeAdsWatched(context)
        return (REQUIRED_ADS_FOR_AQUARIUM - watched).coerceAtLeast(0)
    }
    
    // Gerekli reklam sayısını al
    fun getRequiredAdsForAquarium(): Int = REQUIRED_ADS_FOR_AQUARIUM
    
    // Test için - tüm unlockları sıfırla
    fun resetUnlocks(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    // Debug bilgileri
    fun getDebugInfo(context: Context): String {
        val adsWatched = getAquariumThemeAdsWatched(context)
        val isUnlocked = isAquariumThemeUnlocked(context)
        val remaining = getRemainingAdsForAquarium(context)
        
        return """
            Akvaryum Teması:
            - İzlenen reklam: $adsWatched/$REQUIRED_ADS_FOR_AQUARIUM
            - Unlock durumu: $isUnlocked
            - Kalan reklam: $remaining
        """.trimIndent()
    }
}
