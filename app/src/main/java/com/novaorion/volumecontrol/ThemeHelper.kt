package com.novaorion.volumecontrol

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

object ThemeHelper {
    
    @Composable
    fun shouldUseDarkTheme(context: Context): Boolean {
        val theme = PreferencesHelper.getTheme(context)
        return when (theme) {
            PreferencesHelper.THEME_LIGHT -> false
            PreferencesHelper.THEME_DARK -> true
            PreferencesHelper.THEME_AUTO -> isSystemInDarkTheme()
            else -> isSystemInDarkTheme()
        }
    }
    
    fun isSystemInDarkMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
    
    fun getThemeName(context: Context): String {
        val theme = PreferencesHelper.getTheme(context)
        return when (theme) {
            PreferencesHelper.THEME_LIGHT -> context.getString(R.string.light_theme)
            PreferencesHelper.THEME_DARK -> context.getString(R.string.dark_theme)
            PreferencesHelper.THEME_AUTO -> context.getString(R.string.auto_theme)
            else -> context.getString(R.string.auto_theme)
        }
    }
}
