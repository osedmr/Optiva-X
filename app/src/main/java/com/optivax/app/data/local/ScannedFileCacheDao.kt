package com.optivax.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for scanned file metadata and SHA-256 hash cache.
 */
@Dao
interface ScannedFileCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: ScannedFileCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(caches: List<ScannedFileCacheEntity>)

    @Query("SELECT * FROM scanned_file_cache WHERE filePath = :filePath")
    suspend fun getByPath(filePath: String): ScannedFileCacheEntity?

    @Query("SELECT * FROM scanned_file_cache WHERE sizeBytes = :sizeBytes AND lastModified = :lastModified")
    suspend fun getBySizeAndDate(sizeBytes: Long, lastModified: Long): List<ScannedFileCacheEntity>

    @Query("""
        SELECT * FROM scanned_file_cache 
        WHERE sha256Hash IN (
            SELECT sha256Hash 
            FROM scanned_file_cache 
            WHERE sha256Hash != '' 
            GROUP BY sha256Hash 
            HAVING COUNT(*) > 1
        ) 
        ORDER BY sha256Hash, sizeBytes DESC
    """)
    fun getDuplicates(): Flow<List<ScannedFileCacheEntity>>

    @Query("DELETE FROM scanned_file_cache WHERE filePath = :filePath")
    suspend fun deleteByPath(filePath: String): Int

    @Query("DELETE FROM scanned_file_cache")
    suspend fun clearAll()
}
