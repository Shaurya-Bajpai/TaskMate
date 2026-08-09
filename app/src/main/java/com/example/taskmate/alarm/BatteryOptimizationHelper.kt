package com.example.taskmate.alarm

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    /**
     * Returns the intent rather than starting it directly so callers can launch it through an
     * `ActivityResultLauncher` and react once the system dialog it shows has actually been
     * dismissed — starting it with `context.startActivity()` returns immediately, well before
     * that dialog is even drawn, which previously let a second, unrelated in-app dialog render on
     * top of it.
     */
    fun ignoreBatteryOptimizationsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * Standard `isIgnoringBatteryOptimizations()` only reports AOSP's own Doze whitelist — it says
     * nothing about the separate, undocumented "autostart" / "background app management" managers
     * several OEMs ship on top (confirmed on this exact codebase: on a Vivo device, the app was
     * granted the standard exemption and its alarm still never fired). There's no public API for
     * these, only known-by-convention component names per OEM that can vanish or move between
     * skin versions — so this is a best-effort deep link into whichever settings screen exists,
     * not a guarantee, and callers must treat a `true` return as "we found the OEM-specific
     * screen," not "the permission is now granted." A `false` return still opens the app's own
     * details screen as a fallback (see below) so the button never appears to do nothing.
     */
    private fun knownAutoStartCandidates(): List<Pair<String, String>> = when (Build.MANUFACTURER.lowercase()) {
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

    /**
     * Checks, without launching anything, whether any of this manufacturer's known autostart
     * component names are actually present on this build — so the caller can decide up front
     * whether to promise a dedicated autostart screen or word the ask around the generic app
     * settings fallback instead. Confirmed on this exact codebase: a Vivo device on Android 14
     * has neither `com.vivo.permissionmanager` nor `com.iqoo.secure` installed any more, so
     * `openAutoStartSettings` silently falls through to the fallback there — this lets the UI
     * know that in advance instead of promising a screen that doesn't exist.
     */
    fun hasKnownAutoStartScreen(context: Context): Boolean {
        val pm = context.packageManager
        return knownAutoStartCandidates().any { (pkg, cls) ->
            try {
                pm.getActivityInfo(ComponentName(pkg, cls), 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    fun openAutoStartSettings(context: Context): Boolean {
        for ((pkg, cls) in knownAutoStartCandidates()) {
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

        // None of the known component names exist on this build (confirmed on this exact
        // a signature-protected activity this app can't launch). Falling back to the app's own
        // details screen — always present in AOSP — so "Open settings" opens *something* the user
        // can act on (its Battery section) instead of silently doing nothing.
        return try {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            false
        } catch (e: ActivityNotFoundException) {
            Log.w("BatteryOptimizationHelper", "No application details settings screen available", e)
            false
        }
    }
}