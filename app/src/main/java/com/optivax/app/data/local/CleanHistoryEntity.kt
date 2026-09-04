package com.optivax.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity recording past optimization and cleaning operations.
 */
@Entity(tableName = "clean_history")
data class CleanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val scanType: String,
    val freedBytes: Long,
    val itemsCount: Int,
    val durationMs: Long
)
