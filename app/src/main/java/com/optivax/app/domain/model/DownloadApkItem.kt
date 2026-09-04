package com.optivax.app.domain.model

import com.optivax.app.core.FileUtils
import java.io.File

enum class DownloadType {
    APK,
    ARCHIVE,
    DOCUMENT,
    OTHER
}

data class DownloadApkItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val sizeBytes: Long = file.length(),
    val lastModified: Long = file.lastModified(),
    val type: DownloadType = resolveDownloadType(file.extension),
    val apkPackageName: String? = null,
    val apkAppName: String? = null,
    val apkVersionName: String? = null,
    val apkVersionCode: Long? = null,
    val isAlreadyInstalled: Boolean = false,
    val installedVersionName: String? = null,
    val isOld: Boolean = false
) {
    val isApk: Boolean get() = type == DownloadType.APK

    val formattedSize: String
        get() = FileUtils.formatFileSize(sizeBytes)

    companion object {
        fun resolveDownloadType(extension: String): DownloadType {
            val ext = extension.lowercase()
            return when {
                ext == "apk" -> DownloadType.APK
                ext in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz") -> DownloadType.ARCHIVE
                ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "epub") -> DownloadType.DOCUMENT
                else -> DownloadType.OTHER
            }
        }
    }
}
