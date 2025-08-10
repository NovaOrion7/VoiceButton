package com.novaorion.volumecontrol

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.media.audiofx.Equalizer
import android.util.Log

object AudioEffectsHelper {
    
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var equalizer: Equalizer? = null
    private var isInitialized = false
    
    data class AudioPreset(
        val name: String,
        val description: String,
        val icon: String,
        val bassBoost: Int = 0,
        val virtualizer: Int = 0,
        val eq: Map<String, Int> = emptyMap()
    )
    
    // AudioEffect'leri başlat
    private fun initializeAudioEffects(context: Context) {
        if (isInitialized) return
        
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val sessionId = 0 // Global session ID
            
            // BassBoost oluştur
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = false
            }
            
            // Virtualizer oluştur
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = false
            }
            
            // Equalizer oluştur (opsiyonel)
            try {
                equalizer = Equalizer(0, sessionId).apply {
                    enabled = false
                }
            } catch (e: Exception) {
                Log.w("AudioEffects", "Equalizer not available: ${e.message}")
                equalizer = null
            }
            
            isInitialized = true
            Log.d("AudioEffects", "Audio effects initialized successfully")
            
        } catch (e: Exception) {
            Log.e("AudioEffects", "Failed to initialize audio effects: ${e.message}")
            isInitialized = false
            bassBoost = null
            virtualizer = null
            equalizer = null
        }
    }
    
    // Güçlü bass boost uygula - asenkron hale getirildi
    fun applyStrongBassBoost(context: Context, level: Int = 80) {
        try {
            initializeAudioEffects(context)
        } catch (e: Exception) {
            Log.w("AudioEffects", "Could not initialize for bass boost: ${e.message}")
            return
        }
        
        try {
            // BassBoost uygula
            bassBoost?.let { bass ->
                bass.enabled = true
                val maxStrength = bass.roundedStrength
                val targetStrength = (maxStrength * level / 100).toShort()
                bass.setStrength(targetStrength)
                Log.d("AudioEffects", "Bass boost applied: $targetStrength/$maxStrength")
            }
            
            // Virtualizer da uygula
            virtualizer?.let { virt ->
                virt.enabled = true
                val maxStrength = virt.roundedStrength
                val targetStrength = (maxStrength * 60 / 100).toShort()
                virt.setStrength(targetStrength)
                Log.d("AudioEffects", "Virtualizer applied: $targetStrength/$maxStrength")
            }
            
            // Volume boost da uygula
            AdvancedVolumeHelper.applyVolumeBoost(context, level)
            
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error applying bass boost: ${e.message}")
            // Fallback: Sadece volume boost
            AdvancedVolumeHelper.applyVolumeBoost(context, level * 2)
        }
    }
    
    fun getDefaultPresets(context: Context): Map<String, AudioPreset> {
        return mapOf(
            "normal" to AudioPreset(
                name = context.getString(R.string.normal_preset),
                description = "Standart ses ayarları",
                icon = "🎵"
            ),
            "rock" to AudioPreset(
                name = context.getString(R.string.rock_preset),
                description = context.getString(R.string.preset_rock_desc),
                icon = "🤘",
                bassBoost = 700,
                virtualizer = 500
            ),
            "pop" to AudioPreset(
                name = context.getString(R.string.pop_preset),
                description = context.getString(R.string.preset_pop_desc),
                icon = "🎤",
                bassBoost = 400,
                virtualizer = 300
            ),
            "classical" to AudioPreset(
                name = context.getString(R.string.classical_preset),
                description = context.getString(R.string.preset_classical_desc),
                icon = "🎼",
                bassBoost = 200,
                virtualizer = 800
            ),
            "jazz" to AudioPreset(
                name = context.getString(R.string.jazz_preset),
                description = context.getString(R.string.preset_jazz_desc),
                icon = "🎷",
                bassBoost = 300,
                virtualizer = 600
            ),
            "electronic" to AudioPreset(
                name = context.getString(R.string.electronic_preset),
                description = context.getString(R.string.preset_electronic_desc),
                icon = "🎧",
                bassBoost = 900,
                virtualizer = 400
            )
        )
    }
    
    fun applyPreset(context: Context, presetId: String) {
        initializeAudioEffects(context)
        
        val prefs = context.getSharedPreferences("audio_effects", Context.MODE_PRIVATE)
        val preset = getDefaultPresets(context)[presetId]
        
        prefs.edit()
            .putString("current_preset", presetId)
            .putInt("bass_boost", preset?.bassBoost ?: 0)
            .putInt("virtualizer", preset?.virtualizer ?: 0)
            .apply()
        
        // Preset'e göre özel ayarlar
        when (presetId) {
            "rock", "electronic" -> applyStrongBassBoost(context, 90)
            "pop" -> applyStrongBassBoost(context, 60)
            "jazz" -> applyStrongBassBoost(context, 40)
            "classical" -> applyStrongBassBoost(context, 20)
            else -> clearAllEffects()
        }
    }
    
    // Tüm efektleri temizle
    private fun clearAllEffects() {
        try {
            bassBoost?.enabled = false
            virtualizer?.enabled = false
            equalizer?.enabled = false
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error clearing effects: ${e.message}")
        }
    }
    
    fun getCurrentPreset(context: Context): String {
        val prefs = context.getSharedPreferences("audio_effects", Context.MODE_PRIVATE)
        return prefs.getString("current_preset", "normal") ?: "normal"
    }
    
    fun getPresetIcon(presetId: String): String {
        return when(presetId) {
            "rock" -> "🤘"
            "pop" -> "🎤"
            "classical" -> "🎼"
            "jazz" -> "🎷"
            "electronic" -> "🎧"
            else -> "🎵"
        }
    }
    
    fun getEffectsSummary(context: Context): String {
        val currentPreset = getCurrentPreset(context)
        val presets = getDefaultPresets(context)
        return presets[currentPreset]?.name ?: "Normal"
    }
    
    fun isEffectsEnabled(context: Context): Boolean {
        val currentPreset = getCurrentPreset(context)
        return currentPreset != "normal"
    }
    
    // AudioEffect'leri temizle
    fun releaseEffects() {
        try {
            bassBoost?.release()
            virtualizer?.release()
            equalizer?.release()
            bassBoost = null
            virtualizer = null
            equalizer = null
            isInitialized = false
            Log.d("AudioEffects", "Audio effects released")
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error releasing effects: ${e.message}")
        }
    }
    
    // Efektlerin aktif olup olmadığını kontrol et
    fun areEffectsActive(): Boolean {
        return try {
            (bassBoost?.enabled == true) || (virtualizer?.enabled == true) || (equalizer?.enabled == true)
        } catch (e: Exception) {
            false
        }
    }
    
    // Bass boost seviyesini kontrol et
    fun getCurrentBassLevel(): Int {
        return try {
            bassBoost?.let { bass ->
                if (bass.enabled) {
                    val current = bass.roundedStrength
                    val max = 1000 // BassBoost max değeri
                    (current * 100 / max)
                } else 0
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
