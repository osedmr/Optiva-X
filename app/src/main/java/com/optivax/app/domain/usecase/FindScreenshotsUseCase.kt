package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.ScreenshotItem
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindScreenshotsUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(minAgeDays: Int = 0): Flow<Resource<List<ScreenshotItem>>> {
        return storageRepository.findScreenshots(minAgeDays)
    }
}
