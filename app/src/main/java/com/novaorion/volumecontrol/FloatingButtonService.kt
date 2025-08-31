package com.novaorion.volumecontrol

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class FloatingButtonService : Service() {
    
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var isExpanded = false
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    
    companion object {
        const val ACTION_START_FLOATING = "START_FLOATING"
        const val ACTION_STOP_FLOATING = "STOP_FLOATING"
        private var isRunning = false
        
        fun isFloatingActive(): Boolean = isRunning
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton()
        isRunning = true
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING -> {
                if (floatingView == null) {
                    createFloatingButton()
                }
            }
            ACTION_STOP_FLOATING -> {
                removeFloatingButton()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        removeFloatingButton()
        isRunning = false
    }
    
    private fun createFloatingButton() {
        if (floatingView != null) return
        
        // Layout inflate
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_volume_controls, null)
        
        // WindowManager parametreleri
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        floatingView?.let { view ->
            setupFloatingControls(view, params)
            windowManager.addView(view, params)
        }
    }
    
    private fun setupFloatingControls(view: View, params: WindowManager.LayoutParams) {
        val mainButton = view.findViewById<ImageButton>(R.id.floating_main_button)
        val expandedLayout = view.findViewById<LinearLayout>(R.id.expanded_controls)
        val volumeUpButton = view.findViewById<ImageButton>(R.id.volume_up_button)
        val volumeDownButton = view.findViewById<ImageButton>(R.id.volume_down_button)
        val closeButton = view.findViewById<ImageButton>(R.id.close_button)
        val volumeText = view.findViewById<TextView>(R.id.volume_text)
        
        // Set the floating button size based on preference
        val buttonSize = PreferencesHelper.getFloatingButtonSize(this)
        val buttonSizePx = (buttonSize * resources.displayMetrics.density).toInt()
        val layoutParams = mainButton.layoutParams
        layoutParams.width = buttonSizePx
        layoutParams.height = buttonSizePx
        mainButton.layoutParams = layoutParams
        
        // Ses seviyesini güncelle
        updateVolumeText(volumeText)
        
        // Ses artırma
        volumeUpButton.setOnClickListener {
            try {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
                updateVolumeText(volumeText)
                
                // Vibrasyon ekle
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            } catch (e: Exception) {
                // Hata durumunda log
            }
        }
        
        // Ses azaltma
        volumeDownButton.setOnClickListener {
            try {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
                updateVolumeText(volumeText)
                
                // Vibrasyon ekle
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            } catch (e: Exception) {
                // Hata durumunda log
            }
        }
        
        // Kapatma
        closeButton.setOnClickListener {
            removeFloatingButton()
            stopSelf()
        }
        
        // Sürükleme ve tıklama özelliği
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        
        mainButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = abs(event.rawY - initialTouchY)
                    
                    if (deltaX > 10 || deltaY > 10) {
                        isDragging = true
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Sadece tıklama ise menüyü aç/kapat
                        isExpanded = !isExpanded
                        expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                        mainButton.setImageResource(
                            if (isExpanded) R.drawable.ic_close else R.drawable.ic_volume_up
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun updateVolumeText(volumeText: TextView) {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percentage = if (maxVolume > 0) (currentVolume * 100 / maxVolume) else 0
        volumeText.text = "$percentage%"
    }
    
    private fun removeFloatingButton() {
        floatingView?.let { view ->
            windowManager.removeView(view)
            floatingView = null
        }
    }
}