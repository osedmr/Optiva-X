package com.optivax.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optivax.app.domain.model.TrashItem

@Entity(tableName = "trash_items")
data class TrashEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val sizeBytes: Long,
    val deletedTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): TrashItem = TrashItem(
        id = id,
        originalPath = originalPath,
        trashPath = trashPath,
        fileName = fileName,
        sizeBytes = sizeBytes,
        deletedTimestamp = deletedTimestamp
    )

    companion object {
        fun fromDomain(item: TrashItem): TrashEntity = TrashEntity(
            id = item.id,
            originalPath = item.originalPath,
            trashPath = item.trashPath,
            fileName = item.fileName,
            sizeBytes = item.sizeBytes,
            deletedTimestamp = item.deletedTimestamp
        )
    }
}
