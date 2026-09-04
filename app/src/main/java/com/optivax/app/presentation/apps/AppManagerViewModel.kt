package com.optivax.app.presentation.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optivax.app.core.PermissionHelper
import com.optivax.app.core.Resource
import com.optivax.app.core.notification.OptivaBannerController
import com.optivax.app.data.local.WhitelistDao
import com.optivax.app.data.local.WhitelistedItemEntity
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.domain.repository.AppManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppFilterMode {
    USER_APPS,
    IDLE_ONLY,
    ALL
}

data class AppManagerUiState(
    val isLoading: Boolean = true,
    val apps: List<AppUsageInfo> = emptyList(),
    val filterMode: AppFilterMode = AppFilterMode.USER_APPS,
    val searchQuery: String = "",
    val hasUsagePermission: Boolean = false,
    val whitelistedPackages: Set<String> = emptySet(),
    val errorMessage: String? = null
) {
    val userAppsCount: Int
        get() = apps.count { !it.isSystemApp }

    val idleAppsCount: Int
        get() = apps.count { it.isIdle && !it.isSystemApp }

    val allAppsCount: Int
        get() = apps.size

    val filteredApps: List<AppUsageInfo>
        get() {
            val baseList = when (filterMode) {
                AppFilterMode.USER_APPS -> apps.filter { !it.isSystemApp }
                AppFilterMode.IDLE_ONLY -> apps.filter { it.isIdle && !it.isSystemApp }
                AppFilterMode.ALL -> apps
            }
            return if (searchQuery.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }
}

@HiltViewModel
class AppManagerViewModel @Inject constructor(
    private val appManagerRepository: AppManagerRepository,
    private val whitelistDao: WhitelistDao,
    private val bannerController: OptivaBannerController,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppManagerUiState())
    val uiState: StateFlow<AppManagerUiState> = _uiState.asStateFlow()

    private var pendingUninstallPackage: String? = null
    private var pendingUninstallAppName: String? = null

    init {
        observeWhitelist()
        loadApps()
    }

    private fun observeWhitelist() {
        viewModelScope.launch {
            whitelistDao.getAll().collectLatest { items ->
                val set = items.filter { it.itemType == "PACKAGE" }.map { it.pathOrPackage }.toSet()
                _uiState.update { it.copy(whitelistedPackages = set) }
            }
        }
    }

    fun setFilterMode(mode: AppFilterMode) {
        _uiState.update { it.copy(filterMode = mode) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadApps() {
        viewModelScope.launch {
            val hasUsage = PermissionHelper.hasUsageStatsPermission(context)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasUsagePermission = hasUsage,
                    errorMessage = null
                )
            }

            appManagerRepository.getInstalledAppsUsage(idleThresholdDays = 30L).collectLatest { res ->
                when (res) {
                    is Resource.Success -> {
                        val currentApps = res.data
                        // Check if previously requested package was uninstalled
                        pendingUninstallPackage?.let { pkg ->
                            val isStillInstalled = currentApps.any { it.packageName == pkg }
                            if (!isStillInstalled) {
                                val name = pendingUninstallAppName ?: pkg
                                bannerController.showSuccess(
                                    title = "Uygulama Kaldırıldı",
                                    message = "$name cihazınızdan başarıyla kaldırıldı."
                                )
                                pendingUninstallPackage = null
                                pendingUninstallAppName = null
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                apps = currentApps,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = res.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun handleUninstallClick(
        app: AppUsageInfo,
        onOpenSettings: (String) -> Unit,
        onLaunchSystemUninstall: (String) -> Unit
    ) {
        val isSystemOrOem = app.isSystemApp ||
                app.packageName.startsWith("com.miui.") ||
                app.packageName.startsWith("com.xiaomi.") ||
                app.packageName.startsWith("com.android.") ||
                app.packageName.startsWith("com.google.android.")

        if (isSystemOrOem) {
            bannerController.showWarning(
                title = "Sistem Uygulaması Korumalı",
                message = "${app.appName} bir sistem/üretici uygulamasıdır. Android güvenlik kalkanı nedeniyle doğrudan silinemez. Ayarlar'dan devre dışı bırakabilirsiniz."
            )
            onOpenSettings(app.packageName)
        } else {
            pendingUninstallPackage = app.packageName
            pendingUninstallAppName = app.appName
            onLaunchSystemUninstall(app.packageName)
        }
    }

    fun toggleWhitelist(packageName: String, appName: String) {
        viewModelScope.launch {
            val isWhitelisted = _uiState.value.whitelistedPackages.contains(packageName)
            if (isWhitelisted) {
                whitelistDao.removeByPath(packageName)
                bannerController.showInfo(
                    title = "Muafiyet Kaldırıldı",
                    message = "$appName temizlik ve analiz listelerine yeniden dahil edildi."
                )
            } else {
                whitelistDao.insert(
                    WhitelistedItemEntity(
                        pathOrPackage = packageName,
                        displayName = appName,
                        itemType = "PACKAGE",
                        addedAt = System.currentTimeMillis()
                    )
                )
                bannerController.showSuccess(
                    title = "Muaf Tutuldu",
                    message = "$appName temizlik ve atıl taramalarından muaf tutuldu."
                )
            }
        }
    }
}
