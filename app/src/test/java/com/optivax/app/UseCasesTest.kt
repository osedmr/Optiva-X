package com.optivax.app

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.StorageBreakdown
import com.optivax.app.domain.model.WhatsAppMediaCategory
import com.optivax.app.domain.repository.AppManagerRepository
import com.optivax.app.domain.repository.StorageRepository
import com.optivax.app.domain.usecase.FindCorpseFilesUseCase
import com.optivax.app.domain.usecase.FindDuplicatesUseCase
import com.optivax.app.domain.usecase.GetIdleAppsUseCase
import com.optivax.app.domain.usecase.GetStorageStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UseCasesTest {

    private val fakeStorageRepository = object : StorageRepository {
        override fun getStorageBreakdown(): Flow<Resource<StorageBreakdown>> {
            return flowOf(
                Resource.Success(
                    StorageBreakdown(
                        totalBytes = 64L * 1024L * 1024L * 1024L,
                        freeBytes = 32L * 1024L * 1024L * 1024L
                    )
                )
            )
        }

        override fun findDuplicates(targetDirectories: List<File>): Flow<Resource<List<DuplicateItem>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun getWhatsAppMediaCategories(): Flow<Resource<List<WhatsAppMediaCategory>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun getTelegramMediaCategories(): Flow<Resource<List<com.optivax.app.domain.model.TelegramMediaCategory>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun findCorpseFiles(): Flow<Resource<List<CorpseItem>>> {
            return flowOf(
                Resource.Success(
                    listOf(
                        CorpseItem(
                            packageName = "com.legacy.game",
                            path = "/storage/emulated/0/Android/data/com.legacy.game",
                            displayName = "com.legacy.game (Artık Veriler)",
                            sizeBytes = 100L * 1024L * 1024L,
                            fileCount = 10
                        )
                    )
                )
            )
        }

        override fun deleteFiles(
            filePaths: List<String>,
            onProgress: ((CleanProgress) -> Unit)?
        ): Flow<Resource<Int>> {
            return flowOf(Resource.Success(filePaths.size))
        }

        override fun findLargeFiles(minSizeBytes: Long): Flow<Resource<List<com.optivax.app.domain.model.LargeFileItem>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun findScreenshots(minAgeDays: Int): Flow<Resource<List<com.optivax.app.domain.model.ScreenshotItem>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun findSimilarPhotos(): Flow<Resource<List<com.optivax.app.domain.model.SimilarPhotoGroup>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun findBlurryPhotos(threshold: Double): Flow<Resource<List<com.optivax.app.domain.model.BlurryPhotoItem>>> {
            return flowOf(Resource.Success(emptyList()))
        }

        override fun deleteCorpseItem(corpseItem: CorpseItem): Flow<Resource<Boolean>> {
            return flowOf(Resource.Success(true))
        }
    }

    private val fakeAppManagerRepository = object : AppManagerRepository {
        override fun getInstalledAppsUsage(idleThresholdDays: Long): Flow<Resource<List<AppUsageInfo>>> {
            return flowOf(
                Resource.Success(
                    listOf(
                        AppUsageInfo(
                            packageName = "com.idle.app",
                            appName = "Idle App",
                            appSizeBytes = 20L * 1024L * 1024L,
                            dataSizeBytes = 10L * 1024L * 1024L,
                            cacheSizeBytes = 5L * 1024L * 1024L,
                            lastTimeUsed = 0L,
                            isIdle = true,
                            isSystemApp = false
                        )
                    )
                )
            )
        }

        override fun getIdleApps(idleThresholdDays: Long): Flow<Resource<List<AppUsageInfo>>> {
            return getInstalledAppsUsage(idleThresholdDays)
        }
    }

    @Test
    fun getStorageStatsUseCase_emitsAccurateData() = runBlocking {
        val useCase = GetStorageStatsUseCase(fakeStorageRepository)
        val result = useCase().first()

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(64L * 1024L * 1024L * 1024L, data.totalBytes)
        assertEquals(32L * 1024L * 1024L * 1024L, data.freeBytes)
        assertEquals(50.0f, data.usedPercentage, 0.01f)
    }

    @Test
    fun getIdleAppsUseCase_filtersIdleApplications() = runBlocking {
        val useCase = GetIdleAppsUseCase(fakeAppManagerRepository)
        val result = useCase(30L).first()

        assertTrue(result is Resource.Success)
        val apps = (result as Resource.Success).data
        assertEquals(1, apps.size)
        assertEquals("com.idle.app", apps.first().packageName)
        assertTrue(apps.first().isIdle)
    }

    @Test
    fun findCorpseFilesUseCase_emitsDetectedRemnants() = runBlocking {
        val useCase = FindCorpseFilesUseCase(fakeStorageRepository)
        val result = useCase().first()

        assertTrue(result is Resource.Success)
        val remnants = (result as Resource.Success).data
        assertEquals(1, remnants.size)
        assertEquals("com.legacy.game", remnants.first().packageName)
    }

    @Test
    fun findDuplicatesUseCase_emitsDuplicateList() = runBlocking {
        val useCase = FindDuplicatesUseCase(fakeStorageRepository)
        val result = useCase(listOf(File("/test"))).first()

        assertTrue(result is Resource.Success)
        val duplicates = (result as Resource.Success).data
        assertEquals(0, duplicates.size)
    }

    @Test
    fun findLargeFilesUseCase_emitsLargeFiles() = runBlocking {
        val useCase = com.optivax.app.domain.usecase.FindLargeFilesUseCase(fakeStorageRepository)
        val result = useCase(50L * 1024L * 1024L).first()

        assertTrue(result is Resource.Success)
        val files = (result as Resource.Success).data
        assertEquals(0, files.size)
    }

    @Test
    fun findScreenshotsUseCase_emitsScreenshots() = runBlocking {
        val useCase = com.optivax.app.domain.usecase.FindScreenshotsUseCase(fakeStorageRepository)
        val result = useCase(14).first()

        assertTrue(result is Resource.Success)
        val screenshots = (result as Resource.Success).data
        assertEquals(0, screenshots.size)
    }

    @Test
    fun getTelegramMediaUseCase_emitsCategories() = runBlocking {
        val useCase = com.optivax.app.domain.usecase.GetTelegramMediaUseCase(fakeStorageRepository)
        val result = useCase().first()

        assertTrue(result is Resource.Success)
        val categories = (result as Resource.Success).data
        assertEquals(0, categories.size)
    }

    @Test
    fun findSimilarPhotosUseCase_emitsSimilarGroups() = runBlocking {
        val useCase = com.optivax.app.domain.usecase.FindSimilarPhotosUseCase(fakeStorageRepository)
        val result = useCase().first()

        assertTrue(result is Resource.Success)
        val groups = (result as Resource.Success).data
        assertEquals(0, groups.size)
    }

    @Test
    fun findBlurryPhotosUseCase_emitsBlurryPhotos() = runBlocking {
        val useCase = com.optivax.app.domain.usecase.FindBlurryPhotosUseCase(fakeStorageRepository)
        val result = useCase(80.0).first()

        assertTrue(result is Resource.Success)
        val blurry = (result as Resource.Success).data
        assertEquals(0, blurry.size)
    }

    @Test
    fun imageHasher_hammingDistanceCalculatesCorrectly() {
        val hashA = 0b10101010L
        val hashB = 0b10101011L // 1 bit difference
        val distance = com.optivax.app.core.ImageHasher.hammingDistance(hashA, hashB)
        assertEquals(1, distance)
    }
}
