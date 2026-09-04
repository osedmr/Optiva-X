package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScreenshotItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val sizeBytes: Long = file.length(),
    val dateTaken: Long = file.lastModified()
) {
    val formattedSize: String
        get() = FileUtils.formatFileSize(sizeBytes)

    val ageDays: Int
        get() {
            val diff = System.currentTimeMillis() - dateTaken
            return (diff / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            return sdf.format(Date(dateTaken))
        }

    val formattedAge: String
        get() = when {
            ageDays == 0 -> "Bugün"
            ageDays == 1 -> "Dün"
            ageDays < 30 -> "$ageDays gün önce"
            ageDays < 365 -> "${ageDays / 30} ay önce"
            else -> "${ageDays / 365} yıl önce"
        }
}
