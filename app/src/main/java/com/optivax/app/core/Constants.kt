package com.optivax.app.core

/**
 * Global application constants for Optiva X.
 */
object Constants {
    const val TAG = "OptivaX"
    const val DATABASE_NAME = "optivax_db"

    // Storage threshold constants
    const val KILOBYTE = 1024L
    const val MEGABYTE = 1024L * 1024L
    const val GIGABYTE = 1024L * 1024L * 1024L

    // Safe Idle application threshold (e.g. 30 days of inactivity)
    const val DEFAULT_IDLE_DAYS = 30L
}
