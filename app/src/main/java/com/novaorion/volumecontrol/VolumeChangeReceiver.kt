package com.novaorion.volumecontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class VolumeChangeReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_VOLUME_CHANGED = "com.example.voicebutton.VOLUME_CHANGED"
        private var onVolumeChangeListener: ((Int) -> Unit)? = null
        
        fun setOnVolumeChangeListener(listener: (Int) -> Unit) {
            onVolumeChangeListener = listener
        }
        
        fun removeOnVolumeChangeListener() {
            onVolumeChangeListener = null
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
            context?.let { ctx ->
                val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                onVolumeChangeListener?.invoke(currentVolume)
            }
        }
    }
}
