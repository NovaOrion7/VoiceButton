package com.novaorion.volumecontrol

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.widget.RemoteViews

class VolumeWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_VOLUME_UP = "com.example.voicebutton.VOLUME_UP"
        const val ACTION_VOLUME_DOWN = "com.example.voicebutton.VOLUME_DOWN"
        const val ACTION_TOGGLE_MUTE = "com.example.voicebutton.TOGGLE_MUTE"
        const val ACTION_REFRESH = "com.example.voicebutton.REFRESH"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volumeStep = PreferencesHelper.getVolumeStep(context)
        
        when (intent.action) {
            ACTION_VOLUME_UP -> {
                for (i in 1..volumeStep) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        if (i == volumeStep) AudioManager.FLAG_SHOW_UI else 0
                    )
                }
                updateAllWidgets(context)
            }
            ACTION_VOLUME_DOWN -> {
                for (i in 1..volumeStep) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        if (i == volumeStep) AudioManager.FLAG_SHOW_UI else 0
                    )
                }
                updateAllWidgets(context)
            }
            ACTION_TOGGLE_MUTE -> {
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                
                if (currentVolume == 0 && maxVolume > 0) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        maxVolume / 2,
                        AudioManager.FLAG_SHOW_UI
                    )
                } else {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        0,
                        AudioManager.FLAG_SHOW_UI
                    )
                }
                updateAllWidgets(context)
            }
            ACTION_REFRESH -> {
                // Widget'ı güncelle ve kullanıcıya geri bildirim ver
                updateAllWidgets(context)
                
                // Vibrasyon geri bildirimi
                if (PreferencesHelper.getVibrationEnabled(context)) {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                    if (vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(50)
                        }
                    }
                }
                
                // Ses seviyesini göster
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI
                )
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VolumeWidget::class.java)
        )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        val views = RemoteViews(context.packageName, R.layout.widget_volume_control)
        
        // Ses seviyesi göstergesi
        val volumeText = if (PreferencesHelper.getShowPercentage(context) && maxVolume > 0) {
            "${(currentVolume * 100 / maxVolume)}%"
        } else {
            "$currentVolume/$maxVolume"
        }
        views.setTextViewText(R.id.widget_volume_text, volumeText)
        
        // Sessiz butonu için icon
        val muteIcon = if (currentVolume == 0) "🔇" else "🔊"
        views.setTextViewText(R.id.widget_mute_button, muteIcon)
        
        // Button click listeners
        views.setOnClickPendingIntent(R.id.widget_volume_down, getPendingIntent(context, ACTION_VOLUME_DOWN))
        views.setOnClickPendingIntent(R.id.widget_volume_up, getPendingIntent(context, ACTION_VOLUME_UP))
        views.setOnClickPendingIntent(R.id.widget_mute_button, getPendingIntent(context, ACTION_TOGGLE_MUTE))
        views.setOnClickPendingIntent(R.id.widget_refresh, getPendingIntent(context, ACTION_REFRESH))
        
        // Widget'ı güncelle
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, VolumeWidget::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
