package com.optivax.app.data.system

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.optivax.app.core.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Standard, Play Store compliant wrapper around Android's [UsageStatsManager]
 * and [PackageManager] for querying app usage history and installed application metadata
 * with unified Logcat telemetry under [Constants.TAG].
 */
@Singleton
class UsageStatsHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    /**
     * Queries and aggregates usage stats between [startTime] and [endTime].
     */
    fun queryUsageStatsMap(startTime: Long, endTime: Long): Map<String, UsageStats> {
        return try {
            val statsList = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            ) ?: emptyList()

            Log.d(
                Constants.TAG,
                "[APPS] UsageStatsManager query completed: ${statsList.size} raw usage entries found."
            )

            statsList.associateBy { it.packageName }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[APPS] Error querying UsageStats: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Gets all installed applications on the device.
     */
    fun getInstalledApplications(includeSystemApps: Boolean = false): List<ApplicationInfo> {
        val allApps = try {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            Log.d(
                Constants.TAG,
                "[APPS] PackageManager found ${apps.size} total installed applications (includeSystemApps: $includeSystemApps)."
            )
            apps
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[APPS] Error retrieving installed applications: ${e.message}", e)
            emptyList()
        }

        val filtered = if (includeSystemApps) {
            allApps
        } else {
            allApps.filter { !isSystemApp(it) }
        }
        Log.d(Constants.TAG, "[APPS] Returning ${filtered.size} applications after system filter.")
        return filtered
    }

    /**
     * Checks whether an application is a pre-installed system application.
     */
    fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
        val isSystem = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return isSystem || isUpdatedSystem
    }

    /**
     * Obtains the user-friendly label for a given application.
     */
    fun getApplicationLabel(applicationInfo: ApplicationInfo): String {
        return try {
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            applicationInfo.packageName
        }
    }

    /**
     * Determines whether a package is currently installed on the device.
     */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
