package com.example.voicebutton

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object StatisticsHelper {
    
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    
    fun getWeekStartString(): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
    
    fun recordVolumeChange(context: Context) {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val today = getCurrentDateString()
        
        // Daily stats
        val dailyKey = "volume_changes_$today"
        val dailyChanges = prefs.getInt(dailyKey, 0)
        
        // Weekly stats
        val weekStart = getWeekStartString()
        val weeklyKey = "volume_changes_week_$weekStart"
        val weeklyChanges = prefs.getInt(weeklyKey, 0)
        
        prefs.edit()
            .putInt(dailyKey, dailyChanges + 1)
            .putInt(weeklyKey, weeklyChanges + 1)
            .apply()
    }
    
    fun recordSessionStart(context: Context) {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("session_start_time", System.currentTimeMillis())
            .apply()
    }
    
    fun recordSessionEnd(context: Context) {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val startTime = prefs.getLong("session_start_time", System.currentTimeMillis())
        val sessionDuration = (System.currentTimeMillis() - startTime) / 1000 / 60 // minutes
        
        val today = getCurrentDateString()
        val dailyTimeKey = "session_time_$today"
        val currentTime = prefs.getInt(dailyTimeKey, 0)
        
        prefs.edit()
            .putInt(dailyTimeKey, currentTime + sessionDuration.toInt())
            .remove("session_start_time")
            .apply()
    }
    
    fun recordProfileUsage(context: Context, profileId: String) {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val today = getCurrentDateString()
        
        val profileUsageKey = "profile_usage_${profileId}_$today"
        val currentUsage = prefs.getInt(profileUsageKey, 0)
        
        prefs.edit()
            .putInt(profileUsageKey, currentUsage + 1)
            .apply()
    }
    
    fun getDailyStats(context: Context): Map<String, Any> {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val today = getCurrentDateString()
        
        return mapOf(
            "today_volume_changes" to prefs.getInt("volume_changes_$today", 0),
            "today_session_time" to prefs.getInt("session_time_$today", 0)
        )
    }
    
    fun getWeeklyStats(context: Context): Map<String, Any> {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val weekStart = getWeekStartString()
        
        return mapOf(
            "week_volume_changes" to prefs.getInt("volume_changes_week_$weekStart", 0)
        )
    }
    
    fun getAllStats(context: Context): Map<String, Any> {
        val dailyStats = getDailyStats(context)
        val weeklyStats = getWeeklyStats(context)
        
        return dailyStats + weeklyStats
    }
    
    fun getMostUsedProfile(context: Context): String {
        val prefs = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
        val today = getCurrentDateString()
        
        val profiles = listOf("gaming", "work", "sleep", "home")
        var mostUsed = "home"
        var maxUsage = 0
        
        profiles.forEach { profileId ->
            val usage = prefs.getInt("profile_usage_${profileId}_$today", 0)
            if (usage > maxUsage) {
                maxUsage = usage
                mostUsed = profileId
            }
        }
        
        return mostUsed
    }
}
