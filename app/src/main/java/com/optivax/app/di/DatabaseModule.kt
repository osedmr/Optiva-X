package com.optivax.app.di

import android.content.Context
import androidx.room.Room
import com.optivax.app.core.Constants
import com.optivax.app.data.local.CleanHistoryDao
import com.optivax.app.data.local.OptivaXDatabase
import com.optivax.app.data.local.ScannedFileCacheDao
import com.optivax.app.data.local.WhitelistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Room database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOptivaXDatabase(
        @ApplicationContext context: Context
    ): OptivaXDatabase {
        return Room.databaseBuilder(
            context,
            OptivaXDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideCleanHistoryDao(database: OptivaXDatabase): CleanHistoryDao {
        return database.cleanHistoryDao()
    }

    @Provides
    @Singleton
    fun provideWhitelistDao(database: OptivaXDatabase): WhitelistDao {
        return database.whitelistDao()
    }

    @Provides
    @Singleton
    fun provideScannedFileCacheDao(database: OptivaXDatabase): ScannedFileCacheDao {
        return database.scannedFileCacheDao()
    }

    @Provides
    @Singleton
    fun provideTrashDao(database: OptivaXDatabase): com.optivax.app.data.local.TrashDao {
        return database.trashDao()
    }
}
