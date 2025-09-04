package com.novaorion.volumecontrol.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

// Enhanced leaf data class to represent each falling leaf
data class Leaf(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val swayAmount: Float,
    val rotationSpeed: Float,
    val color: Color,
    val leafType: LeafType = LeafType.MAPLE,
    val opacity: Float = 0.8f,
    val windResistance: Float = 1.0f
)

// Different leaf types for variety
enum class LeafType {
    MAPLE, OAK, BIRCH, WILLOW
}

@Composable
fun FallingLeavesBackground(modifier: Modifier = Modifier) {
    // More visible animation for testing
    val leaves = remember {
        List(10) { // Fewer leaves but more visible
            Leaf(
                id = it,
                startX = Random.nextFloat(),
                startY = -Random.nextFloat() * 300f,
                size = 20f + Random.nextFloat() * 15f, // Larger sizes (20-35f)
                speed = 0.8f + Random.nextFloat() * 0.7f,
                swayAmount = 30f + Random.nextFloat() * 30f,
                rotationSpeed = -2f + Random.nextFloat() * 4f,
                color = getAutumnLeafColor(),
                leafType = LeafType.values()[Random.nextInt(LeafType.values().size)],
                opacity = 0.6f + Random.nextFloat() * 0.4f, // More opaque (0.6-1.0)
                windResistance = 0.7f + Random.nextFloat() * 0.6f
            )
        }
    }

    // More visible canvas
    VisibleLeafCanvas(leaves = leaves, modifier = modifier)
}

@Composable
fun VisibleLeafCanvas(leaves: List<Leaf>, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Wind effect
    val windEffect by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val leafAnimations = leaves.map { leaf ->
        // Falling animation
        val fallAnimation by infiniteTransition.animateFloat(
            initialValue = leaf.startY,
            targetValue = 1600f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (10000 / leaf.speed).toInt(), // Faster falling
                    easing = LinearEasing
                )
            )
        )
        
        // Swaying
        val swayAnimation by infiniteTransition.animateFloat(
            initialValue = -leaf.swayAmount,
            targetValue = leaf.swayAmount,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4000 + (leaf.id * 300),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        // Rotation
        val rotationAnimation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 6000 + (leaf.id * 500),
                    easing = LinearEasing
                )
            )
        )
        
        Triple(fallAnimation, swayAnimation, rotationAnimation) to leaf
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        leafAnimations.forEach { (animations, leaf) ->
            val (fallAnimation, swayAnimation, rotationAnimation) = animations
            
            val windForce = windEffect * 20f
            val x = size.width * leaf.startX + swayAnimation + windForce
            val y = fallAnimation
            
            // Draw if visible
            if (y > -100f && y < size.height + 100f && x > -100f && x < size.width + 100f) {
                rotate(rotationAnimation, pivot = Offset(x, y)) {
                    drawVisibleLeaf(
                        position = Offset(x, y), 
                        size = leaf.size, 
                        color = leaf.color.copy(alpha = leaf.opacity),
                        leafType = leaf.leafType
                    )
                }
            }
        }
    }
}

// More visible leaf drawing
fun DrawScope.drawVisibleLeaf(
    position: Offset,
    size: Float,
    color: Color,
    leafType: LeafType = LeafType.MAPLE
) {
    when (leafType) {
        LeafType.MAPLE -> drawVisibleMapleLeaf(position, size, color)
        LeafType.OAK -> drawVisibleOakLeaf(position, size, color)
        LeafType.BIRCH -> drawVisibleBirchLeaf(position, size, color)
        LeafType.WILLOW -> drawVisibleWillowLeaf(position, size, color)
    }
}

fun DrawScope.drawVisibleMapleLeaf(position: Offset, size: Float, color: Color) {
    val path = Path()
    val centerX = position.x
    val centerY = position.y
    
    // More prominent maple leaf
    path.moveTo(centerX, centerY - size * 0.4f)
    path.cubicTo(
        centerX + size * 0.3f, centerY - size * 0.1f,
        centerX + size * 0.35f, centerY + size * 0.3f,
        centerX, centerY + size * 0.5f
    )
    path.cubicTo(
        centerX - size * 0.35f, centerY + size * 0.3f,
        centerX - size * 0.3f, centerY - size * 0.1f,
        centerX, centerY - size * 0.4f
    )
    
    drawPath(path, color = color)
}

