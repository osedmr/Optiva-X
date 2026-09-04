package com.optivax.app

import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DuplicateFileDetail
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.StorageBreakdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelsTest {

    @Test
    fun storageBreakdown_calculatesCorrectUsedAndPercentages() {
        val total = 100L * 1024L * 1024L * 1024L // 100 GB
        val free = 25L * 1024L * 1024L * 1024L   // 25 GB
        val breakdown = StorageBreakdown(
            totalBytes = total,
            freeBytes = free,
            appsBytes = 40L * 1024L * 1024L * 1024L,
            mediaBytes = 20L * 1024L * 1024L * 1024L,
            systemBytes = 15L * 1024L * 1024L * 1024L
        )

        assertEquals(75L * 1024L * 1024L * 1024L, breakdown.usedBytes)
        assertEquals(75.0f, breakdown.usedPercentage, 0.01f)
        assertEquals(25.0f, breakdown.freePercentage, 0.01f)
        assertEquals("100.00 GB", breakdown.formattedTotal)
        assertEquals("25.00 GB", breakdown.formattedFree)
        assertEquals("75.00 GB", breakdown.formattedUsed)
    }

    @Test
    fun duplicateItem_calculatesWastedSpaceAccurately() {
        val fileSize = 5L * 1024L * 1024L // 5 MB each
        val file1 = DuplicateFileDetail(
            filePath = "/storage/emulated/0/DCIM/photo1.jpg",
            fileName = "photo1.jpg",
            lastModified = 1000L,
            mimeCategory = "IMAGE",
            isOriginal = true
        )
        val file2 = DuplicateFileDetail(
            filePath = "/storage/emulated/0/Download/photo_copy.jpg",
            fileName = "photo_copy.jpg",
            lastModified = 2000L,
            mimeCategory = "IMAGE",
            isSuggestedDeletion = true
        )
        val file3 = DuplicateFileDetail(
            filePath = "/storage/emulated/0/Backup/photo_backup.jpg",
            fileName = "photo_backup.jpg",
            lastModified = 3000L,
            mimeCategory = "IMAGE",
            isSuggestedDeletion = true
        )

        val duplicateGroup = DuplicateItem(
            sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sizeBytes = fileSize,
            duplicates = listOf(file1, file2, file3)
        )

        // 3 files of 5MB -> 1 original + 2 duplicates = 10MB wasted space
        assertEquals(10L * 1024L * 1024L, duplicateGroup.wastedBytes)
        assertEquals("5.00 MB", duplicateGroup.formattedSize)
        assertEquals("10.00 MB", duplicateGroup.formattedWastedSize)
    }

    @Test
    fun appUsageInfo_computesTotalSizeProperly() {
        val app = AppUsageInfo(
            packageName = "com.test.app",
            appName = "Test App",
            appSizeBytes = 50L * 1024L * 1024L,   // 50 MB
            dataSizeBytes = 30L * 1024L * 1024L,  // 30 MB
            cacheSizeBytes = 20L * 1024L * 1024L, // 20 MB
            lastTimeUsed = System.currentTimeMillis() - (40L * 24 * 60 * 60 * 1000L),
            isIdle = true,
            isSystemApp = false
        )

        assertEquals(100L * 1024L * 1024L, app.totalSizeBytes)
        assertEquals("100.00 MB", app.formattedTotalSize)
        assertEquals("20.00 MB", app.formattedCacheSize)
        assertTrue(app.isIdle)
    }

    @Test
    fun corpseItem_formatsSizeProperly() {
        val corpse = CorpseItem(
            packageName = "com.uninstalled.game",
            path = "/storage/emulated/0/Android/data/com.uninstalled.game",
            displayName = "com.uninstalled.game (Residual)",
            sizeBytes = 250L * 1024L * 1024L,
            fileCount = 42
        )

        assertEquals("250.00 MB", corpse.formattedSize)
        assertEquals(42, corpse.fileCount)
    }
}
