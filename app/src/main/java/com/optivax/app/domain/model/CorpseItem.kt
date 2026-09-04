package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils

/**
 * Pure Kotlin domain model representing residual leftover files and directories
 * belonging to uninstalled applications.
 */
data class CorpseItem(
    val packageName: String,
    val path: String,
    val displayName: String,
    val sizeBytes: Long,
    val fileCount: Int
) {
    val formattedSize: String get() = FileUtils.formatFileSize(sizeBytes)
}
