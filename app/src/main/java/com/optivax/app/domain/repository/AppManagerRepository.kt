package com.optivax.app.domain.repository

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing installed applications, evaluating usage stats,
 * and identifying idle apps.
 */
interface AppManagerRepository {

    /**
     * Retrieves all installed applications with detailed storage and last-used metrics.
     */
    fun getInstalledAppsUsage(idleThresholdDays: Long = 30L): Flow<Resource<List<AppUsageInfo>>>

    /**
     * Filters applications that have not been used for at least [idleThresholdDays]
     * and are non-system apps.
     */
    fun getIdleApps(idleThresholdDays: Long = 30L): Flow<Resource<List<AppUsageInfo>>>
}
