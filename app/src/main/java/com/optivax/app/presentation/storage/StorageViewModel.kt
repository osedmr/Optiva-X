package com.optivax.app.presentation.storage

import android.content.IntentSender
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optivax.app.core.Constants
import com.optivax.app.core.FileUtils
import com.optivax.app.core.MediaStoreHelper
import com.optivax.app.core.Resource
import com.optivax.app.core.notification.OptivaBannerController
import com.optivax.app.domain.model.BlurryPhotoItem
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.model.LargeFileItem
import com.optivax.app.domain.model.ScreenshotItem
import com.optivax.app.domain.model.SimilarPhotoGroup
import com.optivax.app.domain.model.TelegramCategoryType
import com.optivax.app.domain.model.TelegramMediaCategory
import com.optivax.app.domain.model.DownloadApkItem
import com.optivax.app.domain.model.TrashItem
import com.optivax.app.domain.model.WhatsAppCategoryType
import com.optivax.app.domain.model.WhatsAppMediaCategory
import com.optivax.app.domain.repository.StorageRepository
import com.optivax.app.domain.usecase.AutoPurgeExpiredTrashUseCase
import com.optivax.app.domain.usecase.EmptyTrashUseCase
import com.optivax.app.domain.usecase.FindBlurryPhotosUseCase
import com.optivax.app.domain.usecase.FindCorpseFilesUseCase
import com.optivax.app.domain.usecase.FindDownloadsAndApksUseCase
import com.optivax.app.domain.usecase.FindDuplicatesUseCase
import com.optivax.app.domain.usecase.FindLargeFilesUseCase
import com.optivax.app.domain.usecase.FindScreenshotsUseCase
import com.optivax.app.domain.usecase.FindSimilarPhotosUseCase
import com.optivax.app.domain.usecase.GetTelegramMediaUseCase
import com.optivax.app.domain.usecase.GetTotalTrashBytesUseCase
import com.optivax.app.domain.usecase.GetTrashItemsUseCase
import com.optivax.app.domain.usecase.GetWhatsAppMediaUseCase
import com.optivax.app.domain.usecase.MoveToTrashUseCase
import com.optivax.app.domain.usecase.PurgeTrashItemUseCase
import com.optivax.app.domain.usecase.RestoreAllTrashUseCase
import com.optivax.app.domain.usecase.RestoreTrashItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class DownloadFilterType {
    ALL,
    ALREADY_INSTALLED_APKS,
    OLD_DOWNLOADS,
    ARCHIVES,
    DOCUMENTS
}

enum class StorageCleanHub {
    SOCIAL,   // WhatsApp & Telegram
    GALLERY,  // Benzer, Bulanık, Ekran Görüntüleri
    SYSTEM    // Çift Dosyalar, İndirilenler/APK, Büyük Dosyalar, Kalıntı Veriler
}

enum class StorageCleanTab {
    DUPLICATES,
    WHATSAPP,
    TELEGRAM,
    SIMILAR_PHOTOS,
    BLURRY_PHOTOS,
    LARGE_FILES,
    SCREENSHOTS,
    CORPSE_FILES,
    DOWNLOADS_APKS
}

