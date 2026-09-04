package com.optivax.app.data.system

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Process
import android.os.UserHandle
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Standard, Play Store compliant wrapper around Android's [StorageStatsManager]
 * and [StorageManager] system services.
 */
@Singleton
class StorageStatsHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val storageStatsManager: StorageStatsManager? by lazy {
        context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
    }

    private val storageManager: StorageManager? by lazy {
        context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
    }

    private val defaultStorageUuid: UUID = StorageManager.UUID_DEFAULT

    /**
     * Queries total internal storage capacity in bytes.
     */
    fun getTotalBytes(): Long {
        return try {
            storageStatsManager?.getTotalBytes(defaultStorageUuid) ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Queries available/free internal storage in bytes.
     */
    fun getFreeBytes(): Long {
        return try {
            storageStatsManager?.getFreeBytes(defaultStorageUuid) ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Safely queries app, data, and cache sizes for a given package name.
     */
    fun getPackageStorageStats(
        packageName: String,
        userHandle: UserHandle = Process.myUserHandle()
    ): PackageStorageMetrics {
        return try {
            val stats: StorageStats? = storageStatsManager?.queryStatsForPackage(
                defaultStorageUuid,
                packageName,
                userHandle
            )
            if (stats != null) {
                PackageStorageMetrics(
                    appBytes = stats.appBytes,
                    dataBytes = stats.dataBytes,
                    cacheBytes = stats.cacheBytes
                )
            } else {
                PackageStorageMetrics(0L, 0L, 0L)
            }
        } catch (e: Exception) {
            PackageStorageMetrics(0L, 0L, 0L)
        }
    }
}

/**
 * Encapsulates app binary, user data, and cache breakdown in bytes.
 */
data class PackageStorageMetrics(
    val appBytes: Long,
    val dataBytes: Long,
    val cacheBytes: Long
) {
    val totalBytes: Long = appBytes + dataBytes + cacheBytes
}
