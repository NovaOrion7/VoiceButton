package com.novaorion.volumecontrol

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object AppColors {
    // Light theme colors
    val LightPrimary = Color(0xFF1976D2)
    val LightOnSurface = Color(0xFF000000)
    val LightOnSurfaceVariant = Color(0xFF424242)
    val LightSurface = Color(0xFFFFFFFF)
    val LightBackground = Color(0xFFFFFFFF)
    
    // Dark theme colors  
    val DarkPrimary = Color(0xFFBB86FC)
    val DarkOnSurface = Color(0xFFFFFFFF)
    val DarkOnSurfaceVariant = Color(0xFFE0E0E0)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkBackground = Color(0xFF121212)
    
    // Common colors
    val Error = Color(0xFFCF6679)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
}

@Composable
fun getTextColor(): Color {
    return if (MaterialTheme.colorScheme.surface.luminance() > 0.5) {
        AppColors.LightOnSurface // Light theme
    } else {
        AppColors.DarkOnSurface // Dark theme
    }
}

@Composable  
fun getSecondaryTextColor(): Color {
    return if (MaterialTheme.colorScheme.surface.luminance() > 0.5) {
        AppColors.LightOnSurfaceVariant // Light theme
    } else {
        AppColors.DarkOnSurfaceVariant // Dark theme
    }
}
