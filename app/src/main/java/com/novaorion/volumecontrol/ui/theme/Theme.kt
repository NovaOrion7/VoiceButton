package com.novaorion.volumecontrol.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.novaorion.volumecontrol.AppColors
import com.novaorion.volumecontrol.PreferencesHelper

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Autumn theme color scheme - more distinctive orange/brown colors
private val AutumnColorScheme = lightColorScheme(
    primary = Color(0xFFD35400), // Rich pumpkin orange
    onPrimary = Color.White,
    secondary = Color(0xFFE67E22), // Light orange
    onSecondary = Color.White,
    tertiary = Color(0xFF8E44AD), // Deep purple for contrast
    onTertiary = Color.White,
    background = Color(0xFFFFE5B4), // Light peach background
    onBackground = Color(0xFF3E2723), // Dark brown text
    surface = Color(0xFFFFCC80), // Light orange surface
    onSurface = Color(0xFF3E2723), // Dark brown text
    onSurfaceVariant = Color(0xFF5D4037) // Medium brown
)

// Sakura theme color scheme - pink and light colors for cherry blossoms
private val SakuraColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63), // Pink
    onPrimary = Color.White,
    secondary = Color(0xFFF8BBD0), // Light pink
    onSecondary = Color(0xFF3E2723), // Dark brown text
    tertiary = Color(0xFFFF80AB), // Lighter pink
    onTertiary = Color.White,
    background = Color(0xFFFFF0F5), // Lavender blush background
    onBackground = Color(0xFF3E2723), // Dark brown text
    surface = Color(0xFFFFE4E1), // Misty rose surface
    onSurface = Color(0xFF3E2723), // Dark brown text
    onSurfaceVariant = Color(0xFF880E4F) // Dark pink
)

@Composable
fun VoiceButtonTheme(
    darkTheme: Boolean? = null,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Tema tercihini kontrol et
    val shouldUseDarkTheme = darkTheme ?: run {
        val themePreference = PreferencesHelper.getTheme(context)
        when (themePreference) {
            PreferencesHelper.THEME_LIGHT -> false
            PreferencesHelper.THEME_DARK -> true
            PreferencesHelper.THEME_AUTUMN -> false // Autumn theme is always light
            PreferencesHelper.THEME_SAKURA -> false // Sakura theme is always light
            PreferencesHelper.THEME_AUTO -> isSystemInDarkTheme()
            else -> isSystemInDarkTheme()
        }
    }
    
    // Check if autumn or sakura theme is selected
    val isAutumnTheme = PreferencesHelper.getTheme(context) == PreferencesHelper.THEME_AUTUMN
    val isSakuraTheme = PreferencesHelper.getTheme(context) == PreferencesHelper.THEME_SAKURA
    
    val colorScheme = when {
        // If sakura theme, use sakura color scheme
        isSakuraTheme -> SakuraColorScheme
        // If autumn theme, always use autumn color scheme regardless of dynamic color
        isAutumnTheme -> AutumnColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (shouldUseDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        shouldUseDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}