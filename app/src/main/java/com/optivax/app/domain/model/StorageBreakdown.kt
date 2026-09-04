package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils

/**
 * Pure Kotlin domain model representing the device storage allocation breakdown.
 */
data class StorageBreakdown(
    val totalBytes: Long,
    val freeBytes: Long,
    val systemBytes: Long = 0L,
    val appsBytes: Long = 0L,
    val mediaBytes: Long = 0L,
    val otherBytes: Long = 0L
) {
    val usedBytes: Long = (totalBytes - freeBytes).coerceAtLeast(0L)

    val usedPercentage: Float
        get() = if (totalBytes > 0L) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100f).toFloat().coerceIn(0f, 100f)
        } else 0f

    val freePercentage: Float
        get() = (100f - usedPercentage).coerceIn(0f, 100f)

    val formattedTotal: String get() = FileUtils.formatFileSize(totalBytes)
    val formattedFree: String get() = FileUtils.formatFileSize(freeBytes)
    val formattedUsed: String get() = FileUtils.formatFileSize(usedBytes)
    val formattedApps: String get() = FileUtils.formatFileSize(appsBytes)
    val formattedSystem: String get() = FileUtils.formatFileSize(systemBytes)
    val formattedMedia: String get() = FileUtils.formatFileSize(mediaBytes)
    val formattedOther: String get() = FileUtils.formatFileSize(otherBytes)
}
