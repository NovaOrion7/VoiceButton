package com.novaorion.volumecontrol

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import kotlinx.coroutines.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import com.novaorion.volumecontrol.ui.theme.VoiceButtonTheme
import com.novaorion.volumecontrol.ui.FallingLeavesBackground
import com.novaorion.volumecontrol.ui.TestAnimation
import com.novaorion.volumecontrol.ui.SimpleAutumnAnimation
import com.novaorion.volumecontrol.ui.SimpleSakuraAnimation
import com.novaorion.volumecontrol.ui.SimpleAquariumAnimation
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // İzin verildi, servisi başlat
        }
    }
    
    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                // Overlay izni verildi, floating button başlatılabilir
            }
        }
    }
    
    private lateinit var volumeChangeReceiver: VolumeChangeReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash ekranını yükle
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Dili zorla uygula
        val languageCode = LanguageHelper.getLanguage(this)
        LanguageHelper.forceUpdateLanguage(this, languageCode)
        
        // Volume change receiver'ı başlat
        volumeChangeReceiver = VolumeChangeReceiver()
        
        // AdMob'u başlat
        AdMobHelper.initializeAds(this)
        AdMobHelper.loadInterstitialAd(this)
        // Ödüllü reklam yükle
        AdMobHelper.loadRewardedAd(this)
        // Test ad configuration
        AdMobHelper.testAdConfiguration()
        
        // Edge-to-edge ve sistem UI ayarları
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Bildirim izni kontrol et
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // İzin var
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        
        setContent {
            VoiceButtonTheme {
                // Normal yapı geri
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VolumeControlScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Volume change receiver'ı kaydet
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeChangeReceiver, filter)
    }
    
    override fun onPause() {
        super.onPause()
        // Volume change receiver'ı kayıttan çıkar
        try {
            unregisterReceiver(volumeChangeReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver zaten unregister edilmiş
        }
    }
    
    fun startOverlayPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            requestOverlayPermissionLauncher.launch(intent)
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LanguageHelper.updateBaseContextLanguage(it) } ?: newBase)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeControlScreen() {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableStateOf(0) }
    var maxVolume by remember { mutableStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(LanguageHelper.getLanguage(context)) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showVolumeStepDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showAdvancedVolumeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showProfilesDialog by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showFloatingDialog by remember { mutableStateOf(false) }
    var showFloatingSizeDialog by remember { mutableStateOf(false) }
    var showVolumeBoostDialog by remember { mutableStateOf(false) }
    var showNightLightDialog by remember { mutableStateOf(false) }
    var showNightLightIntensityDialog by remember { mutableStateOf(false) }
    var showPercentage by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var volumeStep by remember { mutableIntStateOf(1) }
    var currentTheme by remember { mutableIntStateOf(PreferencesHelper.THEME_AUTO) }
    var scheduledVolumeEnabled by remember { mutableStateOf(false) }
    var floatingButtonSize by remember { mutableIntStateOf(PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE) }
    var volumeBoostLevel by remember { mutableIntStateOf(AdvancedVolumeHelper.getVolumeBoostLevel(context)) }
    var allVolumeInfo by remember { mutableStateOf(emptyMap<String, AdvancedVolumeHelper.VolumeInfo>()) }
    var currentProfile by remember { mutableStateOf("varsayilan") }
    var isNightLightRunning by remember { mutableStateOf(NightLightService.isRunning()) }
    var nightLightIntensity by remember { mutableIntStateOf(PreferencesHelper.getNightLightIntensity(context)) }
    var statsData by remember { mutableStateOf(emptyMap<String, Any>()) }
    // Interstitial ad counter
    var adCounter by remember { mutableIntStateOf(0) }
    
    // Sonbahar teması unlock sistemi için state'ler
    var showAutumnUnlockDialog by remember { mutableStateOf(false) }
    var autumnAdsWatched by remember { mutableIntStateOf(0) }
    var autumnAdsRemaining by remember { mutableIntStateOf(3) }
    var isAutumnUnlocked by remember { mutableStateOf(false) }
    
    // Sakura theme unlock variables
    var showSakuraUnlockDialog by remember { mutableStateOf(false) }
    var sakuraAdsWatched by remember { mutableIntStateOf(0) }
    var sakuraAdsRemaining by remember { mutableIntStateOf(3) }
    var isSakuraUnlocked by remember { mutableStateOf(false) }
    
    // Aquarium theme unlock variables
    var showAquariumUnlockDialog by remember { mutableStateOf(false) }
    var aquariumAdsWatched by remember { mutableIntStateOf(0) }
    var aquariumAdsRemaining by remember { mutableIntStateOf(3) }
    var isAquariumUnlocked by remember { mutableStateOf(false) }
    
    var isRewardedAdReady by remember { mutableStateOf(false) }
    
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val scrollState = rememberScrollState()
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
    }
    
    // Ses seviyesini güncelle
    LaunchedEffect(Unit) {
        try {
            // Volume change listener'ı ayarla
            VolumeChangeReceiver.setOnVolumeChangeListener { newVolume ->
                currentVolume = newVolume
            }
            
            // Önce temel ses ayarlarını al
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            // Arka plan işlemlerini IO dispatcher'da yap
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Session başlangıcını kaydet
                    StatisticsHelper.recordSessionStart(context)
                    // Stats data yükle
                    val stats = StatisticsHelper.getAllStats(context)
                    // Current profile yükle
                    val profile = ProfileHelper.getCurrentProfile(context)
                    // Preferences yükle
                    val language = LanguageHelper.getLanguage(context)
                    val showPerc = PreferencesHelper.getShowPercentage(context)
                    val vibEnabled = PreferencesHelper.getVibrationEnabled(context)
                    val volStep = PreferencesHelper.getVolumeStep(context)
                    val theme = PreferencesHelper.getTheme(context)
                    val schedEnabled = PreferencesHelper.isScheduledVolumeEnabled(context)
                    val floatButtonSize = PreferencesHelper.getFloatingButtonSize(context)
                    val volumeInfo = AdvancedVolumeHelper.getAllVolumeInfo(context)
                    
                    // Night light preferences
                    val nightLightRunning = NightLightService.isRunning()
                    val nightLightIntens = PreferencesHelper.getNightLightIntensity(context)
                    
                    // Sonbahar teması unlock durumunu kontrol et
                    val autumnUnlocked = RewardedUnlockHelper.isAutumnThemeUnlocked(context)
                    val adsWatched = RewardedUnlockHelper.getAutumnThemeAdsWatched(context)
                    val adsRemaining = RewardedUnlockHelper.getRemainingAdsForAutumn(context)
                    
                    // Sakura teması unlock durumunu kontrol et
                    val sakuraUnlocked = SakuraUnlockHelper.isSakuraThemeUnlocked(context)
                    val sakuraAdsWatchedVal = SakuraUnlockHelper.getSakuraThemeAdsWatched(context)
                    val sakuraAdsRemainingVal = SakuraUnlockHelper.getRemainingAdsForSakura(context)
                    
                    // Akvaryum teması unlock durumunu kontrol et
                    val aquariumUnlocked = AquariumUnlockHelper.isAquariumThemeUnlocked(context)
                    val aquariumAdsWatchedVal = AquariumUnlockHelper.getAquariumThemeAdsWatched(context)
                    val aquariumAdsRemainingVal = AquariumUnlockHelper.getRemainingAdsForAquarium(context)
                    
                    // UI güncellemelerini Main dispatcher'da yap
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        statsData = stats
                        currentProfile = profile
                        selectedLanguage = language
                        showPercentage = showPerc
                        vibrationEnabled = vibEnabled
                        volumeStep = volStep
                        currentTheme = theme
                        scheduledVolumeEnabled = schedEnabled
                        floatingButtonSize = floatButtonSize
                        allVolumeInfo = volumeInfo
                        
                        // Night light states
                        isNightLightRunning = nightLightRunning
                        nightLightIntensity = nightLightIntens
                        
                        // Sonbahar teması unlock durumunu güncelle
                        isAutumnUnlocked = autumnUnlocked
                        autumnAdsWatched = adsWatched
                        autumnAdsRemaining = adsRemaining
                        
                        // Sakura teması unlock durumunu güncelle
                        isSakuraUnlocked = sakuraUnlocked
                        sakuraAdsWatched = sakuraAdsWatchedVal
                        sakuraAdsRemaining = sakuraAdsRemainingVal
                        
                        // Akvaryum teması unlock durumunu güncelle
                        isAquariumUnlocked = aquariumUnlocked
                        aquariumAdsWatched = aquariumAdsWatchedVal
                        aquariumAdsRemaining = aquariumAdsRemainingVal
                    }
                } catch (e: Exception) {
                    // Hata durumunda varsayılan değerleri kullan
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        statsData = emptyMap()
                        currentProfile = "varsayilan"
                        selectedLanguage = "tr"
                        showPercentage = true
                        vibrationEnabled = true
                        volumeStep = 1
                        currentTheme = PreferencesHelper.THEME_AUTO
                        scheduledVolumeEnabled = false
                        floatingButtonSize = PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE
                        allVolumeInfo = emptyMap()
                    }
                }
            }
            
            // Zamanlanmış ses kontrolünü uygula (kısa gecikme ile)
            kotlinx.coroutines.delay(500)
            try {
                ScheduledVolumeHelper.applyScheduledVolume(context)
                // Ses seviyesini tekrar güncelle
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            } catch (e: Exception) {
                // Scheduled volume hatası önemli değil, devam et
            }
            
            // Ödüllü reklam yükle (eğer sonbahar, sakura veya akvaryum unlock edilmemişse)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    if (!RewardedUnlockHelper.isAutumnThemeUnlocked(context) || 
                        !SakuraUnlockHelper.isSakuraThemeUnlocked(context) ||
                        !AquariumUnlockHelper.isAquariumThemeUnlocked(context)) {
                        AdMobHelper.loadRewardedAd(context)
                    }
                } catch (e: Exception) {
                    // Reklam yükleme hatası önemli değil
                }
            }
        } catch (e: Exception) {
            // Genel hata durumunda temel değerleri ayarla
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }
    }
    
    // Reklam durumunu periyodik olarak kontrol et
    LaunchedEffect(showAutumnUnlockDialog) {
        if (showAutumnUnlockDialog) {
            while (showAutumnUnlockDialog) {
                isRewardedAdReady = AdMobHelper.isRewardedAdReady()
                kotlinx.coroutines.delay(1000) // 1 saniyede bir kontrol et
            }
        }
    }
    
    // Dil değişikliği takibi için LaunchedEffect
    LaunchedEffect(context) {
        selectedLanguage = LanguageHelper.getLanguage(context)
    }
    
    // Session bitişini kaydet
    DisposableEffect(Unit) {
        onDispose {
            StatisticsHelper.recordSessionEnd(context)
            VolumeChangeReceiver.removeOnVolumeChangeListener()
        }
    }
    
    // Titreşim fonksiyonu
    fun vibrate() {
        if (vibrationEnabled && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }
    
    // Ses ayarlama fonksiyonu
    fun adjustVolume(direction: Int) {
        for (i in 1..volumeStep) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                if (i == volumeStep) AudioManager.FLAG_SHOW_UI else 0
            )
        }
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        // İstatistik kaydet
        StatisticsHelper.recordVolumeChange(context)
        // Stats data güncelle
        statsData = StatisticsHelper.getAllStats(context)
        
        // Titreşim
        vibrate()
    }
    
    // Belirli ses seviyesi ayarlama
    fun setVolumeLevel(level: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_SHOW_UI)
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        vibrate()
    }
    
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                BannerAdView(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // YAPRAK ANİMASYONU - Scaffold içinde
            if (PreferencesHelper.getTheme(context) == PreferencesHelper.THEME_AUTUMN) {
                SimpleAutumnAnimation()
            }
            
            // SAKURA ANİMASYONU - Scaffold içinde
            if (PreferencesHelper.getTheme(context) == PreferencesHelper.THEME_SAKURA) {
                SimpleSakuraAnimation()
            }
            
            // AKVARYUM ANİMASYONU - Scaffold içinde
            if (PreferencesHelper.getTheme(context) == PreferencesHelper.THEME_AQUARIUM) {
                SimpleAquariumAnimation()
            }
            
            // İçerik üstte
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(16.dp), // Removed the bottom padding that was pushing content up
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dil seçimi butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showLanguageDialog = true },
                    modifier = Modifier.size(width = 120.dp, height = 40.dp)
                ) {
                    Text(
                        text = "🌐 ${context.getString(R.string.language)}",
                        fontSize = 12.sp
                    )
                }
            }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Başlık
        Text(
            text = context.getString(R.string.volume_control_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = context.getString(R.string.volume_control_subtitle),
            fontSize = 16.sp,
            color = getSecondaryTextColor(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Mevcut ses seviyesi
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.current_volume_level),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (maxVolume > 0) {
                        if (showPercentage) 
                            "${(currentVolume * 100 / maxVolume)}%" 
                        else 
                            "$currentVolume / $maxVolume"
                    } else {
                        if (showPercentage) "0%" else "0 / 0"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { if (maxVolume > 0) currentVolume.toFloat() / maxVolume.toFloat() else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Ses kontrolü butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Ses azalt butonu
            Button(
                onClick = { adjustVolume(AudioManager.ADJUST_LOWER) },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text(
                    text = "−",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Sessiz/Sesi aç butonu
            Button(
                onClick = {
                    if (currentVolume == 0) {
                        setVolumeLevel(if (maxVolume > 0) maxVolume / 2 else 5)
                    } else {
                        setVolumeLevel(0)
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentVolume == 0) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = if (currentVolume == 0) "🔇" else "🔊",
                    fontSize = 24.sp
                )
            }
            
            // Ses artır butonu
            Button(
                onClick = { adjustVolume(AudioManager.ADJUST_RAISE) },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Hızlı ayarlar
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.quick_settings),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { setVolumeLevel(0) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = context.getString(R.string.min_volume),
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedButton(
                        onClick = { setVolumeLevel(if (maxVolume > 0) maxVolume / 2 else 0) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = context.getString(R.string.half_volume),
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedButton(
                        onClick = { setVolumeLevel(if (maxVolume > 0) maxVolume else 0) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = context.getString(R.string.max_volume),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Ayarlar
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.settings),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ses adım boyutu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.volume_step),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = context.getString(R.string.volume_step_description),
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    TextButton(onClick = { showVolumeStepDialog = true }) {
                        Text(
                            text = when(volumeStep) {
                                1 -> context.getString(R.string.small_step)
                                2 -> context.getString(R.string.medium_step)
                                3 -> context.getString(R.string.large_step)
                                else -> context.getString(R.string.medium_step)
                            }
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Yüzde gösterimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.show_percentage),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = context.getString(R.string.show_percentage_description),
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    Switch(
                        checked = showPercentage,
                        onCheckedChange = { 
                            showPercentage = it
                            PreferencesHelper.setShowPercentage(context, it)
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Titreşim
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.vibration),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = context.getString(R.string.vibration_description),
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { 
                            vibrationEnabled = it
                            PreferencesHelper.setVibrationEnabled(context, it)
                            if (it) vibrate()
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Floating button size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.floating_button_size),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = context.getString(R.string.floating_button_size_description),
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    TextButton(onClick = { showFloatingSizeDialog = true }) {
                        Text(
                            text = when(floatingButtonSize) {
                                PreferencesHelper.FLOATING_BUTTON_SIZE_SMALL -> context.getString(R.string.small_button)
                                PreferencesHelper.FLOATING_BUTTON_SIZE_MEDIUM -> context.getString(R.string.medium_button)
                                PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE -> context.getString(R.string.large_button)
                                else -> context.getString(R.string.large_button)
                            }
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Tema seçimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.theme),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when(currentTheme) {
                                PreferencesHelper.THEME_LIGHT -> context.getString(R.string.light_theme)
                                PreferencesHelper.THEME_DARK -> context.getString(R.string.dark_theme)
                                PreferencesHelper.THEME_AUTUMN -> context.getString(R.string.autumn_theme)
                                PreferencesHelper.THEME_SAKURA -> context.getString(R.string.sakura_theme)
                                PreferencesHelper.THEME_AQUARIUM -> context.getString(R.string.aquarium_theme)
                                else -> context.getString(R.string.auto_theme)
                            } + " • ${ScheduledVolumeHelper.getScheduleStatus(context)}",
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    TextButton(onClick = { 
                        showThemeDialog = true
                        // Ayarlar açılırken reklam göster
                        if (context is MainActivity) {
                            AdMobHelper.showInterstitialAd(context)
                        }
                    }) {
                        Text(
                            text = when(currentTheme) {
                                PreferencesHelper.THEME_LIGHT -> "☀️"
                                PreferencesHelper.THEME_DARK -> "🌙"
                                PreferencesHelper.THEME_AUTUMN -> "🍂"
                                PreferencesHelper.THEME_SAKURA -> "🌸"
                                PreferencesHelper.THEME_AQUARIUM -> "🐠"
                                else -> "🌙/☀️"
                            }
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Zamanlanmış ses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.scheduled_volume),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = context.getString(R.string.morning_volume) + ", " + 
                                  context.getString(R.string.evening_volume) + ", " +
                                  context.getString(R.string.night_volume),
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    TextButton(onClick = { showScheduleDialog = true }) {
                        Text(text = if (scheduledVolumeEnabled) "⏰" else "⏱️")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Profiller
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.profiles),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${ProfileHelper.getDefaultProfiles(context)[currentProfile]?.icon ?: "🏠"} ${ProfileHelper.getDefaultProfiles(context)[currentProfile]?.name ?: "Ev"}",
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    TextButton(onClick = { 
                        showProfileDialog = true
                        // Profiller açılırken reklam göster
                        if (context is MainActivity) {
                            AdMobHelper.showInterstitialAd(context)
                        }
                    }) {
                        Text(text = "🎮")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Night Light
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.night_light),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isNightLightRunning) 
                                "${context.getString(R.string.night_light_enabled)} (${nightLightIntensity}%)"
                            else 
                                context.getString(R.string.night_light_disabled),
                            fontSize = 12.sp,
                            color = if (isNightLightRunning) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                getSecondaryTextColor()
                        )
                    }
                    Switch(
                        checked = isNightLightRunning,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Check overlay permission
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    // Request overlay permission
                                    (context as MainActivity).startOverlayPermissionRequest()
                                } else {
                                    // Start night light
                                    val startIntent = Intent(context, NightLightService::class.java).apply {
                                        action = NightLightService.ACTION_START_NIGHT_LIGHT
                                        putExtra(NightLightService.EXTRA_INTENSITY, nightLightIntensity)
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(startIntent)
                                    } else {
                                        context.startService(startIntent)
                                    }
                                    isNightLightRunning = true
                                }
                            } else {
                                // Stop night light
                                val stopIntent = Intent(context, NightLightService::class.java).apply {
                                    action = NightLightService.ACTION_STOP_NIGHT_LIGHT
                                }
                                context.startService(stopIntent)
                                isNightLightRunning = false
                            }
                        }
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // İstatistikler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = context.getString(R.string.statistics),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${stringResource(R.string.today)}: ${statsData["today_volume_changes"]} ${stringResource(R.string.changes)}",
                            fontSize = 12.sp,
                            color = getSecondaryTextColor()
                        )
                    }
                    TextButton(onClick = { 
                        showStatsDialog = true
                        // İstatistikler açılırken reklam göster
                        if (context is MainActivity) {
                            AdMobHelper.showInterstitialAd(context)
                        }
                    }) {
                        Text(text = "📊")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Kalıcı bildirim kontrolü
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.persistent_notification),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isServiceRunning) 
                        context.getString(R.string.notification_active) 
                    else 
                        context.getString(R.string.notification_inactive),
                    fontSize = 16.sp,
                    color = if (isServiceRunning) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        getSecondaryTextColor()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (isServiceRunning) {
                            // Servisi durdur
                            val stopIntent = Intent(context, VolumeControlService::class.java).apply {
                                action = VolumeControlService.ACTION_STOP_SERVICE
                            }
                            context.startService(stopIntent)
                            isServiceRunning = false
                        } else {
                            // Servisi başlat
                            val startIntent = Intent(context, VolumeControlService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(startIntent)
                            } else {
                                context.startService(startIntent)
                            }
                            isServiceRunning = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isServiceRunning) 
                            context.getString(R.string.stop_service_button) 
                        else 
                            context.getString(R.string.start_service),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (!isServiceRunning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = context.getString(R.string.notification_description),
                        fontSize = 12.sp,
                        color = getSecondaryTextColor(),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Widget ekleme butonu
                OutlinedButton(
                    onClick = {
                        // Widget ekleme işlemi
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val myProvider = ComponentName(context, VolumeWidget::class.java)
                            
                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                            } else {
                                // Fallback: Widget sayfasını aç
                                try {
                                    val intent = Intent("android.appwidget.action.APPWIDGET_PICK")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Manuel widget ekleme talimatı göster
                                }
                            }
                        } else {
                            // Eski Android versiyonları için widget sayfasını aç
                            try {
                                val intent = Intent("android.appwidget.action.APPWIDGET_PICK")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Manuel widget ekleme talimatı göster
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("📱")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = context.getString(R.string.add_widget),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Text(
                    text = context.getString(R.string.widget_description),
                    fontSize = 12.sp,
                    color = getSecondaryTextColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Floating Button kontrolü
                OutlinedButton(
                    onClick = { showFloatingDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎯")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (FloatingButtonService.isFloatingActive()) 
                                context.getString(R.string.floating_button_active) 
                            else 
                                context.getString(R.string.floating_button),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Text(
                    text = context.getString(R.string.floating_button_description),
                    fontSize = 12.sp,
                    color = getSecondaryTextColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Volume Boost kontrolü
                OutlinedButton(
                    onClick = { showVolumeBoostDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔊")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (AdvancedVolumeHelper.isVolumeBoostEnabled(context)) 
                                "${context.getString(R.string.volume_boost_enabled)} (${volumeBoostLevel}%)"
                            else 
                                context.getString(R.string.volume_boost),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Text(
                    text = context.getString(R.string.volume_boost_description),
                    fontSize = 12.sp,
                    color = getSecondaryTextColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Night Light kontrolü
                OutlinedButton(
                    onClick = { showNightLightDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🌙")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNightLightRunning) 
                                "${context.getString(R.string.night_light_enabled)} (${nightLightIntensity}%)"
                            else 
                                context.getString(R.string.night_light),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Text(
                    text = context.getString(R.string.night_light_description),
                    fontSize = 12.sp,
                    color = getSecondaryTextColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Nova Orion geliştirici imzası
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                modifier = Modifier.width(100.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "✨ Nova Orion ✨",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Developed with ❤️",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
        } // Box kapanışı
        
        // Dil seçimi dialog'u
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Text(text = context.getString(R.string.language))
                },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "tr") {
                                    selectedLanguage = "tr"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇹🇷",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.turkish),
                                    color = if (selectedLanguage == "tr") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "en") {
                                    selectedLanguage = "en"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇺🇸",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.english),
                                    color = if (selectedLanguage == "en") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "hi") {
                                    selectedLanguage = "hi"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇮🇳",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.hindi),
                                    color = if (selectedLanguage == "hi") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "de") {
                                    selectedLanguage = "de"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇩🇪",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.german),
                                    color = if (selectedLanguage == "de") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "ar") {
                                    selectedLanguage = "ar"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇸🇦",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.arabic),
                                    color = if (selectedLanguage == "ar") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "es") {
                                    selectedLanguage = "es"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇪🇸",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.spanish),
                                    color = if (selectedLanguage == "es") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "bn") {
                                    selectedLanguage = "bn"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇧🇩",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.bengali),
                                    color = if (selectedLanguage == "bn") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                LanguageHelper.changeLanguageInstantly(context as MainActivity, "ja") {
                                    selectedLanguage = "ja"
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🇯🇵",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = context.getString(R.string.japanese),
                                    color = if (selectedLanguage == "ja") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Ses adım boyutu dialog'u
        if (showVolumeStepDialog) {
            AlertDialog(
                onDismissRequest = { showVolumeStepDialog = false },
                title = {
                    Text(text = context.getString(R.string.volume_step))
                },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                volumeStep = 1
                                PreferencesHelper.setVolumeStep(context, 1)
                                showVolumeStepDialog = false
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.small_step),
                                color = if (volumeStep == 1) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                volumeStep = 2
                                PreferencesHelper.setVolumeStep(context, 2)
                                showVolumeStepDialog = false
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.medium_step),
                                color = if (volumeStep == 2) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                volumeStep = 3
                                PreferencesHelper.setVolumeStep(context, 3)
                                showVolumeStepDialog = false
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.large_step),
                                color = if (volumeStep == 3) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVolumeStepDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Tema seçimi dialog'u
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = {
                    Text(text = context.getString(R.string.theme))
                },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                currentTheme = PreferencesHelper.THEME_LIGHT
                                PreferencesHelper.setTheme(context, PreferencesHelper.THEME_LIGHT)
                                showThemeDialog = false
                                // Activity'yi yeniden başlat
                                (context as ComponentActivity).recreate()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "☀️",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = context.getString(R.string.light_theme),
                                    fontSize = 16.sp,
                                    fontWeight = if (currentTheme == PreferencesHelper.THEME_LIGHT) 
                                        FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTheme == PreferencesHelper.THEME_LIGHT) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                currentTheme = PreferencesHelper.THEME_DARK
                                PreferencesHelper.setTheme(context, PreferencesHelper.THEME_DARK)
                                showThemeDialog = false
                                // Activity'yi yeniden başlat
                                (context as ComponentActivity).recreate()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🌙",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = context.getString(R.string.dark_theme),
                                    fontSize = 16.sp,
                                    fontWeight = if (currentTheme == PreferencesHelper.THEME_DARK) 
                                        FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTheme == PreferencesHelper.THEME_DARK) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Autumn theme option - Unlock sistemi ile
                        TextButton(
                            onClick = {
                                if (isAutumnUnlocked) {
                                    // Unlock edilmişse direkt değiştir
                                    currentTheme = PreferencesHelper.THEME_AUTUMN
                                    PreferencesHelper.setTheme(context, PreferencesHelper.THEME_AUTUMN)
                                    showThemeDialog = false
                                    (context as ComponentActivity).recreate()
                                } else {
                                    // Unlock edilmemişse unlock dialog'unu göster
                                    showThemeDialog = false
                                    showAutumnUnlockDialog = true
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🍂",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = context.getString(R.string.autumn_theme),
                                        fontSize = 16.sp,
                                        fontWeight = if (currentTheme == PreferencesHelper.THEME_AUTUMN) 
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentTheme == PreferencesHelper.THEME_AUTUMN) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isAutumnUnlocked) {
                                        Text(
                                            text = context.getString(R.string.autumn_theme_locked, autumnAdsRemaining),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!isAutumnUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        // Sakura theme option - Unlock sistemi ile
                        TextButton(
                            onClick = {
                                if (isSakuraUnlocked) {
                                    // Unlock edilmişse direkt değiştir
                                    currentTheme = PreferencesHelper.THEME_SAKURA
                                    PreferencesHelper.setTheme(context, PreferencesHelper.THEME_SAKURA)
                                    showThemeDialog = false
                                    (context as ComponentActivity).recreate()
                                } else {
                                    // Unlock edilmemişse unlock dialog'unu göster
                                    showThemeDialog = false
                                    showSakuraUnlockDialog = true
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🌸",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = context.getString(R.string.sakura_theme),
                                        fontSize = 16.sp,
                                        fontWeight = if (currentTheme == PreferencesHelper.THEME_SAKURA) 
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentTheme == PreferencesHelper.THEME_SAKURA) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isSakuraUnlocked) {
                                        Text(
                                            text = context.getString(R.string.sakura_theme_locked, sakuraAdsRemaining),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!isSakuraUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        // Aquarium theme option - Unlock sistemi ile
                        TextButton(
                            onClick = {
                                if (isAquariumUnlocked) {
                                    // Unlock edilmişse direkt değiştir
                                    currentTheme = PreferencesHelper.THEME_AQUARIUM
                                    PreferencesHelper.setTheme(context, PreferencesHelper.THEME_AQUARIUM)
                                    showThemeDialog = false
                                    (context as ComponentActivity).recreate()
                                } else {
                                    // Unlock edilmemişse unlock dialog'unu göster
                                    showThemeDialog = false
                                    showAquariumUnlockDialog = true
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🐠",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = context.getString(R.string.aquarium_theme),
                                        fontSize = 16.sp,
                                        fontWeight = if (currentTheme == PreferencesHelper.THEME_AQUARIUM) 
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentTheme == PreferencesHelper.THEME_AQUARIUM) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isAquariumUnlocked) {
                                        Text(
                                            text = context.getString(R.string.aquarium_theme_locked, aquariumAdsRemaining),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!isAquariumUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                currentTheme = PreferencesHelper.THEME_AUTO
                                PreferencesHelper.setTheme(context, PreferencesHelper.THEME_AUTO)
                                showThemeDialog = false
                                // Activity'yi yeniden başlat
                                (context as ComponentActivity).recreate()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🌙/☀️",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = context.getString(R.string.auto_theme),
                                    fontSize = 16.sp,
                                    fontWeight = if (currentTheme == PreferencesHelper.THEME_AUTO) 
                                        FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTheme == PreferencesHelper.THEME_AUTO) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Zamanlanmış ses dialog'u
        if (showScheduleDialog) {
            AlertDialog(
                onDismissRequest = { showScheduleDialog = false },
                title = {
                    Text(text = context.getString(R.string.scheduled_volume))
                },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(context.getString(R.string.enable_schedule))
                            Switch(
                                checked = scheduledVolumeEnabled,
                                onCheckedChange = { 
                                    scheduledVolumeEnabled = it
                                    PreferencesHelper.setScheduledVolumeEnabled(context, it)
                                }
                            )
                        }
                        
                        if (scheduledVolumeEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "🌅 ${context.getString(R.string.morning_volume)} (06:00-12:00): ${PreferencesHelper.getMorningVolume(context)}%",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "🌆 ${context.getString(R.string.evening_volume)} (12:00-22:00): ${PreferencesHelper.getEveningVolume(context)}%",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "🌙 ${context.getString(R.string.night_volume)} (22:00-06:00): ${PreferencesHelper.getNightVolume(context)}%",
                                fontSize = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Şu anki durum
                            Text(
                                text = "${stringResource(R.string.currently)}: ${ScheduledVolumeHelper.getCurrentTimeSlot(context)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Test butonu
                            Button(
                                onClick = { 
                                    ScheduledVolumeHelper.forceApplySchedule(context)
                                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🔄 ${stringResource(R.string.apply_now)}")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showScheduleDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Profil seçimi dialog'ı
        if (showProfileDialog) {
            ProfileSelectionDialog(
                profiles = ProfileHelper.getDefaultProfiles(context),
                currentProfile = currentProfile,
                onProfileSelected = { profileId ->
                    ProfileHelper.applyProfile(context, profileId) { volume ->
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    }
                    StatisticsHelper.recordProfileUsage(context, profileId)
                    currentProfile = profileId
                    statsData = StatisticsHelper.getAllStats(context)
                    showProfileDialog = false
                },
                onDismiss = { showProfileDialog = false }
            )
        }
        
        // İstatistikler dialog'ı
        if (showStatsDialog) {
            StatisticsDialog(
                stats = statsData,
                onDismiss = { showStatsDialog = false }
            )
        }
        
        // Floating Button dialog'u
        if (showFloatingDialog) {
            AlertDialog(
                onDismissRequest = { showFloatingDialog = false },
                title = {
                    Text(text = context.getString(R.string.floating_button))
                },
                text = {
                    Column {
                        if (FloatingButtonService.isFloatingActive()) {
                            Text(
                                text = context.getString(R.string.floating_button_active_description),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Button(
                                onClick = {
                                    val intent = Intent(context, FloatingButtonService::class.java).apply {
                                        action = FloatingButtonService.ACTION_STOP_FLOATING
                                    }
                                    context.startService(intent)
                                    showFloatingDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(context.getString(R.string.stop_floating_button))
                            }
                        } else {
                            Text(
                                text = context.getString(R.string.floating_button_permission_info),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Button(
                                onClick = {
                                    // Overlay izni kontrol et
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (!Settings.canDrawOverlays(context)) {
                                            // İzin yok, ayarlara yönlendir
                                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            (context as MainActivity).startOverlayPermissionRequest()
                                        } else {
                                            // İzin var, floating button başlat
                                            val intent = Intent(context, FloatingButtonService::class.java).apply {
                                                action = FloatingButtonService.ACTION_START_FLOATING
                                            }
                                            context.startService(intent)
                                            showFloatingDialog = false
                                        }
                                    } else {
                                        // Eski Android versiyonu, doğrudan başlat
                                        val intent = Intent(context, FloatingButtonService::class.java).apply {
                                            action = FloatingButtonService.ACTION_START_FLOATING
                                        }
                                        context.startService(intent)
                                        showFloatingDialog = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(context.getString(R.string.start_floating_button))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFloatingDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Floating Button size dialog'u
        if (showFloatingSizeDialog) {
            AlertDialog(
                onDismissRequest = { showFloatingSizeDialog = false },
                title = {
                    Text(text = context.getString(R.string.floating_button_size))
                },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                floatingButtonSize = PreferencesHelper.FLOATING_BUTTON_SIZE_SMALL
                                PreferencesHelper.setFloatingButtonSize(context, PreferencesHelper.FLOATING_BUTTON_SIZE_SMALL)
                                showFloatingSizeDialog = false
                                // If floating button is active, restart it to apply the new size
                                if (FloatingButtonService.isFloatingActive()) {
                                    val stopIntent = Intent(context, FloatingButtonService::class.java).apply {
                                        action = FloatingButtonService.ACTION_STOP_FLOATING
                                    }
                                    context.startService(stopIntent)
                                    
                                    // Small delay to ensure the service is stopped
                                    kotlinx.coroutines.MainScope().launch {
                                        delay(100)
                                        val startIntent = Intent(context, FloatingButtonService::class.java).apply {
                                            action = FloatingButtonService.ACTION_START_FLOATING
                                        }
                                        context.startService(startIntent)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.small_button) + " (${PreferencesHelper.FLOATING_BUTTON_SIZE_SMALL}dp)",
                                color = if (floatingButtonSize == PreferencesHelper.FLOATING_BUTTON_SIZE_SMALL) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                floatingButtonSize = PreferencesHelper.FLOATING_BUTTON_SIZE_MEDIUM
                                PreferencesHelper.setFloatingButtonSize(context, PreferencesHelper.FLOATING_BUTTON_SIZE_MEDIUM)
                                showFloatingSizeDialog = false
                                // If floating button is active, restart it to apply the new size
                                if (FloatingButtonService.isFloatingActive()) {
                                    val stopIntent = Intent(context, FloatingButtonService::class.java).apply {
                                        action = FloatingButtonService.ACTION_STOP_FLOATING
                                    }
                                    context.startService(stopIntent)
                                    
                                    // Small delay to ensure the service is stopped
                                    kotlinx.coroutines.MainScope().launch {
                                        delay(100)
                                        val startIntent = Intent(context, FloatingButtonService::class.java).apply {
                                            action = FloatingButtonService.ACTION_START_FLOATING
                                        }
                                        context.startService(startIntent)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.medium_button) + " (${PreferencesHelper.FLOATING_BUTTON_SIZE_MEDIUM}dp)",
                                color = if (floatingButtonSize == PreferencesHelper.FLOATING_BUTTON_SIZE_MEDIUM) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                floatingButtonSize = PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE
                                PreferencesHelper.setFloatingButtonSize(context, PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE)
                                showFloatingSizeDialog = false
                                // If floating button is active, restart it to apply the new size
                                if (FloatingButtonService.isFloatingActive()) {
                                    val stopIntent = Intent(context, FloatingButtonService::class.java).apply {
                                        action = FloatingButtonService.ACTION_STOP_FLOATING
                                    }
                                    context.startService(stopIntent)
                                    
                                    // Small delay to ensure the service is stopped
                                    kotlinx.coroutines.MainScope().launch {
                                        delay(100)
                                        val startIntent = Intent(context, FloatingButtonService::class.java).apply {
                                            action = FloatingButtonService.ACTION_START_FLOATING
                                        }
                                        context.startService(startIntent)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.large_button) + " (${PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE}dp)",
                                color = if (floatingButtonSize == PreferencesHelper.FLOATING_BUTTON_SIZE_LARGE) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFloatingSizeDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Volume Boost dialog'u
        if (showVolumeBoostDialog) {
            AlertDialog(
                onDismissRequest = { showVolumeBoostDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔊")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = context.getString(R.string.volume_boost_title))
                    }
                },
                text = {
                    Column {
                        // Mevcut seviye göstergesi
                        Text(
                            text = "${context.getString(R.string.volume_boost_level)}: ${volumeBoostLevel}%",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Güvenlik uyarısı
                        if (volumeBoostLevel > 75) {
                            Text(
                                text = context.getString(R.string.warning_high_volume),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Seviye butonları
                        val levels = listOf(
                            0 to context.getString(R.string.disabled),
                            25 to context.getString(R.string.low),
                            50 to context.getString(R.string.medium),
                            75 to context.getString(R.string.high),
                            90 to context.getString(R.string.maximum)
                        )
                        
                        levels.forEach { (level, label) ->
                            TextButton(
                                onClick = {
                                    volumeBoostLevel = level
                                    AdvancedVolumeHelper.applyLoudnessBoost(context, level)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$label ($level%)",
                                        color = if (volumeBoostLevel == level) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    if (level > 75) {
                                        Text("⚠️", fontSize = 16.sp)
                                    }
                                }
                            }
                            
                            // Maximum seviye için özel uyarı
                            if (level == 90) {
                                Text(
                                    text = context.getString(R.string.maximum_level_warning),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                        
                        // Acil durum butonu
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                AdvancedVolumeHelper.emergencyVolumeBoost(context)
                                volumeBoostLevel = 75
                                showVolumeBoostDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚨 ${context.getString(R.string.emergency_boost)}")
                        }
                        
                        // Cihaz desteği bilgisi
                        if (!AdvancedVolumeHelper.isLoudnessEnhancerSupported()) {
                            Text(
                                text = context.getString(R.string.boost_not_supported),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVolumeBoostDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Night Light Dialog
        if (showNightLightDialog) {
            AlertDialog(
                onDismissRequest = { showNightLightDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌙")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = context.getString(R.string.night_light_title))
                    }
                },
                text = {
                    Column {
                        // Night Light status
                        Text(
                            text = if (isNightLightRunning) 
                                context.getString(R.string.night_light_enabled)
                            else 
                                context.getString(R.string.night_light_disabled),
                            fontWeight = FontWeight.Medium,
                            color = if (isNightLightRunning) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                getSecondaryTextColor(),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Eye protection info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(text = context.getString(R.string.eye_protection))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = context.getString(R.string.reduces_blue_light),
                                fontSize = 12.sp,
                                color = getSecondaryTextColor()
                            )
                        }
                        
                        // Start/Stop Button
                        Button(
                            onClick = {
                                if (isNightLightRunning) {
                                    // Stop night light
                                    val stopIntent = Intent(context, NightLightService::class.java).apply {
                                        action = NightLightService.ACTION_STOP_NIGHT_LIGHT
                                    }
                                    context.startService(stopIntent)
                                    isNightLightRunning = false
                                } else {
                                    // Check overlay permission
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        // Request overlay permission
                                        (context as MainActivity).startOverlayPermissionRequest()
                                    } else {
                                        // Start night light
                                        val startIntent = Intent(context, NightLightService::class.java).apply {
                                            action = NightLightService.ACTION_START_NIGHT_LIGHT
                                            putExtra(NightLightService.EXTRA_INTENSITY, nightLightIntensity)
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(startIntent)
                                        } else {
                                            context.startService(startIntent)
                                        }
                                        isNightLightRunning = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNightLightRunning) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (isNightLightRunning) 
                                    context.getString(R.string.stop_night_light) 
                                else 
                                    context.getString(R.string.start_night_light)
                            )
                        }
                        
                        if (!isNightLightRunning && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            Text(
                                text = context.getString(R.string.night_light_permission_info),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Intensity control
                        OutlinedButton(
                            onClick = { 
                                showNightLightDialog = false
                                showNightLightIntensityDialog = true 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⚙️ ${context.getString(R.string.night_light_intensity)} (${nightLightIntensity}%)")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNightLightDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Night Light Intensity Dialog
        if (showNightLightIntensityDialog) {
            AlertDialog(
                onDismissRequest = { showNightLightIntensityDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌙")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = context.getString(R.string.night_light_intensity))
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "${context.getString(R.string.night_light_intensity_description)}: ${nightLightIntensity}%",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        val intensityLevels = listOf(
                            PreferencesHelper.NIGHT_LIGHT_INTENSITY_LOW to context.getString(R.string.intensity_low),
                            PreferencesHelper.NIGHT_LIGHT_INTENSITY_MEDIUM to context.getString(R.string.intensity_medium),
                            PreferencesHelper.NIGHT_LIGHT_INTENSITY_HIGH to context.getString(R.string.intensity_high),
                            PreferencesHelper.NIGHT_LIGHT_INTENSITY_MAXIMUM to context.getString(R.string.intensity_maximum)
                        )
                        
                        intensityLevels.forEach { (level, label) ->
                            TextButton(
                                onClick = {
                                    nightLightIntensity = level
                                    PreferencesHelper.setNightLightIntensity(context, level)
                                    
                                    // If night light is running, update intensity
                                    if (isNightLightRunning) {
                                        val updateIntent = Intent(context, NightLightService::class.java).apply {
                                            action = NightLightService.ACTION_UPDATE_INTENSITY
                                            putExtra(NightLightService.EXTRA_INTENSITY, level)
                                        }
                                        context.startService(updateIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        color = if (nightLightIntensity == level) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    if (nightLightIntensity == level) {
                                        Text("✓", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showNightLightIntensityDialog = false
                        showNightLightDialog = true
                    }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Autumn unlock dialog
        if (showAutumnUnlockDialog) {
            AutumnUnlockDialog(
                adsWatched = autumnAdsWatched,
                adsRemaining = autumnAdsRemaining,
                isAdReady = isRewardedAdReady,
                onWatchAd = {
                    // Reklam göster
                    if (context is Activity) {
                        AdMobHelper.showRewardedAdForAutumnTheme(
                            context as Activity,
                            onProgress = { watched, remaining ->
                                autumnAdsWatched = watched
                                autumnAdsRemaining = remaining
                                isAutumnUnlocked = RewardedUnlockHelper.isAutumnThemeUnlocked(context)
                            },
                            onUnlocked = {
                                isAutumnUnlocked = true
                                autumnAdsWatched = 3
                                autumnAdsRemaining = 0
                            },
                            onAdDismissed = {
                                // Reklam kapandı, durumu güncelle
                                val newWatched = RewardedUnlockHelper.getAutumnThemeAdsWatched(context)
                                val newRemaining = RewardedUnlockHelper.getRemainingAdsForAutumn(context)
                                val newUnlocked = RewardedUnlockHelper.isAutumnThemeUnlocked(context)
                                
                                autumnAdsWatched = newWatched
                                autumnAdsRemaining = newRemaining
                                isAutumnUnlocked = newUnlocked
                                
                                // Eğer unlock olduysa dialog'u kapat
                                if (newUnlocked) {
                                    showAutumnUnlockDialog = false
                                }
                            }
                        )
                    }
                },
                onDismiss = { 
                    showAutumnUnlockDialog = false
                    
                    // Eğer tema unlock edildiyse, direkt uygula
                    if (RewardedUnlockHelper.isAutumnThemeUnlocked(context)) {
                        showAutumnUnlockDialog = false
                        
                        // Temayı direkt uygula
                        currentTheme = PreferencesHelper.THEME_AUTUMN
                        PreferencesHelper.setTheme(context, PreferencesHelper.THEME_AUTUMN)
                        if (context is Activity) {
                            (context as Activity).recreate()
                        }
                    }
                }
            )
        }
        
        // Sakura unlock dialog
        if (showSakuraUnlockDialog) {
            SakuraUnlockDialog(
                adsWatched = sakuraAdsWatched,
                adsRemaining = sakuraAdsRemaining,
                isAdReady = isRewardedAdReady,
                onWatchAd = {
                    // Reklam göster
                    if (context is Activity) {
                        AdMobHelper.showRewardedAdForSakuraTheme(
                            context as Activity,
                            onProgress = { watched, remaining ->
                                sakuraAdsWatched = watched
                                sakuraAdsRemaining = remaining
                                isSakuraUnlocked = SakuraUnlockHelper.isSakuraThemeUnlocked(context)
                            },
                            onUnlocked = {
                                isSakuraUnlocked = true
                                sakuraAdsWatched = 3
                                sakuraAdsRemaining = 0
                            },
                            onAdDismissed = {
                                // Reklam kapandı, durumu güncelle
                                val newWatched = SakuraUnlockHelper.getSakuraThemeAdsWatched(context)
                                val newRemaining = SakuraUnlockHelper.getRemainingAdsForSakura(context)
                                val newUnlocked = SakuraUnlockHelper.isSakuraThemeUnlocked(context)
                                
                                sakuraAdsWatched = newWatched
                                sakuraAdsRemaining = newRemaining
                                isSakuraUnlocked = newUnlocked
                                
                                // Eğer unlock olduysa dialog'u kapat
                                if (newUnlocked) {
                                    showSakuraUnlockDialog = false
                                }
                            }
                        )
                    }
                },
                onDismiss = { 
                    showSakuraUnlockDialog = false
                    
                    // Eğer tema unlock edildiyse, direkt uygula
                    if (SakuraUnlockHelper.isSakuraThemeUnlocked(context)) {
                        showSakuraUnlockDialog = false
                        
                        // Temayı direkt uygula
                        currentTheme = PreferencesHelper.THEME_SAKURA
                        PreferencesHelper.setTheme(context, PreferencesHelper.THEME_SAKURA)
                        if (context is Activity) {
                            (context as Activity).recreate()
                        }
                    }
                }
            )
        }
        
        // Aquarium unlock dialog
        if (showAquariumUnlockDialog) {
            AquariumUnlockDialog(
                adsWatched = aquariumAdsWatched,
                adsRemaining = aquariumAdsRemaining,
                isAdReady = isRewardedAdReady,
                onWatchAd = {
                    // Reklam göster
                    if (context is Activity) {
                        AdMobHelper.showRewardedAdForAquariumTheme(
                            context as Activity,
                            onProgress = { watched, remaining ->
                                aquariumAdsWatched = watched
                                aquariumAdsRemaining = remaining
                                isAquariumUnlocked = AquariumUnlockHelper.isAquariumThemeUnlocked(context)
                            },
                            onUnlocked = {
                                isAquariumUnlocked = true
                                aquariumAdsRemaining = 0
                                
                                // Başarı bildirimi göster
                                val newUnlocked = AquariumUnlockHelper.isAquariumThemeUnlocked(context)
                                if (newUnlocked) {
                                    // Theme unlock edildi, dialog'u kapat
                                    showAquariumUnlockDialog = false
                                }
                            }
                        )
                    }
                },
                onDismiss = { 
                    showAquariumUnlockDialog = false
                    
                    // Eğer tema unlock edildiyse, direkt uygula
                    if (AquariumUnlockHelper.isAquariumThemeUnlocked(context)) {
                        showAquariumUnlockDialog = false
                        
                        // Temayı direkt uygula
                        currentTheme = PreferencesHelper.THEME_AQUARIUM
                        PreferencesHelper.setTheme(context, PreferencesHelper.THEME_AQUARIUM)
                        if (context is Activity) {
                            (context as Activity).recreate()
                        }
                    }
                }
            )
        }
        
    }
}

@Composable
fun ProfileSelectionDialog(
    profiles: Map<String, ProfileHelper.VolumeProfile>,
    currentProfile: String,
    onProfileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.select_profile),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                items(profiles.keys.toList()) { profileId ->
                    val profile = profiles[profileId]!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onProfileSelected(profileId) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (profileId == currentProfile) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile.icon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = profile.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (profileId == currentProfile) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = profile.description,
                                    fontSize = 14.sp,
                                    color = if (profileId == currentProfile) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        getSecondaryTextColor()
                                    }
                                )
                                Text(
                                    text = "🎵${profile.mediaVolume}% 📱${profile.ringVolume}% 🔔${profile.notificationVolume}% ⏰${profile.alarmVolume}%",
                                    fontSize = 12.sp,
                                    color = if (profileId == currentProfile) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun StatisticsDialog(
    stats: Map<String, Any>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.statistics),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                item {
                    Text(
                        text = "📊 ${stringResource(R.string.today)}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = getTextColor(),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    StatsCard(
                        icon = "🔊",
                        title = stringResource(R.string.volume_changes),
                        value = "${stats["today_volume_changes"]} ${stringResource(R.string.times)}"
                    )
                    
                    StatsCard(
                        icon = "⏱️",
                        title = stringResource(R.string.session_time),
                        value = "${stats["today_session_time"]} ${stringResource(R.string.minutes)}"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "📈 ${stringResource(R.string.this_week)}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = getTextColor(),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    StatsCard(
                        icon = "🔊",
                        title = stringResource(R.string.total_changes),
                        value = "${stats["week_volume_changes"]} ${stringResource(R.string.times)}"
                    )
                    
                    StatsCard(
                        icon = "📱",
                        title = stringResource(R.string.daily_average),
                        value = "${(stats["week_volume_changes"] as? Int ?: 0) / 7} ${stringResource(R.string.times)}"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun StatsCard(
    icon: String,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = getSecondaryTextColor()
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AudioEffectsDialog(
    currentPreset: String,
    presets: Map<String, AudioEffectsHelper.AudioPreset>,
    onPresetSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.audio_effects),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                items(presets.keys.toList()) { presetId ->
                    val preset = presets[presetId]!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onPresetSelected(presetId) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (presetId == currentPreset) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.icon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = preset.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preset.description,
                                    fontSize = 14.sp,
                                    color = Color(0xFF212121)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
