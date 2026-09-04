package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.domain.repository.AppManagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to filter and retrieve applications not opened for a specified duration.
 */
class GetIdleAppsUseCase @Inject constructor(
    private val appManagerRepository: AppManagerRepository
) {
    operator fun invoke(idleThresholdDays: Long = 30L): Flow<Resource<List<AppUsageInfo>>> {
        return appManagerRepository.getIdleApps(idleThresholdDays)
    }
}
