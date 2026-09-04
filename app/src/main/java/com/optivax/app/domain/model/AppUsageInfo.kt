package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils

/**
 * Pure Kotlin domain model representing an installed application's storage usage
 * and activity/idleness state.
 */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val appSizeBytes: Long,
    val dataSizeBytes: Long,
    val cacheSizeBytes: Long,
    val lastTimeUsed: Long,
    val isIdle: Boolean,
    val isSystemApp: Boolean,
    val uid: Int = 0
) {
    val totalSizeBytes: Long = appSizeBytes + dataSizeBytes + cacheSizeBytes

    val formattedTotalSize: String get() = FileUtils.formatFileSize(totalSizeBytes)
    val formattedCacheSize: String get() = FileUtils.formatFileSize(cacheSizeBytes)
    val formattedDataSize: String get() = FileUtils.formatFileSize(dataSizeBytes)
    val formattedAppSize: String get() = FileUtils.formatFileSize(appSizeBytes)
}
