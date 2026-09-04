package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File

enum class TelegramCategoryType {
    VIDEOS,
    IMAGES,
    DOCUMENTS,
    AUDIO,
    STORIES;

    val isVisualMedia: Boolean
        get() = this == VIDEOS || this == IMAGES || this == STORIES
}

data class TelegramMediaCategory(
    val type: TelegramCategoryType,
    val title: String,
    val description: String,
    val totalBytes: Long,
    val fileCount: Int,
    val files: List<File>,
    val isRecommendedClean: Boolean = true
) {
    val formattedSize: String
        get() = FileUtils.formatFileSize(totalBytes)
}
