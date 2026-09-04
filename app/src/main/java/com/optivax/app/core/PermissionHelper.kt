package com.optivax.app.core

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Standard, Play Store compliant helper for checking and requesting
 * system permissions and usage access settings with unified logging.
 */
object PermissionHelper {

    /**
     * Checks if the app has been granted PACKAGE_USAGE_STATS permission
     * by the user via system settings.
     */
    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        if (appOps == null) {
            Log.e(Constants.TAG, "[PERM] AppOpsManager is null")
            return false
        }

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        val isGranted = (mode == AppOpsManager.MODE_ALLOWED)
        Log.d(Constants.TAG, "[PERM] UsageStatsPermission status: $isGranted (mode: $mode)")
        return isGranted
    }

    /**
     * Checks if the app has All Files Access on Android 11+ (API 30+)
     * or standard storage permissions on older versions.
     */
    fun hasAllFilesAccess(context: Context): Boolean {
        val hasAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            hasStoragePermission(context)
        }
        Log.d(Constants.TAG, "[PERM] AllFilesAccess (API ${Build.VERSION.SDK_INT}) status: $hasAccess")
        return hasAccess
    }

    /**
     * Creates an Intent to prompt the user for All Files Access on Android 11+.
     */
    fun getAllFilesAccessIntent(context: Context): Intent {
        Log.d(Constants.TAG, "[PERM] Launching AllFilesAccess settings intent")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } catch (e: Exception) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        } else {
            getAppSettingsIntent(context)
        }
    }

    /**
     * Creates an Intent directing the user to the system Usage Access Settings.
     */
    fun getUsageAccessIntent(context: Context): Intent {
        Log.d(Constants.TAG, "[PERM] Launching UsageAccess settings intent")
        val packageIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (packageIntent.resolveActivity(context.packageManager) != null) {
            packageIntent
        } else {
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    /**
     * Checks if appropriate media/storage permissions are granted based on Android OS version.
     */
    fun hasStoragePermission(context: Context): Boolean {
        val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasImages = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            val hasVideo = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED

            val hasAudio = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            hasImages && hasVideo && hasAudio
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        Log.d(Constants.TAG, "[PERM] StoragePermission (API ${Build.VERSION.SDK_INT}) status: $isGranted")
        return isGranted
    }

    /**
     * Returns the list of runtime permissions needed to scan media & storage.
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Creates an Intent pointing to this application's App Details screen.
     */
    fun getAppSettingsIntent(context: Context): Intent {
        Log.d(Constants.TAG, "[PERM] Launching ApplicationDetails settings intent")
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