data class StorageCleanUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val cleaningProgress: CleanProgress? = null,
    val inspectingCategory: WhatsAppMediaCategory? = null,
    val inspectingTelegramCategory: TelegramMediaCategory? = null,
    val activeHub: StorageCleanHub = StorageCleanHub.SOCIAL,
    val activeTab: StorageCleanTab = StorageCleanTab.WHATSAPP,
    val duplicates: List<DuplicateItem> = emptyList(),
    val corpseFiles: List<CorpseItem> = emptyList(),
    val whatsAppCategories: List<WhatsAppMediaCategory> = emptyList(),
    val telegramCategories: List<TelegramMediaCategory> = emptyList(),
    val selectedTelegramCategories: Set<TelegramCategoryType> = emptySet(),
    val selectedTelegramFilePaths: Map<TelegramCategoryType, Set<String>> = emptyMap(),
    val similarPhotoGroups: List<SimilarPhotoGroup> = emptyList(),
    val selectedSimilarPhotoPaths: Set<String> = emptySet(),
    val blurryPhotos: List<BlurryPhotoItem> = emptyList(),
    val selectedBlurryPhotoPaths: Set<String> = emptySet(),
    val blurryThreshold: Double = 80.0,
    val largeFiles: List<LargeFileItem> = emptyList(),
    val selectedLargeFilePaths: Set<String> = emptySet(),
    val largeFileSizeThreshold: Long = 50L * 1024L * 1024L,
    val screenshots: List<ScreenshotItem> = emptyList(),
    val selectedScreenshotPaths: Set<String> = emptySet(),
    val screenshotAgeThresholdDays: Int = 14,
    val selectedDuplicatePaths: Set<String> = emptySet(),
    val selectedCorpseItems: Set<CorpseItem> = emptySet(),
    val selectedWhatsAppCategories: Set<WhatsAppCategoryType> = emptySet(),
    val selectedWhatsAppFilePaths: Map<WhatsAppCategoryType, Set<String>> = emptyMap(),
    val downloadsAndApks: List<DownloadApkItem> = emptyList(),
    val selectedDownloadPaths: Set<String> = emptySet(),
    val downloadFilterType: DownloadFilterType = DownloadFilterType.ALL,
    val downloadAgeThresholdDays: Int = 0,
    val trashItems: List<TrashItem> = emptyList(),
    val totalTrashBytes: Long = 0L,
    val isTrashDialogVisible: Boolean = false,
    val useTrashBin: Boolean = true,
    val cleanedBytesResult: Long? = null,
    val errorMessage: String? = null,
    val pendingDeleteIntentSender: IntentSender? = null
) {
    fun getSelectedFilesForCategory(category: WhatsAppMediaCategory): Set<String> {
        return selectedWhatsAppFilePaths[category.type] ?: (
            if (selectedWhatsAppCategories.contains(category.type)) {
                category.files.map { it.absolutePath }.toSet()
            } else {
                emptySet()
            }
        )
    }

    fun getSelectedFilesForTelegramCategory(category: TelegramMediaCategory): Set<String> {
        return selectedTelegramFilePaths[category.type] ?: (
            if (selectedTelegramCategories.contains(category.type)) {
                category.files.map { it.absolutePath }.toSet()
            } else {
                emptySet()
            }
        )
    }

    val selectedBytesToClean: Long
        get() {
            var sum = 0L
            // Duplicates
            for (item in duplicates) {
                for (detail in item.duplicates) {
                    if (selectedDuplicatePaths.contains(detail.filePath)) {
                        sum += item.sizeBytes
                    }
                }
            }
            // Corpse
            for (corpse in selectedCorpseItems) {
                sum += corpse.sizeBytes
            }
            // WhatsApp
            for (category in whatsAppCategories) {
                val selectedPaths = getSelectedFilesForCategory(category)
                sum += category.files.filter { selectedPaths.contains(it.absolutePath) }.sumOf { it.length() }
            }
            // Telegram
            for (category in telegramCategories) {
                val selectedPaths = getSelectedFilesForTelegramCategory(category)
                sum += category.files.filter { selectedPaths.contains(it.absolutePath) }.sumOf { it.length() }
            }
            // Large Files
            for (file in largeFiles) {
                if (selectedLargeFilePaths.contains(file.path)) {
                    sum += file.sizeBytes
                }
            }
            // Screenshots
            for (screenshot in screenshots) {
                if (selectedScreenshotPaths.contains(screenshot.path)) {
                    sum += screenshot.sizeBytes
                }
            }
            // Similar Photos
            for (group in similarPhotoGroups) {
                for (photo in group.similarPhotos) {
                    if (selectedSimilarPhotoPaths.contains(photo.absolutePath)) {
                        sum += photo.length()
                    }
                }
            }
            // Blurry Photos
            for (blurry in blurryPhotos) {
                if (selectedBlurryPhotoPaths.contains(blurry.path)) {
                    sum += blurry.sizeBytes
                }
            }
            // Downloads & APKs
            for (download in downloadsAndApks) {
                if (selectedDownloadPaths.contains(download.path)) {
                    sum += download.sizeBytes
                }
            }
            return sum
        }

    val totalWhatsAppBytes: Long
        get() = whatsAppCategories.sumOf { it.totalBytes }

    val totalTelegramBytes: Long
        get() = telegramCategories.sumOf { it.totalBytes }

    val totalSimilarPhotosSavingsBytes: Long
        get() = similarPhotoGroups.sumOf { it.totalSavingsBytes }

    val totalBlurryBytes: Long
        get() = blurryPhotos.sumOf { it.sizeBytes }

    val totalDownloadsBytes: Long
        get() = downloadsAndApks.sumOf { it.sizeBytes }

    val alreadyInstalledApksCount: Int
        get() = downloadsAndApks.count { it.isAlreadyInstalled }

    val socialTotalBytes: Long
        get() = totalWhatsAppBytes + totalTelegramBytes

    val socialItemCount: Int
        get() = whatsAppCategories.sumOf { it.fileCount } + telegramCategories.sumOf { it.fileCount }

    val galleryTotalBytes: Long
        get() = totalSimilarPhotosSavingsBytes + totalBlurryBytes + screenshots.sumOf { it.sizeBytes }

    val galleryItemCount: Int
        get() = similarPhotoGroups.sumOf { it.similarPhotos.size } + blurryPhotos.size + screenshots.size

    val systemTotalBytes: Long
        get() = duplicates.sumOf { it.wastedBytes } + totalDownloadsBytes + largeFiles.sumOf { it.sizeBytes } + corpseFiles.sumOf { it.sizeBytes }

    val systemItemCount: Int
        get() = duplicates.sumOf { it.duplicates.size } + downloadsAndApks.size + largeFiles.size + corpseFiles.size
}

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val findDuplicatesUseCase: FindDuplicatesUseCase,
    private val findCorpseFilesUseCase: FindCorpseFilesUseCase,
    private val getWhatsAppMediaUseCase: GetWhatsAppMediaUseCase,
    private val getTelegramMediaUseCase: GetTelegramMediaUseCase,
    private val findSimilarPhotosUseCase: FindSimilarPhotosUseCase,
    private val findBlurryPhotosUseCase: FindBlurryPhotosUseCase,
    private val findLargeFilesUseCase: FindLargeFilesUseCase,
    private val findScreenshotsUseCase: FindScreenshotsUseCase,
    private val findDownloadsAndApksUseCase: FindDownloadsAndApksUseCase,
    private val getTrashItemsUseCase: GetTrashItemsUseCase,
    private val getTotalTrashBytesUseCase: GetTotalTrashBytesUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val restoreTrashItemUseCase: RestoreTrashItemUseCase,
    private val restoreAllTrashUseCase: RestoreAllTrashUseCase,
    private val purgeTrashItemUseCase: PurgeTrashItemUseCase,
    private val emptyTrashUseCase: EmptyTrashUseCase,
    private val autoPurgeExpiredTrashUseCase: AutoPurgeExpiredTrashUseCase,
    private val storageRepository: StorageRepository,
    private val bannerController: OptivaBannerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageCleanUiState())
    val uiState: StateFlow<StorageCleanUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        observeTrash()
    }

    private fun observeTrash() {
        viewModelScope.launch {
            getTrashItemsUseCase().collectLatest { items ->
                _uiState.update { it.copy(trashItems = items) }
            }
        }
        viewModelScope.launch {
            getTotalTrashBytesUseCase().collectLatest { total ->
                _uiState.update { it.copy(totalTrashBytes = total) }
            }
        }
        viewModelScope.launch {
            autoPurgeExpiredTrashUseCase(30)
        }
    }

    fun switchHub(hub: StorageCleanHub) {
        val defaultTab = when (hub) {
            StorageCleanHub.SOCIAL -> StorageCleanTab.WHATSAPP
            StorageCleanHub.GALLERY -> StorageCleanTab.SIMILAR_PHOTOS
            StorageCleanHub.SYSTEM -> StorageCleanTab.DUPLICATES
        }
        _uiState.update { it.copy(activeHub = hub, activeTab = defaultTab) }
    }

    fun switchTab(tab: StorageCleanTab) {
        val hub = when (tab) {
            StorageCleanTab.WHATSAPP, StorageCleanTab.TELEGRAM -> StorageCleanHub.SOCIAL
            StorageCleanTab.SIMILAR_PHOTOS, StorageCleanTab.BLURRY_PHOTOS, StorageCleanTab.SCREENSHOTS -> StorageCleanHub.GALLERY
            StorageCleanTab.DUPLICATES, StorageCleanTab.DOWNLOADS_APKS, StorageCleanTab.LARGE_FILES, StorageCleanTab.CORPSE_FILES -> StorageCleanHub.SYSTEM
        }
        _uiState.update { it.copy(activeTab = tab, activeHub = hub) }
    }

    fun dismissCleanResult() {
        _uiState.update { it.copy(cleanedBytesResult = null) }
    }

    fun openCategoryInspection(category: WhatsAppMediaCategory) {
        _uiState.update { it.copy(inspectingCategory = category) }
    }

    fun closeCategoryInspection() {
        _uiState.update { it.copy(inspectingCategory = null) }
    }

    fun openTelegramCategoryInspection(category: TelegramMediaCategory) {
        _uiState.update { it.copy(inspectingTelegramCategory = category) }
    }

    fun closeTelegramCategoryInspection() {
        _uiState.update { it.copy(inspectingTelegramCategory = null) }
    }

    fun openTrashDialog() {
        _uiState.update { it.copy(isTrashDialogVisible = true) }
    }

    fun closeTrashDialog() {
        _uiState.update { it.copy(isTrashDialogVisible = false) }
    }

    fun toggleUseTrashBin(useTrash: Boolean) {
        _uiState.update { it.copy(useTrashBin = useTrash) }
    }

    fun startScan(resetResult: Boolean = true, tab: StorageCleanTab = _uiState.value.activeTab) {
        if (_uiState.value.isCleaning) return
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    activeTab = tab,
                    cleanedBytesResult = if (resetResult) null else it.cleanedBytesResult,
                    errorMessage = null
                )
            }

            val external = Environment.getExternalStorageDirectory()
            val targetDirs = mutableListOf<File>()

            if (tab == StorageCleanTab.DUPLICATES && external != null && external.exists() && external.isDirectory) {
                listOf(
                    Environment.DIRECTORY_DOWNLOADS,
                    Environment.DIRECTORY_DCIM,
                    Environment.DIRECTORY_PICTURES,
                    Environment.DIRECTORY_DOCUMENTS,
                    Environment.DIRECTORY_MOVIES,
                    Environment.DIRECTORY_MUSIC
                ).forEach { dirName ->
                    val dir = Environment.getExternalStoragePublicDirectory(dirName)
                    if (dir != null && dir.exists() && dir.isDirectory) {
                        targetDirs.add(dir)
                    }
                }

                external.listFiles()?.forEach { child ->
                    if (child.isDirectory && child.name != "Android" && !child.name.startsWith(".")) {
                        if (!targetDirs.any { it.absolutePath == child.absolutePath }) {
                            targetDirs.add(child)
                        }
                    }
                }

                val androidMedia = File(external, "Android/media")
                if (androidMedia.exists() && androidMedia.isDirectory) {
                    targetDirs.add(androidMedia)
                }
            }

            Log.d(Constants.TAG, "[SCAN_INIT] Starting scan with ${targetDirs.size} target directories.")

            coroutineScope {
                // 1. Scan Duplicates
                if (tab == StorageCleanTab.DUPLICATES) launch {
                    findDuplicatesUseCase(targetDirs).collectLatest { res ->
                        when (res) {
                            is Resource.Success -> {
                                val items = res.data
                                _uiState.update {
                                    it.copy(
                                        duplicates = items,
                                        selectedDuplicatePaths = emptySet()
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update { it.copy(errorMessage = res.message) }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }

                // 2. Scan WhatsApp Media Hygiene
                if (tab == StorageCleanTab.WHATSAPP) launch {
                    getWhatsAppMediaUseCase().collectLatest { res ->
                        when (res) {
                            is Resource.Success -> {
                                val categories = res.data
                                _uiState.update {
                                    it.copy(
                                        whatsAppCategories = categories,
                                        selectedWhatsAppCategories = emptySet(),
                                        selectedWhatsAppFilePaths = emptyMap()
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update { it.copy(errorMessage = res.message) }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }

                // 2.5. Scan Telegram Media Hygiene
                if (tab == StorageCleanTab.TELEGRAM) launch {
                    getTelegramMediaUseCase().collectLatest { res ->
                        when (res) {
                            is Resource.Success -> {
                                val categories = res.data
                                _uiState.update {
                                    it.copy(
                                        telegramCategories = categories,
                                        selectedTelegramCategories = emptySet(),
                                        selectedTelegramFilePaths = emptyMap()
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update { it.copy(errorMessage = res.message) }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }

                // 3. Scan Corpse Files
                if (tab == StorageCleanTab.CORPSE_FILES) launch {
                    findCorpseFilesUseCase().collectLatest { res ->
                        when (res) {
                            is Resource.Success -> {
                                val corpses = res.data
                                _uiState.update {
                                    it.copy(
                                        corpseFiles = corpses,
                                        selectedCorpseItems = emptySet()
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        errorMessage = res.message
                                    )
                                }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }

                // 4. Scan Large Files
                if (tab == StorageCleanTab.LARGE_FILES) launch {
                    findLargeFilesUseCase(_uiState.value.largeFileSizeThreshold).collectLatest { res ->
                        if (res is Resource.Error) _uiState.update { it.copy(errorMessage = res.message) }
                        if (res is Resource.Success) {
                            _uiState.update { it.copy(largeFiles = res.data) }
                        }
                    }
                }

                // 5. Scan Screenshots
                if (tab == StorageCleanTab.SCREENSHOTS) launch {
                    findScreenshotsUseCase(_uiState.value.screenshotAgeThresholdDays).collectLatest { res ->
                        if (res is Resource.Error) _uiState.update { it.copy(errorMessage = res.message) }
                        if (res is Resource.Success) {
                            _uiState.update { it.copy(screenshots = res.data) }
                        }
                    }
                }

                // 6. Scan Similar Photos (dHash)
                if (tab == StorageCleanTab.SIMILAR_PHOTOS) launch {
                    findSimilarPhotosUseCase().collectLatest { res ->
                        if (res is Resource.Error) _uiState.update { it.copy(errorMessage = res.message) }
                        if (res is Resource.Success) {
                            val groups = res.data
                            _uiState.update {
                                it.copy(
                                    similarPhotoGroups = groups,
                                    selectedSimilarPhotoPaths = emptySet()
                                )
                            }
                        }
                    }
                }

                // 7. Scan Blurry Photos (Laplacian Variance)
                if (tab == StorageCleanTab.BLURRY_PHOTOS) launch {
                    findBlurryPhotosUseCase(_uiState.value.blurryThreshold).collectLatest { res ->
                        if (res is Resource.Error) _uiState.update { it.copy(errorMessage = res.message) }
                        if (res is Resource.Success) {
                            val blurry = res.data
                            _uiState.update {
                                it.copy(
                                    blurryPhotos = blurry,
                                    selectedBlurryPhotoPaths = emptySet()
                                )
                            }
                        }
                    }
                }

                // 8. Scan Downloads & APKs
                if (tab == StorageCleanTab.DOWNLOADS_APKS) launch {
                    findDownloadsAndApksUseCase(_uiState.value.downloadAgeThresholdDays).collectLatest { res ->
                        if (res is Resource.Error) _uiState.update { it.copy(errorMessage = res.message) }
                        if (res is Resource.Success) {
                            val items = res.data
                            _uiState.update {
                                it.copy(
                                    downloadsAndApks = items,
                                    selectedDownloadPaths = emptySet()
                                )
                            }
                        }
                    }
                }
            }
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun toggleDuplicateSelection(path: String) {
        _uiState.update { state ->
            val updated = state.selectedDuplicatePaths.toMutableSet()
            if (updated.contains(path)) {
                updated.remove(path)
            } else {
                updated.add(path)
            }
            state.copy(selectedDuplicatePaths = updated)
        }
    }

    fun toggleCorpseSelection(item: CorpseItem) {
        _uiState.update { state ->
            val updated = state.selectedCorpseItems.toMutableSet()
            if (updated.contains(item)) {
                updated.remove(item)
            } else {
                updated.add(item)
            }
            state.copy(selectedCorpseItems = updated)
        }
    }

    fun toggleWhatsAppCategory(type: WhatsAppCategoryType) {
        _uiState.update { state ->
            val category = state.whatsAppCategories.find { it.type == type } ?: return@update state
            val updatedCats = state.selectedWhatsAppCategories.toMutableSet()
            val updatedFileMap = state.selectedWhatsAppFilePaths.toMutableMap()

            if (updatedCats.contains(type)) {
                updatedCats.remove(type)
                updatedFileMap[type] = emptySet()
            } else {
                updatedCats.add(type)
                updatedFileMap[type] = category.files.map { it.absolutePath }.toSet()
            }

            state.copy(
                selectedWhatsAppCategories = updatedCats,
                selectedWhatsAppFilePaths = updatedFileMap
            )
        }
    }

    fun toggleFileInWhatsAppCategory(categoryType: WhatsAppCategoryType, filePath: String) {
        _uiState.update { state ->
            val category = state.whatsAppCategories.find { it.type == categoryType } ?: return@update state
            val currentSelected = state.getSelectedFilesForCategory(category).toMutableSet()

            if (currentSelected.contains(filePath)) {
                currentSelected.remove(filePath)
            } else {
                currentSelected.add(filePath)
            }

            val updatedFileMap = state.selectedWhatsAppFilePaths.toMutableMap()
            updatedFileMap[categoryType] = currentSelected

            val updatedCats = state.selectedWhatsAppCategories.toMutableSet()
            if (currentSelected.isNotEmpty()) {
                updatedCats.add(categoryType)
            } else {
                updatedCats.remove(categoryType)
            }

            state.copy(
                selectedWhatsAppFilePaths = updatedFileMap,
                selectedWhatsAppCategories = updatedCats
            )
        }
    }

    fun selectAllFilesInWhatsAppCategory(categoryType: WhatsAppCategoryType) {
        _uiState.update { state ->
            val category = state.whatsAppCategories.find { it.type == categoryType } ?: return@update state
            val allPaths = category.files.map { it.absolutePath }.toSet()
            val updatedFileMap = state.selectedWhatsAppFilePaths.toMutableMap()
            updatedFileMap[categoryType] = allPaths

            val updatedCats = state.selectedWhatsAppCategories.toMutableSet()
            updatedCats.add(categoryType)

            state.copy(
                selectedWhatsAppFilePaths = updatedFileMap,
                selectedWhatsAppCategories = updatedCats
            )
        }
    }

    fun deselectAllFilesInWhatsAppCategory(categoryType: WhatsAppCategoryType) {
        _uiState.update { state ->
            val updatedFileMap = state.selectedWhatsAppFilePaths.toMutableMap()
            updatedFileMap[categoryType] = emptySet()

            val updatedCats = state.selectedWhatsAppCategories.toMutableSet()
            updatedCats.remove(categoryType)

            state.copy(
                selectedWhatsAppFilePaths = updatedFileMap,
                selectedWhatsAppCategories = updatedCats
            )
        }
    }

    fun selectAllDuplicatesSuggested() {
        val suggested = mutableSetOf<String>()
        for (item in _uiState.value.duplicates) {
            for (d in item.duplicates) {
                if (d.isSuggestedDeletion) {
                    suggested.add(d.filePath)
                }
            }
        }
        _uiState.update { it.copy(selectedDuplicatePaths = suggested) }
    }

    fun autoSelectDuplicatesKeepOldest() {
        val suggested = mutableSetOf<String>()
        for (item in _uiState.value.duplicates) {
            val sorted = item.duplicates.sortedBy { it.lastModified }
            if (sorted.size > 1) {
                sorted.drop(1).forEach { suggested.add(it.filePath) }
            }
        }
        _uiState.update { it.copy(selectedDuplicatePaths = suggested) }
    }

    fun autoSelectDuplicatesKeepNewest() {
        val suggested = mutableSetOf<String>()
        for (item in _uiState.value.duplicates) {
            val sorted = item.duplicates.sortedByDescending { it.lastModified }
            if (sorted.size > 1) {
                sorted.drop(1).forEach { suggested.add(it.filePath) }
            }
        }
        _uiState.update { it.copy(selectedDuplicatePaths = suggested) }
    }

    fun deselectAllDuplicates() {
        _uiState.update { it.copy(selectedDuplicatePaths = emptySet()) }
    }

    fun selectAllCorpseFiles() {
        _uiState.update { it.copy(selectedCorpseItems = it.corpseFiles.toSet()) }
    }

    fun deselectAllCorpseFiles() {
        _uiState.update { it.copy(selectedCorpseItems = emptySet()) }
    }

    fun selectAllWhatsAppCategories() {
        _uiState.update { state ->
            val allCats = state.whatsAppCategories.map { it.type }.toSet()
            val fileMap = mutableMapOf<WhatsAppCategoryType, Set<String>>()
            for (cat in state.whatsAppCategories) {
                fileMap[cat.type] = cat.files.map { it.absolutePath }.toSet()
            }
            state.copy(
                selectedWhatsAppCategories = allCats,
                selectedWhatsAppFilePaths = fileMap
            )
        }
    }

    fun deselectAllWhatsAppCategories() {
        _uiState.update { state ->
            state.copy(
                selectedWhatsAppCategories = emptySet(),
                selectedWhatsAppFilePaths = emptyMap()
            )
        }
    }

    fun selectAllWhatsAppSentFolders() {
        _uiState.update { state ->
            val sentCategories = state.whatsAppCategories.filter {
                it.type == WhatsAppCategoryType.SENT_VIDEOS ||
                it.type == WhatsAppCategoryType.SENT_IMAGES ||
                it.type == WhatsAppCategoryType.SENT_DOCUMENTS ||
                it.type == WhatsAppCategoryType.SENT_AUDIO
            }
            val fileMap = state.selectedWhatsAppFilePaths.toMutableMap()
            for (cat in sentCategories) {
                fileMap[cat.type] = cat.files.map { it.absolutePath }.toSet()
            }
            state.copy(
                selectedWhatsAppCategories = state.selectedWhatsAppCategories + sentCategories.map { it.type }.toSet(),
                selectedWhatsAppFilePaths = fileMap
            )
        }
    }

    // Large Files Controls
    fun setLargeFileSizeThreshold(thresholdBytes: Long) {
        _uiState.update { it.copy(largeFileSizeThreshold = thresholdBytes, selectedLargeFilePaths = emptySet()) }
        viewModelScope.launch {
            findLargeFilesUseCase(thresholdBytes).collectLatest { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(largeFiles = res.data) }
                }
            }
        }
    }

    fun toggleLargeFileSelection(path: String) {
        _uiState.update { state ->
            val updated = state.selectedLargeFilePaths.toMutableSet()
            if (updated.contains(path)) updated.remove(path) else updated.add(path)
            state.copy(selectedLargeFilePaths = updated)
        }
    }

    fun selectAllLargeFiles() {
        _uiState.update { state ->
            state.copy(selectedLargeFilePaths = state.largeFiles.map { it.path }.toSet())
        }
    }

    fun deselectAllLargeFiles() {
        _uiState.update { it.copy(selectedLargeFilePaths = emptySet()) }
    }

    // Screenshot Controls
    fun setScreenshotAgeThreshold(days: Int) {
        _uiState.update { it.copy(screenshotAgeThresholdDays = days, selectedScreenshotPaths = emptySet()) }
        viewModelScope.launch {
            findScreenshotsUseCase(days).collectLatest { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(screenshots = res.data) }
                }
            }
        }
    }

    fun toggleScreenshotSelection(path: String) {
        _uiState.update { state ->
            val updated = state.selectedScreenshotPaths.toMutableSet()
            if (updated.contains(path)) updated.remove(path) else updated.add(path)
            state.copy(selectedScreenshotPaths = updated)
        }
    }

    fun selectAllScreenshots() {
        _uiState.update { state ->
            state.copy(selectedScreenshotPaths = state.screenshots.map { it.path }.toSet())
        }
    }

    fun deselectAllScreenshots() {
        _uiState.update { it.copy(selectedScreenshotPaths = emptySet()) }
    }

    // Telegram Controls
    fun toggleTelegramCategory(type: TelegramCategoryType) {
        _uiState.update { state ->
            val updatedCats = state.selectedTelegramCategories.toMutableSet()
            val updatedFiles = state.selectedTelegramFilePaths.toMutableMap()
            val cat = state.telegramCategories.firstOrNull { it.type == type }

            if (updatedCats.contains(type)) {
                updatedCats.remove(type)
                updatedFiles.remove(type)
            } else {
                updatedCats.add(type)
                if (cat != null) {
                    updatedFiles[type] = cat.files.map { it.absolutePath }.toSet()
                }
            }
            state.copy(
                selectedTelegramCategories = updatedCats,
                selectedTelegramFilePaths = updatedFiles
            )
        }
    }

    fun toggleFileInTelegramCategory(type: TelegramCategoryType, filePath: String) {
        _uiState.update { state ->
            val currentFiles = state.selectedTelegramFilePaths[type]?.toMutableSet() ?: mutableSetOf()
            if (currentFiles.contains(filePath)) {
                currentFiles.remove(filePath)
            } else {
                currentFiles.add(filePath)
            }
            val updatedMap = state.selectedTelegramFilePaths.toMutableMap()
            updatedMap[type] = currentFiles

            val updatedCats = state.selectedTelegramCategories.toMutableSet()
            if (currentFiles.isNotEmpty()) {
                updatedCats.add(type)
            } else {
                updatedCats.remove(type)
            }

            state.copy(
                selectedTelegramCategories = updatedCats,
                selectedTelegramFilePaths = updatedMap
            )
        }
    }

    fun selectAllFilesInTelegramCategory(type: TelegramCategoryType) {
        _uiState.update { state ->
            val cat = state.telegramCategories.firstOrNull { it.type == type } ?: return@update state
            val updatedMap = state.selectedTelegramFilePaths.toMutableMap()
            updatedMap[type] = cat.files.map { it.absolutePath }.toSet()
            val updatedCats = state.selectedTelegramCategories.toMutableSet()
            updatedCats.add(type)
            state.copy(
                selectedTelegramCategories = updatedCats,
                selectedTelegramFilePaths = updatedMap
            )
        }
    }

    fun deselectAllFilesInTelegramCategory(type: TelegramCategoryType) {
        _uiState.update { state ->
            val updatedMap = state.selectedTelegramFilePaths.toMutableMap()
            updatedMap.remove(type)
            val updatedCats = state.selectedTelegramCategories.toMutableSet()
            updatedCats.remove(type)
            state.copy(
                selectedTelegramCategories = updatedCats,
                selectedTelegramFilePaths = updatedMap
            )
        }
    }

    fun selectAllTelegramCategories() {
        _uiState.update { state ->
            val allCats = state.telegramCategories.map { it.type }.toSet()
            val fileMap = mutableMapOf<TelegramCategoryType, Set<String>>()
            for (cat in state.telegramCategories) {
                fileMap[cat.type] = cat.files.map { it.absolutePath }.toSet()
            }
            state.copy(
                selectedTelegramCategories = allCats,
                selectedTelegramFilePaths = fileMap
            )
        }
    }

    fun deselectAllTelegramCategories() {
        _uiState.update { state ->
            state.copy(
                selectedTelegramCategories = emptySet(),
                selectedTelegramFilePaths = emptyMap()
            )
        }
    }

    // Similar Photos Controls
    fun toggleSimilarPhotoSelection(path: String) {
        _uiState.update { state ->
            val updated = state.selectedSimilarPhotoPaths.toMutableSet()
            if (updated.contains(path)) updated.remove(path) else updated.add(path)
            state.copy(selectedSimilarPhotoPaths = updated)
        }
    }

    fun selectAllSimilarPhotosSuggested() {
        _uiState.update { state ->
            val suggested = state.similarPhotoGroups.flatMap { g -> g.similarPhotos.map { it.absolutePath } }.toSet()
            state.copy(selectedSimilarPhotoPaths = suggested)
        }
    }

    fun deselectAllSimilarPhotos() {
        _uiState.update { it.copy(selectedSimilarPhotoPaths = emptySet()) }
    }

    // Blurry Photos Controls
    fun toggleBlurryPhotoSelection(path: String) {
        _uiState.update { state ->
            val updated = state.selectedBlurryPhotoPaths.toMutableSet()
            if (updated.contains(path)) updated.remove(path) else updated.add(path)
            state.copy(selectedBlurryPhotoPaths = updated)
        }
    }

    fun selectAllBlurryPhotos() {
        _uiState.update { state ->
            state.copy(selectedBlurryPhotoPaths = state.blurryPhotos.map { it.path }.toSet())
        }
    }

    fun deselectAllBlurryPhotos() {
        _uiState.update { it.copy(selectedBlurryPhotoPaths = emptySet()) }
    }

    // Downloads & APKs Controls
    fun toggleDownloadSelection(path: String) {
        _uiState.update { state ->
            val updated = state.selectedDownloadPaths.toMutableSet()
            if (updated.contains(path)) updated.remove(path) else updated.add(path)
            state.copy(selectedDownloadPaths = updated)
        }
    }

    fun selectAllInstalledApksSuggested() {
        _uiState.update { state ->
            val suggested = state.downloadsAndApks.filter { it.isAlreadyInstalled }.map { it.path }.toSet()
            state.copy(selectedDownloadPaths = suggested)
        }
    }

    fun selectAllDownloads() {
        _uiState.update { state ->
            state.copy(selectedDownloadPaths = state.downloadsAndApks.map { it.path }.toSet())
        }
    }

    fun deselectAllDownloads() {
        _uiState.update { it.copy(selectedDownloadPaths = emptySet()) }
    }

    fun setDownloadFilterType(type: DownloadFilterType) {
        _uiState.update { it.copy(downloadFilterType = type) }
    }

    fun setBlurryThreshold(threshold: Double) {
        _uiState.update { it.copy(blurryThreshold = threshold) }
        viewModelScope.launch {
            findBlurryPhotosUseCase(threshold).collectLatest { res ->
                if (res is Resource.Success) {
                    _uiState.update {
                        it.copy(
                            blurryPhotos = res.data,
                            selectedBlurryPhotoPaths = res.data.map { item -> item.path }.toSet()
                        )
                    }
                }
            }
        }
    }

    // Trash Bin Actions
    fun restoreTrashItem(item: TrashItem) {
        viewModelScope.launch {
            val res = restoreTrashItemUseCase(item)
            if (res is Resource.Success) {
                bannerController.showSuccess(
                    title = "Dosya Geri Yüklendi",
                    message = "${item.fileName} başarıyla eski konumuna döndürüldü."
                )
                startScan(resetResult = false)
            } else if (res is Resource.Error) {
                bannerController.showError(
                    title = "Geri Yükleme Hatası",
                    message = res.message
                )
            }
        }
    }

    fun restoreAllTrash() {
        viewModelScope.launch {
            val res = restoreAllTrashUseCase()
            if (res is Resource.Success) {
                bannerController.showSuccess(
                    title = "Tüm Dosyalar Kurtarıldı",
                    message = "${res.data} adet dosya başarıyla orijinal konumlarına geri yüklendi."
                )
                startScan(resetResult = false)
            }
        }
    }

    fun purgeTrashItem(item: TrashItem) {
        viewModelScope.launch {
            val res = purgeTrashItemUseCase(item)
            if (res is Resource.Success) {
                bannerController.showInfo(
                    title = "Kalıcı Silindi",
                    message = "${item.fileName} diskten kalıcı olarak temizlendi."
                )
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            val res = emptyTrashUseCase()
            if (res is Resource.Success) {
                bannerController.showSuccess(
                    title = "Çöp Kutusu Boşaltıldı",
                    message = "${res.data} dosya kalıcı olarak silindi, depolama alanı temizlendi."
                )
            }
        }
    }

    fun cleanSelected(tab: StorageCleanTab) {
        if (_uiState.value.isCleaning || _uiState.value.isScanning || _uiState.value.errorMessage != null || _uiState.value.activeTab != tab) return
        val state = _uiState.value.selectionFor(tab).let {
            if (tab == StorageCleanTab.CORPSE_FILES) it.copy(useTrashBin = false) else it
        }
        _uiState.update { it.copy(isCleaning = true) }
        viewModelScope.launch {

            // 1. Gather all file paths to delete
            val duplicateFilesToDelete = state.selectedDuplicatePaths.toList()
            val whatsAppFilesToDelete = mutableListOf<String>()
            for (cat in state.whatsAppCategories) {
                val selected = state.getSelectedFilesForCategory(cat)
                whatsAppFilesToDelete.addAll(selected)
            }
            val telegramFilesToDelete = mutableListOf<String>()
            for (cat in state.telegramCategories) {
                val selected = state.getSelectedFilesForTelegramCategory(cat)
                telegramFilesToDelete.addAll(selected)
            }
            val largeFilesToDelete = state.selectedLargeFilePaths.toList()
            val screenshotsToDelete = state.selectedScreenshotPaths.toList()
            val similarPhotosToDelete = state.selectedSimilarPhotoPaths.toList()
            val blurryPhotosToDelete = state.selectedBlurryPhotoPaths.toList()
            val downloadsToDelete = state.selectedDownloadPaths.toList()

            val allFilesToDelete = (duplicateFilesToDelete + whatsAppFilesToDelete + telegramFilesToDelete +
                    largeFilesToDelete + screenshotsToDelete + similarPhotosToDelete + blurryPhotosToDelete + downloadsToDelete).distinct()
            val totalBatchSize = allFilesToDelete.size + state.selectedCorpseItems.size

            if (totalBatchSize == 0) {
                _uiState.update { it.copy(isCleaning = false) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isCleaning = true,
                    cleaningProgress = CleanProgress(0, totalBatchSize, "İşlem başlatılıyor...", 0L)
                )
            }

            var totalFreed = 0L

            // 2. Delete or Move to Safe Trash Bin
            if (allFilesToDelete.isNotEmpty()) {
                if (state.useTrashBin) {
                    Log.d(Constants.TAG, "[TRASH] Moving ${allFilesToDelete.size} files to safe trash...")
                    val result = moveToTrashUseCase(allFilesToDelete) { progress ->
                        _uiState.update {
                            it.copy(cleaningProgress = progress.copy(total = totalBatchSize))
                        }
                    }
                    if (result is Resource.Success) {
                        Log.d(Constants.TAG, "[TRASH] Successfully moved ${result.data} files to safe trash.")
                        totalFreed += calculateFreedBytes(state)
                    } else if (result is Resource.Error) {
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }
                } else {
                    Log.d(Constants.TAG, "[CLEAN] Initiating permanent batch delete for ${allFilesToDelete.size} files...")
                    storageRepository.deleteFiles(allFilesToDelete) { progress ->
                        _uiState.update {
                            it.copy(cleaningProgress = progress.copy(total = totalBatchSize))
                        }
                    }.collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val deletedCount = result.data
                                Log.d(Constants.TAG, "[CLEAN] Batch delete complete: $deletedCount files deleted.")
                                totalFreed += calculateFreedBytes(state)
                            }
                            is Resource.Error -> {
                                Log.e(Constants.TAG, "[CLEAN] Error deleting files: ${result.message}")
                                _uiState.update { it.copy(errorMessage = result.message) }
                            }
                            is Resource.Loading -> {}
                        }
                    }
                }
            }

            // 3. Delete selected corpse files
            for (corpse in state.selectedCorpseItems) {
                Log.d(Constants.TAG, "[CLEAN] Deleting corpse item '${corpse.displayName}'...")
                _uiState.update {
                    it.copy(
                        cleaningProgress = CleanProgress(
                            current = allFilesToDelete.size + 1,
                            total = totalBatchSize,
                            currentFileName = corpse.displayName,
                            freedBytes = totalFreed
                        )
                    )
                }
                storageRepository.deleteCorpseItem(corpse).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data) {
                                totalFreed += corpse.sizeBytes
                                Log.d(Constants.TAG, "[CLEAN] Successfully deleted corpse directory: ${corpse.path}")
                            }
                        }
                        is Resource.Error -> {
                            Log.e(Constants.TAG, "[CLEAN] Error deleting corpse: ${result.message}")
                        }
                        is Resource.Loading -> {}
                    }
                }
            }

            Log.d(Constants.TAG, "[CLEAN] Total cleanup operation finished. Total freed: $totalFreed bytes.")

            _uiState.update {
                it.copy(
                    isCleaning = false,
                    cleaningProgress = null,
                    cleanedBytesResult = totalFreed,
                    selectedDuplicatePaths = emptySet(),
                    selectedCorpseItems = emptySet(),
                    selectedWhatsAppCategories = emptySet(),
                    selectedWhatsAppFilePaths = emptyMap(),
                    selectedTelegramCategories = emptySet(),
                    selectedTelegramFilePaths = emptyMap(),
                    selectedSimilarPhotoPaths = emptySet(),
                    selectedBlurryPhotoPaths = emptySet(),
                    selectedLargeFilePaths = emptySet(),
                    selectedScreenshotPaths = emptySet(),
                    selectedDownloadPaths = emptySet()
                )
            }

            if (totalFreed > 0L) {
                val bannerTitle = if (state.useTrashBin) "Geri Dönüşüm Kutusuna Taşındı" else "Temizlik Başarıyla Tamamlandı"
                val bannerMessage = if (state.useTrashBin) {
                    "${FileUtils.formatFileSize(totalFreed)} alan çöp kutusuna taşındı. 30 gün içinde geri yükleyebilirsiniz."
                } else {
                    "${FileUtils.formatFileSize(totalFreed)} boş depolama alanı kalıcı olarak geri kazanıldı."
                }
                bannerController.showSuccess(
                    title = bannerTitle,
                    message = bannerMessage
                )
            }

            // Refresh scan after cleaning without resetting cleanedBytesResult
            startScan(resetResult = false)
        }
    }

    private fun calculateFreedBytes(state: StorageCleanUiState): Long {
        var freed = 0L
        freed += state.duplicates.sumOf { item ->
            item.duplicates.filter { state.selectedDuplicatePaths.contains(it.filePath) }.size * item.sizeBytes
        }
        for (cat in state.whatsAppCategories) {
            val selectedPaths = state.getSelectedFilesForCategory(cat)
            freed += cat.files.filter { selectedPaths.contains(it.absolutePath) }.sumOf { it.length() }
        }
        for (cat in state.telegramCategories) {
            val selectedPaths = state.getSelectedFilesForTelegramCategory(cat)
            freed += cat.files.filter { selectedPaths.contains(it.absolutePath) }.sumOf { it.length() }
        }
        for (group in state.similarPhotoGroups) {
            for (photo in group.similarPhotos) {
                if (state.selectedSimilarPhotoPaths.contains(photo.absolutePath)) {
                    freed += photo.length()
                }
            }
        }
        freed += state.blurryPhotos.filter { state.selectedBlurryPhotoPaths.contains(it.path) }.sumOf { it.sizeBytes }
        freed += state.largeFiles.filter { state.selectedLargeFilePaths.contains(it.path) }.sumOf { it.sizeBytes }
        freed += state.screenshots.filter { state.selectedScreenshotPaths.contains(it.path) }.sumOf { it.sizeBytes }
        freed += state.downloadsAndApks.filter { state.selectedDownloadPaths.contains(it.path) }.sumOf { it.sizeBytes }
        return freed
    }

    fun clearPendingDeleteIntentSender() {
        _uiState.update { it.copy(pendingDeleteIntentSender = null) }
    }

    fun onDeleteIntentSenderApproved() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pendingDeleteIntentSender = null,
                    isCleaning = false
                )
            }
            startScan()
        }
    }
}
