package com.optivax.app.domain.model

import java.io.File

enum class WhatsAppCategoryType {
    SENT_VIDEOS,
    SENT_IMAGES,
    VOICE_NOTES,
    STICKERS,
    STATUS_CACHE,
    SENT_DOCUMENTS,
    SENT_AUDIO;

    val isVisualMedia: Boolean
        get() = this == SENT_IMAGES || this == SENT_VIDEOS || this == STICKERS || this == STATUS_CACHE
}

data class WhatsAppMediaCategory(
    val type: WhatsAppCategoryType,
    val title: String,
    val description: String,
    val totalBytes: Long,
    val fileCount: Int,
    val files: List<File>,
    val isRecommendedClean: Boolean = true
) {
    val formattedSize: String
        get() = com.optivax.app.core.FileUtils.formatFileSize(totalBytes)
}
