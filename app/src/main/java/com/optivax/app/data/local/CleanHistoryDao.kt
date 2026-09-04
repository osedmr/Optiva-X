package com.optivax.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cleanup history operations.
 */
@Dao
interface CleanHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CleanHistoryEntity): Long

    @Query("SELECT * FROM clean_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CleanHistoryEntity>>

    @Query("SELECT SUM(freedBytes) FROM clean_history")
    fun getTotalFreedBytes(): Flow<Long?>

    @Query("DELETE FROM clean_history")
    suspend fun clearAll()
}
