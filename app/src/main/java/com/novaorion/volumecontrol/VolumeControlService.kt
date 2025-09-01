package com.novaorion.volumecontrol

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class VolumeControlService : Service() {
    
    companion object {
        const val CHANNEL_ID = "VolumeControlChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_VOLUME_UP = "com.example.voicebutton.VOLUME_UP"
        const val ACTION_VOLUME_DOWN = "com.example.voicebutton.VOLUME_DOWN"
        const val ACTION_STOP_SERVICE = "com.example.voicebutton.STOP_SERVICE"
    }
    
    private lateinit var audioManager: AudioManager
    private lateinit var vibrator: Vibrator
    
    override fun attachBaseContext(base: Context?) {
        val context = base?.let { LanguageHelper.updateBaseContextLanguage(it) } ?: base
        super.attachBaseContext(context)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Dil ayarını uygula
        LanguageHelper.applyLanguage(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_VOLUME_UP -> {
                adjustVolume(AudioManager.ADJUST_RAISE)
            }
            ACTION_VOLUME_DOWN -> {
                adjustVolume(AudioManager.ADJUST_LOWER)
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
            }
        }
        
        // Servisi yeniden başlat
        if (intent?.action == ACTION_VOLUME_UP || intent?.action == ACTION_VOLUME_DOWN) {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.volume_control_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.volume_control_subtitle)
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    
    private fun createNotification(): Notification {
        val volumeUpIntent = Intent(this, VolumeControlService::class.java).apply {
            action = ACTION_VOLUME_UP
        }
        val volumeUpPendingIntent = PendingIntent.getService(
            this, 1, volumeUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val volumeDownIntent = Intent(this, VolumeControlService::class.java).apply {
            action = ACTION_VOLUME_DOWN
        }
        val volumeDownPendingIntent = PendingIntent.getService(
            this, 2, volumeDownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, VolumeControlService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val showPercentage = PreferencesHelper.getShowPercentage(this)
        
        val volumeText = if (showPercentage && maxVolume > 0) {
            "${(currentVolume * 100 / maxVolume)}%"
        } else {
            "$currentVolume/$maxVolume"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.volume_control_active))
            .setContentText("${getString(R.string.current_volume_level)}: $volumeText")
            .setSmallIcon(R.drawable.ic_volume_up)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_volume_down, getString(R.string.volume_down), volumeDownPendingIntent)
            .addAction(R.drawable.ic_volume_up, getString(R.string.volume_up), volumeUpPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop_service), stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    private fun adjustVolume(direction: Int) {
        try {
            val volumeStep = PreferencesHelper.getVolumeStep(this)
            val vibrationEnabled = PreferencesHelper.getVibrationEnabled(this)
            
            // Ses adım boyutuna göre ayarla
            for (i in 1..volumeStep) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    direction,
                    if (i == volumeStep) AudioManager.FLAG_SHOW_UI else 0
                )
            }
            
            // Titreşim
            if (vibrationEnabled && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
            
            // Bildirimi güncelle
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
