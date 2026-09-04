package com.optivax.app.domain.repository

import com.optivax.app.core.Resource
import com.optivax.app.domain.model.CleanProgress
import com.optivax.app.domain.model.TrashItem
import kotlinx.coroutines.flow.Flow

interface TrashRepository {
    fun getTrashItems(): Flow<List<TrashItem>>
    fun getTotalTrashBytes(): Flow<Long>
    suspend fun moveToTrash(
        filePaths: List<String>,
        onProgress: ((CleanProgress) -> Unit)? = null
    ): Resource<Int>
    suspend fun restoreItem(item: TrashItem): Resource<Boolean>
    suspend fun restoreAll(): Resource<Int>
    suspend fun purgeItem(item: TrashItem): Resource<Boolean>
    suspend fun emptyTrash(): Resource<Int>
    suspend fun autoPurgeExpired(retentionDays: Int = 30): Int
}
