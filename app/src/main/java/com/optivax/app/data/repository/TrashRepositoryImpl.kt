package com.optivax.app.data.repository

import android.os.Environment
import android.util.Log
import com.optivax.app.core.Constants
import com.optivax.app.core.FileUtils
import com.optivax.app.core.Resource
import com.optivax.app.data.local.TrashDao
import com.optivax.app.data.local.TrashEntity
import com.optivax.app.di.IoDispatcher
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.TrashItem
import com.optivax.app.domain.repository.TrashRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRepositoryImpl @Inject constructor(
    private val trashDao: TrashDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TrashRepository {

    private val trashDir: File by lazy {
        val root = Environment.getExternalStorageDirectory()
        val dir = File(root, ".optivax_trash")
        if (!dir.exists()) {
            dir.mkdirs()
            // Create .nomedia so system media scanners ignore recycled files
            File(dir, ".nomedia").createNewFile()
        }
        dir
    }

    override fun getTrashItems(): Flow<List<TrashItem>> {
        return trashDao.getAllTrashItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTotalTrashBytes(): Flow<Long> {
        return trashDao.getTotalTrashBytes().map { it ?: 0L }
    }

    override suspend fun moveToTrash(
        filePaths: List<String>,
        onProgress: ((CleanProgress) -> Unit)?
    ): Resource<Int> = withContext(ioDispatcher) {
        try {
            if (!trashDir.exists()) {
                trashDir.mkdirs()
                File(trashDir, ".nomedia").createNewFile()
            }

            var movedCount = 0
            var freedBytes = 0L
            val entitiesToInsert = mutableListOf<TrashEntity>()

            filePaths.forEachIndexed { index, path ->
                val srcFile = File(path)
                if (srcFile.exists() && srcFile.isFile) {
                    val size = srcFile.length()
                    val uniqueTrashName = "${System.currentTimeMillis()}_${srcFile.name}"
                    val destFile = File(trashDir, uniqueTrashName)

                    val success = moveFilePhysically(srcFile, destFile)
                    if (success) {
                        movedCount++
                        freedBytes += size
                        entitiesToInsert.add(
                            TrashEntity(
                                originalPath = srcFile.absolutePath,
                                trashPath = destFile.absolutePath,
                                fileName = srcFile.name,
                                sizeBytes = size
                            )
                        )
                        Log.d(Constants.TAG, "[TRASH] Moved to safe trash: ${srcFile.name} -> ${destFile.name}")
                    }
                }

                onProgress?.invoke(
                    CleanProgress(
                        current = index + 1,
                        total = filePaths.size,
                        currentFileName = srcFile.name,
                        freedBytes = freedBytes
                    )
                )
            }

            if (entitiesToInsert.isNotEmpty()) {
                trashDao.insertAll(entitiesToInsert)
            }

            Resource.Success(movedCount)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[TRASH] Error moving files to trash: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Dosyalar çöp kutusuna taşınırken hata oluştu", e)
        }
    }

    override suspend fun restoreItem(item: TrashItem): Resource<Boolean> = withContext(ioDispatcher) {
        try {
            val srcFile = File(item.trashPath)
            val destFile = File(item.originalPath)

            if (!srcFile.exists()) {
                trashDao.deleteById(item.id)
                return@withContext Resource.Error("Çöp kutusundaki dosya bulunamadı.")
            }

            destFile.parentFile?.mkdirs()
            val success = moveFilePhysically(srcFile, destFile)

            if (success) {
                trashDao.deleteById(item.id)
                Log.d(Constants.TAG, "[TRASH] Restored file: ${item.fileName} -> ${destFile.absolutePath}")
                Resource.Success(true)
            } else {
                Resource.Error("Dosya geri yüklenemedi.")
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[TRASH] Error restoring item: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Dosya geri yüklenirken hata oluştu", e)
        }
    }

    override suspend fun restoreAll(): Resource<Int> = withContext(ioDispatcher) {
        try {
            val items = trashDao.getAllTrashItemsSync()
            var restoredCount = 0

            for (entity in items) {
                val srcFile = File(entity.trashPath)
                val destFile = File(entity.originalPath)

                if (srcFile.exists()) {
                    destFile.parentFile?.mkdirs()
                    if (moveFilePhysically(srcFile, destFile)) {
                        restoredCount++
                        trashDao.deleteById(entity.id)
                    }
                } else {
                    trashDao.deleteById(entity.id)
                }
            }

            Resource.Success(restoredCount)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[TRASH] Error restoring all: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Tüm dosyalar geri yüklenirken hata oluştu", e)
        }
    }

    override suspend fun purgeItem(item: TrashItem): Resource<Boolean> = withContext(ioDispatcher) {
        try {
            val file = File(item.trashPath)
            if (file.exists()) {
                file.delete()
            }
            trashDao.deleteById(item.id)
            Log.d(Constants.TAG, "[TRASH] Purged permanently: ${item.fileName}")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[TRASH] Error purging item: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Dosya kalıcı olarak silinirken hata oluştu", e)
        }
    }

    override suspend fun emptyTrash(): Resource<Int> = withContext(ioDispatcher) {
        try {
            val items = trashDao.getAllTrashItemsSync()
            var purgedCount = 0

            for (entity in items) {
                val file = File(entity.trashPath)
                if (file.exists()) {
                    file.delete()
                }
                purgedCount++
            }

            trashDao.clearAll()
            Log.d(Constants.TAG, "[TRASH] Empty trash complete: $purgedCount items purged.")
            Resource.Success(purgedCount)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[TRASH] Error emptying trash: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Çöp kutusu boşaltılırken hata oluştu", e)
        }
    }

    override suspend fun autoPurgeExpired(retentionDays: Int): Int = withContext(ioDispatcher) {
        try {
            val threshold = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L)
            val items = trashDao.getAllTrashItemsSync()
            var expiredCount = 0

            for (entity in items) {
                if (entity.deletedTimestamp < threshold) {
                    val file = File(entity.trashPath)
                    if (file.exists()) {
                        file.delete()
                    }
                    trashDao.deleteById(entity.id)
                    expiredCount++
                }
            }
            expiredCount
        } catch (e: Exception) {
            0
        }
    }

    private fun moveFilePhysically(src: File, dest: File): Boolean {
        if (src.renameTo(dest)) {
            return true
        }
        // Fallback to copy and delete if renameTo fails (cross-filesystem)
        return try {
            FileInputStream(src).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            src.delete()
            true
        } catch (e: Exception) {
            false
        }
    }
}
