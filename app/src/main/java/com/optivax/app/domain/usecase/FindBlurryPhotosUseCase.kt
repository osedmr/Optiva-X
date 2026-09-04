package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.BlurryPhotoItem
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindBlurryPhotosUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(threshold: Double = 80.0): Flow<Resource<List<BlurryPhotoItem>>> {
        return storageRepository.findBlurryPhotos(threshold)
    }
}
