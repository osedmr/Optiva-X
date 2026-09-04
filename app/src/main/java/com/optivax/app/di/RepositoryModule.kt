package com.optivax.app.di

import com.optivax.app.data.repository.AppManagerRepositoryImpl
import com.optivax.app.data.repository.StorageRepositoryImpl
import com.optivax.app.domain.repository.AppManagerRepository
import com.optivax.app.domain.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding domain repository interfaces to their data implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindAppManagerRepository(
        appManagerRepositoryImpl: AppManagerRepositoryImpl
    ): AppManagerRepository

    @Binds
    @Singleton
    abstract fun bindTrashRepository(
        trashRepositoryImpl: com.optivax.app.data.repository.TrashRepositoryImpl
    ): com.optivax.app.domain.repository.TrashRepository
}
