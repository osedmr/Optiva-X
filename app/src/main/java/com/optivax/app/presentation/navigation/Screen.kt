package com.optivax.app.presentation.navigation

/**
 * Type-safe navigation screen routes for Optiva X application.
 */
sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard_screen", "Optiva Dashboard")
    data object StorageClean : Screen("storage_clean_screen", "Depolama Temizliği")
    data object AppManager : Screen("app_manager_screen", "Uygulama Yöneticisi")
}
