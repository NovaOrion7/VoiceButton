package com.novaorion.volumecontrol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat

class NightLightService : Service() {
    
    companion object {
        const val ACTION_START_NIGHT_LIGHT = "START_NIGHT_LIGHT"
        const val ACTION_STOP_NIGHT_LIGHT = "STOP_NIGHT_LIGHT"
        const val ACTION_UPDATE_INTENSITY = "UPDATE_INTENSITY"
        const val EXTRA_INTENSITY = "intensity"
        
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "night_light_channel"
        
        private var isServiceRunning = false
        
        fun isRunning(): Boolean = isServiceRunning
    }
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isRunning = false
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_NIGHT_LIGHT -> {
                val intensity = intent.getIntExtra(EXTRA_INTENSITY, PreferencesHelper.NIGHT_LIGHT_INTENSITY_MEDIUM)
                startNightLight(intensity)
            }
            ACTION_STOP_NIGHT_LIGHT -> {
                stopNightLight()
            }
            ACTION_UPDATE_INTENSITY -> {
                val intensity = intent.getIntExtra(EXTRA_INTENSITY, PreferencesHelper.NIGHT_LIGHT_INTENSITY_MEDIUM)
                updateIntensity(intensity)
            }
        }
        
        return if (PreferencesHelper.isNightLightPersistent(this)) {
            START_STICKY
        } else {
            START_NOT_STICKY
        }
    }
    
    private fun startNightLight(intensity: Int) {
        if (isRunning) return
        
        try {
            createOverlay(intensity)
            createNotification()
            isRunning = true
            isServiceRunning = true
            
            // Save state
            PreferencesHelper.setNightLightEnabled(this, true)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }
    
    private fun stopNightLight() {
        if (!isRunning) return
        
        removeOverlay()
        stopForeground(true)
        isRunning = false
        isServiceRunning = false
        
        // Save state
        PreferencesHelper.setNightLightEnabled(this, false)
        
        stopSelf()
    }
    
    private fun updateIntensity(intensity: Int) {
        if (!isRunning) return
        
        removeOverlay()
        createOverlay(intensity)
        
        // Save new intensity
        PreferencesHelper.setNightLightIntensity(this, intensity)
    }
    
    private fun createOverlay(intensity: Int) {
        if (overlayView != null) return
        
        // Create a semi-transparent orange overlay to filter blue light
        val overlay = FrameLayout(this).apply {
            setBackgroundColor(getFilterColor(intensity))
        }
        
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
        }
        
        windowManager?.addView(overlay, params)
        overlayView = overlay
    }
    
    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }
    
    private fun getFilterColor(intensity: Int): Int {
        // Orange/amber filter to reduce blue light
        // Higher intensity = more filtering
        val alpha = (255 * intensity / 100).coerceIn(0, 255)
        return Color.argb(alpha, 255, 150, 50) // Orange-amber color
    }
    
    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, NightLightService::class.java).apply {
            action = ACTION_STOP_NIGHT_LIGHT
        }
        
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.night_light_notification_title))
            .setContentText(getString(R.string.night_light_notification_content))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.stop_night_light), stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.night_light_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.night_light_description)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        isRunning = false
        isServiceRunning = false
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
