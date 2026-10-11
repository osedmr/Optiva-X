package com.optivax.app.presentation.navigation

/**
 * Type-safe navigation screen routes for Optiva X application.
 */
sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard_screen", "Temizle")
    data object GalleryClean : Screen("gallery_clean_screen", "Galeri")
    data object FilesClean : Screen("files_clean_screen", "Dosyalar")
    data object AppManager : Screen("app_manager_screen", "Uygulamalar")

    // Retained for backwards compatibility if needed
    data object StorageClean : Screen("storage_clean_screen", "Depolama Temizliği")
}
