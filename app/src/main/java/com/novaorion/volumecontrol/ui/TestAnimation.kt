package com.novaorion.volumecontrol.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TestAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val animatedY by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Büyük test yazısı - her zaman görünür olmalı
        Text(
            text = "🍂 YAPRAK ANİMASYONU TEST 🍂",
            color = Color.Red,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        )
        
        // Düşen emojiler - çok büyük
        listOf("🍂", "🍁", "🌿", "🍃", "🎃").forEachIndexed { index, emoji ->
            Text(
                text = emoji,
                fontSize = 48.sp, // Çok büyük
                modifier = Modifier
                    .offset(
                        x = (20 + index * 70).dp,
                        y = (animatedY + index * 150).dp
                    )
            )
        }
        
        // Sabit kontrol yazısı
        Text(
            text = "Bu yazı görünüyorsa animasyonlar çalışıyor!",
            color = Color.Blue,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )
        
        // Alt kısımda da sabit yazı
        Text(
            text = "TEST: Sonbahar Animasyonu",
            color = Color.Green,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp)
        )
    }
}
