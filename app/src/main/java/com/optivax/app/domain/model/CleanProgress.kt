package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils

data class CleanProgress(
    val current: Int,
    val total: Int,
    val currentFileName: String,
    val freedBytes: Long
) {
    val progressPercentage: Float
        get() = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedFreed: String
        get() = FileUtils.formatFileSize(freedBytes)
}
