package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File

data class SimilarPhotoGroup(
    val id: String,
    val bestPhoto: File,
    val similarPhotos: List<File>,
    val totalSavingsBytes: Long
) {
    val allPhotos: List<File>
        get() = listOf(bestPhoto) + similarPhotos

    val totalCount: Int
        get() = allPhotos.size

    val formattedSavings: String
        get() = FileUtils.formatFileSize(totalSavingsBytes)
}
