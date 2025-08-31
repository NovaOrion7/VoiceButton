package com.novaorion.volumecontrol

import android.os.Build

/**
 * Helper class to determine build type since we're having issues with generated BuildConfig
 */
object BuildConfigHelper {
    // For now, we'll determine build type based on debuggable flag
    // In a real implementation, you might want to use other methods
    val DEBUG = isDebuggable()
    
    private fun isDebuggable(): Boolean {
        // This is a simple heuristic - in debug builds, the build fingerprint often contains "debug"
        // You might want to use a more reliable method in production
        return Build.FINGERPRINT.contains("debug") || 
               Build.FINGERPRINT.contains("userdebug") ||
               Build.TYPE == "eng"
    }
}