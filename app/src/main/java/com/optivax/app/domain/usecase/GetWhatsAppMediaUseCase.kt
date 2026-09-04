package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.WhatsAppMediaCategory
import com.optivax.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to scan and categorize redundant WhatsApp media files (Sent copies, voice notes, stickers, status caches).
 */
class GetWhatsAppMediaUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<Resource<List<WhatsAppMediaCategory>>> {
        return storageRepository.getWhatsAppMediaCategories()
    }
}
