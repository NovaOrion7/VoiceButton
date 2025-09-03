package com.novaorion.volumecontrol

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobHelper {
    
    // Test reklam ID'leri (Debug/APK) - Google'ın güncel test ID'leri
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    
    // Gerçek reklam ID'leri (Release/AAB)
    private const val REAL_BANNER_AD_UNIT_ID = "ca-app-pub-2239637684721708/1666826847"
    private const val REAL_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2239637684721708/8040663505"
    
    // Default to test ads
    var BANNER_AD_UNIT_ID = TEST_BANNER_AD_UNIT_ID
    var INTERSTITIAL_AD_UNIT_ID = TEST_INTERSTITIAL_AD_UNIT_ID
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var interstitialCounter = 0
    
    fun initializeAds(context: Context) {
        // Set the ad IDs based on build config field
        setUseRealAds(getUseRealAdsFromBuildConfig())
        
        // Test device configuration SADECE test reklamları için
        val useRealAds = getUseRealAdsFromBuildConfig()
        if (!useRealAds) {
            // Sadece APK/test builds için test device configuration
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR, "YOUR_DEVICE_ID"))
                .build()
            MobileAds.setRequestConfiguration(configuration)
            Log.d("AdMob", "Test device configuration enabled for APK build")
        } else {
            Log.d("AdMob", "Production mode - no test device configuration for AAB build")
        }
        
        MobileAds.initialize(context) { initializationStatus ->
            Log.d("AdMob", "AdMob başlatıldı: ${initializationStatus.adapterStatusMap}")
        }
    }
    
    fun setUseRealAds(useReal: Boolean) {
        BANNER_AD_UNIT_ID = if (useReal) REAL_BANNER_AD_UNIT_ID else TEST_BANNER_AD_UNIT_ID
        INTERSTITIAL_AD_UNIT_ID = if (useReal) REAL_INTERSTITIAL_AD_UNIT_ID else TEST_INTERSTITIAL_AD_UNIT_ID
        Log.d("AdMob", "AdMobHelper: Using ${if (useReal) "REAL" else "TEST"} ads")
    }
    
    fun getUseRealAdsFromBuildConfig(): Boolean {
        // Default to false (test ads) to be safe
        return try {
            // Try to get the build config field
            val buildConfigClass = Class.forName("com.novaorion.volumecontrol.BuildConfig")
            val field = buildConfigClass.getField("USE_REAL_ADS")
            val useRealAds = field.getBoolean(null)
            Log.d("AdMob", "BuildConfig.USE_REAL_ADS = $useRealAds")
            useRealAds
        } catch (e: Exception) {
            // If we can't determine, default to false (use test ads) to avoid ban risk
            Log.e("AdMob", "Could not read BuildConfig.USE_REAL_ADS, defaulting to false", e)
            false
        }
    }
    
    fun loadInterstitialAd(context: Context) {
        if (isLoadingInterstitial) return
        
        isLoadingInterstitial = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Interstitial reklam yüklenemedi: ${adError.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
                
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("AdMob", "Interstitial reklam yüklendi")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    
                    // Reklam gösterildikten sonra yeni reklam yükle
                    ad.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdMob", "Interstitial reklam kapatıldı")
                            interstitialAd = null
                            loadInterstitialAd(context) // Yeni reklam yükle
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("AdMob", "Interstitial reklam gösterilemedi: ${adError.message}")
                            interstitialAd = null
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdMob", "Interstitial reklam gösterildi")
                        }
                    }
                }
            }
        )
    }
    
    fun showInterstitialAd(activity: Activity): Boolean {
        interstitialCounter++
        
        // Only show ad every 3rd time
        if (interstitialCounter % 3 != 0) {
            return false
        }
        
        return if (interstitialAd != null) {
            interstitialAd?.show(activity)
            true
        } else {
            Log.d("AdMob", "Interstitial reklam henüz yüklenmedi")
            loadInterstitialAd(activity) // Reklam yoksa yükle
            false
        }
    }
    
    fun isInterstitialReady(): Boolean {
        return interstitialAd != null
    }
    
    // Test method for debugging
    fun testAdConfiguration() {
        try {
            val useRealAds = getUseRealAdsFromBuildConfig()
            Log.d("AdMobTest", "USE_REAL_ADS = $useRealAds")
            Log.d("AdMobTest", "BANNER_AD_UNIT_ID = $BANNER_AD_UNIT_ID")
            Log.d("AdMobTest", "INTERSTITIAL_AD_UNIT_ID = $INTERSTITIAL_AD_UNIT_ID")
            
            if (useRealAds) {
                Log.d("AdMobTest", "SUCCESS: Using REAL ads for AAB build")
            } else {
                Log.d("AdMobTest", "SUCCESS: Using TEST ads for APK build")
            }
        } catch (e: Exception) {
            Log.e("AdMobTest", "Error testing ad configuration", e)
        }
    }
}

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Log that we're trying to show the banner
    LaunchedEffect(Unit) {
        Log.d("AdMob", "Attempting to show banner ad with ID: ${AdMobHelper.BANNER_AD_UNIT_ID}")
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp) // Banner reklam standart yüksekliği
            .background(androidx.compose.ui.graphics.Color.LightGray), // Debug için arka plan
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobHelper.BANNER_AD_UNIT_ID
                
                // Force test ads for APK builds - modern approach
                val adRequest = if (!AdMobHelper.getUseRealAdsFromBuildConfig()) {
                    AdRequest.Builder()
                        .build() // Test device configuration is handled globally
                } else {
                    AdRequest.Builder().build()
                }
                
                loadAd(adRequest)
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdMob", "Banner reklam yüklendi successfully")
                        Log.d("AdMob", "Banner ad unit ID used: ${AdMobHelper.BANNER_AD_UNIT_ID}")
                        Log.d("AdMob", "USE_REAL_ADS: ${AdMobHelper.getUseRealAdsFromBuildConfig()}")
                    }
                    
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("AdMob", "Banner reklam yüklenemedi: ${adError.message}")
                        Log.e("AdMob", "Banner ad unit ID: ${AdMobHelper.BANNER_AD_UNIT_ID}")
                        Log.e("AdMob", "Error code: ${adError.code}")
                        Log.e("AdMob", "Error domain: ${adError.domain}")
                    }
                    
                    override fun onAdClicked() {
                        Log.d("AdMob", "Banner reklam tıklandı")
                    }
                }
            }
        }
    )
}