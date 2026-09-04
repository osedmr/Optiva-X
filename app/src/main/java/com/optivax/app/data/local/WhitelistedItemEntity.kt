package com.optivax.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity storing items (files, folders, or package names) that the user
 * has explicitly exempted from cleanup operations.
 */
@Entity(
    tableName = "whitelisted_items",
    indices = [Index(value = ["pathOrPackage"], unique = true)]
)
data class WhitelistedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val pathOrPackage: String,
    val displayName: String,
    val itemType: String, // "PACKAGE", "FILE", "DIRECTORY"
    val addedAt: Long
)
