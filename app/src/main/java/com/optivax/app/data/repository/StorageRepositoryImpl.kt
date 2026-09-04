package com.optivax.app.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.optivax.app.core.Constants
import com.optivax.app.core.FileUtils
import com.optivax.app.core.Resource
import com.optivax.app.data.local.CleanHistoryDao
import com.optivax.app.data.local.CleanHistoryEntity
import com.optivax.app.data.local.ScannedFileCacheDao
import com.optivax.app.data.local.ScannedFileCacheEntity
import com.optivax.app.data.local.WhitelistDao
import com.optivax.app.data.system.StorageStatsHelper
import com.optivax.app.data.system.UsageStatsHelper
import com.optivax.app.di.IoDispatcher
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.core.BlurDetector
import com.optivax.app.core.ImageHasher
import com.optivax.app.domain.model.BlurryPhotoItem
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DownloadApkItem
import com.optivax.app.domain.model.DownloadType
import com.optivax.app.domain.model.DuplicateFileDetail
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.LargeFileItem
import com.optivax.app.domain.model.ScreenshotItem
import com.optivax.app.domain.model.SimilarPhotoGroup
import com.optivax.app.domain.model.StorageBreakdown
import com.optivax.app.domain.model.TelegramCategoryType
import com.optivax.app.domain.model.TelegramMediaCategory
import com.optivax.app.domain.model.WhatsAppCategoryType
import com.optivax.app.domain.model.WhatsAppMediaCategory
import com.optivax.app.domain.repository.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-performance repository implementing [StorageRepository] with MediaStore + FileSystem scanning,
 * WhatsApp media hygiene, reliable batch deletion, and detailed Logcat telemetry under [Constants.TAG].
 */
