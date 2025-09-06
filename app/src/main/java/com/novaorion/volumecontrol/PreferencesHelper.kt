package com.novaorion.volumecontrol

import android.content.Context
import androidx.preference.PreferenceManager

object PreferencesHelper {
    private const val VOLUME_STEP_KEY = "volume_step"
    private const val SHOW_PERCENTAGE_KEY = "show_percentage"
    private const val VIBRATION_KEY = "vibration_enabled"
    private const val AUTO_START_KEY = "auto_start"
    private const val THEME_KEY = "theme_mode"
    private const val SCHEDULED_VOLUME_KEY = "scheduled_volume"
    private const val MORNING_VOLUME_KEY = "morning_volume"
    private const val EVENING_VOLUME_KEY = "evening_volume"
    private const val NIGHT_VOLUME_KEY = "night_volume"
    private const val FLOATING_BUTTON_SIZE_KEY = "floating_button_size"
    private const val NIGHT_LIGHT_ENABLED_KEY = "night_light_enabled"
    private const val NIGHT_LIGHT_INTENSITY_KEY = "night_light_intensity"
    
    // Tema modları
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_AUTO = 2
    const val THEME_AUTUMN = 3
    const val THEME_SAKURA = 4  // Add new sakura theme constant
    const val THEME_AQUARIUM = 5  // Add new aquarium theme constant
    
    // Floating button sizes (in dp)
    const val FLOATING_BUTTON_SIZE_SMALL = 25
    const val FLOATING_BUTTON_SIZE_MEDIUM = 40
    const val FLOATING_BUTTON_SIZE_LARGE = 56
    
    // Night light intensity levels
    const val NIGHT_LIGHT_INTENSITY_LOW = 25
    const val NIGHT_LIGHT_INTENSITY_MEDIUM = 50
    const val NIGHT_LIGHT_INTENSITY_HIGH = 75
    const val NIGHT_LIGHT_INTENSITY_MAXIMUM = 100
    
    fun getVolumeStep(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(VOLUME_STEP_KEY, 1)
    }
    
    fun setVolumeStep(context: Context, step: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(VOLUME_STEP_KEY, step).apply()
    }
    
    fun getShowPercentage(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(SHOW_PERCENTAGE_KEY, false)
    }
    
    fun setShowPercentage(context: Context, show: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(SHOW_PERCENTAGE_KEY, show).apply()
    }
    
    fun getVibrationEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(VIBRATION_KEY, true)
    }
    
    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(VIBRATION_KEY, enabled).apply()
    }
    
    fun getAutoStart(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(AUTO_START_KEY, false)
    }
    
    fun setAutoStart(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(AUTO_START_KEY, enabled).apply()
    }
    
    // Tema ayarları
    fun getTheme(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(THEME_KEY, THEME_AUTO)
    }
    
    fun setTheme(context: Context, theme: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(THEME_KEY, theme).apply()
    }
    
    // Zamanlanmış ses ayarları
    fun isScheduledVolumeEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(SCHEDULED_VOLUME_KEY, false)
    }
    
    fun setScheduledVolumeEnabled(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(SCHEDULED_VOLUME_KEY, enabled).apply()
    }
    
    fun getMorningVolume(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(MORNING_VOLUME_KEY, 50)
    }
    
    fun setMorningVolume(context: Context, volume: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(MORNING_VOLUME_KEY, volume).apply()
    }
    
    fun getEveningVolume(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(EVENING_VOLUME_KEY, 70)
    }
    
    fun setEveningVolume(context: Context, volume: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(EVENING_VOLUME_KEY, volume).apply()
    }
    
    fun getNightVolume(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(NIGHT_VOLUME_KEY, 20)
    }
    
    fun setNightVolume(context: Context, volume: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(NIGHT_VOLUME_KEY, volume).apply()
    }
    
    // Floating button size
    fun getFloatingButtonSize(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(FLOATING_BUTTON_SIZE_KEY, FLOATING_BUTTON_SIZE_LARGE)
    }
    
    fun setFloatingButtonSize(context: Context, size: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(FLOATING_BUTTON_SIZE_KEY, size).apply()
    }
    
    // Night light settings
    fun isNightLightEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(NIGHT_LIGHT_ENABLED_KEY, false)
    }
    
    fun setNightLightEnabled(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(NIGHT_LIGHT_ENABLED_KEY, enabled).apply()
    }
    
    fun getNightLightIntensity(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(NIGHT_LIGHT_INTENSITY_KEY, NIGHT_LIGHT_INTENSITY_MEDIUM)
    }
    
    fun setNightLightIntensity(context: Context, intensity: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putInt(NIGHT_LIGHT_INTENSITY_KEY, intensity).apply()
    }
}