package com.novaorion.volumecontrol

import android.content.Context
import android.media.AudioManager
import android.util.Log
import java.util.*

object ScheduledVolumeHelper {
    
    fun applyScheduledVolume(context: Context) {
        if (!PreferencesHelper.isScheduledVolumeEnabled(context)) {
            Log.d("ScheduledVolume", "Zamanlanmış ses devre dışı")
            return
        }
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val targetVolumePercent = when (hour) {
            in 6..11 -> PreferencesHelper.getMorningVolume(context) // Sabah 06:00-12:00
            in 12..21 -> PreferencesHelper.getEveningVolume(context) // Akşam 12:00-22:00
            else -> PreferencesHelper.getNightVolume(context) // Gece 22:00-06:00
        }
        
        Log.d("ScheduledVolume", "Saat: $hour:$minute, Hedef ses: $targetVolumePercent%")
        
        setVolumeByPercent(context, targetVolumePercent)
    }
    
    private fun setVolumeByPercent(context: Context, percent: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        if (maxVolume > 0) {
            val targetVolume = (maxVolume * percent / 100).coerceIn(0, maxVolume)
            
            Log.d("ScheduledVolume", "Mevcut ses: $currentVolume/$maxVolume, Hedef: $targetVolume ($percent%)")
            
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVolume,
                AudioManager.FLAG_SHOW_UI
            )
            
            Log.d("ScheduledVolume", "Ses seviyesi ayarlandı: $targetVolume")
        }
    }
    
    fun getCurrentTimeSlot(context: Context): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 6..11 -> context.getString(R.string.morning_volume)
            in 12..21 -> context.getString(R.string.evening_volume)
            else -> context.getString(R.string.night_volume)
        }
    }
    
    fun getScheduleStatus(context: Context): String {
        if (!PreferencesHelper.isScheduledVolumeEnabled(context)) {
            return context.getString(R.string.closed)
        }
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val timeSlot = when (hour) {
            in 6..11 -> "morning"
            in 12..21 -> "evening"
            else -> "night"
        }
        
        val volume = when (timeSlot) {
            "morning" -> PreferencesHelper.getMorningVolume(context)
            "evening" -> PreferencesHelper.getEveningVolume(context)
            else -> PreferencesHelper.getNightVolume(context)
        }
        
        val timeSlotName = when (timeSlot) {
            "morning" -> context.getString(R.string.morning_volume)
            "evening" -> context.getString(R.string.evening_volume)
            else -> context.getString(R.string.night_volume)
        }
        
        return "$timeSlotName: $volume% (${hour}:xx)"
    }
    
    // Test için manuel tetikleme
    fun forceApplySchedule(context: Context) {
        Log.d("ScheduledVolume", "Manuel zamanlanmış ses uygulanıyor...")
        applyScheduledVolume(context)
    }
}
