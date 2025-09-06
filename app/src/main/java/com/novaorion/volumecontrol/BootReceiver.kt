package com.novaorion.volumecontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // Auto start kontrolü
            if (PreferencesHelper.getAutoStart(context)) {
                // Volume control service'i başlat
                val volumeServiceIntent = Intent(context, VolumeControlService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(volumeServiceIntent)
                } else {
                    context.startService(volumeServiceIntent)
                }
            }
        }
    }
}
