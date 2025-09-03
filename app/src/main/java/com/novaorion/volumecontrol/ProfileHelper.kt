package com.novaorion.volumecontrol

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.app.NotificationManager
import android.util.Log

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
        private const val TAG = "ProfileHelper"
        
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
        
        private fun hasNotificationPolicyAccess(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.isNotificationPolicyAccessGranted
            } else {
                true
            }
        }
        
        private fun setVolumeStreamSafely(audioManager: AudioManager, streamType: Int, volume: Int) {
            try {
                val maxVolume = audioManager.getStreamMaxVolume(streamType)
                val targetVolume = (volume * maxVolume / 100).coerceIn(0, maxVolume)
                
                // Check if the stream is adjustable
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (!audioManager.isVolumeFixed) {
                        audioManager.setStreamVolume(streamType, targetVolume, 0)
                    }
                } else {
                    audioManager.setStreamVolume(streamType, targetVolume, 0)
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Security exception when setting volume for stream $streamType: ${e.message}")
                // Ignore security exceptions - user doesn't have DND permission
            } catch (e: Exception) {
                Log.e(TAG, "Error setting volume for stream $streamType", e)
            }
        }
        
        fun applyProfile(context: Context, profileId: String, onVolumeChanged: (Int) -> Unit = {}) {
            val profiles = getDefaultProfiles(context)
            val profile = profiles[profileId] ?: return
            
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Check notification policy access for ring and notification volumes
            val hasNotificationAccess = hasNotificationPolicyAccess(context)
            
            try {
                // Media volume - usually safe to change
                setVolumeStreamSafely(audioManager, AudioManager.STREAM_MUSIC, profile.mediaVolume)
                
                // Ring and notification volumes - require DND permission on modern Android
                if (hasNotificationAccess) {
                    setVolumeStreamSafely(audioManager, AudioManager.STREAM_RING, profile.ringVolume)
                    setVolumeStreamSafely(audioManager, AudioManager.STREAM_NOTIFICATION, profile.notificationVolume)
                } else {
                    Log.w(TAG, "No notification policy access - skipping ring/notification volume changes")
                }
                
                // Alarm volume - usually safe to change
                setVolumeStreamSafely(audioManager, AudioManager.STREAM_ALARM, profile.alarmVolume)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error applying profile $profileId", e)
            }
            
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
        
        fun requestNotificationPolicyAccess(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        false
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening notification policy settings", e)
                        false
                    }
                } else {
                    true
                }
            } else {
                true
            }
        }
    }
}
