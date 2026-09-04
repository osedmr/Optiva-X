package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.LargeFileItem
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindLargeFilesUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(minSizeBytes: Long = 50L * 1024L * 1024L): Flow<Resource<List<LargeFileItem>>> {
        return storageRepository.findLargeFiles(minSizeBytes)
    }
}
