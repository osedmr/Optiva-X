package com.optivax.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for whitelisted item management.
 */
@Dao
interface WhitelistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WhitelistedItemEntity): Long

    @Query("DELETE FROM whitelisted_items WHERE pathOrPackage = :pathOrPackage")
    suspend fun removeByPath(pathOrPackage: String): Int

    @Query("SELECT * FROM whitelisted_items ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WhitelistedItemEntity>>

    @Query("SELECT COUNT(*) > 0 FROM whitelisted_items WHERE pathOrPackage = :pathOrPackage")
    suspend fun isWhitelisted(pathOrPackage: String): Boolean

    @Query("SELECT pathOrPackage FROM whitelisted_items")
    suspend fun getAllPaths(): List<String>
}
