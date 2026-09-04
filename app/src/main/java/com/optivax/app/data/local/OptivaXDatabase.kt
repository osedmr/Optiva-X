package com.optivax.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room Database for Optiva X application.
 * Persists cleanup histories, user-defined whitelists, and fast file hash caches.
 */
@Database(
    entities = [
        CleanHistoryEntity::class,
        WhitelistedItemEntity::class,
        ScannedFileCacheEntity::class,
        TrashEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OptivaXDatabase : RoomDatabase() {

    abstract fun cleanHistoryDao(): CleanHistoryDao

    abstract fun whitelistDao(): WhitelistDao

    abstract fun scannedFileCacheDao(): ScannedFileCacheDao

    abstract fun trashDao(): TrashDao
}
