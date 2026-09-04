package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.CorpseItem
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to scan and locate residual remnant files belonging to uninstalled applications.
 */
class FindCorpseFilesUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<Resource<List<CorpseItem>>> {
        return storageRepository.findCorpseFiles()
    }
}
