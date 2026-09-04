package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.DownloadApkItem
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindDownloadsAndApksUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(minAgeDays: Int = 0): Flow<Resource<List<DownloadApkItem>>> {
        return storageRepository.findDownloadsAndApks(minAgeDays)
    }
}
