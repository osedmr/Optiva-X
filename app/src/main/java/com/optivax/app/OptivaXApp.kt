package com.optivax.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Optiva X Application entry point annotated with [HiltAndroidApp]
 * to trigger Hilt's code generation, including a base class for the application
 * that serves as the application-level dependency container.
 */
@HiltAndroidApp
class OptivaXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.optivax.app.work.StorageReminderWorker.schedule(this)
    }
}
