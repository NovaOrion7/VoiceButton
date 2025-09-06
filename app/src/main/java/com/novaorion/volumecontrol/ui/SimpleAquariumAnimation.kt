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
fun SimpleAquariumAnimation(modifier: Modifier = Modifier) {
    // Baloncuklar için emoji listesi
    val bubbles = remember { 
        listOf("🫧", "💧", "🔵", "⚪", "🟢") // Küçük baloncuklar için emojiler
    }
    
    // Balıklar için emoji listesi
    val fish = remember {
        listOf("🐠", "🐟", "🦈", "🐡", "🦑")
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Baloncukları düşür (sonbahar teması gibi)
        bubbles.forEachIndexed { index, emoji ->
            AnimatedBubbleEmoji(
                emoji = emoji,
                startDelay = index * 800L,
                horizontalOffset = (index * 80).dp
            )
        }
        
        // Balıkları rastgele dolandır
        fish.forEachIndexed { index, emoji ->
            AnimatedFishEmoji(
                emoji = emoji,
                startDelay = index * 1200L,
                fishId = index
            )
        }
    }
}

@Composable
fun AnimatedBubbleEmoji(
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
        
        // Y position animation - bubbles rise up (opposite of falling leaves)
        val animatedY by infiniteTransition.animateFloat(
            initialValue = 1200f,
            targetValue = -100f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 8000 + (startDelay.toInt()),
                    easing = LinearEasing
                )
            )
        )
        
        // X sway animation for bubbles
        val animatedSway by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Text(
            text = emoji,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(
                x = horizontalOffset + animatedSway.dp,
                y = animatedY.dp
            )
        )
    }
}

@Composable
fun AnimatedFishEmoji(
    emoji: String,
    startDelay: Long,
    fishId: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Start animation after delay
    LaunchedEffect(Unit) {
        delay(startDelay)
        isVisible = true
    }
    
    if (isVisible) {
        val infiniteTransition = rememberInfiniteTransition()
        
        // Random movement pattern based on fishId
        val movementType = fishId % 3
        
        when (movementType) {
            0 -> {
                // Horizontal swimming (left to right and back)
                val animatedX by infiniteTransition.animateFloat(
                    initialValue = -50f,
                    targetValue = 400f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 10000 + (fishId * 1000),
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                val animatedY by infiniteTransition.animateFloat(
                    initialValue = 200f + (fishId * 100f),
                    targetValue = 250f + (fishId * 100f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 4000,
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
                        x = animatedX.dp,
                        y = animatedY.dp
                    )
                )
            }
            1 -> {
                // Circular movement pattern
                val time by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 12000 + (fishId * 800),
                            easing = LinearEasing
                        )
                    )
                )
                
                val centerX = 200f
                val centerY = 400f + (fishId * 80f)
                val radius = 80f + (fishId * 20f)
                
                val animatedX = centerX + radius * kotlin.math.cos(Math.toRadians(time.toDouble())).toFloat()
                val animatedY = centerY + radius * kotlin.math.sin(Math.toRadians(time.toDouble())).toFloat()
                
                Text(
                    text = emoji,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(
                        x = animatedX.dp,
                        y = animatedY.dp
                    )
                )
            }
            else -> {
                // Vertical swimming (up and down)
                val animatedY by infiniteTransition.animateFloat(
                    initialValue = 100f,
                    targetValue = 700f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 8000 + (fishId * 1500),
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                val animatedX by infiniteTransition.animateFloat(
                    initialValue = 50f + (fishId * 80f),
                    targetValue = 100f + (fishId * 80f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 3000,
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
                        x = animatedX.dp,
                        y = animatedY.dp
                    )
                )
            }
        }
    }
}
