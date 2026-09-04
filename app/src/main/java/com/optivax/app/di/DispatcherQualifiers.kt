package com.optivax.app.di

import javax.inject.Qualifier

/**
 * Qualifier annotation for Injecting Dispatchers.IO
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier annotation for Injecting Dispatchers.Default
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Qualifier annotation for Injecting Dispatchers.Main
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
