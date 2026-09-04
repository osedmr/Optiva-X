package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils

/**
 * Pure Kotlin domain model representing a group of identical files sharing the same SHA-256 hash.
 */
data class DuplicateItem(
    val sha256Hash: String,
    val sizeBytes: Long,
    val duplicates: List<DuplicateFileDetail>
) {
    val wastedBytes: Long = if (duplicates.size > 1) {
        sizeBytes * (duplicates.size - 1)
    } else {
        0L
    }

    val formattedSize: String get() = FileUtils.formatFileSize(sizeBytes)
    val formattedWastedSize: String get() = FileUtils.formatFileSize(wastedBytes)
}

/**
 * Details of an individual file within a duplicate file group.
 */
data class DuplicateFileDetail(
    val filePath: String,
    val fileName: String,
    val lastModified: Long,
    val mimeCategory: String,
    val isOriginal: Boolean = false,
    val isSuggestedDeletion: Boolean = false
)