fun DrawScope.drawVisibleOakLeaf(position: Offset, size: Float, color: Color) {
    val path = Path()
    val centerX = position.x
    val centerY = position.y
    
    path.moveTo(centerX, centerY - size * 0.3f)
    path.cubicTo(
        centerX + size * 0.25f, centerY - size * 0.05f,
        centerX + size * 0.25f, centerY + size * 0.35f,
        centerX, centerY + size * 0.5f
    )
    path.cubicTo(
        centerX - size * 0.25f, centerY + size * 0.35f,
        centerX - size * 0.25f, centerY - size * 0.05f,
        centerX, centerY - size * 0.3f
    )
    
    drawPath(path, color = color)
}

fun DrawScope.drawVisibleBirchLeaf(position: Offset, size: Float, color: Color) {
    val path = Path()
    val centerX = position.x
    val centerY = position.y
    
    path.moveTo(centerX, centerY - size * 0.4f)
    path.cubicTo(
        centerX + size * 0.2f, centerY - size * 0.15f,
        centerX + size * 0.2f, centerY + size * 0.15f,
        centerX, centerY + size * 0.4f
    )
    path.cubicTo(
        centerX - size * 0.2f, centerY + size * 0.15f,
        centerX - size * 0.2f, centerY - size * 0.15f,
        centerX, centerY - size * 0.4f
    )
    
    drawPath(path, color = color)
}

fun DrawScope.drawVisibleWillowLeaf(position: Offset, size: Float, color: Color) {
    val path = Path()
    val centerX = position.x
    val centerY = position.y
    
    path.moveTo(centerX, centerY - size * 0.5f)
    path.cubicTo(
        centerX + size * 0.12f, centerY - size * 0.15f,
        centerX + size * 0.12f, centerY + size * 0.15f,
        centerX, centerY + size * 0.5f
    )
    path.cubicTo(
        centerX - size * 0.12f, centerY + size * 0.15f,
        centerX - size * 0.12f, centerY - size * 0.15f,
        centerX, centerY - size * 0.5f
    )
    
    drawPath(path, color = color)
}

// Function to get realistic autumn colors with better visibility
fun getAutumnLeafColor(): Color {
    return when (Random.nextInt(6)) {
        0 -> Color(0xCCFF6B35) // Semi-transparent bright orange
        1 -> Color(0xCCD84315) // Semi-transparent deep orange-red
        2 -> Color(0xCCFF8F00) // Semi-transparent amber
        3 -> Color(0xCC8BC34A) // Semi-transparent late green
        4 -> Color(0xCC5D4037) // Semi-transparent brown
        else -> Color(0xCCFFD54F) // Semi-transparent golden yellow
    }
}

@Composable
fun FallingLeavesCanvas(
    leaves: List<Leaf>, 
    modifier: Modifier = Modifier,
    layerDepth: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Simplified wind effect - more subtle
    val windEffect by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Create simpler animations for each leaf
    val leafAnimations = leaves.map { leaf ->
        // Faster vertical falling animation 
        val fallAnimation by infiniteTransition.animateFloat(
            initialValue = leaf.startY,
            targetValue = 1500f, // Shorter fall distance
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (8000 / (leaf.speed * layerDepth)).toInt(), // Faster falling (8s instead of 15s)
                    easing = LinearEasing
                )
            )
        )
        
        // Simplified horizontal swaying
        val swayAnimation by infiniteTransition.animateFloat(
            initialValue = -leaf.swayAmount,
            targetValue = leaf.swayAmount,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4000, // Fixed timing for predictability
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        // Simple rotation animation
        val rotationAnimation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 6000, // Fixed 6 second rotation
                    easing = LinearEasing
                )
            )
        )
        
        Triple(fallAnimation, swayAnimation, rotationAnimation) to leaf
    }
    
    Canvas(modifier = modifier) {
        leafAnimations.forEach { (animations, leaf) ->
            val (fallAnimation, swayAnimation, rotationAnimation) = animations
            
            // Simplified positioning
            val windForce = windEffect * 20f // Reduced wind effect
            val x = size.width * leaf.startX + swayAnimation + windForce
            val y = fallAnimation
            
            // Apply layer depth scaling
            val scaledSize = leaf.size * layerDepth
            val adjustedOpacity = (leaf.opacity * layerDepth).coerceAtLeast(0.7f) // Ensure minimum visibility
            
            // Only draw if the leaf is visible on screen (with buffer)
            if (y > -100f && y < size.height + 100f && x > -100f && x < size.width + 100f) {
                // Draw leaf with rotation
                rotate(
                    rotationAnimation, 
                    pivot = Offset(x, y)
                ) {
                    drawEnhancedLeaf(
                        position = Offset(x, y), 
                        size = scaledSize, 
                        color = leaf.color.copy(alpha = adjustedOpacity),
                        leafType = leaf.leafType
                    )
                }
            }
        }
    }
}

