package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.SimilarPhotoGroup
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindSimilarPhotosUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<Resource<List<SimilarPhotoGroup>>> {
        return storageRepository.findSimilarPhotos()
    }
}
