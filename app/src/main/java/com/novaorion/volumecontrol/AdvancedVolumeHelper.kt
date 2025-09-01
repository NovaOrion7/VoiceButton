package com.novaorion.volumecontrol

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.preference.PreferenceManager

object AdvancedVolumeHelper {
    
    // LoudnessEnhancer için değişkenler
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private const val VOLUME_BOOST_KEY = "volume_boost_level"
    private const val VOLUME_BOOST_ENABLED_KEY = "volume_boost_enabled"
    
    // Ses türleri için sabitler
    object StreamTypes {
        const val MEDIA = AudioManager.STREAM_MUSIC
        const val RING = AudioManager.STREAM_RING
        const val NOTIFICATION = AudioManager.STREAM_NOTIFICATION
        const val ALARM = AudioManager.STREAM_ALARM
        const val CALL = AudioManager.STREAM_VOICE_CALL
        const val SYSTEM = AudioManager.STREAM_SYSTEM
    }
    
    data class VolumeInfo(
        val current: Int,
        val max: Int,
        val percentage: Int
    )
    
    fun getVolumeInfo(context: Context, streamType: Int): VolumeInfo {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val current = audioManager.getStreamVolume(streamType)
        val max = audioManager.getStreamMaxVolume(streamType)
        val percentage = if (max > 0) (current * 100 / max) else 0
        
        return VolumeInfo(current, max, percentage)
    }
    
    fun setVolume(context: Context, streamType: Int, volume: Int, showUI: Boolean = false) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val targetVolume = volume.coerceIn(0, maxVolume)
        
