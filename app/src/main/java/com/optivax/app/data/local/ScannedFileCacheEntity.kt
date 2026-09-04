package com.optivax.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity caching file metadata, partial hashes, and SHA-256 signatures
 * to accelerate repeated duplicate file checks and avoid expensive disk re-reads.
 */
@Entity(
    tableName = "scanned_file_cache",
    indices = [
        Index(value = ["sizeBytes"]),
        Index(value = ["partialHash"]),
        Index(value = ["sha256Hash"])
    ]
)
data class ScannedFileCacheEntity(
    @PrimaryKey
    val filePath: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val partialHash: String,
    val sha256Hash: String,
    val mimeCategory: String
)