fun DrawScope.drawEnhancedLeaf(
    position: Offset,
    size: Float,
    color: Color,
    leafType: LeafType = LeafType.MAPLE
) {
    val path = Path()
    
    when (leafType) {
        LeafType.MAPLE -> drawMapleLeaf(path, position, size, color)
        LeafType.OAK -> drawOakLeaf(path, position, size, color)
        LeafType.BIRCH -> drawBirchLeaf(path, position, size, color)
        LeafType.WILLOW -> drawWillowLeaf(path, position, size, color)
    }
}

fun DrawScope.drawMapleLeaf(path: Path, position: Offset, size: Float, color: Color) {
    path.reset()
    
    // Simplified maple leaf shape - more visible
    val centerX = position.x
    val centerY = position.y
    
    // Main body of the leaf - larger and simpler
    path.moveTo(centerX, centerY - size * 0.2f)
    
    // Right side
    path.cubicTo(
        centerX + size * 0.4f, centerY,
        centerX + size * 0.5f, centerY + size * 0.3f,
        centerX + size * 0.3f, centerY + size * 0.6f
    )
    
    // Bottom point
    path.lineTo(centerX, centerY + size * 0.8f)
    
    // Left side
    path.cubicTo(
        centerX - size * 0.3f, centerY + size * 0.6f,
        centerX - size * 0.5f, centerY + size * 0.3f,
        centerX - size * 0.4f, centerY
    )
    
    // Top point
    path.lineTo(centerX, centerY - size * 0.2f)
    
    path.close()
    drawPath(path, color = color)
}

fun DrawScope.drawOakLeaf(path: Path, position: Offset, size: Float, color: Color) {
    path.reset()
    
    val centerX = position.x
    val centerY = position.y
    
    // Simplified oak leaf - oval with wavy edges
    path.moveTo(centerX, centerY - size * 0.4f)
    path.cubicTo(
        centerX + size * 0.4f, centerY - size * 0.2f,
        centerX + size * 0.4f, centerY + size * 0.4f,
        centerX, centerY + size * 0.6f
    )
    path.cubicTo(
        centerX - size * 0.4f, centerY + size * 0.4f,
        centerX - size * 0.4f, centerY - size * 0.2f,
        centerX, centerY - size * 0.4f
    )
    
    path.close()
    drawPath(path, color = color)
}

fun DrawScope.drawBirchLeaf(path: Path, position: Offset, size: Float, color: Color) {
    path.reset()
    
    // Simple oval shape
    val centerX = position.x
    val centerY = position.y
    
    path.moveTo(centerX, centerY - size * 0.5f)
    path.cubicTo(
        centerX + size * 0.25f, centerY - size * 0.3f,
        centerX + size * 0.25f, centerY + size * 0.3f,
        centerX, centerY + size * 0.5f
    )
    path.cubicTo(
        centerX - size * 0.25f, centerY + size * 0.3f,
        centerX - size * 0.25f, centerY - size * 0.3f,
        centerX, centerY - size * 0.5f
    )
    
    path.close()
    drawPath(path, color = color)
}

fun DrawScope.drawWillowLeaf(path: Path, position: Offset, size: Float, color: Color) {
    path.reset()
    
    // Long, narrow leaf
    val centerX = position.x
    val centerY = position.y
    
    path.moveTo(centerX, centerY - size * 0.6f)
    path.cubicTo(
        centerX + size * 0.12f, centerY - size * 0.2f,
        centerX + size * 0.12f, centerY + size * 0.2f,
        centerX, centerY + size * 0.6f
    )
    path.cubicTo(
        centerX - size * 0.12f, centerY + size * 0.2f,
        centerX - size * 0.12f, centerY - size * 0.2f,
        centerX, centerY - size * 0.6f
    )
    
    path.close()
    drawPath(path, color = color)
}