        audioManager.setStreamVolume(
            streamType,
            targetVolume,
            if (showUI) AudioManager.FLAG_SHOW_UI else 0
        )
    }
    
    fun setVolumeByPercentage(context: Context, streamType: Int, percentage: Int, showUI: Boolean = false) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val targetVolume = if (maxVolume > 0) (maxVolume * percentage / 100) else 0
        
        setVolume(context, streamType, targetVolume, showUI)
    }
    
    fun adjustVolume(context: Context, streamType: Int, direction: Int, steps: Int = 1) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        for (i in 1..steps) {
            audioManager.adjustStreamVolume(
                streamType,
                direction,
                if (i == steps) AudioManager.FLAG_SHOW_UI else 0
            )
        }
    }
    
    fun getAllVolumeInfo(context: Context): Map<String, VolumeInfo> {
        return mapOf(
            "media" to getVolumeInfo(context, StreamTypes.MEDIA),
            "ring" to getVolumeInfo(context, StreamTypes.RING),
            "notification" to getVolumeInfo(context, StreamTypes.NOTIFICATION),
            "alarm" to getVolumeInfo(context, StreamTypes.ALARM),
            "call" to getVolumeInfo(context, StreamTypes.CALL),
            "system" to getVolumeInfo(context, StreamTypes.SYSTEM)
        )
    }
    
    fun getStreamTypeByName(name: String): Int {
        return when (name.lowercase()) {
            "media" -> StreamTypes.MEDIA
            "ring" -> StreamTypes.RING
            "notification" -> StreamTypes.NOTIFICATION
            "alarm" -> StreamTypes.ALARM
            "call" -> StreamTypes.CALL
            "system" -> StreamTypes.SYSTEM
            else -> StreamTypes.MEDIA
        }
    }
    
    fun getStreamDisplayName(context: Context, streamType: Int): String {
        return when (streamType) {
            StreamTypes.MEDIA -> context.getString(R.string.media_volume)
            StreamTypes.RING -> context.getString(R.string.ring_volume)
            StreamTypes.NOTIFICATION -> context.getString(R.string.notification_volume)
            StreamTypes.ALARM -> context.getString(R.string.alarm_volume)
            StreamTypes.CALL -> context.getString(R.string.call_volume)
            StreamTypes.SYSTEM -> context.getString(R.string.system_volume)
            else -> context.getString(R.string.media_volume)
        }
    }
    
    // Hızlı ses seviyeleri
    fun setQuickVolume(context: Context, streamType: Int, level: QuickVolumeLevel) {
        val percentage = when (level) {
            QuickVolumeLevel.SILENT -> 0
            QuickVolumeLevel.LOW -> 25
            QuickVolumeLevel.MEDIUM -> 50
            QuickVolumeLevel.HIGH -> 75
            QuickVolumeLevel.MAX -> 100
        }
        setVolumeByPercentage(context, streamType, percentage, true)
    }
    
    enum class QuickVolumeLevel {
        SILENT, LOW, MEDIUM, HIGH, MAX
    }
    
    // Volume Boost fonksiyonu - Gerçek ses artırma
    fun applyVolumeBoost(context: Context, boostPercentage: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Ana stream türleri için boost uygula
        val streamTypes = listOf(
            StreamTypes.MEDIA,
            StreamTypes.RING,
            StreamTypes.NOTIFICATION,
            StreamTypes.ALARM
        )
        
        streamTypes.forEach { streamType ->
            val currentVolume = audioManager.getStreamVolume(streamType)
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            
            if (currentVolume > 0) {
                // Mevcut ses seviyesini yüzde olarak hesapla
                val currentPercentage = (currentVolume * 100) / maxVolume
                
                // Boost ekle ama maksimum %100'ü geçmesin
                val targetPercentage = (currentPercentage + boostPercentage).coerceAtMost(100)
                val targetVolume = (maxVolume * targetPercentage / 100)
                
                audioManager.setStreamVolume(streamType, targetVolume, 0)
                
                // Ayrıca AudioEffect ile bass boost uygula
                try {
                    AudioEffectsHelper.applyPreset(context, "electronic") // Yüksek bass preset
                } catch (e: Exception) {
                    // AudioEffect çalışmazsa sadece volume boost yap
                }
            }
        }
        
        // İstatistik kaydet
        StatisticsHelper.recordVolumeChange(context)
    }
    
    // Volume boost durumunu kontrol et
    fun isVolumeBoostActive(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mediaVolume = audioManager.getStreamVolume(StreamTypes.MEDIA)
        val maxVolume = audioManager.getStreamMaxVolume(StreamTypes.MEDIA)
        
        // Eğer ses seviyesi maksimumun %85'inden fazlaysa boost aktif sayılır
        return mediaVolume > (maxVolume * 0.85)
    }
    
    // Boost'u temizle/normale döndür
    fun clearVolumeBoost(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val streamTypes = listOf(
            StreamTypes.MEDIA,
            StreamTypes.RING,
            StreamTypes.NOTIFICATION,
            StreamTypes.ALARM
        )
        
        streamTypes.forEach { streamType ->
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val normalVolume = (maxVolume * 0.7).toInt() // %70 seviyesine düşür
            
            audioManager.setStreamVolume(streamType, normalVolume, 0)
        }
        
        // LoudnessEnhancer'ı da temizle
        clearLoudnessBoost()
    }
    
    // ========== Yeni LoudnessEnhancer Tabanlı Volume Boost ==========
    
    // Volume boost seviyesini kaydet
    fun setVolumeBoostLevel(context: Context, level: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putInt(VOLUME_BOOST_KEY, level)
            .putBoolean(VOLUME_BOOST_ENABLED_KEY, level > 0)
            .apply()
    }
    
    // Volume boost seviyesini al
    fun getVolumeBoostLevel(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(VOLUME_BOOST_KEY, 0)
    }
    
    // Volume boost aktif mi?
    fun isVolumeBoostEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(VOLUME_BOOST_ENABLED_KEY, false)
    }
    
    // LoudnessEnhancer ile gerçek ses artırma
    fun applyLoudnessBoost(context: Context, boostLevel: Int = -1) {
        try {
            val level = if (boostLevel >= 0) boostLevel else getVolumeBoostLevel(context)
            
            // Mevcut LoudnessEnhancer'ı temizle
            clearLoudnessBoost()
            
            if (level > 0) {
                // LoudnessEnhancer oluştur (global session ID ile)
                loudnessEnhancer = LoudnessEnhancer(0).apply {
                    // Seviye 0-100 arası, LoudnessEnhancer millibell cinsinden çalışır
                    // 1000 millibell = 1 decibel, maksimum 2000 millibell (2dB) güvenli
                    val targetGain = (level * 20).toFloat() // 0-2000 millibell arası
                    setTargetGain(targetGain.toInt())
                    enabled = true
                    Log.d("VolumeBoost", "Applied loudness boost: ${targetGain}mB (${level}%)")
                }
                
                // Ayarı kaydet
                setVolumeBoostLevel(context, level)
            } else {
                // Volume boost kapalı
                setVolumeBoostLevel(context, 0)
                Log.d("VolumeBoost", "Loudness boost disabled")
            }
            
        } catch (e: Exception) {
            Log.e("VolumeBoost", "Error applying loudness boost: ${e.message}")
            // LoudnessEnhancer desteklenmiyorsa normal volume boost yap
            applyVolumeBoost(context, boostLevel)
        }
    }
    
    // LoudnessEnhancer'ı temizle
    fun clearLoudnessBoost() {
        try {
            loudnessEnhancer?.apply {
                enabled = false
                release()
            }
            loudnessEnhancer = null
            Log.d("VolumeBoost", "LoudnessEnhancer cleared")
        } catch (e: Exception) {
            Log.e("VolumeBoost", "Error clearing LoudnessEnhancer: ${e.message}")
        }
    }
    
    // Volume boost durumunu al
    fun getVolumeBoostStatus(context: Context): String {
        val level = getVolumeBoostLevel(context)
        return when {
            level == 0 -> context.getString(R.string.disabled)
            level <= 30 -> context.getString(R.string.low)
            level <= 60 -> context.getString(R.string.medium)
            level <= 85 -> context.getString(R.string.high)
            else -> context.getString(R.string.maximum)
        }
    }
    
    // Güvenli seviye kontrolü
    fun isSafeLevel(level: Int): Boolean {
        return level <= 75 // %75'ten sonra tehlikeli kabul et
    }
    
    // LoudnessEnhancer desteği kontrolü
    fun isLoudnessEnhancerSupported(): Boolean {
        return try {
            val test = LoudnessEnhancer(0)
            test.release()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Acil durum volume boost (maksimum güvenli seviye)
    fun emergencyVolumeBoost(context: Context) {
        try {
            // Önce sistem sesini maksimuma çıkar
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI)
            
            // Sonra LoudnessEnhancer ile boost ekle
            applyLoudnessBoost(context, 75) // Güvenli maksimum
            
            Log.d("VolumeBoost", "Emergency volume boost applied")
            
        } catch (e: Exception) {
            Log.e("VolumeBoost", "Error in emergency volume boost: ${e.message}")
        }
    }
}
