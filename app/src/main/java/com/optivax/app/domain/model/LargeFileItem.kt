package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File

data class LargeFileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val sizeBytes: Long = file.length(),
    val isVideo: Boolean = file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "mov", "avi", "webm"),
    val lastModified: Long = file.lastModified()
) {
    val formattedSize: String
        get() = FileUtils.formatFileSize(sizeBytes)
}
