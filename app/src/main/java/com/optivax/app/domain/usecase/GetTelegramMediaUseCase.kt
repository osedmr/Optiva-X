package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.TelegramMediaCategory
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTelegramMediaUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<Resource<List<TelegramMediaCategory>>> {
        return storageRepository.getTelegramMediaCategories()
    }
}
