package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File

data class BlurryPhotoItem(
    val file: File,
    val varianceScore: Double,
    val blurPercentage: Int,
    val sizeBytes: Long
) {
    val name: String
        get() = file.name

    val path: String
        get() = file.absolutePath

    val formattedSize: String
        get() = FileUtils.formatFileSize(sizeBytes)
}
