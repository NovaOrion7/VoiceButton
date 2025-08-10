package com.novaorion.volumecontrol

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobHelper {
    
    // Test reklam ID'leri
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    
    fun initializeAds(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d("AdMob", "AdMob başlatıldı: ${initializationStatus.adapterStatusMap}")
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
}

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp), // Banner reklam standart yüksekliği
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobHelper.BANNER_AD_UNIT_ID
                
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdMob", "Banner reklam yüklendi")
                    }
                    
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("AdMob", "Banner reklam yüklenemedi: ${adError.message}")
                    }
                    
                    override fun onAdClicked() {
                        Log.d("AdMob", "Banner reklam tıklandı")
                    }
                }
            }
        }
    )
}
