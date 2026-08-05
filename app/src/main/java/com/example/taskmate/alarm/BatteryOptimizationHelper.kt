package com.example.taskmate.alarm

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Even though task alarms use AlarmManager.setAlarmClock() — the one scheduling API that's exempt
 * from Doze/App Standby throttling — several OEM Android skins (Xiaomi/MIUI, Oppo/ColorOS,
 * Vivo/FuntouchOS, Samsung, OnePlus, etc.) layer their own, non-AOSP battery managers on top that
 * can still freeze or kill the app process before a scheduled alarm fires, unless the app is
 * explicitly whitelisted from battery optimization. This is the single most common reason an
 * alarm reliably fires on one phone (typically the developer's own, already-whitelisted device)
 * but silently never fires on someone else's — and there is no code-only fix for it: only the
 * user, via this system dialog, can grant the exemption.
 */
object BatteryOptimizationHelper {

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
