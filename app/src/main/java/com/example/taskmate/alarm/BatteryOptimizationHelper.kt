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

    /**
     * Standard `isIgnoringBatteryOptimizations()` only reports AOSP's own Doze whitelist — it says
     * nothing about the separate, undocumented "autostart" / "background app management" managers
     * several OEMs ship on top (confirmed on this exact codebase: on a Vivo device, the app was
     * granted the standard exemption and its alarm still never fired). There's no public API for
     * these, only known-by-convention component names per OEM that can vanish or move between
     * skin versions — so this is a best-effort deep link into whichever settings screen exists,
     * not a guarantee, and callers must treat a `true` return as "we found something to show the
     * user," not "the permission is now granted."
     */
    fun openAutoStartSettings(context: Context): Boolean {
        val candidates = when (Build.MANUFACTURER.lowercase()) {
            "xiaomi" -> listOf(
                "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
            "vivo" -> listOf(
                "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
                "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.PurviewTabActivity",
                "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
            "oppo" -> listOf(
                "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
                "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
            "huawei", "honor" -> listOf(
                "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
            "oneplus" -> listOf(
                "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
            "samsung" -> listOf(
                "com.samsung.android.lool" to "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
            "letv", "leeco" -> listOf(
                "com.letv.android.letvsafe" to "com.letv.android.letvsafe.AutobootManageActivity"
            )
            "asus" -> listOf(
                "com.asus.mobilemanager" to "com.asus.mobilemanager.autostart.AutoStartActivity"
            )
            else -> emptyList()
        }

        for ((pkg, cls) in candidates) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(pkg, cls)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (e: ActivityNotFoundException) {
                // Try the next known candidate for this manufacturer.
            } catch (e: SecurityException) {
                Log.w("BatteryOptimizationHelper", "Denied launching $pkg/$cls", e)
            }
        }
        return false
    }
}
