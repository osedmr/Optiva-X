package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TrashItem(
    val id: Long = 0L,
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val sizeBytes: Long,
    val deletedTimestamp: Long
) {
    val file: File
        get() = File(trashPath)

    val formattedSize: String
        get() = FileUtils.formatFileSize(sizeBytes)

    val daysSinceDeletion: Int
        get() {
            val diff = System.currentTimeMillis() - deletedTimestamp
            return (diff / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
        }

    val daysRemainingUntilAutoDelete: Int
        get() = (30 - daysSinceDeletion).coerceAtLeast(0)

    val formattedDeletedDate: String
        get() {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(deletedTimestamp))
        }

    val isVideo: Boolean
        get() = fileName.substringAfterLast('.', "").lowercase() in listOf("mp4", "mkv", "3gp", "mov", "avi")

    val isImage: Boolean
        get() = fileName.substringAfterLast('.', "").lowercase() in listOf("jpg", "jpeg", "png", "webp")
}
