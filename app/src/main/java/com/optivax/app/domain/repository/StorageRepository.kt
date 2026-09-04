package com.optivax.app.domain.repository

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.LargeFileItem
import com.optivax.app.domain.model.ScreenshotItem
import com.optivax.app.domain.model.StorageBreakdown
import com.optivax.app.domain.model.WhatsAppMediaCategory
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for storage analysis, duplicate finding, WhatsApp media cleaner, and corpse file cleanup.
 */
interface StorageRepository {

    /**
     * Queries device storage breakdown (total, free, system, apps, media, other).
     */
    fun getStorageBreakdown(): Flow<Resource<StorageBreakdown>>

    /**
     * Scans specified directories for duplicate files using partial and full SHA-256 signatures.
     */
    fun findDuplicates(targetDirectories: List<File>): Flow<Resource<List<DuplicateItem>>>

    /**
     * Scans and organizes redundant WhatsApp media files (Sent folders, voice notes, stickers, status caches).
     */
    fun getWhatsAppMediaCategories(): Flow<Resource<List<WhatsAppMediaCategory>>>

    /**
     * Scans and organizes redundant Telegram media files (Videos, Images, Documents, Audio, Stories).
     */
    fun getTelegramMediaCategories(): Flow<Resource<List<com.optivax.app.domain.model.TelegramMediaCategory>>>

    /**
     * Discovers residual files and directories left behind by uninstalled applications.
     */
    fun findCorpseFiles(): Flow<Resource<List<CorpseItem>>>

    /**
     * Scans storage for large files exceeding minSizeBytes, ordered from largest to smallest.
     */
    fun findLargeFiles(minSizeBytes: Long = 50L * 1024L * 1024L): Flow<Resource<List<LargeFileItem>>>

    /**
     * Scans storage for screenshot images, optionally filtering by age.
     */
    fun findScreenshots(minAgeDays: Int = 0): Flow<Resource<List<ScreenshotItem>>>

    /**
     * Scans photos for similar and burst shots using perceptual difference hash (dHash).
     */
    fun findSimilarPhotos(): Flow<Resource<List<com.optivax.app.domain.model.SimilarPhotoGroup>>>

    /**
     * Scans photos for blurry / out-of-focus images using Laplacian variance sharpness analysis.
     */
    fun findBlurryPhotos(threshold: Double = 80.0): Flow<Resource<List<com.optivax.app.domain.model.BlurryPhotoItem>>>

    /**
     * Scans Download directories for forgotten downloads and analyzed APK packages (installed vs uninstalled).
     */
    fun findDownloadsAndApks(minAgeDays: Int = 0): Flow<Resource<List<com.optivax.app.domain.model.DownloadApkItem>>>

    /**
     * Deletes specified list of file paths with optional real-time live progress reporting.
     * Returns total number of successfully deleted files.
     */
    fun deleteFiles(
        filePaths: List<String>,
        onProgress: ((CleanProgress) -> Unit)? = null
    ): Flow<Resource<Int>>

    /**
     * Deletes all files and directories associated with a corpse item.
     */
    fun deleteCorpseItem(corpseItem: CorpseItem): Flow<Resource<Boolean>>
}
