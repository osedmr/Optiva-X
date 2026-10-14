package com.optivax.app.core.accessibility

import android.content.Context

object AccessibilityConsent {
    fun isAccepted(context: Context): Boolean = context
        .getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)
        .getBoolean("accessibility_cache_v1", false)

    fun accept(context: Context) {
        context.getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)
            .edit().putBoolean("accessibility_cache_v1", true).apply()
    }
}
