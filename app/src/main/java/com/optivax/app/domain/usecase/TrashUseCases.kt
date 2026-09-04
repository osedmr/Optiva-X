package com.optivax.app.domain.usecase

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.TrashItem
import com.optivax.app.domain.repository.TrashRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrashItemsUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    operator fun invoke(): Flow<List<TrashItem>> = trashRepository.getTrashItems()
}

class GetTotalTrashBytesUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    operator fun invoke(): Flow<Long> = trashRepository.getTotalTrashBytes()
}

class MoveToTrashUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(
        filePaths: List<String>,
        onProgress: ((CleanProgress) -> Unit)? = null
    ): Resource<Int> = trashRepository.moveToTrash(filePaths, onProgress)
}

class RestoreTrashItemUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(item: TrashItem): Resource<Boolean> = trashRepository.restoreItem(item)
}

class RestoreAllTrashUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(): Resource<Int> = trashRepository.restoreAll()
}

class PurgeTrashItemUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(item: TrashItem): Resource<Boolean> = trashRepository.purgeItem(item)
}

class EmptyTrashUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(): Resource<Int> = trashRepository.emptyTrash()
}

class AutoPurgeExpiredTrashUseCase @Inject constructor(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(retentionDays: Int = 30): Int = trashRepository.autoPurgeExpired(retentionDays)
}
