package com.optivax.app.presentation.storage

/** A deletion request must never inherit selections from a different category. */
fun StorageCleanUiState.selectionFor(tab: StorageCleanTab): StorageCleanUiState = copy(
    selectedDuplicatePaths = if (tab == StorageCleanTab.DUPLICATES) selectedDuplicatePaths else emptySet(),
    selectedCorpseItems = if (tab == StorageCleanTab.CORPSE_FILES) selectedCorpseItems else emptySet(),
    selectedWhatsAppCategories = if (tab == StorageCleanTab.WHATSAPP) selectedWhatsAppCategories else emptySet(),
    selectedWhatsAppFilePaths = if (tab == StorageCleanTab.WHATSAPP) selectedWhatsAppFilePaths else emptyMap(),
    selectedTelegramCategories = if (tab == StorageCleanTab.TELEGRAM) selectedTelegramCategories else emptySet(),
    selectedTelegramFilePaths = if (tab == StorageCleanTab.TELEGRAM) selectedTelegramFilePaths else emptyMap(),
    selectedSimilarPhotoPaths = if (tab == StorageCleanTab.SIMILAR_PHOTOS) selectedSimilarPhotoPaths else emptySet(),
    selectedBlurryPhotoPaths = if (tab == StorageCleanTab.BLURRY_PHOTOS) selectedBlurryPhotoPaths else emptySet(),
    selectedLargeFilePaths = if (tab == StorageCleanTab.LARGE_FILES) selectedLargeFilePaths else emptySet(),
    selectedScreenshotPaths = if (tab == StorageCleanTab.SCREENSHOTS) selectedScreenshotPaths else emptySet(),
    selectedDownloadPaths = if (tab == StorageCleanTab.DOWNLOADS_APKS) selectedDownloadPaths else emptySet()
)
