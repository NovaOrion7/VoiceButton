package com.example.voicebutton

import android.content.Context
import android.media.AudioManager

class ProfileHelper(private val context: Context) {
    
    data class VolumeProfile(
        val name: String,
        val icon: String,
        val description: String,
        val mediaVolume: Int,
        val ringVolume: Int,
        val notificationVolume: Int,
        val alarmVolume: Int
    )
    
    companion object {
        fun getDefaultProfiles(context: Context): Map<String, VolumeProfile> {
            return mapOf(
                "gaming" to VolumeProfile(
                    name = context.getString(R.string.profile_gaming),
                    icon = "🎮",
                    description = context.getString(R.string.profile_gaming_desc),
                    mediaVolume = 80,
                    ringVolume = 50,
                    notificationVolume = 30,
                    alarmVolume = 90
                ),
                "work" to VolumeProfile(
                    name = context.getString(R.string.profile_work),
                    icon = "💼",
                    description = context.getString(R.string.profile_work_desc),
                    mediaVolume = 40,
                    ringVolume = 30,
                    notificationVolume = 20,
                    alarmVolume = 70
                ),
                "sleep" to VolumeProfile(
                    name = context.getString(R.string.profile_sleep),
                    icon = "🌙",
                    description = context.getString(R.string.profile_sleep_desc),
                    mediaVolume = 10,
                    ringVolume = 5,
                    notificationVolume = 0,
                    alarmVolume = 50
                ),
                "home" to VolumeProfile(
                    name = context.getString(R.string.profile_home),
                    icon = "🏠",
                    description = context.getString(R.string.profile_home_desc),
                    mediaVolume = 60,
                    ringVolume = 70,
                    notificationVolume = 50,
                    alarmVolume = 80
                )
            )
        }
        
        fun applyProfile(context: Context, profileId: String, onVolumeChanged: (Int) -> Unit = {}) {
            val profiles = getDefaultProfiles(context)
            val profile = profiles[profileId] ?: return
            
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Apply volume settings
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
                (profile.mediaVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100), 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 
                (profile.ringVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 100), 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 
                (profile.notificationVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 100), 0)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 
                (profile.alarmVolume * audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 100), 0)
            
            // Save current profile
            val prefs = context.getSharedPreferences("volume_profiles", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("current_profile", profileId)
                .apply()
            
            // Track usage
            StatisticsHelper.recordProfileUsage(context, profileId)
            
            // Notify volume change
            onVolumeChanged(profile.mediaVolume)
        }
        
        fun getCurrentProfile(context: Context): String {
            val prefs = context.getSharedPreferences("volume_profiles", Context.MODE_PRIVATE)
            return prefs.getString("current_profile", "home") ?: "home"
        }
    }
}
