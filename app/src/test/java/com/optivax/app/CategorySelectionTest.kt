package com.optivax.app

import com.optivax.app.domain.model.BlurryPhotoItem
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.model.TelegramCategoryType
import com.optivax.app.domain.model.WhatsAppCategoryType
import com.optivax.app.presentation.storage.StorageCleanTab
import com.optivax.app.presentation.storage.StorageCleanUiState
import com.optivax.app.presentation.storage.selectionFor
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class CategorySelectionTest {
    private val corpse = CorpseItem("cache", "/cache", "Cache", 100, 1)
    private val allSelected = StorageCleanUiState(
        selectedDuplicatePaths = setOf("duplicate"),
        selectedCorpseItems = setOf(corpse),
        selectedWhatsAppCategories = setOf(WhatsAppCategoryType.SENT_IMAGES),
        selectedWhatsAppFilePaths = mapOf(WhatsAppCategoryType.SENT_IMAGES to setOf("whatsapp")),
        selectedTelegramCategories = setOf(TelegramCategoryType.IMAGES),
        selectedTelegramFilePaths = mapOf(TelegramCategoryType.IMAGES to setOf("telegram")),
        selectedSimilarPhotoPaths = setOf("similar"),
        selectedBlurryPhotoPaths = setOf("blurry"),
        selectedLargeFilePaths = setOf("large"),
        selectedScreenshotPaths = setOf("screenshot"),
        selectedDownloadPaths = setOf("download")
    )

    @Test fun everyCategoryExcludesAllOtherSelections() {
        for (tab in StorageCleanTab.entries) {
            val scoped = allSelected.selectionFor(tab)
            val categoriesWithSelection = buildSet {
                if (scoped.selectedDuplicatePaths.isNotEmpty()) add(StorageCleanTab.DUPLICATES)
                if (scoped.selectedCorpseItems.isNotEmpty()) add(StorageCleanTab.CORPSE_FILES)
                if (scoped.selectedWhatsAppCategories.isNotEmpty() || scoped.selectedWhatsAppFilePaths.isNotEmpty()) add(StorageCleanTab.WHATSAPP)
                if (scoped.selectedTelegramCategories.isNotEmpty() || scoped.selectedTelegramFilePaths.isNotEmpty()) add(StorageCleanTab.TELEGRAM)
                if (scoped.selectedSimilarPhotoPaths.isNotEmpty()) add(StorageCleanTab.SIMILAR_PHOTOS)
                if (scoped.selectedBlurryPhotoPaths.isNotEmpty()) add(StorageCleanTab.BLURRY_PHOTOS)
                if (scoped.selectedLargeFilePaths.isNotEmpty()) add(StorageCleanTab.LARGE_FILES)
                if (scoped.selectedScreenshotPaths.isNotEmpty()) add(StorageCleanTab.SCREENSHOTS)
                if (scoped.selectedDownloadPaths.isNotEmpty()) add(StorageCleanTab.DOWNLOADS_APKS)
            }
            assertEquals(tab.name, setOf(tab), categoriesWithSelection)
        }
    }

    @Test fun galleryConfirmationTotalExcludesSelectedTemporaryFiles() {
        val photo = File("photo.jpg")
        val state = allSelected.copy(
            blurryPhotos = listOf(BlurryPhotoItem(photo, 2.0, 90, 24)),
            selectedBlurryPhotoPaths = setOf(photo.absolutePath)
        )
        assertEquals(124L, state.selectedBytesToClean)
        assertEquals(24L, state.selectionFor(StorageCleanTab.BLURRY_PHOTOS).selectedBytesToClean)
        assertEquals(100L, state.selectionFor(StorageCleanTab.CORPSE_FILES).selectedBytesToClean)
        assertEquals(0L, state.selectionFor(StorageCleanTab.SCREENSHOTS).selectedBytesToClean)
        assertEquals(setOf(corpse), state.selectedCorpseItems)
    }
}