@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storageStatsHelper: StorageStatsHelper,
    private val usageStatsHelper: UsageStatsHelper,
    private val scannedFileCacheDao: ScannedFileCacheDao,
    private val whitelistDao: WhitelistDao,
    private val cleanHistoryDao: CleanHistoryDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:ApplicationContext private val context: Context
) : StorageRepository {

    override fun getStorageBreakdown(): Flow<Resource<StorageBreakdown>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[STORAGE] Beginning getStorageBreakdown calculation...")
        try {
            var totalBytes = storageStatsHelper.getTotalBytes()
            var freeBytes = storageStatsHelper.getFreeBytes()

            if (totalBytes <= 0L) {
                val dataDir = Environment.getDataDirectory()
                totalBytes = dataDir.totalSpace
                freeBytes = dataDir.freeSpace
                Log.d(Constants.TAG, "[STORAGE] Used Environment fallback: Total=$totalBytes, Free=$freeBytes")
            } else {
                Log.d(Constants.TAG, "[STORAGE] StorageStatsManager returned: Total=$totalBytes, Free=$freeBytes")
            }

            // Sum application sizes
            val installedApps = usageStatsHelper.getInstalledApplications(includeSystemApps = true)
            var totalAppsBytes = 0L
            for (app in installedApps) {
                val stats = storageStatsHelper.getPackageStorageStats(app.packageName)
                totalAppsBytes += stats.totalBytes
            }
            Log.d(Constants.TAG, "[STORAGE] Total app footprint across ${installedApps.size} apps: $totalAppsBytes bytes")

            // Media size approximation
            var mediaBytes = 0L
            val standardMediaDirs = listOf(
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_DOCUMENTS,
                Environment.DIRECTORY_DOWNLOADS
            )

            for (dirType in standardMediaDirs) {
                val dir = Environment.getExternalStoragePublicDirectory(dirType)
                if (dir != null && dir.exists() && dir.isDirectory) {
                    val size = calculateDirectorySize(dir, maxDepth = 4)
                    mediaBytes += size
                }
            }
            Log.d(Constants.TAG, "[STORAGE] Approximate media size in public folders: $mediaBytes bytes")

            val totalUsed = (totalBytes - freeBytes).coerceAtLeast(0L)
            val systemEstimatedBytes = (totalBytes * 0.12).toLong().coerceAtMost(16L * 1024L * 1024L * 1024L)
            val otherBytes = (totalUsed - totalAppsBytes - mediaBytes - systemEstimatedBytes).coerceAtLeast(0L)

            val breakdown = StorageBreakdown(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                systemBytes = systemEstimatedBytes,
                appsBytes = totalAppsBytes,
                mediaBytes = mediaBytes,
                otherBytes = otherBytes
            )
            Log.d(Constants.TAG, "[STORAGE] Breakdown complete: Used=${breakdown.formattedUsed}, Free=${breakdown.formattedFree}")
            emit(Resource.Success(breakdown))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[STORAGE] Breakdown failed: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Depolama analizi sırasında hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun findDuplicates(targetDirectories: List<File>): Flow<Resource<List<DuplicateItem>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[DUPLICATES] ================= STARTING SCAN =================")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            Log.d(Constants.TAG, "[DUPLICATES] Whitelist loaded: ${whitelistedPaths.size} paths.")

            val allFiles = mutableListOf<File>()

            // 1. MediaStore Scan (Essential for Android 10-16 Scoped Storage compliance)
            val mediaStoreFiles = queryMediaStoreFiles(whitelistedPaths)
            allFiles.addAll(mediaStoreFiles)
            Log.d(Constants.TAG, "[DUPLICATES] MediaStore query gathered ${mediaStoreFiles.size} files.")

            // 2. Direct Directory Crawl across all accessible folders (depth 12 to reach nested WhatsApp Sent files)
            for (dir in targetDirectories) {
                if (dir.exists() && dir.isDirectory) {
                    val countBefore = allFiles.size
                    collectFilesRecursively(dir, allFiles, whitelistedPaths, maxDepth = 12)
                    val added = allFiles.size - countBefore
                    Log.d(Constants.TAG, "[DUPLICATES] Crawled '${dir.name}': found $added files.")
                } else {
                    Log.d(Constants.TAG, "[DUPLICATES] Directory '${dir.path}' does not exist or cannot be opened.")
                }
            }

            // Deduplicate files by absolute path
            val uniqueFiles = allFiles.distinctBy { it.absolutePath }.filter { it.exists() && it.isFile && it.length() > 0L }
            Log.d(Constants.TAG, "[DUPLICATES] Total unique non-empty files collected: ${uniqueFiles.size}")

            if (uniqueFiles.isEmpty()) {
                Log.d(Constants.TAG, "[DUPLICATES] No files to analyze.")
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Phase 2: Size Match Filter (0 I/O penalty for unique sizes)
            val filesBySize = uniqueFiles
                .groupBy { it.length() }
                .filter { (size, list) -> size > 0L && list.size > 1 }

            val totalSizeCandidates = filesBySize.values.sumOf { it.size }
            Log.d(
                Constants.TAG,
                "[DUPLICATES] Size Collisions: Found ${filesBySize.size} size groups containing $totalSizeCandidates files."
            )

            if (filesBySize.isEmpty()) {
                Log.d(Constants.TAG, "[DUPLICATES] No size collisions detected. Finished.")
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Phase 3: Fast 8KB Partial Hash Filter
            val partialHashCandidates = mutableListOf<File>()
            for ((size, list) in filesBySize) {
                val byPartial = list.groupBy { file ->
                    FileUtils.calculatePartialSha256(file)
                }.filter { (hash, files) -> hash.isNotEmpty() && files.size > 1 }

                for ((hash, matchingFiles) in byPartial) {
                    Log.d(
                        Constants.TAG,
                        "[DUPLICATES] Partial hash match (${matchingFiles.size} files, size $size bytes): hash=$hash"
                    )
                    partialHashCandidates.addAll(matchingFiles)
                }
            }

            Log.d(Constants.TAG, "[DUPLICATES] Partial hash candidates to verify with full SHA-256: ${partialHashCandidates.size}")

            if (partialHashCandidates.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Phase 4: Full SHA-256 with Room Hash Cache
            val fullHashGroups = mutableMapOf<String, MutableList<File>>()

            for (file in partialHashCandidates) {
                val cached = scannedFileCacheDao.getByPath(file.absolutePath)
                val fullHash = if (cached != null &&
                    cached.sizeBytes == file.length() &&
                    cached.lastModified == file.lastModified() &&
                    cached.sha256Hash.isNotEmpty()
                ) {
                    cached.sha256Hash
                } else {
                    val computed = FileUtils.calculateSha256(file)
                    if (computed.isNotEmpty()) {
                        val ext = FileUtils.getFileExtension(file.name)
                        scannedFileCacheDao.insert(
                            ScannedFileCacheEntity(
                                filePath = file.absolutePath,
                                sizeBytes = file.length(),
                                lastModified = file.lastModified(),
                                partialHash = FileUtils.calculatePartialSha256(file),
                                sha256Hash = computed,
                                mimeCategory = FileUtils.getMimeCategory(ext)
                            )
                        )
                    }
                    computed
                }

                if (fullHash.isNotEmpty()) {
                    fullHashGroups.getOrPut(fullHash) { mutableListOf() }.add(file)
                }
            }

            // Build DuplicateItem domain models
            // Prioritize Sent copies for deletion (non-Sent files sorted first as originals)
            val duplicateItems = fullHashGroups
                .filter { it.value.size > 1 }
                .map { (hash, files) ->
                    val sortedFiles = files.sortedWith(
                        compareBy<File> { it.absolutePath.contains("/Sent/") }
                            .thenBy { it.lastModified() }
                    )
                    val details = sortedFiles.mapIndexed { index, file ->
                        val ext = FileUtils.getFileExtension(file.name)
                        val isSentCopy = file.absolutePath.contains("/Sent/")
                        DuplicateFileDetail(
                            filePath = file.absolutePath,
                            fileName = file.name,
                            lastModified = file.lastModified(),
                            mimeCategory = FileUtils.getMimeCategory(ext),
                            isOriginal = (index == 0),
                            isSuggestedDeletion = (index > 0 || isSentCopy)
                        )
                    }
                    DuplicateItem(
                        sha256Hash = hash,
                        sizeBytes = files.first().length(),
                        duplicates = details
                    )
                }
                .sortedByDescending { it.wastedBytes }

            val totalWasted = duplicateItems.sumOf { it.wastedBytes }
            Log.d(
                Constants.TAG,
                "[DUPLICATES] SCAN COMPLETE: ${duplicateItems.size} duplicate groups found, total ${FileUtils.formatFileSize(totalWasted)} wasted space."
            )

            emit(Resource.Success(duplicateItems))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[DUPLICATES] Error during duplicate scan: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Çift dosya taramasında hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun getWhatsAppMediaCategories(): Flow<Resource<List<WhatsAppMediaCategory>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[WHATSAPP] ================= STARTING WHATSAPP SCAN =================")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val categories = mutableListOf<WhatsAppMediaCategory>()

            val externalStorage = Environment.getExternalStorageDirectory()
            val candidateRoots = listOf(
                File(externalStorage, "Android/media/com.whatsapp/WhatsApp/Media"),
                File(externalStorage, "WhatsApp/Media"),
                File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media")
            )

            val activeMediaDirs = candidateRoots.filter { it.exists() && it.isDirectory }
            if (activeMediaDirs.isEmpty()) {
                Log.d(Constants.TAG, "[WHATSAPP] No active WhatsApp media directory found.")
                emit(Resource.Success(emptyList()))
                return@flow
            }

            for (mediaDir in activeMediaDirs) {
                Log.d(Constants.TAG, "[WHATSAPP] Scanning media directory: ${mediaDir.path}")

                fun scanCategory(
                    type: WhatsAppCategoryType,
                    title: String,
                    description: String,
                    relativePaths: List<String>,
                    isRecommended: Boolean = true
                ) {
                    val fileList = mutableListOf<File>()
                    for (rel in relativePaths) {
                        val target = File(mediaDir, rel)
                        if (target.exists()) {
                            if (target.isDirectory) {
                                collectFilesRecursively(target, fileList, whitelistedPaths, maxDepth = 8, allowHidden = true)
                            } else if (target.isFile && target.length() > 0L) {
                                fileList.add(target)
                            }
                        }
                    }
                    val validFiles = fileList.distinctBy { it.absolutePath }.filter { it.exists() && it.isFile && it.length() > 0L }
                    val totalBytes = validFiles.sumOf { it.length() }
                    if (validFiles.isNotEmpty() && totalBytes > 0L) {
                        categories.add(
                            WhatsAppMediaCategory(
                                type = type,
                                title = title,
                                description = description,
                                totalBytes = totalBytes,
                                fileCount = validFiles.size,
                                files = validFiles,
                                isRecommendedClean = isRecommended
                            )
                        )
                        Log.d(Constants.TAG, "[WHATSAPP] Found '$title': ${validFiles.size} files, ${FileUtils.formatFileSize(totalBytes)}")
                    }
                }

                // 1. Sent Videos (largest space waster!)
                scanCategory(
                    WhatsAppCategoryType.SENT_VIDEOS,
                    "Gönderilen Videolar",
                    "Başkalarına gönderdiğiniz video kopyaları. Orijinaller korunur.",
                    listOf("WhatsApp Video/Sent")
                )

                // 2. Sent Images
                scanCategory(
                    WhatsAppCategoryType.SENT_IMAGES,
                    "Gönderilen Fotoğraflar",
                    "Sohbetlerde ilettiğiniz fotoğrafların fazlalık kopyaları.",
                    listOf("WhatsApp Images/Sent")
                )

                // 3. Status Cache (.Statuses)
                scanCategory(
                    WhatsAppCategoryType.STATUS_CACHE,
                    "Durum Önbelleği (.Statuses)",
                    "İzlediğiniz durum fotoğrafları ve videolarının gizli önbelleği.",
                    listOf(".Statuses")
                )

                // 4. Stickers
                scanCategory(
                    WhatsAppCategoryType.STICKERS,
                    "Çıkartmalar (Stickers)",
                    "Sohbetlerden kaydedilen çıkartma dosyaları (.webp).",
                    listOf("WhatsApp Stickers")
                )

                // 5. Voice Notes
                scanCategory(
                    WhatsAppCategoryType.VOICE_NOTES,
                    "Sesli Notlar (Voice Notes)",
                    "Dinlenen ve biriken ses kayıtları (.opus).",
                    listOf("WhatsApp Voice Notes"),
                    isRecommended = false // Keep false so user explicitly confirms
                )

                // 6. Sent Documents
                scanCategory(
                    WhatsAppCategoryType.SENT_DOCUMENTS,
                    "Gönderilen Belgeler",
                    "Başkalarına yolladığınız PDF ve belge kopyaları.",
                    listOf("WhatsApp Documents/Sent")
                )

                // 7. Sent Audio
                scanCategory(
                    WhatsAppCategoryType.SENT_AUDIO,
                    "Gönderilen Sesler",
                    "Paylaştığınız müzik ve ses dosyası kopyaları.",
                    listOf("WhatsApp Audio/Sent")
                )
            }

            val totalBytesAll = categories.sumOf { it.totalBytes }
            Log.d(Constants.TAG, "[WHATSAPP] SCAN COMPLETE: ${categories.size} categories found, total ${FileUtils.formatFileSize(totalBytesAll)} space.")
            emit(Resource.Success(categories.sortedByDescending { it.totalBytes }))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[WHATSAPP] Error scanning WhatsApp media: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "WhatsApp medyası taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun findCorpseFiles(): Flow<Resource<List<CorpseItem>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[CORPSE] ================= STARTING CORPSE SCAN =================")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val corpseItems = mutableListOf<CorpseItem>()

            val externalStorage = Environment.getExternalStorageDirectory()
            val candidateDirs = mutableListOf<File>()

            // 1. Android/media directory (accessible with MANAGE_EXTERNAL_STORAGE, contains app packages)
            val androidMedia = File(externalStorage, "Android/media")
            if (androidMedia.exists() && androidMedia.isDirectory) {
                candidateDirs.add(androidMedia)
            }

            // 2. Android/data and Android/obb
            val dataDir = File(externalStorage, "Android/data")
            val obbDir = File(externalStorage, "Android/obb")
            candidateDirs.add(dataDir)
            candidateDirs.add(obbDir)

            for (parentDir in candidateDirs) {
                if (parentDir.exists() && parentDir.isDirectory) {
                    val subDirs = parentDir.listFiles()
                    if (subDirs == null) {
                        Log.d(Constants.TAG, "[CORPSE] Notice: '${parentDir.path}' is restricted by Android OS FUSE policy.")
                        continue
                    }
                    Log.d(Constants.TAG, "[CORPSE] Scanning '${parentDir.path}': found ${subDirs.size} subdirectories.")

                    for (dir in subDirs) {
                        if (!dir.isDirectory) continue
                        val dirName = dir.name

                        // Standard package name format check (e.g. com.example.app)
                        if (dirName.contains(".") && !dirName.startsWith(".")) {
                            val isInstalled = usageStatsHelper.isPackageInstalled(dirName)
                            val isWhitelisted = whitelistedPaths.contains(dir.absolutePath)

                            Log.d(
                                Constants.TAG,
                                "[CORPSE] Package check: '$dirName' | Installed: $isInstalled | Whitelisted: $isWhitelisted"
                            )

                            if (!isInstalled && !isWhitelisted) {
                                val size = calculateDirectorySize(dir, maxDepth = 6)
                                val count = countDirectoryFiles(dir, maxDepth = 6)

                                if (size > 0L) {
                                    Log.d(Constants.TAG, "[CORPSE] FOUND CORPSE: '$dirName' with $count files, $size bytes.")
                                    corpseItems.add(
                                        CorpseItem(
                                            packageName = dirName,
                                            path = dir.absolutePath,
                                            displayName = "$dirName (Artık Veriler)",
                                            sizeBytes = size,
                                            fileCount = count
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Check for leftover APK installer packages in Downloads
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists() && downloadDir.isDirectory) {
                val apkFiles = downloadDir.listFiles { file -> file.isFile && file.extension.equals("apk", ignoreCase = true) }
                if (apkFiles != null && apkFiles.isNotEmpty()) {
                    for (apk in apkFiles) {
                        val size = apk.length()
                        if (size > 0L) {
                            corpseItems.add(
                                CorpseItem(
                                    packageName = apk.name,
                                    path = apk.absolutePath,
                                    displayName = "${apk.name} (Kalıntı Kurulum Dosyası)",
                                    sizeBytes = size,
                                    fileCount = 1
                                )
                            )
                        }
                    }
                }
            }

            // 4. Check for .thumbnails cache folders (DCIM/.thumbnails, Pictures/.thumbnails)
            val thumbnailDirs = listOf(
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), ".thumbnails"),
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ".thumbnails")
            )
            for (thumbDir in thumbnailDirs) {
                if (thumbDir.exists() && thumbDir.isDirectory && !whitelistedPaths.contains(thumbDir.absolutePath)) {
                    val size = calculateDirectorySize(thumbDir, maxDepth = 2)
                    val count = countDirectoryFiles(thumbDir, maxDepth = 2)
                    if (size > 0L) {
                        Log.d(Constants.TAG, "[CORPSE] Found thumbnail cache: '${thumbDir.path}' with $count files, $size bytes.")
                        corpseItems.add(
                            CorpseItem(
                                packageName = ".thumbnails",
                                path = thumbDir.absolutePath,
                                displayName = "${thumbDir.parentFile?.name ?: "Medya"}/.thumbnails (Önizleme Önbelleği)",
                                sizeBytes = size,
                                fileCount = count
                            )
                        )
                    }
                }
            }

            // 5. Check for temporary / log junk files in public folders
            val junkExtensions = setOf("tmp", "temp", "log", "dmp", "bak")
            val publicDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            ).filterNotNull().filter { it.exists() && it.isDirectory }

            for (pDir in publicDirs) {
                val junkFiles = pDir.listFiles { f -> f.isFile && junkExtensions.contains(f.extension.lowercase()) }
                if (junkFiles != null) {
                    for (jFile in junkFiles) {
                        if (!whitelistedPaths.contains(jFile.absolutePath) && jFile.length() > 0L) {
                            corpseItems.add(
                                CorpseItem(
                                    packageName = jFile.name,
                                    path = jFile.absolutePath,
                                    displayName = "${jFile.name} (Geçici/Log Artığı)",
                                    sizeBytes = jFile.length(),
                                    fileCount = 1
                                )
                            )
                        }
                    }
                }
            }

            Log.d(Constants.TAG, "[CORPSE] SCAN COMPLETE: Found ${corpseItems.size} corpse folders/files.")
            emit(Resource.Success(corpseItems.sortedByDescending { it.sizeBytes }))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[CORPSE] Error during corpse scan: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Artık dosya taramasında hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun deleteFiles(
        filePaths: List<String>,
        onProgress: ((CleanProgress) -> Unit)?
    ): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[DELETE] Starting batch deletion for ${filePaths.size} files.")
        try {
            var deletedCount = 0
            var freedBytes = 0L
            val total = filePaths.size

            for ((index, path) in filePaths.withIndex()) {
                val file = File(path)
                if (file.exists() && file.isFile) {
                    val length = file.length()
                    val parent = file.parentFile
                    val deleted = file.delete()
                    Log.d(Constants.TAG, "[DELETE] File '$path' (size $length bytes): deleted=$deleted")
                    if (deleted) {
                        deletedCount++
                        freedBytes += length
                        scannedFileCacheDao.deleteByPath(path)

                        // Prune empty parent folder (e.g. WhatsApp Voice Notes/2024xx/ or subfolders)
                        if (parent != null && parent.isDirectory &&
                            parent.name != "WhatsApp Voice Notes" &&
                            parent.name != "Media" &&
                            parent.name != "WhatsApp" &&
                            parent.name != "Sent"
                        ) {
                            val remaining = parent.listFiles()
                            if (remaining != null && remaining.isEmpty()) {
                                parent.delete()
                            }
                        }
                    }
                } else {
                    Log.d(Constants.TAG, "[DELETE] File does not exist or is not a file: '$path'")
                }

                // Report progress live
                onProgress?.invoke(
                    CleanProgress(
                        current = index + 1,
                        total = total,
                        currentFileName = file.name,
                        freedBytes = freedBytes
                    )
                )
            }

            if (freedBytes > 0L) {
                cleanHistoryDao.insert(
                    CleanHistoryEntity(
                        timestamp = System.currentTimeMillis(),
                        scanType = "DUPLICATE",
                        freedBytes = freedBytes,
                        itemsCount = deletedCount,
                        durationMs = 0L
                    )
                )
                Log.d(Constants.TAG, "[DELETE] Batch complete: Successfully freed ${FileUtils.formatFileSize(freedBytes)} ($freedBytes bytes).")
            }

            emit(Resource.Success(deletedCount))
        } catch (e: CancellationException) {
            Log.d(Constants.TAG, "[DELETE] Deletion coroutine cancelled.")
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[DELETE] Error deleting files: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Dosya silme işlemi sırasında hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun deleteCorpseItem(corpseItem: CorpseItem): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[DELETE_CORPSE] Deleting corpse item: '${corpseItem.path}'")
        try {
            val file = File(corpseItem.path)
            var success = false
            val freed = corpseItem.sizeBytes

            if (file.exists()) {
                success = if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
                Log.d(Constants.TAG, "[DELETE_CORPSE] Delete on '${file.path}': result=$success")
                if (success) {
                    cleanHistoryDao.insert(
                        CleanHistoryEntity(
                            timestamp = System.currentTimeMillis(),
                            scanType = "CORPSE_FILES",
                            freedBytes = freed,
                            itemsCount = corpseItem.fileCount,
                            durationMs = 0L
                        )
                    )
                }
            }
            emit(Resource.Success(success))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[DELETE_CORPSE] Error deleting corpse: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Artık dosya silinirken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    /**
     * Queries MediaStore (Images, Video, Audio, Downloads) via standard Android ContentResolver.
     * Guarantees file discovery even on Android 10-16 scoped storage.
     */
    private fun queryMediaStoreFiles(whitelistedPaths: Set<String>): List<File> {
        val result = mutableListOf<File>()
        val uris = mutableListOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uris.add(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
        }

        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.SIZE)

        for (uri in uris) {
            try {
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    while (cursor.moveToNext()) {
                        val path = cursor.getString(dataColumn) ?: continue
                        val size = cursor.getLong(sizeColumn)
                        if (size > 0L && !whitelistedPaths.contains(path)) {
                            val file = File(path)
                            if (file.exists() && file.isFile) {
                                result.add(file)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(Constants.TAG, "[MEDIASTORE] Error querying $uri: ${e.message}")
            }
        }
        return result
    }

    private fun collectFilesRecursively(
        dir: File,
        results: MutableList<File>,
        whitelistedPaths: Set<String>,
        maxDepth: Int,
        currentDepth: Int = 0,
        allowHidden: Boolean = false
    ) {
        if (currentDepth > maxDepth) return
        val children = dir.listFiles() ?: return

        for (child in children) {
            if (!allowHidden && child.name.startsWith(".")) continue
            if (whitelistedPaths.contains(child.absolutePath)) continue

            if (child.isFile) {
                results.add(child)
            } else if (child.isDirectory) {
                collectFilesRecursively(child, results, whitelistedPaths, maxDepth, currentDepth + 1, allowHidden)
            }
        }
    }

    private fun calculateDirectorySize(dir: File, maxDepth: Int, currentDepth: Int = 0): Long {
        if (currentDepth > maxDepth) return 0L
        var size = 0L
        val children = dir.listFiles() ?: return 0L
        for (child in children) {
            size += if (child.isFile) {
                child.length()
            } else {
                calculateDirectorySize(child, maxDepth, currentDepth + 1)
            }
        }
        return size
    }

    private fun countDirectoryFiles(dir: File, maxDepth: Int, currentDepth: Int = 0): Int {
        if (currentDepth > maxDepth) return 0
        var count = 0
        val children = dir.listFiles() ?: return 0
        for (child in children) {
            count += if (child.isFile) {
                1
            } else {
                countDirectoryFiles(child, maxDepth, currentDepth + 1)
            }
        }
        return count
    }

    override fun findLargeFiles(minSizeBytes: Long): Flow<Resource<List<LargeFileItem>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[LARGE_FILES] Starting scan (minSize: ${FileUtils.formatFileSize(minSizeBytes)})...")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val largeFiles = mutableListOf<LargeFileItem>()
            val externalStorage = Environment.getExternalStorageDirectory()

            val candidateDirs = listOf(
                File(externalStorage, Environment.DIRECTORY_MOVIES),
                File(externalStorage, Environment.DIRECTORY_DOWNLOADS),
                File(externalStorage, Environment.DIRECTORY_DCIM),
                File(externalStorage, Environment.DIRECTORY_DOCUMENTS),
                File(externalStorage, "Telegram"),
                File(externalStorage, "Android/media"),
                File(externalStorage, "Recordings"),
                File(externalStorage, "MIUI/sound_recorder"),
                File(externalStorage, "Pictures")
            )

            fun scanDir(dir: File, depth: Int = 0) {
                if (depth > 6 || !dir.exists() || !dir.isDirectory) return
                val children = dir.listFiles() ?: return
                for (child in children) {
                    if (child.isDirectory) {
                        val name = child.name
                        if (name.equals("data", ignoreCase = true) || name.equals("obb", ignoreCase = true)) continue
                        scanDir(child, depth + 1)
                    } else if (child.isFile) {
                        val len = child.length()
                        if (len >= minSizeBytes && !whitelistedPaths.contains(child.absolutePath)) {
                            largeFiles.add(LargeFileItem(file = child))
                        }
                    }
                }
            }

            for (dir in candidateDirs) {
                if (dir.exists()) {
                    scanDir(dir)
                }
            }

            // Also check root external storage
            val rootChildren = externalStorage.listFiles() ?: emptyArray()
            for (child in rootChildren) {
                if (child.isFile && child.length() >= minSizeBytes && !whitelistedPaths.contains(child.absolutePath)) {
                    largeFiles.add(LargeFileItem(file = child))
                }
            }

            val sortedList = largeFiles.distinctBy { it.path }.sortedByDescending { it.sizeBytes }
            Log.d(Constants.TAG, "[LARGE_FILES] Scan finished: ${sortedList.size} large files found.")
            emit(Resource.Success(sortedList))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[LARGE_FILES] Error scanning large files: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Büyük dosyalar taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun findScreenshots(minAgeDays: Int): Flow<Resource<List<ScreenshotItem>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[SCREENSHOTS] Starting scan (minAgeDays: $minAgeDays)...")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val screenshots = mutableListOf<ScreenshotItem>()
            val externalStorage = Environment.getExternalStorageDirectory()

            val candidateDirs = listOf(
                File(externalStorage, "Pictures/Screenshots"),
                File(externalStorage, "DCIM/Screenshots"),
                File(externalStorage, "Screenshots"),
                File(externalStorage, "Pictures/Ekran görüntüleri"),
                File(externalStorage, "DCIM/Ekran görüntüleri")
            )

            val now = System.currentTimeMillis()
            val thresholdMillis = minAgeDays * 24L * 60L * 60L * 1000L

            for (dir in candidateDirs) {
                if (dir.exists() && dir.isDirectory) {
                    val files = dir.listFiles() ?: continue
                    for (file in files) {
                        if (file.isFile && !whitelistedPaths.contains(file.absolutePath)) {
                            val ext = file.extension.lowercase()
                            if (ext in listOf("png", "jpg", "jpeg", "webp")) {
                                val age = now - file.lastModified()
                                if (age >= thresholdMillis) {
                                    screenshots.add(ScreenshotItem(file = file))
                                }
                            }
                        }
                    }
                }
            }

            val sortedList = screenshots.distinctBy { it.path }.sortedByDescending { it.dateTaken }
            Log.d(Constants.TAG, "[SCREENSHOTS] Scan finished: ${sortedList.size} screenshots found.")
            emit(Resource.Success(sortedList))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[SCREENSHOTS] Error scanning screenshots: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Ekran görüntüleri taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun findDownloadsAndApks(minAgeDays: Int): Flow<Resource<List<DownloadApkItem>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[DOWNLOADS_APKS] Starting scan (minAgeDays: $minAgeDays)...")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val pm = context.packageManager
            val installedPackagesMap = try {
                pm.getInstalledPackages(0).associate { it.packageName to (it.versionName ?: "") }
            } catch (e: Exception) {
                emptyMap()
            }

            val resultList = mutableListOf<DownloadApkItem>()
            val external = Environment.getExternalStorageDirectory()
            val now = System.currentTimeMillis()
            val thresholdMillis = minAgeDays * 24L * 60L * 60L * 1000L
            val oldThresholdMillis = 30L * 24L * 60L * 60L * 1000L

            val downloadCandidateDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(external, "Download"),
                File(external, "Downloads"),
                File(external, "Telegram/Telegram Documents"),
                File(external, "WhatsApp/Media/WhatsApp Documents")
            ).filter { it.exists() && it.isDirectory }

            val scannedFiles = mutableSetOf<String>()

            fun inspectFile(file: File) {
                if (!file.isFile || file.length() <= 0L || whitelistedPaths.contains(file.absolutePath)) return
                if (scannedFiles.contains(file.absolutePath)) return
                scannedFiles.add(file.absolutePath)

                val ext = file.extension.lowercase()
                val age = now - file.lastModified()
                if (thresholdMillis > 0 && age < thresholdMillis && ext != "apk") {
                    return
                }

                val downloadType = DownloadApkItem.resolveDownloadType(ext)
                if (downloadType == DownloadType.OTHER && ext !in listOf("bin", "iso", "dmg", "exe", "torrent", "log")) {
                    return
                }

                var apkPackageName: String? = null
                var apkAppName: String? = null
                var apkVersionName: String? = null
                var apkVersionCode: Long? = null
                var isAlreadyInstalled = false
                var installedVerName: String? = null

                if (downloadType == DownloadType.APK) {
                    try {
                        val archiveInfo = pm.getPackageArchiveInfo(file.absolutePath, 0)
                        if (archiveInfo != null) {
                            apkPackageName = archiveInfo.packageName
                            archiveInfo.applicationInfo?.let { appInfo ->
                                appInfo.sourceDir = file.absolutePath
                                appInfo.publicSourceDir = file.absolutePath
                                apkAppName = appInfo.loadLabel(pm).toString()
                            }
                            apkVersionName = archiveInfo.versionName
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                apkVersionCode = archiveInfo.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                apkVersionCode = archiveInfo.versionCode.toLong()
                            }

                            if (apkPackageName != null && installedPackagesMap.containsKey(apkPackageName)) {
                                isAlreadyInstalled = true
                                installedVerName = installedPackagesMap[apkPackageName]
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(Constants.TAG, "[DOWNLOADS_APKS] Error parsing APK: ${file.name}: ${e.message}")
                    }
                }

                val isOld = age >= oldThresholdMillis

                resultList.add(
                    DownloadApkItem(
                        file = file,
                        name = file.name,
                        path = file.absolutePath,
                        sizeBytes = file.length(),
                        lastModified = file.lastModified(),
                        type = downloadType,
                        apkPackageName = apkPackageName,
                        apkAppName = apkAppName,
                        apkVersionName = apkVersionName,
                        apkVersionCode = apkVersionCode,
                        isAlreadyInstalled = isAlreadyInstalled,
                        installedVersionName = installedVerName,
                        isOld = isOld
                    )
                )
            }

            for (dir in downloadCandidateDirs) {
                dir.walkTopDown()
                    .maxDepth(3)
                    .filter { it.isFile }
                    .forEach { inspectFile(it) }
            }

            // Also check external storage root for stray APKs
            external.listFiles()?.filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }?.forEach {
                inspectFile(it)
            }

            // Sort: Already installed APKs first, then by size descending
            val sortedList = resultList.sortedWith(
                compareByDescending<DownloadApkItem> { it.isAlreadyInstalled }
                    .thenByDescending { it.sizeBytes }
            )

            Log.d(Constants.TAG, "[DOWNLOADS_APKS] Scan finished: ${sortedList.size} items found (${sortedList.count { it.isAlreadyInstalled }} installed APKs).")
            emit(Resource.Success(sortedList))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[DOWNLOADS_APKS] Error scanning downloads: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "İndirilenler taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun getTelegramMediaCategories(): Flow<Resource<List<TelegramMediaCategory>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[TELEGRAM] Starting Telegram media scan...")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val categories = mutableListOf<TelegramMediaCategory>()
            val external = Environment.getExternalStorageDirectory()

            val baseRoots = listOf(
                File(external, "Telegram"),
                File(external, "Android/media/org.telegram.messenger/Telegram"),
                File(external, "Android/media/org.telegram.messenger.web/Telegram"),
                File(external, "Movies/Telegram"),
                File(external, "Pictures/Telegram")
            )

            fun scanSubdir(
                type: TelegramCategoryType,
                title: String,
                description: String,
                folderNames: List<String>
            ) {
                val candidateDirs = mutableListOf<File>()
                for (root in baseRoots) {
                    for (folderName in folderNames) {
                        if (folderName.isNotEmpty()) {
                            candidateDirs.add(File(root, folderName))
                        } else {
                            candidateDirs.add(root)
                        }
                    }
                }

                val allFiles = mutableListOf<File>()
                for (dir in candidateDirs) {
                    if (dir.exists() && dir.isDirectory) {
                        val collected = mutableListOf<File>()
                        collectFilesRecursively(dir, collected, whitelistedPaths, maxDepth = 4, allowHidden = false)
                        allFiles.addAll(collected)
                    }
                }

                val distinctFiles = allFiles.distinctBy { it.absolutePath }.sortedByDescending { it.length() }
                if (distinctFiles.isNotEmpty()) {
                    val totalBytes = distinctFiles.sumOf { it.length() }
                    categories.add(
                        TelegramMediaCategory(
                            type = type,
                            title = title,
                            description = description,
                            totalBytes = totalBytes,
                            fileCount = distinctFiles.size,
                            files = distinctFiles
                        )
                    )
                }
            }

            // 1. Telegram Videos
            scanSubdir(
                TelegramCategoryType.VIDEOS,
                "Telegram Videoları",
                "Kanallardan ve sohbetlerden otomatik indirilen videolar.",
                listOf("Telegram Video", "Video")
            )

            // 2. Telegram Images
            scanSubdir(
                TelegramCategoryType.IMAGES,
                "Telegram Görselleri",
                "Gruplardan ve kanallardan kaydedilen fotoğraflar.",
                listOf("Telegram Images", "Images", "Pictures")
            )

            // 3. Telegram Documents
            scanSubdir(
                TelegramCategoryType.DOCUMENTS,
                "Telegram Belgeleri",
                "İndirilen PDF, zip, apk ve ofis belgeleri.",
                listOf("Telegram Documents", "Documents")
            )

            // 4. Telegram Audio
            scanSubdir(
                TelegramCategoryType.AUDIO,
                "Telegram Sesleri ve Müzikleri",
                "Kanallardan dinlenen ses kayıtları ve müzikler.",
                listOf("Telegram Audio", "Audio")
            )

            // 5. Telegram Stories
            scanSubdir(
                TelegramCategoryType.STORIES,
                "Telegram Hikaye Önbelleği",
                "İzlenen geçici hikaye medya dosyaları.",
                listOf("Telegram Stories", "Stories")
            )

            val sorted = categories.sortedByDescending { it.totalBytes }
            Log.d(Constants.TAG, "[TELEGRAM] Scan complete: ${sorted.size} categories found.")
            emit(Resource.Success(sorted))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[TELEGRAM] Error scanning Telegram: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Telegram medyası taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun findSimilarPhotos(): Flow<Resource<List<SimilarPhotoGroup>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[SIMILAR_PHOTOS] Starting perceptual similar photo scan...")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val external = Environment.getExternalStorageDirectory()
            val cameraDirs = listOf(
                File(external, "DCIM/Camera"),
                File(external, "DCIM"),
                File(external, "Pictures")
            )

            val photoFiles = mutableListOf<File>()
            for (dir in cameraDirs) {
                if (dir.exists() && dir.isDirectory) {
                    val files = dir.listFiles() ?: continue
                    for (file in files) {
                        if (file.isFile && !whitelistedPaths.contains(file.absolutePath)) {
                            val ext = file.extension.lowercase()
                            if (ext in listOf("jpg", "jpeg", "png", "webp")) {
                                photoFiles.add(file)
                            }
                        }
                    }
                }
            }

            // Sort chronologically to only compare photos taken in burst / close proximity
            photoFiles.sortBy { it.lastModified() }

            val hashCache = mutableMapOf<String, Long>()
            val processed = mutableSetOf<String>()
            val groups = mutableListOf<SimilarPhotoGroup>()
            val timeWindowMillis = 20 * 60 * 1000L

            for (i in photoFiles.indices) {
                val fileA = photoFiles[i]
                if (processed.contains(fileA.absolutePath)) continue

                val hashA = hashCache.getOrPut(fileA.absolutePath) {
                    ImageHasher.computeDHash(fileA) ?: 0L
                }
                if (hashA == 0L) continue

                val cluster = mutableListOf<File>()
                cluster.add(fileA)

                for (j in i + 1 until photoFiles.size) {
                    val fileB = photoFiles[j]
                    if (processed.contains(fileB.absolutePath)) continue
                    if (fileB.lastModified() - fileA.lastModified() > timeWindowMillis) {
                        break
                    }

                    val hashB = hashCache.getOrPut(fileB.absolutePath) {
                        ImageHasher.computeDHash(fileB) ?: 0L
                    }
                    if (hashB == 0L) continue

                    val distance = ImageHasher.hammingDistance(hashA, hashB)
                    if (distance <= 8) { // over 87.5% similarity
                        cluster.add(fileB)
                        processed.add(fileB.absolutePath)
                    }
                }

                if (cluster.size > 1) {
                    processed.add(fileA.absolutePath)
                    val sortedCluster = cluster.sortedByDescending { it.length() }
                    val best = sortedCluster.first()
                    val similar = sortedCluster.drop(1)
                    val savings = similar.sumOf { it.length() }

                    groups.add(
                        SimilarPhotoGroup(
                            id = "group_${fileA.nameWithoutExtension}_${cluster.size}",
                            bestPhoto = best,
                            similarPhotos = similar,
                            totalSavingsBytes = savings
                        )
                    )
                }
            }

            val result = groups.sortedByDescending { it.totalSavingsBytes }
            Log.d(Constants.TAG, "[SIMILAR_PHOTOS] Scan complete: ${result.size} similar groups found.")
            emit(Resource.Success(result))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[SIMILAR_PHOTOS] Error scanning similar photos: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Benzer fotoğraflar taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun findBlurryPhotos(threshold: Double): Flow<Resource<List<BlurryPhotoItem>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[BLURRY_PHOTOS] Starting Laplacian variance blurry photo scan (threshold=$threshold)...")
        try {
            val whitelistedPaths = whitelistDao.getAllPaths().toSet()
            val external = Environment.getExternalStorageDirectory()
            val cameraDirs = listOf(
                File(external, "DCIM/Camera"),
                File(external, "DCIM"),
                File(external, "Pictures")
            )

            val blurryList = mutableListOf<BlurryPhotoItem>()

            for (dir in cameraDirs) {
                if (dir.exists() && dir.isDirectory) {
                    val files = dir.listFiles() ?: continue
                    for (file in files) {
                        if (file.isFile && !whitelistedPaths.contains(file.absolutePath)) {
                            val ext = file.extension.lowercase()
                            if (ext in listOf("jpg", "jpeg", "png", "webp")) {
                                val variance = BlurDetector.calculateLaplacianVariance(file)
                                if (variance != null && variance < threshold) {
                                    val blurPercent = BlurDetector.calculateBlurPercentage(variance, threshold)
                                    blurryList.add(
                                        BlurryPhotoItem(
                                            file = file,
                                            varianceScore = variance,
                                            blurPercentage = blurPercent,
                                            sizeBytes = file.length()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val sorted = blurryList.sortedByDescending { it.blurPercentage }
            Log.d(Constants.TAG, "[BLURRY_PHOTOS] Scan complete: ${sorted.size} blurry photos found.")
            emit(Resource.Success(sorted))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[BLURRY_PHOTOS] Error scanning blurry photos: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Bulanık fotoğraflar taranırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)
}
