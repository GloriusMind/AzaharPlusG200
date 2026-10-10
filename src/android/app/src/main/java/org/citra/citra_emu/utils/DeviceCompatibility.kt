// Copyright 2026 Azahar Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.citra.citra_emu.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import org.citra.citra_emu.R

object DeviceCompatibility {
    private const val MIN_ADVERTISED_RAM_BYTES = 8_000_000_000L
    private const val MIN_LEGACY_RAM_BYTES = 7_000_000_000L

    fun isSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        if (Build.SOC_MANUFACTURER.uppercase(Locale.ROOT) !in setOf("QUALCOMM", "QTI")) {
            return false
        }
        if (Build.SOC_MODEL.filter(Char::isLetterOrDigit).uppercase(Locale.ROOT) != "SM8350AC") {
            return false
        }
        if (Build.SUPPORTED_ABIS.none { it == "arm64-v8a" }) return false

        val memoryInfo = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getMemoryInfo(memoryInfo)
        val totalMemory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            memoryInfo.advertisedMem
        } else {
            memoryInfo.totalMem
        }
        val minimumMemory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            MIN_ADVERTISED_RAM_BYTES
        } else {
            // Older Android versions report usable RAM after hardware-reserved memory.
            MIN_LEGACY_RAM_BYTES
        }

        return totalMemory >= minimumMemory
    }

    fun showUnsupportedDeviceDialog(activity: Activity): AlertDialog =
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.unsupported_device_title)
            .setMessage(R.string.unsupported_device_message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> activity.finish() }
            .show()
}
