package com.lokotube.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Helps the user keep playback alive in the background. Xiaomi/HyperOS
 * (and other aggressive OEMs) kill the playback service once the screen
 * turns off unless the app is whitelisted in two places: battery
 * optimization and auto-start. This object only deep-links to the right
 * system screens — the user makes the choice.
 */
object PowerSettings {
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return pm?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val direct = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:${context.packageName}")),
                )
                true
            } else {
                false
            }
        }.getOrDefault(false)
        if (direct) return
        runCatching {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    fun openAutoStartSettings(context: Context) {
        val opened = runCatching {
            context.startActivity(
                Intent().setComponent(
                    ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity",
                    ),
                ),
            )
            true
        }.getOrDefault(false)
        if (opened) return
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${context.packageName}")),
            )
        }
    }
}