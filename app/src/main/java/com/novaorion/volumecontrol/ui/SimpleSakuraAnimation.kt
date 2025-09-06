package com.novaorion.volumecontrol.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SimpleSakuraAnimation(modifier: Modifier = Modifier) {
    // Çok basit emoji tabanlı animasyon
    val sakura = remember { 
        listOf("🌸", "🌺", "💮", "🏵️", "🌹")
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        sakura.forEachIndexed { index, emoji ->
            AnimatedSakuraEmoji(
                emoji = emoji,
                startDelay = index * 500L,
                horizontalOffset = (index * 80).dp
            )
        }
    }
}

@Composable
fun AnimatedSakuraEmoji(
    emoji: String,
    startDelay: Long,
    horizontalOffset: Dp
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Start animation after delay
    LaunchedEffect(Unit) {
        delay(startDelay)
        isVisible = true
    }
    
    if (isVisible) {
        val infiniteTransition = rememberInfiniteTransition()
        
        // Y position animation
        val animatedY by infiniteTransition.animateFloat(
            initialValue = -100f,
            targetValue = 1200f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 8000 + (startDelay.toInt()),
                    easing = LinearEasing
                )
            )
        )
        
        // X sway animation
        val animatedSway by infiniteTransition.animateFloat(
            initialValue = -20f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        // Rotation animation
        val rotation by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Text(
            text = emoji,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(
                x = horizontalOffset + animatedSway.dp,
                y = animatedY.dp
            )
        )
    }
}