package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.DuplicateItem
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

/**
 * Use case to discover duplicate files across target storage directories.
 */
class FindDuplicatesUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(targetDirectories: List<File>): Flow<Resource<List<DuplicateItem>>> {
        return storageRepository.findDuplicates(targetDirectories)
    }
}
