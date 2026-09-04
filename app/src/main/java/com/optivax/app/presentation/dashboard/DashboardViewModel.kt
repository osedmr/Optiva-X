package com.optivax.app.presentation.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optivax.app.core.Constants
import com.optivax.app.core.PermissionHelper
import com.optivax.app.core.Resource
import com.optivax.app.core.notification.OptivaBannerController
import com.optivax.app.data.local.CleanHistoryDao
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.domain.model.StorageBreakdown
import com.optivax.app.domain.repository.AppManagerRepository
import com.optivax.app.domain.usecase.GetIdleAppsUseCase
import com.optivax.app.domain.usecase.GetStorageStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val storageBreakdown: StorageBreakdown? = null,
    val totalFreedBytes: Long = 0L,
    val idleAppsCount: Int = 0,
    val installedApps: List<AppUsageInfo> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val hasAllFilesPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val errorMessage: String? = null
) {
    val totalCacheBytes: Long get() = installedApps.filter { !it.isSystemApp }.sumOf { it.cacheSizeBytes }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getStorageStatsUseCase: GetStorageStatsUseCase,
    private val getIdleAppsUseCase: GetIdleAppsUseCase,
    private val appManagerRepository: AppManagerRepository,
    private val cleanHistoryDao: CleanHistoryDao,
    private val bannerController: OptivaBannerController,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        checkPermissions()
        observeCleanHistory()
        loadDashboard()
    }

    fun checkPermissions() {
        val hasUsage = PermissionHelper.hasUsageStatsPermission(context)
        val hasAllFiles = PermissionHelper.hasAllFilesAccess(context)
        val hasStorage = PermissionHelper.hasStoragePermission(context)

        Log.d(
            Constants.TAG,
            "[DASHBOARD] Permissions checked -> Usage: $hasUsage, AllFiles: $hasAllFiles, Storage: $hasStorage"
        )

        _uiState.update {
            it.copy(
                hasUsagePermission = hasUsage,
                hasAllFilesPermission = hasAllFiles,
                hasStoragePermission = hasStorage
            )
        }
    }

    fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            checkPermissions()
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Storage Breakdown Flow
            launch {
                getStorageStatsUseCase().collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            Log.d(Constants.TAG, "[DASHBOARD] Storage breakdown loaded successfully.")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    storageBreakdown = result.data,
                                    errorMessage = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            Log.e(Constants.TAG, "[DASHBOARD] Storage breakdown error: ${result.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.message
                                )
                            }
                        }
                    }
                }
            }

            // Idle Apps count
            launch {
                getIdleAppsUseCase(idleThresholdDays = 30L).collectLatest { result ->
                    if (result is Resource.Success) {
                        Log.d(Constants.TAG, "[DASHBOARD] Idle apps count updated: ${result.data.size}")
                        _uiState.update { it.copy(idleAppsCount = result.data.size) }
                    }
                }
            }

            // Installed Apps for Deep Cache Cleaning
            launch {
                appManagerRepository.getInstalledAppsUsage().collectLatest { result ->
                    if (result is Resource.Success) {
                        _uiState.update { it.copy(installedApps = result.data) }
                    }
                }
            }
        }
    }

    fun refreshDashboard() {
        loadDashboard()
        bannerController.showInfo(
            title = "Telemetri Yenilendi",
            message = "Depolama ve bellek durum verileri güncellendi."
        )
    }

    private fun observeCleanHistory() {
        viewModelScope.launch {
            cleanHistoryDao.getTotalFreedBytes().collectLatest { freed ->
                _uiState.update { it.copy(totalFreedBytes = freed ?: 0L) }
            }
        }
    }
}
