package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.StorageBreakdown
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to query the comprehensive device storage breakdown.
 */
class GetStorageStatsUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<Resource<StorageBreakdown>> {
        return storageRepository.getStorageBreakdown()
    }
}
